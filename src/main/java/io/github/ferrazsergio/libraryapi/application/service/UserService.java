package io.github.ferrazsergio.libraryapi.application.service;

import io.github.ferrazsergio.libraryapi.domain.model.User;
import io.github.ferrazsergio.libraryapi.infrastructure.repository.LoanRepository;
import io.github.ferrazsergio.libraryapi.infrastructure.repository.UserRepository;
import io.github.ferrazsergio.libraryapi.interfaces.dto.UserDTO;
import io.github.ferrazsergio.libraryapi.interfaces.dto.UserStatisticsDTO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final LoanRepository loanRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActivityService activityService;

    @Transactional(readOnly = true)
    public UserDTO findById(Integer id) {
        return userRepository.findById(id)
                .map(UserDTO::fromEntity)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public Optional<UserDTO> findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(UserDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> findAll(Pageable pageable) {
        return userRepository.findByDeletedFalse(pageable)
                .map(UserDTO::fromEntity);
    }

    @Transactional
    @CacheEvict(value = "userStats", allEntries = true)
    public UserDTO create(UserDTO userDTO) {
        userRepository.findByEmail(userDTO.getEmail())
                .ifPresent(user -> {
                    throw new RuntimeException("Email already in use: " + userDTO.getEmail());
                });

        User user = new User();
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setPhone(userDTO.getPhone());
        user.setAddress(userDTO.getAddress());
        user.setRole(userDTO.getRole());
        user.setStatus(userDTO.getStatus());
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        activityService.logActivity(
                "USER_CREATED",
                "Usuário criado: " + savedUser.getName(),
                savedUser.getName(),
                null
        );

        return UserDTO.fromEntity(savedUser);
    }

    @Transactional
    @CacheEvict(value = "userStats", allEntries = true)
    public UserDTO update(Integer id, UserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        if (!user.getEmail().equals(userDTO.getEmail())) {
            userRepository.findByEmail(userDTO.getEmail())
                    .ifPresent(existingUser -> {
                        throw new RuntimeException("Email already in use: " + userDTO.getEmail());
                    });
        }

        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());
        user.setAddress(userDTO.getAddress());

        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        if (userDTO.getRole() != null) {
            user.setRole(userDTO.getRole());
        }

        if (userDTO.getStatus() != null) {
            user.setStatus(userDTO.getStatus());
        }

        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(user);

        activityService.logActivity(
                "USER_UPDATED",
                "Usuário atualizado: " + updatedUser.getName(),
                updatedUser.getName(),
                null
        );

        return UserDTO.fromEntity(updatedUser);
    }

    @Transactional
    @CacheEvict(value = "userStats", allEntries = true)
    public void delete(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);

        activityService.logActivity(
                "USER_DELETED",
                "Usuário removido: " + user.getName(),
                user.getName(),
                null
        );
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "userStats", key = "'totalUsers'")
    public long getTotalUsers() {
        return userRepository.countByDeletedFalse();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "userStats", key = "'newUsersLastMonth'")
    public long getNewUsersLastMonth() {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        return userRepository.countByCreatedAtAfterAndDeletedFalse(oneMonthAgo);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "userStats", key = "'mostActiveUsers:' + #limit")
    public List<UserStatisticsDTO> getMostActiveUsers(int limit) {
        return userRepository.findMostActiveUsers(PageRequest.of(0, limit))
                .stream()
                .map(result -> {
                    User user = (User) result[0];
                    Long loanCount = (Long) result[1];
                    return UserStatisticsDTO.builder()
                            .userId(user.getId())
                            .name(user.getName())
                            .email(user.getEmail())
                            .loanCount(loanCount)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "userStats", key = "'activeUsersPercentage'")
    public double getActiveUsersPercentage() {
        long totalUsers = getTotalUsers();
        if (totalUsers == 0) {
            return 0.0;
        }

        LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);
        long activeUsers = userRepository.countUsersWithLoansAfter(threeMonthsAgo);

        return (double) activeUsers / totalUsers * 100.0;
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getRecentlyRegisteredUsers(int limit) {
        return userRepository.findByDeletedFalseOrderByCreatedAtDesc(PageRequest.of(0, limit))
                .stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public String saveUserAvatar(User user, MultipartFile file) {
        try {
            Path uploadPath = Paths.get(System.getProperty("user.dir"), "uploads", "avatars");

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".png";

            String filename = "user_" + user.getId() + "_" + System.currentTimeMillis() + extension;
            Path filePath = uploadPath.resolve(filename);

            file.transferTo(filePath.toFile());

            return "/api/v1/users/files/avatars/" + filename;

        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar avatar: " + e.getMessage(), e);
        }
    }
}