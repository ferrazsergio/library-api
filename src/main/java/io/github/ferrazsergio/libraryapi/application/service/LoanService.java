package io.github.ferrazsergio.libraryapi.application.service;

import io.github.ferrazsergio.libraryapi.domain.model.Book;
import io.github.ferrazsergio.libraryapi.domain.model.Loan;
import io.github.ferrazsergio.libraryapi.domain.model.User;
import io.github.ferrazsergio.libraryapi.infrastructure.repository.BookRepository;
import io.github.ferrazsergio.libraryapi.infrastructure.repository.FineRepository;
import io.github.ferrazsergio.libraryapi.infrastructure.repository.LoanRepository;
import io.github.ferrazsergio.libraryapi.infrastructure.repository.UserRepository;
import io.github.ferrazsergio.libraryapi.interfaces.dto.LoanDTO;
import io.github.ferrazsergio.libraryapi.interfaces.dto.RecentActivityDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    public LoanDTO returnBook(Integer loanId) {
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
    public LoanDTO renewLoan(Integer loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found with ID: " + loanId));

        loan.renew();
        Loan updatedLoan = loanRepository.save(loan);

        return LoanDTO.fromEntity(updatedLoan);
    }

    @Transactional(readOnly = true)
    public LoanDTO findById(Integer id) {
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
    public Page<LoanDTO> findByUser(Integer userId, Pageable pageable) {
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

    // ==================== NOVOS MÉTODOS PARA DASHBOARD ====================

    /**
     * Retorna o número total de empréstimos no sistema.
     *
     * @return total de empréstimos
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "loanStats", key = "'totalLoans'")
    public long getTotalLoans() {
        return loanRepository.getTotalLoans();
    }

    /**
     * Retorna o número de empréstimos ativos no sistema.
     *
     * @return total de empréstimos ativos
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "loanStats", key = "'activeLoans'")
    public long getActiveLoansCount() {
        return loanRepository.countByStatus(Loan.LoanStatus.ACTIVE);
    }

    /**
     * Retorna o número de empréstimos em atraso no sistema.
     *
     * @return total de empréstimos em atraso
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "loanStats", key = "'overdueLoans'")
    public long getOverdueLoansCount() {
        return loanRepository.countOverdueLoans();
    }

    /**
     * Retorna a taxa de devolução dentro do prazo (em porcentagem).
     *
     * @return taxa de devolução dentro do prazo
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "loanStats", key = "'onTimeReturnRate'")
    public double getOnTimeReturnRate() {
        long totalReturned = loanRepository.countByStatus(Loan.LoanStatus.RETURNED);
        if (totalReturned == 0) {
            return 0.0;
        }

        long returnedOnTime = loanRepository.countReturnedOnTime();
        return (double) returnedOnTime / totalReturned * 100.0;
    }

    /**
     * Retorna as atividades de empréstimo mais recentes para o dashboard.
     *
     * @param limit número máximo de atividades a retornar
     * @return lista de atividades recentes
     */
    @Transactional(readOnly = true)
    public List<RecentActivityDTO> getRecentLoanActivities(int limit) {
        return loanRepository.findRecentLoanActivities(PageRequest.of(0, limit))
                .stream()
                .map(loan -> {
                    String activityType;
                    String description;

                    if (loan.getReturnDate() != null) {
                        activityType = "RETURN";
                        description = "Livro devolvido";
                    } else if (loan.getRenewalCount() > 0) {
                        activityType = "RENEWAL";
                        description = "Empréstimo renovado";
                    } else {
                        activityType = "LOAN";
                        description = "Novo empréstimo";
                    }

                    return RecentActivityDTO.builder()
                            .id(Long.valueOf(loan.getId()))
                            .activityType(activityType)
                            .description(description)
                            .timestamp(loan.getLastUpdated() != null ? loan.getLastUpdated() : LocalDateTime.now())
                            .userName(loan.getUser().getName())
                            .bookTitle(loan.getBook().getTitle())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Retorna estatísticas de empréstimos por mês (últimos 6 meses).
     *
     * @return mapa com contagem de empréstimos por mês
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "loanStats", key = "'loansByMonth'")
    public List<Object[]> getLoanStatisticsByMonth() {
        return loanRepository.getLoanStatisticsByMonth();
    }
}