#!/bin/bash

# IICS Documentation Generator - Test Script

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

# Parse arguments
TEST_CLASS=""
COVERAGE=false
VERBOSE=false

while [[ $# -gt 0 ]]; do 
    case $1 in 
        --class)
            TEST_CLASS="$2"
            shift 2 
            ;;
        --coverage)
            COVERAGE=true
            shift 
            ;;
        --verbose)
            VERBOSE=true
            shift
            ;;
        --help)
            echo "Usage: ./test.sh [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  --class     Run specific test class (e.g., XMLParserServiceTest)"
            echo "  --coverage  Generate code coverage report"
            echo "  --verbose   Show detailed test output"
            echo "  --help      Show this help message"
            echo ""
            echo "Example:"
            echo "  ./test.sh"
            echo "  ./test.sh --class XMLParserServiceTest"
            echo "  ./test.sh --coverage"

            exit 0
            ;;
        *)
            echo -e "${RED}Unknown option: $1${NC}"
            exit 1 
            ;;
    esac
done

# Build test command
TEST_CMD="mvn test"

if [ -n "$TEST_CLASS" ]; then 
    TEST_CMD="$TEST_CMD -Dtest=$TEST_CLASS"
    echo -e "${YELLOW}Running test class: $TEST_CLASS${NC}"
else 
    echo -e "${YELLOW}Running all tests${NC}"
fi 

if [ "$COVERAGE" = true ]; then 
    TEST_CMD="$TEST_CMD jacoco:report"
    echo -e "${YELLOW}Coverate report will be generated${NC}"
fi

echo ""

# Run tests
if $TEST_CMD; then 
    echo ""
    echo -e "${GREEN}==========================="
    echo -e "${GREEN} All Tests Passed!${NC}"
    echo -e "${GREEN}==========================="

    if [ "$COVERAGE" = true ]; then 
        COVERAGE_REPORT="target/site/jacoco/index.html"

        if [ -f "$COVERAGE_REPORT" ]; then 
            echo ""
            echo -e "${BLUE}Coverage report:${NC} $COVERAGE_REPORT"

            if command -v open &> /dev/null; then 
                open "$COVERAGE_REPORT"
            elif command -v xdg-open &> /dev/null; then 
                xdg-open "$COVERAGE_REPORT"
            fi 
        fi 
    fi

    exit 0
else 
    echo ""
    echo -e "${RED}==============================="
    echo -e "${RED} Tests Failed!${NC}"
    echo -e "${RED}==============================="

    exit 1
fi