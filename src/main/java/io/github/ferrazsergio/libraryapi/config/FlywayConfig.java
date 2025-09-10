package io.github.ferrazsergio.libraryapi.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import jakarta.annotation.PostConstruct;

@Configuration
public class FlywayConfig {

    @Autowired
    private DataSource dataSource;

    @PostConstruct
    public void repairFlyway() {
        Flyway.configure()
                .dataSource(dataSource)
                .load()
                .repair();
    }
}