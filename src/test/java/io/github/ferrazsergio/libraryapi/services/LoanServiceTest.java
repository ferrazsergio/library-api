package io.github.ferrazsergio.libraryapi.services;

import io.github.ferrazsergio.libraryapi.application.service.LoanService;
import io.github.ferrazsergio.libraryapi.domain.model.Book;
import io.github.ferrazsergio.libraryapi.domain.model.Loan;
import io.github.ferrazsergio.libraryapi.domain.model.User;
import io.github.ferrazsergio.libraryapi.infrastructure.repository.BookRepository;
import io.github.ferrazsergio.libraryapi.infrastructure.repository.FineRepository;
import io.github.ferrazsergio.libraryapi.infrastructure.repository.LoanRepository;
import io.github.ferrazsergio.libraryapi.infrastructure.repository.UserRepository;
import io.github.ferrazsergio.libraryapi.interfaces.dto.LoanDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FineRepository fineRepository;

    @InjectMocks
    private LoanService loanService;

    private User user;
    private Book book;
    private Loan loan;
    private LoanDTO loanDTO;

    @BeforeEach
    void setUp() {
        // Setup user
        user = new User();
        user.setId(1);
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setPassword("password");
        user.setRole(User.Role.READER);
        user.setCreatedAt(LocalDateTime.now());

        // Setup book
        book = new Book();
        book.setId(1);
        book.setTitle("1984");
        book.setIsbn("9780451524935");
        book.setAvailableQuantity(4);
        book.setTotalQuantity(5);
        book.setDeleted(false);

        // Setup loan
        loan = new Loan();
        loan.setId(1);
        loan.setUser(user);
        loan.setBook(book);
        loan.setLoanDate(LocalDate.now());
        loan.setExpectedReturnDate(LocalDate.now().plusDays(14));
        loan.setStatus(Loan.LoanStatus.ACTIVE);

        // Setup loanDTO
        loanDTO = new LoanDTO();
        loanDTO.setUserId(1);
        loanDTO.setBookId(1);
    }

    @Test
    void createLoanShouldCreateNewLoan() {
        // Arrange
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(fineRepository.getTotalUnpaidFinesForUser(1)).thenReturn(0.0);
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);

        // Act
        LoanDTO result = loanService.createLoan(loanDTO);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getBookId());
        assertEquals(1, result.getUserId());
        verify(bookRepository, times(1)).save(any(Book.class)); // Check that book quantity was updated
    }

    @Test
    void createLoanShouldThrowExceptionWhenBookNotAvailable() {
        // Arrange
        book.setAvailableQuantity(0);
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            loanService.createLoan(loanDTO);
        });

        assertTrue(exception.getMessage().contains("not available"));
        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    void createLoanShouldThrowExceptionWhenUserHasUnpaidFines() {
        // Arrange
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(fineRepository.getTotalUnpaidFinesForUser(1)).thenReturn(10.0);

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            loanService.createLoan(loanDTO);
        });

        assertTrue(exception.getMessage().contains("unpaid fines"));
        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    void returnBookShouldUpdateLoanStatus() {
        // Arrange
        when(loanRepository.findById(1)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);

        // Act
        LoanDTO result = loanService.returnBook(1);

        // Assert
        assertNotNull(result);
        assertEquals(Loan.LoanStatus.RETURNED.name(), result.getStatus());
        verify(bookRepository, times(1)).save(any(Book.class)); // Check that book quantity was updated
    }

    @Test
    void returnBookShouldThrowExceptionWhenLoanNotActive() {
        // Arrange
        loan.setStatus(Loan.LoanStatus.RETURNED);
        when(loanRepository.findById(1)).thenReturn(Optional.of(loan));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            loanService.returnBook(1);
        });

        assertTrue(exception.getMessage().contains("already been returned"));
        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    void renewLoanShouldExtendExpectedReturnDate() {
        // Arrange
        LocalDate originalDate = loan.getExpectedReturnDate();
        when(loanRepository.findById(1)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);

        // Act
        LoanDTO result = loanService.renewLoan(1);

        // Assert
        assertNotNull(result);
        assertTrue(loan.getExpectedReturnDate().isAfter(originalDate));
        verify(loanRepository, times(1)).save(any(Loan.class));
    }

    @Test
    void findByIdShouldReturnLoanWhenExists() {
        // Arrange
        when(loanRepository.findById(1)).thenReturn(Optional.of(loan));

        // Act
        LoanDTO result = loanService.findById(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getUserId());
        assertEquals(1, result.getBookId());
    }

    @Test
    void findAllShouldReturnPageOfLoans() {
        // Arrange
        List<Loan> loans = List.of(loan);
        Page<Loan> loanPage = new PageImpl<>(loans);
        Pageable pageable = PageRequest.of(0, 10);

        when(loanRepository.findAll(pageable)).thenReturn(loanPage);

        // Act
        Page<LoanDTO> result = loanService.findAll(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void findOverdueLoansShouldReturnList() {
        // Arrange
        List<Loan> loans = List.of(loan);
        when(loanRepository.findAllOverdueLoans()).thenReturn(loans);

        // Act
        List<LoanDTO> result = loanService.findOverdueLoans();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
