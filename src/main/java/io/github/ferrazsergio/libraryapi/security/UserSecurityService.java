package io.github.ferrazsergio.libraryapi.security;

import io.github.ferrazsergio.libraryapi.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserSecurityService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public boolean isSameUser(Integer userId, Authentication authentication) {
        if (authentication == null) {
            return false;
        }

        String email = authentication.getName();

        return userRepository.findById(userId)
                .map(user -> user.getEmail().equals(email))
                .orElse(false);
    }
}