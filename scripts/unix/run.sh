#!/bin/bash

# IICS Documentation Generator - Run Script
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

# Find the JAR file
JAR_FILE=$(find target -name "*.jar" -not -name "*-sources.jar" -not -name "*-javadoc.jar" | head -n 1)

if [ -z "$JAR_FILE" ]; then 
    echo -e "${RED}Error: No JAR file found in target directory${NC}"
    echo "Please build the project first:"
    echo "  ./scripts/build.sh"
    
    exit 1
fi

echo -e "${GREEN}Found JAR:${NC} $JAR_FILE"
echo ""

# Parse arguments
PROFILE="dev"
INPUT_FILE=""
OUTPUT_DIR=""
JAVA_OPTS=""

while [[ $# -gt 0 ]]; do 
    case $1 in 
        --profile)
            PROFILE="$2"
            shift 2 
            ;;
        --input)
            INPUT_FILE="$2"
            shift 2
            ;;
        --output)
            OUTPUT_DIR="$2"
            shift 2
            ;;
        --java-opts)
            JAVA_OPTS="$2"
            shift 2
            ;;
        --help)
            echo "Usage: ./run.sh [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  --profile    Set profile (dev|prod), default: dev"
            echo "  --input      Input XML file"
            echo "  --output     Output directory path"
            echo "  --java-opts  Additional Java options (e.g., 'Xmx2g')"
            echo "  --help       Show this help message"
            echo ""
            echo "Examples:"
            echo "  ./run.sh --input sample.xml"
            echo "  ./run.sh --profile prod --input sample.xml --output ./docs"
            
            exit 0
            ;;
        *)
            echo -e "${RED}Unknown option: $1${NC}"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

# Build command
RUN_CMD="java $JAVA_OPTS -Dspring.profiles.active=$PROFILE -jar $JAR_FILE"

if [ -n "$INPUT_FILE" ]; then
    RUN_CMD="$RUN_CMD --input=$INPUT_FILE"
fi 

if [ -n "$OUTPUT_DIR" ]; then 
    RUN_CMD="$RUN_CMD --output=$OUTPUT_DIR"
fi 

echo -e "${YELLOW}Starting application{$NC}"
echo -e "${BLUE}Profile:${NC} $PROFILE"

if [ -n "$INPUT_FILE" ]; then 
    echo -e "${BLUE}Input:${NC} $INPUT_FILE"
fi

if [ -n "$OUTPUT_DIR" ]; then 
    echo -e "${BLUE}Output:${NC} $OUTPUT_DIR"
fi 

echo ""
echo -e "${BLUE}Command:${NC} $RUN_CMD"
echo "==========================================="
echo ""

# Run the application
exec $RUN_CMD