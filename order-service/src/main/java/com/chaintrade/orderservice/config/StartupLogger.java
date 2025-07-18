package com.chaintrade.orderservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.file.Paths;

@Component
public class StartupLogger implements CommandLineRunner {

    @Value("${spring.datasource.db-path}")
    private String dbPath;

    @Override
    public void run(String... args) {
        if (StringUtils.hasText(dbPath)) {
            String expandedPath = dbPath.startsWith("~")
                    ? System.getProperty("user.home") + dbPath.substring(1)
                    : dbPath;
            try {
                String databaseHome = Paths.get(expandedPath).toAbsolutePath().normalize().toString();
                System.out.println("Database directory found: " + databaseHome);
            } catch (Exception e) {
                System.err.println("Failed to resolve DB path: " + e.getMessage());
            }
        }
    }
}