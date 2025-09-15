/*package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ferrazsergio.libraryapi.application.service.BookService;
import io.github.ferrazsergio.libraryapi.interfaces.controller.BookController;
import io.github.ferrazsergio.libraryapi.interfaces.dto.BookDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
public class BookControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public BookService bookService() {
            return Mockito.mock(BookService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookService bookService;

    private BookDTO bookDTO;

    @BeforeEach
    void setUp() {
        // Reset mock before each test
        Mockito.reset(bookService);

        bookDTO = new BookDTO();
        bookDTO.setId(1);
        bookDTO.setIsbn("9780451524935");
        bookDTO.setTitle("1984");
        bookDTO.setDescription("Dystopian novel");
        bookDTO.setPublishDate(LocalDate.of(1949, 6, 8));
        bookDTO.setAvailableQuantity(5);
        bookDTO.setTotalQuantity(5);
        bookDTO.setPublisher("Secker & Warburg");
        bookDTO.setCategoryId(1);
        bookDTO.setAuthorIds(Set.of(1));
    }

    @Test
    @WithMockUser(roles = "READER")
    void getAllBooksShouldReturnBooks() throws Exception {
        when(bookService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(bookDTO)));

        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", is("1984")));
    }

    @Test
    @WithMockUser(roles = "READER")
    void getBookByIdShouldReturnBook() throws Exception {
        when(bookService.findById(1)).thenReturn(bookDTO);

        mockMvc.perform(get("/api/v1/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("1984")))
                .andExpect(jsonPath("$.isbn", is("9780451524935")));
    }

    @Test
    @WithMockUser(roles = "READER")
    void getBookByIsbnShouldReturnBook() throws Exception {
        when(bookService.findByIsbn("9780451524935")).thenReturn(bookDTO);

        mockMvc.perform(get("/api/v1/books/isbn/9780451524935"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("1984")));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void createBookShouldReturnCreatedBook() throws Exception {
        when(bookService.create(any(BookDTO.class))).thenReturn(bookDTO);

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("1984")));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void updateBookShouldReturnUpdatedBook() throws Exception {
        when(bookService.update(eq(1), any(BookDTO.class))).thenReturn(bookDTO);

        mockMvc.perform(put("/api/v1/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("1984")));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void deleteBookShouldReturnNoContent() throws Exception {
        doNothing().when(bookService).delete(1);

        mockMvc.perform(delete("/api/v1/books/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "READER")
    void searchBooksByTitleShouldReturnBooks() throws Exception {
        when(bookService.findByTitle(anyString(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(bookDTO)));

        mockMvc.perform(get("/api/v1/books/search/title?title=1984"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", is("1984")));
    }

    @Test
    @WithMockUser(roles = "READER")
    void getMostBorrowedBooksShouldReturnBooks() throws Exception {
        when(bookService.findMostBorrowedBooks(10))
                .thenReturn(List.of(bookDTO));

        mockMvc.perform(get("/api/v1/books/most-borrowed?limit=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("1984")));
    }

    @Test
    void unauthorizedUserCannotAccessBooks() throws Exception {
        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "READER")
    void readerCannotCreateBook() throws Exception {
        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookDTO)))
                .andExpect(status().isForbidden());
    }
}*/