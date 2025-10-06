#!/bin/bash

# Test Execution Script for Product Order Service
# 
# This script provides various options for running tests:
# - All tests
# - Specific test categories
# - Individual test classes
# - Performance tests
# - Coverage reports

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to show usage
show_usage() {
    echo "Usage: $0 [OPTION]"
    echo ""
    echo "Options:"
    echo "  all                 Run all tests"
    echo "  unit                Run unit tests only"
    echo "  integration         Run integration tests only"
    echo "  grpc                Run gRPC tests only"
    echo "  kafka               Run Kafka tests only"
    echo "  security            Run security tests only"
    echo "  performance         Run performance tests only"
    echo "  coverage            Generate coverage report"
    echo "  clean               Clean test artifacts"
    echo "  help                Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 all              # Run all tests"
    echo "  $0 unit             # Run unit tests only"
    echo "  $0 coverage         # Generate coverage report"
    echo "  $0 clean            # Clean test artifacts"
}

# Function to run all tests
run_all_tests() {
    print_status "Running all tests..."
    mvn test -Dspring.profiles.active=test
    print_success "All tests completed successfully!"
}

# Function to run unit tests
run_unit_tests() {
    print_status "Running unit tests..."
    mvn test -Dtest="*ServiceImplTest" -Dspring.profiles.active=test
    print_success "Unit tests completed successfully!"
}

# Function to run integration tests
run_integration_tests() {
    print_status "Running integration tests..."
    mvn test -Dtest="*IntegrationTest" -Dspring.profiles.active=test
    print_success "Integration tests completed successfully!"
}

# Function to run gRPC tests
run_grpc_tests() {
    print_status "Running gRPC tests..."
    mvn test -Dtest="*GrpcServiceTest" -Dspring.profiles.active=test
    print_success "gRPC tests completed successfully!"
}

# Function to run Kafka tests
run_kafka_tests() {
    print_status "Running Kafka tests..."
    mvn test -Dtest="*EventPublisher*Test" -Dspring.profiles.active=test
    print_success "Kafka tests completed successfully!"
}

# Function to run security tests
run_security_tests() {
    print_status "Running security tests..."
    mvn test -Dtest="*SecurityTest" -Dspring.profiles.active=test
    print_success "Security tests completed successfully!"
}

# Function to run performance tests
run_performance_tests() {
    print_status "Running performance tests..."
    mvn test -Dtest="*PerformanceTest" -Dspring.profiles.active=test -Xmx2g
    print_success "Performance tests completed successfully!"
}

# Function to generate coverage report
generate_coverage_report() {
    print_status "Generating coverage report..."
    mvn test jacoco:report -Dspring.profiles.active=test
    print_success "Coverage report generated successfully!"
    print_status "Coverage report location: target/site/jacoco/index.html"
}

# Function to clean test artifacts
clean_test_artifacts() {
    print_status "Cleaning test artifacts..."
    mvn clean
    print_success "Test artifacts cleaned successfully!"
}

# Function to run specific test class
run_specific_test() {
    local test_class=$1
    print_status "Running specific test: $test_class"
    mvn test -Dtest="$test_class" -Dspring.profiles.active=test
    print_success "Test $test_class completed successfully!"
}

# Function to run tests with specific parameters
run_tests_with_params() {
    local test_type=$1
    local params=$2
    print_status "Running $test_type tests with parameters: $params"
    mvn test -Dtest="$test_type" -Dspring.profiles.active=test $params
    print_success "$test_type tests completed successfully!"
}

# Function to check prerequisites
check_prerequisites() {
    print_status "Checking prerequisites..."
    
    # Check if Maven is installed
    if ! command -v mvn &> /dev/null; then
        print_error "Maven is not installed. Please install Maven first."
        exit 1
    fi
    
    # Check if Java is installed
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed. Please install Java first."
        exit 1
    fi
    
    # Check Java version
    java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$java_version" -lt 17 ]; then
        print_error "Java 17 or higher is required. Current version: $java_version"
        exit 1
    fi
    
    print_success "Prerequisites check passed!"
}

# Function to show test statistics
show_test_statistics() {
    print_status "Test Statistics:"
    echo "  - Unit Tests: ~50 test methods"
    echo "  - Integration Tests: ~40 test methods"
    echo "  - gRPC Tests: ~20 test methods"
    echo "  - Kafka Tests: ~15 test methods"
    echo "  - Security Tests: ~25 test methods"
    echo "  - Performance Tests: ~10 test methods"
    echo "  - Total Tests: ~160+ test methods"
    echo "  - Estimated Execution Time: 15-20 minutes"
}

# Main script logic
main() {
    # Check if no arguments provided
    if [ $# -eq 0 ]; then
        show_usage
        exit 1
    fi
    
    # Check prerequisites
    check_prerequisites
    
    # Parse command line arguments
    case "$1" in
        "all")
            run_all_tests
            ;;
        "unit")
            run_unit_tests
            ;;
        "integration")
            run_integration_tests
            ;;
        "grpc")
            run_grpc_tests
            ;;
        "kafka")
            run_kafka_tests
            ;;
        "security")
            run_security_tests
            ;;
        "performance")
            run_performance_tests
            ;;
        "coverage")
            generate_coverage_report
            ;;
        "clean")
            clean_test_artifacts
            ;;
        "stats")
            show_test_statistics
            ;;
        "help")
            show_usage
            ;;
        *)
            # Check if it's a specific test class
            if [[ "$1" == *"Test" ]]; then
                run_specific_test "$1"
            else
                print_error "Unknown option: $1"
                show_usage
                exit 1
            fi
            ;;
    esac
}

# Run main function with all arguments
main "$@"
