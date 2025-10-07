#!/bin/bash

# Product Order Service - Test Execution Script
# This script runs various types of tests for the application

set -e

# Configuration
TEST_PROFILE="test"
COVERAGE_THRESHOLD=80

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging function
log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')] $1${NC}"
}

warn() {
    echo -e "${YELLOW}[$(date +'%Y-%m-%d %H:%M:%S')] WARNING: $1${NC}"
}

error() {
    echo -e "${RED}[$(date +'%Y-%m-%d %H:%M:%S')] ERROR: $1${NC}"
    exit 1
}

info() {
    echo -e "${BLUE}[$(date +'%Y-%m-%d %H:%M:%S')] $1${NC}"
}

# Check prerequisites
check_prerequisites() {
    log "Checking prerequisites..."
    
    # Check if mvn is installed
    if ! command -v mvn &> /dev/null; then
        error "mvn is not installed or not in PATH"
    fi
    
    # Check if java is installed
    if ! command -v java &> /dev/null; then
        error "java is not installed or not in PATH"
    fi
    
    log "Prerequisites check passed"
}

# Run unit tests
run_unit_tests() {
    log "Running unit tests..."
    
    mvn test -Dtest.profile=${TEST_PROFILE}
    
    if [ $? -ne 0 ]; then
        error "Unit tests failed"
    fi
    
    log "Unit tests completed successfully"
}

# Run integration tests
run_integration_tests() {
    log "Running integration tests..."
    
    mvn verify -Pintegration-tests
    
    if [ $? -ne 0 ]; then
        error "Integration tests failed"
    fi
    
    log "Integration tests completed successfully"
}

# Run performance tests
run_performance_tests() {
    log "Running performance tests..."
    
    mvn verify -Pperformance-tests
    
    if [ $? -ne 0 ]; then
        warn "Performance tests failed"
    fi
    
    log "Performance tests completed"
}

# Run security tests
run_security_tests() {
    log "Running security tests..."
    
    # Dependency check
    mvn dependency-check:check
    
    if [ $? -ne 0 ]; then
        warn "Security scan found vulnerabilities"
    fi
    
    # OWASP dependency check
    mvn org.owasp:dependency-check-maven:check
    
    if [ $? -ne 0 ]; then
        warn "OWASP dependency check found issues"
    fi
    
    log "Security tests completed"
}

# Generate test coverage report
generate_coverage_report() {
    log "Generating test coverage report..."
    
    mvn jacoco:report
    
    if [ $? -ne 0 ]; then
        error "Coverage report generation failed"
    fi
    
    # Check coverage threshold
    COVERAGE=$(mvn jacoco:check | grep -o '[0-9]*%' | head -1 | sed 's/%//')
    
    if [ "$COVERAGE" -lt "$COVERAGE_THRESHOLD" ]; then
        error "Coverage threshold not met: ${COVERAGE}% < ${COVERAGE_THRESHOLD}%"
    fi
    
    log "Coverage report generated successfully (${COVERAGE}%)"
}

# Run mutation tests
run_mutation_tests() {
    log "Running mutation tests..."
    
    mvn org.pitest:pitest-maven:mutationCoverage
    
    if [ $? -ne 0 ]; then
        warn "Mutation tests failed"
    fi
    
    log "Mutation tests completed"
}

# Run contract tests
run_contract_tests() {
    log "Running contract tests..."
    
    mvn verify -Pcontract-tests
    
    if [ $? -ne 0 ]; then
        warn "Contract tests failed"
    fi
    
    log "Contract tests completed"
}

# Run load tests
run_load_tests() {
    log "Running load tests..."
    
    # Start application
    mvn spring-boot:run &
    APP_PID=$!
    
    # Wait for application to start
    sleep 30
    
    # Run load tests
    mvn verify -Pload-tests
    
    # Stop application
    kill $APP_PID
    
    if [ $? -ne 0 ]; then
        warn "Load tests failed"
    fi
    
    log "Load tests completed"
}

# Generate test report
generate_test_report() {
    log "Generating test report..."
    
    # Create reports directory
    mkdir -p target/reports
    
    # Copy test reports
    cp -r target/surefire-reports target/reports/
    cp -r target/site/jacoco target/reports/
    
    # Generate HTML report
    mvn surefire-report:report
    
    log "Test report generated successfully"
}

# Clean up
cleanup() {
    log "Cleaning up..."
    
    # Stop any running processes
    pkill -f "spring-boot:run" || true
    
    # Clean target directory
    mvn clean
    
    log "Cleanup completed"
}

# Main test function
main() {
    log "Starting test execution..."
    
    check_prerequisites
    
    # Run different types of tests
    run_unit_tests
    run_integration_tests
    run_security_tests
    generate_coverage_report
    
    # Optional tests
    if [ "$1" = "--full" ]; then
        run_performance_tests
        run_mutation_tests
        run_contract_tests
        run_load_tests
    fi
    
    generate_test_report
    cleanup
    
    log "Test execution completed successfully!"
    log "Reports available in target/reports/"
}

# Show usage
usage() {
    echo "Usage: $0 [--full]"
    echo "  --full    Run all tests including performance, mutation, and load tests"
    echo "  (default) Run unit, integration, security, and coverage tests"
}

# Parse arguments
if [ "$1" = "--help" ] || [ "$1" = "-h" ]; then
    usage
    exit 0
fi

# Run main function
main "$@"
