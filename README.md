# IICS Documentation Generator

Automated documentation generator for Informatica Intelligent Cloud Services (IICS) CAI/CDI processes.

## Features

- **XML Parsing**: robust parsing of complex IICS metadata XML files
- **AI Integration**: automatic mermaid diagram generation using AI
- **Documentation Generation**: template-based Markdown document creation
- **High Performance**: parallel processing and optimizing parsing
- **Type Safety**: comprehensive validation and error handling

## Prerequisites

- Java 21 or higher
- Maven 3.8+
- Git

## Quick Start

### 1. Clone the repository

```bash
git clone https://github.com/csalgueroppg/doc-automation
cd doc-automation
```

### 2. Configure application

Edit `src/main/resources/application.yml` and set your configurations:

```yaml
iics:
    ai:
        api-key: your-api-key-here
    deployment:
        docusuarus-path: /path/to/docusaurus/docs
```

Or use environment variables

```bash
export AI_API_KEY=your-api-key-here
export DOCUSAURUS_PATH=/path/to/docusuarus/docs
```

### 3. Build the project

```bash
mvn clean package
```

### 4. Run the application

```bash
java -jar target/iics-documentation-generator-1.0.0-SNAPSHOT.jar
```

## Usage

### Command Line

```bash
# Process a single XML file
java -jar target/iics-documentation-generator-1.0.0-SNAPSHOT.jar \
    --input /path/to/process.xml

# With custom output directory
java -jar target/iics-documentation-generator-1.0.0-SNAPSHOT.jar \
    --input /path/to/process.xml \
    --output /path/to/output
```

### GitHub Actions

The project includes a GitHub Actions workflow. See `.github/workflows/documentation-pipeline.yml`.

## Configuration

### Parser Configuration

```yaml
iics:
    parser: 
        max-file-size-bytes: 104857600 # 100 MB
        validate-schema: true 
        strict-mode: false 
```

### AI Configuration

```yaml
iics:
    ai:
        api-url: https://api.anthropic.com/v1/messages
        api-key: ${AI_API_KEY}
        model: claude-3-5-sonnet-20241022
        timeout-seconds: 30
        max-retries: 3
```

### Deployment Configuration

```yaml
iics:
    deployment:
        docusaurus-path: ../docusaurus-site/docs
        git-repo-url: https://github.com/ppg/docs
        git-branch: main
        auto-commit: true 
```

## Development

### Running Tests

```bash
mvn test
```

### Running with Development Profile

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Building for Production

```bash
mvn clean package -Pprod
```

## Logging

Logs are written to:

- Console output
- `logs/iics-doc-gen.log`: all logs
- `logs/iics-doc-gen-error.log`: error logs only
- `logs/iics-doc-gen-performance.log`: performance metrics

## License 

See the [LICENSE](LICENSE) file for more details.

## Support

For issues and questions, please create an issue in the GitHub repository.