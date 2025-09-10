package io.github.ferrazsergio.libraryapi.security;

import io.github.ferrazsergio.libraryapi.infrastructure.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoanSecurityService {

    private final LoanRepository loanRepository;

    @Transactional(readOnly = true)
    public boolean isLoanOwner(Integer loanId, Authentication authentication) {
        if (authentication == null) {
            return false;
        }

        String email = authentication.getName();

        return loanRepository.findById(loanId)
                .map(loan -> loan.getUser().getEmail().equals(email))
                .orElse(false);
    }
}