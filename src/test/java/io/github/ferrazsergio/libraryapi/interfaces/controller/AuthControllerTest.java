package io.github.ferrazsergio.libraryapi.interfaces.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ferrazsergio.libraryapi.domain.model.User;
import io.github.ferrazsergio.libraryapi.infrastructure.repository.UserRepository;
import io.github.ferrazsergio.libraryapi.interfaces.dto.AuthRequestDTO;
import io.github.ferrazsergio.libraryapi.interfaces.dto.UserDTO;
import io.github.ferrazsergio.libraryapi.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = AuthController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private UserDTO userDTO;
    private AuthRequestDTO authRequestDTO;
    private User user;

    @BeforeEach
    void setUp() {
        reset(authenticationManager, userRepository, passwordEncoder, jwtTokenProvider);

        userDTO = new UserDTO();
        userDTO.setName("John Doe");
        userDTO.setEmail("john@example.com");
        userDTO.setPassword("password123");
        userDTO.setRole(User.Role.READER);

        authRequestDTO = new AuthRequestDTO();
        authRequestDTO.setEmail("john@example.com");
        authRequestDTO.setPassword("password123");

        user = new User();
        user.setId(1);
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setPassword("encodedPassword");
        user.setRole(User.Role.READER);
        user.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void loginShouldReturnToken() throws Exception {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                authRequestDTO.getEmail(), authRequestDTO.getPassword(), Collections.emptyList());

        doReturn(authentication).when(authenticationManager).authenticate(any(Authentication.class));

        when(jwtTokenProvider.createToken(any(Authentication.class)))
                .thenReturn("jwt-token");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequestDTO))
                        .with(csrf())) // Adicione o token CSRF
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void registerShouldCreateNewUser() throws Exception {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO))
                        .with(csrf())) // Adicione o token CSRF
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void registerShouldReturnConflictWhenEmailExists() throws Exception {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO))
                        .with(csrf())) // Adicione o token CSRF
                .andExpect(status().isConflict());
    }

    @Test
    void loginShouldFailWithInvalidCredentials() throws Exception {
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequestDTO))
                        .with(csrf())) // Adicione o token CSRF
                .andExpect(status().isUnauthorized());
    }
}