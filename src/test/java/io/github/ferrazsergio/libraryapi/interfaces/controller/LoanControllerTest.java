package io.github.ferrazsergio.libraryapi.interfaces.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ferrazsergio.libraryapi.application.service.LoanService;
import io.github.ferrazsergio.libraryapi.config.SecurityConfig;
import io.github.ferrazsergio.libraryapi.interfaces.dto.LoanDTO;
import io.github.ferrazsergio.libraryapi.security.JwtTokenProvider;
import io.github.ferrazsergio.libraryapi.security.LoanSecurityService;
import io.github.ferrazsergio.libraryapi.security.UserSecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LoanController.class)
@Import(SecurityConfig.class)
class LoanControllerTest {

    @TestConfiguration
    static class SecurityBeans {
        @Bean("loanSecurityService")
        public LoanSecurityService loanSecurityService() {
            return Mockito.mock(LoanSecurityService.class);
        }
        @Bean("userSecurityService")
        public UserSecurityService userSecurityService() {
            return Mockito.mock(UserSecurityService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LoanService loanService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserDetailsService userDetailsService;

    // NÃO declare @MockitoBean para LoanSecurityService e UserSecurityService, pois já estão como beans nomeados acima

    @Autowired
    private LoanSecurityService loanSecurityService;
    @Autowired
    private UserSecurityService userSecurityService;

    private LoanDTO loanDTO;

    @BeforeEach
    void setUp() {
        Mockito.reset(loanService, loanSecurityService, userSecurityService);

        loanDTO = new LoanDTO();
        loanDTO.setId(1);
        loanDTO.setUserId(1);
        loanDTO.setBookId(1);
        loanDTO.setLoanDate(LocalDate.now());
        loanDTO.setExpectedReturnDate(LocalDate.now().plusDays(14));
        loanDTO.setStatus("ACTIVE");
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void getAllLoansShouldReturnLoans() throws Exception {
        when(loanService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(loanDTO)));

        mockMvc.perform(get("/api/v1/loans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].status", is("ACTIVE")));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void getLoanByIdShouldReturnLoan() throws Exception {
        when(loanService.findById(1)).thenReturn(loanDTO);

        mockMvc.perform(get("/api/v1/loans/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is(1)))
                .andExpect(jsonPath("$.bookId", is(1)));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void getLoansByUserShouldReturnLoans() throws Exception {
        when(loanService.findByUser(eq(1), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(loanDTO)));

        mockMvc.perform(get("/api/v1/loans/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    @WithMockUser(roles = "READER")
    void getLoansByUserAsOwnerShouldReturnLoans() throws Exception {
        when(userSecurityService.isSameUser(eq(1), any())).thenReturn(true);
        when(loanService.findByUser(eq(1), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(loanDTO)));

        mockMvc.perform(get("/api/v1/loans/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void createLoanShouldReturnCreatedLoan() throws Exception {
        when(loanService.createLoan(any(LoanDTO.class))).thenReturn(loanDTO);

        mockMvc.perform(post("/api/v1/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loanDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void returnBookShouldReturnUpdatedLoan() throws Exception {
        LoanDTO returnedLoan = new LoanDTO();
        returnedLoan.setId(1);
        returnedLoan.setUserId(1);
        returnedLoan.setBookId(1);
        returnedLoan.setLoanDate(LocalDate.now().minusDays(5));
        returnedLoan.setExpectedReturnDate(LocalDate.now().plusDays(9));
        returnedLoan.setReturnDate(LocalDate.now());
        returnedLoan.setStatus("RETURNED");

        when(loanService.returnBook(1)).thenReturn(returnedLoan);

        mockMvc.perform(put("/api/v1/loans/1/return"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("RETURNED")));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void renewLoanShouldReturnRenewedLoan() throws Exception {
        LoanDTO renewedLoan = new LoanDTO();
        renewedLoan.setId(1);
        renewedLoan.setUserId(1);
        renewedLoan.setBookId(1);
        renewedLoan.setLoanDate(LocalDate.now().minusDays(5));
        renewedLoan.setExpectedReturnDate(LocalDate.now().plusDays(23)); // Extended by 14 days
        renewedLoan.setStatus("ACTIVE");

        when(loanService.renewLoan(1)).thenReturn(renewedLoan);

        mockMvc.perform(put("/api/v1/loans/1/renew"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }

    @Test
    @WithMockUser(roles = "READER")
    void readerCanRenewOwnLoan() throws Exception {
        when(loanSecurityService.isLoanOwner(eq(1), any())).thenReturn(true);

        LoanDTO renewedLoan = new LoanDTO();
        renewedLoan.setId(1);
        renewedLoan.setUserId(1);
        renewedLoan.setBookId(1);
        renewedLoan.setExpectedReturnDate(LocalDate.now().plusDays(23));
        renewedLoan.setStatus("ACTIVE");

        when(loanService.renewLoan(1)).thenReturn(renewedLoan);

        mockMvc.perform(put("/api/v1/loans/1/renew"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void getOverdueLoansShouldReturnLoans() throws Exception {
        when(loanService.findOverdueLoans())
                .thenReturn(List.of(loanDTO));

        mockMvc.perform(get("/api/v1/loans/overdue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void unauthorizedUserCannotAccessLoans() throws Exception {
        mockMvc.perform(get("/api/v1/loans"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "READER")
    void readerCannotAccessAllLoans() throws Exception {
        mockMvc.perform(get("/api/v1/loans"))
                .andExpect(status().isForbidden());
    }
}