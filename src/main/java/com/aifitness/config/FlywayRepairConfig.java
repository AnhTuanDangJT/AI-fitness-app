package com.aifitness.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

/**
 * Production-only Flyway migration strategy that offers a one-time repair hook.
 * This allows us to fix checksum mismatches without touching migration files.
 */
@Configuration
@Profile("production")
public class FlywayRepairConfig {

    private static final Logger logger = LoggerFactory.getLogger(FlywayRepairConfig.class);
    private static final String REPAIR_FLAG = "FLYWAY_REPAIR_ONCE";

    @Bean
    public FlywayMigrationStrategy flywayRepairStrategy(Environment environment) {
        return flyway -> {
            if (shouldRunRepair(environment)) {
                logger.warn("Running Flyway REPAIR (one-time)");
                flyway.repair();
            } else {
                logger.info("Flyway repair flag disabled; running migrate() only");
            }
            logger.info("Executing Flyway migrate()");
            flyway.migrate();
        };
    }

    private boolean shouldRunRepair(Environment environment) {
        String flagValue = environment.getProperty(REPAIR_FLAG, "false");
        return "true".equalsIgnoreCase(flagValue);
    }
}






