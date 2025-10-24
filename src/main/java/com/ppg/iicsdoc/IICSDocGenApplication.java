package com.ppg.iicsdoc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Bootstrap class for the IICS Documentation Generator application.
 * 
 * <p>
 * This class serves as the main entry point for launching the application
 * in either CLI mode or standard Spring Boot mode, depending on the 
 * provided command-line arguments.
 * </p>
 * 
 * <p>
 * If the first argument starts with {@code --input=} or {@code --help}, the
 * application switches to the {@code cli} Spring profile and delegates
 * execution to the {@link DocumentationGeneratorCLI} context.
 * </p>
 * 
 * <p>
 * CLI mode is intended for headless execution, typically used in 
 * automation scripts or termina-based workflows. Standard mode enables the
 * full application-stack, including web interfaces or scheduled jobs if
 * configured.
 * </p>
 * 
 * <p>
 * Example CLI Usage:
 * </p>
 * <pre>{@code 
 * java -jar iics-doc-gen.jar --input=process.xml --output=./docs
 * }</pre>
 * 
 * @see DocumentationGeneratorCLI
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-21
 */
@SpringBootApplication
@EnableAsync
public class IICSDocGenApplication {
    public static void main(String[] args) {
        if (args.length > 0 &&
                (args[0].startsWith("--input=") ||
                        args[0].startsWith("--help"))) {
            System.setProperty("spring.profiles.active", "cli");
            SpringApplication.run(
                    com.ppg.iicsdoc.cli.DocumentationGeneratorCLI.class,
                    args);
        } else {
            SpringApplication.run(IICSDocGenApplication.class, args);
        }
    }
}