/*package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ferrazsergio.libraryapi.application.service.CategoryService;
import io.github.ferrazsergio.libraryapi.interfaces.controller.CategoryController;
import io.github.ferrazsergio.libraryapi.interfaces.dto.CategoryDTO;
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

@WebMvcTest(CategoryController.class)
public class CategoryControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public CategoryService categoryService() {
            return Mockito.mock(CategoryService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryService categoryService;

    private CategoryDTO categoryDTO;

    @BeforeEach
    void setUp() {
        // Reset mock before each test
        Mockito.reset(categoryService);

        categoryDTO = new CategoryDTO();
        categoryDTO.setId(1);
        categoryDTO.setName("Fiction");
        categoryDTO.setDescription("Fiction books");
    }

    @Test
    @WithMockUser(roles = "READER")
    void getAllCategoriesShouldReturnCategories() throws Exception {
        when(categoryService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(categoryDTO)));

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("Fiction")));
    }

    @Test
    @WithMockUser(roles = "READER")
    void getCategoryByIdShouldReturnCategory() throws Exception {
        when(categoryService.findById(1)).thenReturn(categoryDTO);

        mockMvc.perform(get("/api/v1/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Fiction")));
    }

    @Test
    @WithMockUser(roles = "READER")
    void getCategoryByNameShouldReturnCategory() throws Exception {
        when(categoryService.findByName("Fiction")).thenReturn(categoryDTO);

        mockMvc.perform(get("/api/v1/categories/name/Fiction"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void createCategoryShouldReturnCreatedCategory() throws Exception {
        when(categoryService.create(any(CategoryDTO.class))).thenReturn(categoryDTO);

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Fiction")));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void updateCategoryShouldReturnUpdatedCategory() throws Exception {
        when(categoryService.update(eq(1), any(CategoryDTO.class))).thenReturn(categoryDTO);

        mockMvc.perform(put("/api/v1/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Fiction")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCategoryShouldReturnNoContent() throws Exception {
        doNothing().when(categoryService).delete(1);

        mockMvc.perform(delete("/api/v1/categories/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void unauthorizedUserCannotAccessCategories() throws Exception {
        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "READER")
    void readerCannotCreateCategory() throws Exception {
        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void librarianCannotDeleteCategory() throws Exception {
        mockMvc.perform(delete("/api/v1/categories/1"))
                .andExpect(status().isForbidden());
    }
}*/