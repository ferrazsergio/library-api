package io.github.ferrazsergio.libraryapi.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Bean
    public OpenAPI libraryApi() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(servers())
                .tags(tags())
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, securityScheme()));
    }

    private Info apiInfo() {
        return new Info()
                .title("Library Management System API")
                .description("RESTful API for managing a library with books, authors, loans, and more")
                .version("1.0.0")
                .contact(new Contact()
                        .name("Sergio Ferraz")
                        .email("contact@ferrazsergio.github.io")
                        .url("https://github.com/ferrazsergio"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"))
                .termsOfService("https://github.com/ferrazsergio/library-api/blob/main/TERMS.md");
    }

    private List<Server> servers() {
        Server localServer = new Server()
                .url("http://localhost:8080")
                .description("Local Development Server");

        Server stagingServer = new Server()
                .url("https://staging-library-api.ferrazsergio.github.io")
                .description("Staging Server");

        Server productionServer = new Server()
                .url("https://library-api.ferrazsergio.github.io")
                .description("Production Server");

        // Só mostra os servidores relevantes para o ambiente atual
        if ("dev".equals(activeProfile)) {
            return List.of(localServer);
        } else if ("staging".equals(activeProfile)) {
            return List.of(stagingServer, localServer);
        } else {
            return List.of(productionServer, stagingServer);
        }
    }

    private List<Tag> tags() {
        return Arrays.asList(
                new Tag().name("Authentication").description("Authentication operations"),
                new Tag().name("Books").description("Book management operations"),
                new Tag().name("Authors").description("Author management operations"),
                new Tag().name("Categories").description("Category management operations"),
                new Tag().name("Loans").description("Loan and return operations"),
                new Tag().name("Users").description("User management operations"),
                new Tag().name("Reports").description("Reporting and statistics operations"),
                new Tag().name("Dashboard").description("Dashboard statistics")
        );
    }

    private SecurityScheme securityScheme() {
        return new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Provide a JWT token. You can get a token by using the Authentication API.");
    }
}