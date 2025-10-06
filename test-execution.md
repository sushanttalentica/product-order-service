# Test Execution Guide

## Overview
This document provides comprehensive guidance for executing all test cases in the Product Order Service.

## Test Categories

### 1. Unit Tests
- **Location**: `src/test/java/com/ecommerce/productorder/domain/service/impl/`
- **Purpose**: Test individual service methods in isolation
- **Coverage**: ProductService, OrderService, PaymentService
- **Execution Time**: ~2-3 minutes

### 2. Integration Tests
- **Location**: `src/test/java/com/ecommerce/productorder/controller/`
- **Purpose**: Test REST API endpoints with full Spring context
- **Coverage**: ProductController, OrderController
- **Execution Time**: ~5-7 minutes

### 3. gRPC Tests
- **Location**: `src/test/java/com/ecommerce/productorder/payment/grpc/`
- **Purpose**: Test gRPC Payment Service endpoints
- **Coverage**: PaymentGrpcService
- **Execution Time**: ~3-4 minutes

### 4. Kafka Tests
- **Location**: `src/test/java/com/ecommerce/productorder/service/impl/`
- **Purpose**: Test Kafka event publishing
- **Coverage**: OrderEventPublisher, PaymentEventPublisher
- **Execution Time**: ~2-3 minutes

### 5. Security Tests
- **Location**: `src/test/java/com/ecommerce/productorder/security/`
- **Purpose**: Test authentication and authorization
- **Coverage**: SecurityConfig, JWT authentication
- **Execution Time**: ~3-4 minutes

### 6. Performance Tests
- **Location**: `src/test/java/com/ecommerce/productorder/performance/`
- **Purpose**: Test system performance under load
- **Coverage**: Concurrent operations, memory usage, response times
- **Execution Time**: ~10-15 minutes

## Test Execution Commands

### Run All Tests
```bash
# Run all tests
mvn test

# Run with specific profile
mvn test -Dspring.profiles.active=test

# Run with detailed output
mvn test -X
```

### Run Specific Test Categories
```bash
# Run only unit tests
mvn test -Dtest="*Test"

# Run only integration tests
mvn test -Dtest="*IntegrationTest"

# Run only performance tests
mvn test -Dtest="*PerformanceTest"

# Run only security tests
mvn test -Dtest="*SecurityTest"
```

### Run Individual Test Classes
```bash
# Run ProductService tests
mvn test -Dtest="ProductServiceImplTest"

# Run ProductController integration tests
mvn test -Dtest="ProductControllerIntegrationTest"

# Run OrderController integration tests
mvn test -Dtest="OrderControllerIntegrationTest"

# Run PaymentService tests
mvn test -Dtest="PaymentServiceImplTest"

# Run PaymentGrpcService tests
mvn test -Dtest="PaymentGrpcServiceTest"

# Run OrderEventPublisher tests
mvn test -Dtest="OrderEventPublisherImplTest"

# Run SecurityConfig tests
mvn test -Dtest="SecurityConfigTest"

# Run ProductService performance tests
mvn test -Dtest="ProductServicePerformanceTest"
```

### Run Tests with Specific Parameters
```bash
# Run tests with specific JVM options
mvn test -Dtest="*PerformanceTest" -Dspring.profiles.active=test -Xmx2g

# Run tests with specific timeout
mvn test -Dtest="*PerformanceTest" -Dtest.timeout=60

# Run tests with specific thread count
mvn test -Dtest="*PerformanceTest" -Dtest.threads=10
```

## Test Configuration

### Test Profiles
- **test**: Default test profile with H2 database
- **integration-test**: Integration test profile with embedded services
- **performance-test**: Performance test profile with optimized settings

### Test Database
- **Type**: H2 in-memory database
- **URL**: `jdbc:h2:mem:testdb`
- **Auto-cleanup**: Enabled
- **Data**: Test data automatically created and cleaned up

### Test Services
- **Kafka**: Embedded Kafka for event testing
- **Redis**: Embedded Redis for cache testing
- **S3**: LocalStack for S3 testing

## Test Data

### Test Data Factory
- **Location**: `src/test/java/com/ecommerce/productorder/util/TestDataFactory.java`
- **Purpose**: Creates test data with realistic values
- **Usage**: Used across all test classes

### Test Data Configuration
- **Products**: 1000 test products across 5 categories
- **Orders**: 100 test orders with various statuses
- **Payments**: 100 test payments with various statuses
- **Customers**: 100 test customers with realistic data

## Test Coverage

### Coverage Goals
- **Line Coverage**: >90%
- **Branch Coverage**: >85%
- **Method Coverage**: >95%
- **Class Coverage**: >90%

### Coverage Reports
```bash
# Generate coverage report
mvn test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

## Test Performance

### Performance Benchmarks
- **Unit Tests**: <5 seconds per test class
- **Integration Tests**: <30 seconds per test class
- **Performance Tests**: <60 seconds per test class
- **Total Execution Time**: <30 minutes

### Performance Optimization
- **Parallel Execution**: Enabled for unit tests
- **Test Isolation**: Each test runs in isolation
- **Database Cleanup**: Automatic cleanup between tests
- **Memory Management**: Optimized for test execution

## Test Reporting

### Test Reports
- **Location**: `target/surefire-reports/`
- **Format**: HTML and XML reports
- **Content**: Test results, coverage, performance metrics

### Test Metrics
- **Total Tests**: ~200+ test methods
- **Test Categories**: 6 categories
- **Test Classes**: 15+ test classes
- **Coverage**: Comprehensive coverage of all features

## Troubleshooting

### Common Issues
1. **Database Connection**: Ensure H2 database is available
2. **Kafka Connection**: Ensure embedded Kafka is running
3. **Memory Issues**: Increase JVM heap size if needed
4. **Timeout Issues**: Increase test timeout if needed

### Debug Mode
```bash
# Run tests in debug mode
mvn test -Dtest.debug=true

# Run specific test in debug mode
mvn test -Dtest="ProductServiceImplTest" -Dtest.debug=true
```

### Test Logs
- **Location**: `target/surefire-reports/`
- **Level**: INFO for normal execution, DEBUG for troubleshooting
- **Format**: Standard logging format

## Continuous Integration

### CI/CD Integration
- **GitHub Actions**: Automated test execution
- **Jenkins**: Pipeline integration
- **Docker**: Containerized test execution

### Test Automation
- **Scheduled**: Daily test execution
- **Triggered**: On code changes
- **Reporting**: Automated test reports

## Best Practices

### Test Writing
1. **AAA Pattern**: Arrange, Act, Assert
2. **Test Isolation**: Each test is independent
3. **Descriptive Names**: Clear test method names
4. **Edge Cases**: Test boundary conditions
5. **Error Scenarios**: Test error conditions

### Test Maintenance
1. **Regular Updates**: Keep tests up to date
2. **Refactoring**: Refactor tests with code changes
3. **Documentation**: Document test purposes
4. **Performance**: Monitor test execution time

### Test Data Management
1. **Factory Pattern**: Use test data factories
2. **Realistic Data**: Use realistic test data
3. **Cleanup**: Ensure proper test cleanup
4. **Isolation**: Avoid test data conflicts

## Conclusion

This comprehensive test suite ensures the Product Order Service is thoroughly tested across all dimensions:
- **Functionality**: All features are tested
- **Performance**: System performance is validated
- **Security**: Authentication and authorization are tested
- **Integration**: All integrations are tested
- **Reliability**: System reliability is validated

The test suite provides confidence in the system's quality and reliability, ensuring it meets all requirements and performs well under various conditions.
