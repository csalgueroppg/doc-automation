package com.ppg.iicsdoc.cli;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;

import com.ppg.iicsdoc.ai.AIAgentService;
import com.ppg.iicsdoc.deployer.DeploymentService;
import com.ppg.iicsdoc.generator.MarkdownGeneratorService;
import com.ppg.iicsdoc.model.ai.MermaidDiagram;
import com.ppg.iicsdoc.model.deployment.DeploymentConfig;
import com.ppg.iicsdoc.model.deployment.DeploymentResult;
import com.ppg.iicsdoc.model.domain.ParsedMetadata;
import com.ppg.iicsdoc.model.markdown.MarkdownDocument;
import com.ppg.iicsdoc.model.validation.SchemaValidationResult;
import com.ppg.iicsdoc.parser.XMLParserService;
import com.ppg.iicsdoc.validation.BusinessRulesValidation;
import com.ppg.iicsdoc.validation.SchemaValidator;
import com.ppg.iicsdoc.validation.report.ValidationReportExporter;
import com.ppg.iicsdoc.validation.report.ValidationReportGenerator;
import com.ppg.iicsdoc.validation.report.ValidationReportGenerator.ReportFormat;

import freemarker.template.Configuration;
import freemarker.template.Version;

import com.ppg.iicsdoc.validation.WellFormednessValidator;
import com.ppg.iicsdoc.validation.XMLValidationService;

import lombok.extern.slf4j.Slf4j;

/**
 * Entry point for the IICS Documentation Generator CLI application.
 * 
 * <p>
 * This Spring Boot Application parses Informatica IICS XML metadata files,
 * generates Markdown documentation enriched with Mermaid diagrams using
 * AI services, and deploys the output to a configured target (e.g.,
 * local filesystem or Docusaurus).
 * </p>
 * 
 * <p>
 * Usage is controlled via command-line arguments. The application supports
 * options for specifying input files, output directories, and displaying
 * help.
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>{@code
 * java -jar iics-doc-gen.jar --input=process.xml --output=./docs
 * }</pre>
 * 
 * <p>
 * Environment variables:
 * </p>
 * 
 * <ul>
 * <li>{@code AI_API_KEY} - API key for diagram generation</li>
 * <li>{@code DOCUSAURUS_PATH} - Default deployment path</li>
 * </ul>
 * 
 * <p>
 * This class is excluded from the {@code test} profile to prevent
 * execution during automated tests.
 * </p>
 * 
 * @see XMLParserService
 * @see AIAgentService
 * @see MarkdownGeneratorService
 * @see DeploymentService
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-24
 */
@Slf4j
@SpringBootApplication
@ComponentScan(basePackages = "com.ppg.iicsdoc")
@Profile("!test")
public class DocumentationGeneratorCLI implements CommandLineRunner {

    private final XMLParserService xmlParser;
    private final AIAgentService aiAgent;
    private final MarkdownGeneratorService markdownGenerator;
    private final DeploymentService deploymentService;
    private final XMLValidationService xmlValidationService;
    private final ValidationReportGenerator validationReportGenerator;
    private final ValidationReportExporter validationExporter;

    public DocumentationGeneratorCLI(
            XMLParserService xmlParser,
            AIAgentService aiAgent,
            MarkdownGeneratorService markdownGenerator,
            DeploymentService deploymentService) {
        this.xmlParser = xmlParser;
        this.aiAgent = aiAgent;
        this.markdownGenerator = markdownGenerator;
        this.deploymentService = deploymentService;

        this.xmlValidationService = new XMLValidationService(
                new SchemaValidator(),
                new BusinessRulesValidation(),
                new WellFormednessValidator());

        Configuration freemarkerConfig = new Configuration(new Version("2.32.0"));
        this.validationReportGenerator = new ValidationReportGenerator(freemarkerConfig);
        this.validationExporter = new ValidationReportExporter(validationReportGenerator);
    }

    public static void main(String[] args) {
        SpringApplication.run(DocumentationGeneratorCLI.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("==============================================");
        log.info("IICS Documentation Generator");
        log.info("Version 1.0.0");
        log.info("==============================================");

        CLIArguments cliArgs = parseArguments(args);
        if (cliArgs.showHelp()) {
            printHelp();
            return;
        }

        if (cliArgs.getInputFile() == null) {
            log.error("Error: --input parameter is required");
            printHelp();

            System.exit(1);
        }

        try {
            executePipeline(cliArgs);

            log.info("");
            log.info("Documentation generation completed successfully");

            System.exit(0);
        } catch (Exception e) {
            log.error("Documentation generation failed: {}", e.getMessage());
            System.exit(1);
        }
    }

    // Helper methods
    /**
     * Executes the documentation generation pipeline.
     * 
     * <p>
     * This method performs the following steps:
     * </p>
     * <ol>
     * <li>Parses the input XML metadata file</li>
     * <li>Generates process flow and API endpoint diagrams using AI services</li>
     * <li>Generates Markdown documentation from the parsed metadata and
     * diagrams</li>
     * <li>Deploys the generated documentation using the configured deployment</li>
     * </ol>
     * 
     * @param args the parsed command-line arguments containing input and output
     *             paths
     * @throws Exception if any step in the pipeline fails
     */
    private void executePipeline(CLIArguments args) throws Exception {
        Path inputFile = Paths.get(args.getInputFile());

        log.info("Validating XML file");
        SchemaValidationResult validationResult = xmlValidationService.validateComplete(inputFile);

        if (args.getReportOutput() != null) {
            ReportFormat format = parseReportFormat(args.getReportFormat());
            Path reportFile;

            if ("all".equalsIgnoreCase(args.getReportFormat())) {
                validationExporter.exportAll(validationResult, Paths.get(args.getReportOutput()));
                log.info("Validation report exported to: {}", args.getReportOutput());
            } else {
                reportFile = validationExporter.export(
                    validationResult,
                    Paths.get(args.getReportOutput()),
                    format);
                
                log.info("Validation report exported to: {}", reportFile.toAbsolutePath());
            }
        } else {
            String report = validationReportGenerator.generateTextReport(validationResult);
            System.out.println("\n" + report);
        }

        if (!validationResult.isValid()) {
            log.error("XML validation failed");
            throw new Exception("XML validation failed with " + validationResult);
        }

        if (validationResult.hasWarnings()) {
            log.warn("XML validation passed with {} warnings", validationResult.getWarningCount());
        } else {
            log.info("XML is valid.");
        }

        log.info("Parsing XML file: {}", inputFile.getFileName());
        ParsedMetadata metadata = xmlParser.parse(inputFile);

        log.info("  Process: {}", metadata.getProcessName());
        log.info("  Type: {}", metadata.getProcessType());
        log.info("Generating diagram");

        MermaidDiagram processFlowDiagram = aiAgent.generateProcessFlowDiagram(metadata);
        log.info("  Process flow diagram generated");

        MermaidDiagram apiDiagram = null;
        if (metadata.getOpenApiEndpoints() != null &&
                !metadata.getOpenApiEndpoints().isEmpty()) {
            apiDiagram = aiAgent.generateApiEndpointDiagram(metadata);
            log.info("  API endpoint diagram generated");
        }

        log.info("Generating markdown documentation");
        MarkdownDocument document = markdownGenerator.generate(
                metadata,
                processFlowDiagram,
                apiDiagram);

        log.info("  Document: {} ({} bytes)", document.getFilename(), document.getSize());
        log.info("Deploying documentation");

        DeploymentConfig deployConfig = createDeploymentConfig(args);
        DeploymentResult result = deploymentService.deploy(document, deployConfig);

        if (result.isSuccess()) {
            log.info("  Deployed to: {}", result.getTargetPath());
            log.info("  Files: {}", String.join(", ", result.getDeployedFiles()));
        } else {
            throw new Exception("Deployment failed: " + result.getMessage());
        }
    }

    /**
     * Creates a {@link DeploymentConfig} based on the provided CLI arguments.
     * 
     * <p>
     * If an output directory is specified, a local filesystem deployment
     * configuration is created. Otherwise, {@code null} is returned, and the
     * default configuration from the application context may be used.
     * </p>
     * 
     * @param args the parsed command-line arguments
     * @return a {@link DeploymentConfig} for local filesystem deployment,
     *         or {@code null} if no output directory is specified.
     */
    private DeploymentConfig createDeploymentConfig(CLIArguments args) {
        if (args.getOutputDir() != null) {
            return DeploymentConfig.localFilesystem(args.getOutputDir());
        }

        return null;
    }

    /**
     * Parses command-line arguments into a {@link CLIArguments} object.
     * 
     * <p>
     * Supported options:
     * </p>
     * 
     * <ul>
     * <li>{@code --input=FILE} - Path to the IICS XML metadata file (required)</li>
     * <li>{@code --output=DIR} - Output directory for generated documentation</li>
     * <li>{@code --help}, {@code -h} - Displays help information</li>
     * </ul>
     * 
     * @param args the row command-line arguments
     * @return a populated {@link CLIArguments} instance
     */
    private CLIArguments parseArguments(String[] args) {
        CLIArguments cliArgs = new CLIArguments();
        for (String arg : args) {
            if (arg.startsWith("--input=")) {
                cliArgs.setInputFile(arg.substring("--input=".length()));
            } else if (arg.startsWith("--output=")) {
                cliArgs.setOutputDir(arg.substring("--output=".length()));
            } else if (arg.equals("--help") || arg.equals("-h")) {
                cliArgs.setShowHelp(true);
            } else if (arg.startsWith("--report-format=")) {
                cliArgs.setReportFormat(arg.substring("--report-format=".length()));
            } else if (arg.startsWith("--report-output=")) {
                cliArgs.setReportOutput(arg.substring("--report-output=".length()));
            }
        }

        return cliArgs;
    }

    /**
     * Parses the user input format into the available ones.
     * 
     * @param format the format to use for validation report exports.
     * @return the matching {@code ReportFormat}
     */
    private ReportFormat parseReportFormat(String format) {
        return switch (format.toLowerCase()) {
            case "markdown", "md" -> ReportFormat.MARKDOWN;
            case "html" -> ReportFormat.HTML;
            case "json" -> ReportFormat.JSON;
            default -> ReportFormat.TEXT;
        };
    }

    /**
     * Prints usage instructions and available command-line options to the console.
     * 
     * <p>
     * This includes examples and environment variables required for AI
     * integration and deployment configuration.
     * </p>
     */
    private void printHelp() {
        System.out.println();
        System.out.println("Usage: java -jar iics-doc-gen.jar [OPTIONS]");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  --input=FILE    Path to IICS XML metadata file (required)");
        System.out.println("  --output=DIR    Output directory for generated documentation");
        System.out.println("                  (default: from application.yml)");
        System.out.println("  --help, -h      Show this help message");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java -jar iics-doc-gen.jar --input=process.xml");
        System.out.println("  java -jar iics-doc-gen.jar --input=process.xml --output=./docs");
        System.out.println("  java -jar iics-doc-gen.jar --input=process.xml --report-format=html --report-output=./reports");
        System.out.println("  java -jar iics-doc-gen.jar --input=process.xml --report-format=all --report-output=./reports");
        System.out.println();
        System.out.println("Environment Variables:");
        System.out.println("  AI_API_KEY       API key for diagram generation");
        System.out.println("  DOCUSAURUS_PATH  Default deployment path");
        System.out.println();
    }

    /**
     * Encapsulates command-line arguments for the {@link DocumentationGeneratorCLI}
     * application.
     *
     * <p>
     * This class holds user-specified options such as:
     * <ul>
     * <li>{@code inputFile} – Path to the IICS XML metadata file</li>
     * <li>{@code outputDir} – Target directory for generated documentation</li>
     * <li>{@code showHelp} – Flag indicating whether help information should be
     * displayed</li>
     * </ul>
     *
     * <p>
     * Instances of this class are populated by parsing raw CLI arguments and used
     * to control the application's execution flow.
     * </p>
     */
    private static class CLIArguments {

        /** Path to the input XML metadata file. */
        private String inputFile;

        /** Path to the output directory for generated documentation. */
        private String outputDir;

        /** Whether help information should be displayed. */
        private boolean showHelp;

        /** Text, markdown, html, json format for the reports */
        private String reportFormat = "text";

        /** Output path for the report */
        private String reportOutput;

        /**
         * Returns the input file path.
         *
         * @return the input file path, or {@code null} if not specified
         */
        public String getInputFile() {
            return inputFile;
        }

        /**
         * Returns the validation report output path.
         * 
         * @return the validation report output path.
         */
        public String getReportOutput() {
            return reportOutput;
        }

        /**
         * Returns the selected output format for validation reports.
         * 
         * @return The selected format option 
         */
        public String getReportFormat() {
            return reportFormat;
        }

        /**
         * Sets the input file path.
         *
         * @param inputFile the path to the XML metadata file
         */
        public void setInputFile(String inputFile) {
            this.inputFile = inputFile;
        }

        /**
         * Returns the output directory path.
         *
         * @return the output directory path, or {@code null} if not specified
         */
        public String getOutputDir() {
            return outputDir;
        }

        /**
         * Sets the output directory path.
         *
         * @param outputDir the path to the output directory
         */
        public void setOutputDir(String outputDir) {
            this.outputDir = outputDir;
        }

        /**
         * Returns whether help information should be displayed.
         *
         * @return {@code true} if help should be shown; {@code false} otherwise
         */
        public boolean showHelp() {
            return showHelp;
        }

        /**
         * Sets the help flag.
         *
         * @param showHelp {@code true} to show help; {@code false} otherwise
         */
        public void setShowHelp(boolean showHelp) {
            this.showHelp = showHelp;
        }

        /**
         * Sets the report format for validation report generation output.
         * 
         * <p>
         * Available formats are:
         * </p>
         * 
         * <ul>
         * <li>Text file</li>
         * <li>Markdown</li>
         * <li>HTML</li>
         * <li>JSON</li>
         * <li>All</li>
         * </ul>
         * 
         * <p>
         * If all is selected, all output formats are used.
         * </p>
         * 
         * @param format Format to use from the available ones.
         */
        public void setReportFormat(String format) {
            this.reportFormat = format;
        }

        /**
         * Sets the report path output where validation reports are going to
         * be placed.
         * 
         * @param reportOutput Path where the files would be deposited
         */
        public void setReportOutput(String reportOutput) {
            this.reportOutput = reportOutput;
        }
    }
}
