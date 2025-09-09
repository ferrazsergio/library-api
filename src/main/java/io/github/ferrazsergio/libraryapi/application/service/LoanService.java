package io.github.ferrazsergio.libraryapi.application.service;

import io.github.ferrazsergio.libraryapi.domain.model.Book;
import io.github.ferrazsergio.libraryapi.domain.model.Loan;
import io.github.ferrazsergio.libraryapi.domain.model.User;
import io.github.ferrazsergio.libraryapi.infrastructure.repository.BookRepository;
import io.github.ferrazsergio.libraryapi.infrastructure.repository.FineRepository;
import io.github.ferrazsergio.libraryapi.infrastructure.repository.LoanRepository;
import io.github.ferrazsergio.libraryapi.infrastructure.repository.UserRepository;
import io.github.ferrazsergio.libraryapi.interfaces.dto.LoanDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final FineRepository fineRepository;

    @Transactional
    public LoanDTO createLoan(LoanDTO loanDTO) {
        // Fetch book and user
        Book book = bookRepository.findById(loanDTO.getBookId())
                .orElseThrow(() -> new RuntimeException("Book not found with ID: " + loanDTO.getBookId()));

        User user = userRepository.findById(loanDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + loanDTO.getUserId()));

        // Check if book is available
        if (!book.isAvailable()) {
            throw new RuntimeException("Book is not available for loan");
        }

        // Check if user has unpaid fines
        Double unpaidFines = fineRepository.getTotalUnpaidFinesForUser(user.getId());
        if (unpaidFines != null && unpaidFines > 0) {
            throw new RuntimeException("User has unpaid fines. Please pay them before borrowing more books.");
        }

        // Create the loan
        Loan loan = new Loan();
        loan.setBook(book);
        loan.setUser(user);
        loan.setLoanDate(LocalDate.now());

        // Default loan period is 14 days
        loan.setExpectedReturnDate(LocalDate.now().plusDays(14));
        loan.setStatus(Loan.LoanStatus.ACTIVE);

        // Update book availability
        book.decreaseAvailableQuantity();
        bookRepository.save(book);

        // Save the loan
        Loan savedLoan = loanRepository.save(loan);

        return LoanDTO.fromEntity(savedLoan);
    }

    @Transactional
    public LoanDTO returnBook(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found with ID: " + loanId));

        if (loan.getStatus() != Loan.LoanStatus.ACTIVE) {
            throw new RuntimeException("This book has already been returned");
        }

        // Return the book
        loan.returnBook();

        // Increase book availability
        Book book = loan.getBook();
        book.increaseAvailableQuantity();
        bookRepository.save(book);

        // Save the updated loan
        Loan updatedLoan = loanRepository.save(loan);

        return LoanDTO.fromEntity(updatedLoan);
    }

    @Transactional
    public LoanDTO renewLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found with ID: " + loanId));

        loan.renew();
        Loan updatedLoan = loanRepository.save(loan);

        return LoanDTO.fromEntity(updatedLoan);
    }

    @Transactional(readOnly = true)
    public LoanDTO findById(Long id) {
        return loanRepository.findById(id)
                .map(LoanDTO::fromEntity)
                .orElseThrow(() -> new RuntimeException("Loan not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public Page<LoanDTO> findAll(Pageable pageable) {
        return loanRepository.findAll(pageable)
                .map(LoanDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<LoanDTO> findByUser(Long userId, Pageable pageable) {
        // Verify user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        return loanRepository.findByUserId(userId, pageable)
                .map(LoanDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<LoanDTO> findOverdueLoans() {
        return loanRepository.findAllOverdueLoans()
                .stream()
                .map(LoanDTO::fromEntity)
                .collect(Collectors.toList());
    }
}