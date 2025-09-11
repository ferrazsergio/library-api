package io.github.ferrazsergio.libraryapi.application.service;

import io.github.ferrazsergio.libraryapi.domain.model.User;
import io.github.ferrazsergio.libraryapi.infrastructure.repository.LoanRepository;
import io.github.ferrazsergio.libraryapi.infrastructure.repository.UserRepository;
import io.github.ferrazsergio.libraryapi.interfaces.dto.UserDTO;
import io.github.ferrazsergio.libraryapi.interfaces.dto.UserStatisticsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        // Verify if email already exists
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
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        return UserDTO.fromEntity(savedUser);
    }

    @Transactional
    @CacheEvict(value = "userStats", allEntries = true)
    public UserDTO update(Integer id, UserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        // Check if email is already in use by another user
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

        // Only update password if it's provided
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        // Only admin can change roles
        if (userDTO.getRole() != null) {
            user.setRole(userDTO.getRole());
        }

        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(user);
        return UserDTO.fromEntity(updatedUser);
    }

    @Transactional
    @CacheEvict(value = "userStats", allEntries = true)
    public void delete(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        // Soft delete
        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    // ==================== MÉTODOS PARA DASHBOARD ====================

    /**
     * Retorna o número total de usuários ativos no sistema.
     *
     * @return total de usuários
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "userStats", key = "'totalUsers'")
    public long getTotalUsers() {
        return userRepository.countByDeletedFalse();
    }

    /**
     * Retorna o número de novos usuários registrados no último mês.
     *
     * @return total de novos usuários
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "userStats", key = "'newUsersLastMonth'")
    public long getNewUsersLastMonth() {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        return userRepository.countByCreatedAtAfterAndDeletedFalse(oneMonthAgo);
    }

    /**
     * Retorna os usuários mais ativos (com mais empréstimos).
     *
     * @param limit número máximo de usuários a retornar
     * @return lista de estatísticas por usuário
     */
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

    /**
     * Retorna a porcentagem de usuários ativos (que fizeram empréstimos recentemente).
     *
     * @return porcentagem de usuários ativos
     */
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

    /**
     * Retorna os usuários recentemente registrados.
     *
     * @param limit número máximo de usuários a retornar
     * @return lista de usuários recentes
     */
    @Transactional(readOnly = true)
    public List<UserDTO> getRecentlyRegisteredUsers(int limit) {
        return userRepository.findByDeletedFalseOrderByCreatedAtDesc(PageRequest.of(0, limit))
                .stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
    }
}