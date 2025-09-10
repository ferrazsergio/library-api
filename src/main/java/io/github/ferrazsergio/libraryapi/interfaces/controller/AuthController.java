package io.github.ferrazsergio.libraryapi.interfaces.controller;

import io.github.ferrazsergio.libraryapi.domain.model.User;
import io.github.ferrazsergio.libraryapi.infrastructure.repository.UserRepository;
import io.github.ferrazsergio.libraryapi.interfaces.dto.AuthRequestDTO;
import io.github.ferrazsergio.libraryapi.interfaces.dto.AuthResponseDTO;
import io.github.ferrazsergio.libraryapi.interfaces.dto.UserDTO;
import io.github.ferrazsergio.libraryapi.interfaces.dto.swagger.ApiErrorResponse;
import io.github.ferrazsergio.libraryapi.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication operations for user login and registration")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    @Operation(
            summary = "Authenticate user and get JWT token",
            description = "Authenticates a user with email and password credentials and returns a JWT token for authorization. " +
                    "This token must be included in the Authorization header for protected endpoints.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Authentication successful",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = AuthResponseDTO.class),
                                    examples = @ExampleObject(value = "{\"token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\"}")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Invalid credentials",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"status\": 401, \"title\": \"Unauthorized\", \"message\": \"Invalid email or password\", \"timestamp\": \"2025-09-10T19:09:13\", \"path\": \"/api/v1/auth/login\"}")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input format or missing required fields",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"status\": 400, \"title\": \"Bad Request\", \"message\": \"Validation failed\", \"timestamp\": \"2025-09-10T19:09:13\", \"path\": \"/api/v1/auth/login\", \"errors\": [{\"field\": \"email\", \"message\": \"must not be blank\", \"rejectedValue\": \"\"}]}")
                            )
                    )
            }
    )
    @SecurityRequirements() // Removes security requirements for this endpoint
    public ResponseEntity<AuthResponseDTO> login(
            @Parameter(
                    description = "Login credentials",
                    required = true,
                    schema = @Schema(implementation = AuthRequestDTO.class),
                    examples = @ExampleObject(value = "{\"email\": \"admin@example.com\", \"password\": \"password123\"}")
            )
            @Valid @RequestBody AuthRequestDTO authRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.getEmail(),
                        authRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtTokenProvider.createToken(authentication);

        return ResponseEntity.ok(new AuthResponseDTO(token));
    }

    @PostMapping("/register")
    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account with the provided details. The role field can be set to ADMIN, LIBRARIAN, " +
                    "or READER. If an invalid role is provided, READER will be used as the default.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "User successfully registered",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = UserDTO.class),
                                    examples = @ExampleObject(value = "{\"id\": 1, \"name\": \"Admin User\", \"email\": \"admin@example.com\", \"role\": \"ADMIN\"}")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Email already in use",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"status\": 409, \"title\": \"Conflict\", \"message\": \"Email already in use\", \"timestamp\": \"2025-09-10T19:09:13\", \"path\": \"/api/v1/auth/register\"}")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input format or missing required fields",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"status\": 400, \"title\": \"Bad Request\", \"message\": \"Validation failed\", \"timestamp\": \"2025-09-10T19:09:13\", \"path\": \"/api/v1/auth/register\", \"errors\": [{\"field\": \"email\", \"message\": \"must be a valid email\", \"rejectedValue\": \"invalid-email\"}]}")
                            )
                    )
            }
    )
    @SecurityRequirements() // Removes security requirements for this endpoint
    public ResponseEntity<UserDTO> register(
            @Parameter(
                    description = "User registration details",
                    required = true,
                    schema = @Schema(implementation = UserDTO.class),
                    examples = @ExampleObject(value = "{\"name\": \"Admin User\", \"email\": \"admin@example.com\", \"password\": \"password123\", \"role\": \"ADMIN\"}")
            )
            @Valid @RequestBody UserDTO userDTO) {

        // Check if user exists
        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        // Create new user
        User user = new User();
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        try {
            user.setRole(User.Role.valueOf(userDTO.getRole()));
        } catch (IllegalArgumentException e) {
            user.setRole(User.Role.READER); // Default role
        }

        User savedUser = userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UserDTO.fromEntity(savedUser));
    }
}