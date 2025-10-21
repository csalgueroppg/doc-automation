#!/bin/bash

# IICS Documentation Generator - Build Script
# This script compiles the project and creates an executable JAR

set -e 

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

echo -e "${BLUE}=========================================${NC}"
echo -e "${BLUE} IICS Documentation Generator${NC}"
echo -e "${BLUE} Build Script${NC}"
echo -e "${BLUE}=========================================${NC}"
echo ""

# Change to project root
cd "$PROJECT_ROOT"

# Check if mvn is installed
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}Error: Maven is not installed or not in PATH${NC}"
    echo "Please install Maven and try again."
    exit 1
fi

# Check Java Version
echo -e "${YELLOW}Checking Java version${NC}"
java -version

# Parse command line arguments
SKIP_TESTS=false
CLEAN=false
PROFILE="dev"

while [[ $# -gt 0 ]]; do
    case $1 in
        --skip-tests)
            SKIP_TESTS=true
            shift
            ;;
        --clean)
            CLEAN=true
            shift
            ;;
        --profile)
            PROFILE="$2"
            shift 2 
            ;;
        --help)
            echo "Usage: ./build.sh [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  --skip-tests Skip running tests"
            echo "  --clean      Run before clean build"
            echo "  --profile    Set build profile (dev|prod), default: dev"
            echo "  --help       Show this help message"
            exit 0
            ;;
        *)
            echo -e "${RED}Unknown option: $1${NC}"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

# Clean if requested
if [ "$CLEAN" = true ]; then 
    echo -e "${YELLOW}Cleaning project{$NC}"
    mvn clean 

    echo ""
fi

# Build command
BUILD_CMD="mvn package -P$PROFILE"

if [ "$SKIP_TESTS" = true ]; then 
    BUILD_CMD="$BUILD_CMD -DskipTests"
    echo -e "${YELLOW}Building project (skipping tests)${NC}"
else 
    echo -e "${YELLOW}Building project (with tests)${NC}"
fi

echo -e "${BLUE}Build profile: $PROFILE${NC}"
echo ""

# Execute build
if $BUILD_CMD; then 
    echo ""
    echo -e "${GREEN}==================================${NC}"
    echo -e "${GREEN} Build Successful!${NC}"
    echo -e "${GREEN}==================================${NC}"
    echo ""

    JAR_FILE=$(find target -name "*.jar" -not -name "*-sources.jar" -not -name "*-javadoc.jar" | head -n 1)
    if [ -n "$JAR_FILE" ]; then 
        JAR_SIZE=$(du -h "$JAR_FILE" | cut -f1)

        echo -e "${GREEN}Jar Location:${NC} $JAR_FILE"
        echo -e "${GREEN}Jar Size:${NC} $JAR_SIZE"
        echo ""
        echo -e "${BLUE}To run the application:${NC}"
        echo " ./scripts/run.sh"
        echo ""
        echo -e "${BLUE}Or directly:${NC}"
        echo " java -jar $JAR_FILE"
    fi

    exit 0
else 
    echo ""
    echo -e "${GREEN}==================================${NC}"
    echo -e "${GREEN} Build Failed!${NC}"
    echo -e "${GREEN}==================================${NC}"
    echo ""
    echo "Check the error messages above for details"
    
    exit 1
fi