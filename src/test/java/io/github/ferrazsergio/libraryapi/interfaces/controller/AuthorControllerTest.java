package io.github.ferrazsergio.libraryapi.interfaces.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ferrazsergio.libraryapi.application.service.AuthorService;
import io.github.ferrazsergio.libraryapi.config.SecurityConfig;
import io.github.ferrazsergio.libraryapi.interfaces.dto.AuthorDTO;
import io.github.ferrazsergio.libraryapi.interfaces.dto.BookDTO;
import io.github.ferrazsergio.libraryapi.security.JwtTokenProvider;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@WebMvcTest(AuthorController.class)
@Import(SecurityConfig.class)
class AuthorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthorService authorService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public AuthorService authorService() {
            return Mockito.mock(AuthorService.class);
        }
    }

    private AuthorDTO authorDTO;
    private BookDTO bookDTO;

    @BeforeEach
    void setUp() {
        // Reset mock before each test
        Mockito.reset(authorService);

        // Setup authorDTO
        authorDTO = new AuthorDTO();
        authorDTO.setId(1);
        authorDTO.setName("George Orwell");
        authorDTO.setBiography("English novelist");
        authorDTO.setBirthDate(LocalDate.of(1903, 6, 25));

        // Setup bookDTO
        bookDTO = new BookDTO();
        bookDTO.setId(1);
        bookDTO.setTitle("1984");
        bookDTO.setIsbn("9780451524935");
    }

    @Test
    @WithMockUser(roles = "READER")
    void getAllAuthorsShouldReturnAuthors() throws Exception {
        when(authorService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(authorDTO)));

        mockMvc.perform(get("/api/v1/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("George Orwell")));
    }

    @Test
    @WithMockUser(roles = "READER")
    void getAuthorByIdShouldReturnAuthor() throws Exception {
        when(authorService.findById(1)).thenReturn(authorDTO);

        mockMvc.perform(get("/api/v1/authors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("George Orwell")));
    }

    @Test
    @WithMockUser(roles = "READER")
    void searchAuthorsByNameShouldReturnAuthors() throws Exception {
        when(authorService.findByName(eq("Orwell"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(authorDTO)));

        mockMvc.perform(get("/api/v1/authors/search?name=Orwell"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("George Orwell")));
    }

    @Test
    @WithMockUser(roles = "READER")
    void getBooksByAuthorShouldReturnBooks() throws Exception {
        when(authorService.findBooksByAuthor(eq(1), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(bookDTO)));

        mockMvc.perform(get("/api/v1/authors/1/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", is("1984")));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void createAuthorShouldReturnCreatedAuthor() throws Exception {
        when(authorService.create(any(AuthorDTO.class))).thenReturn(authorDTO);

        mockMvc.perform(post("/api/v1/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authorDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("George Orwell")));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void updateAuthorShouldReturnUpdatedAuthor() throws Exception {
        when(authorService.update(eq(1), any(AuthorDTO.class))).thenReturn(authorDTO);

        mockMvc.perform(put("/api/v1/authors/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authorDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("George Orwell")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteAuthorShouldReturnNoContent() throws Exception {
        doNothing().when(authorService).delete(1);

        mockMvc.perform(delete("/api/v1/authors/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void unauthorizedUserCannotAccessAuthors() throws Exception {
        mockMvc.perform(get("/api/v1/authors"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "READER")
    void readerCannotCreateAuthor() throws Exception {
        mockMvc.perform(post("/api/v1/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authorDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void librarianCannotDeleteAuthor() throws Exception {
        mockMvc.perform(delete("/api/v1/authors/1"))
                .andExpect(status().isForbidden());
    }
}