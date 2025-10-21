#!/bin/bash

# IICS Documentation Generator - Clean script

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
FULL_CLEAN=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --full)
            FULL_CLEAN=true
            shift
        ;;
        --help)
            echo "Usage: ./clean.sh [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  --full Also clean logs and generated docs"
            echo "  --help Shows this help message"
            
            exit 0
        ;;
        *)
            echo -e "${RED}Unknown option: $1${NC}"
            exit 1
        ;;
    esac
done

echo -e "${YELLOW}Cleaning Maven build artifacts${NC}"
mvn clean

if [ "$FULL_CLEAN" = true ]; then
    echo -e "${YELLOW}Performing full clean${NC}"
    
    if [ -d "logs" ]; then
        echo " - Removing logs directory"
        rm -rf logs
    fi
    
    if [ -d "generated-docs" ]; then
        echo " - Removing genereated-docs directory"
        rm -rf generated-docs
    fi
    
    echo " - Removing IDE files"
    find . -name "*.iml" -type f -delete 2>/dev/null || true
    
    rm -rf .idea 2>/dev/null || true
    rm -rf .vscode 2>/dev/null || true
    rm -rf .settings 2>/dev/null || true
    rm -f .classpath .project 2>/dev/null || true
fi

echo ""
echo -e "${GREEN}Clean complete${NC}"