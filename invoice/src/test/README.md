# Invoice Module Test Suite

This directory contains comprehensive JUnit tests for the Invoice module components.

## Test Structure

### 1. Entity Tests
- **`InvoiceTest.java`** - Tests for the Invoice entity class
  - Constructor tests
  - Business logic tests (canBeDownloaded, canBeRegenerated, markAsSent, markAsFailed)
  - InvoiceStatus enum tests
  - Getter and setter tests
  - Edge cases and validation tests

### 2. Repository Tests
- **`InvoiceRepositoryTest.java`** - Tests for the InvoiceRepository interface
  - Basic CRUD operations
  - Customer-based queries
  - Status-based queries
  - Date range queries
  - Amount range queries
  - Count queries
  - Failed invoices retry queries
  - Edge cases tests

### 3. Service Tests
- **`InvoiceServiceImplTest.java`** - Tests for the InvoiceServiceImpl class
  - Invoice generation tests
  - Invoice URL retrieval tests
  - Invoice deletion tests
  - Invoice existence checks
  - Integration tests
  - Edge cases tests

### 4. PDF Generator Tests
- **`PdfGeneratorServiceImplTest.java`** - Tests for the PdfGeneratorServiceImpl class
  - Invoice PDF generation
  - Receipt PDF generation
  - Shipping label PDF generation
  - InvoiceGeneratorService interface implementation
  - Edge cases and performance tests

### 5. S3 Service Tests
- **`S3ServiceImplTest.java`** - Tests for the S3ServiceImpl class
  - File upload operations
  - File download operations
  - File deletion operations
  - File existence checks
  - File URL generation
  - Presigned URL generation
  - Bucket policy management
  - Edge cases and integration tests

## Test Configuration

### Test Properties
The tests use `application-test.yml` configuration:
- H2 in-memory database for repository tests
- Mock S3 configuration
- Test-specific logging levels
- Disabled security for unit tests

### Test Dependencies
- **JUnit 5** - Core testing framework
- **Mockito** - Mocking framework for dependencies
- **Spring Boot Test** - Spring Boot testing utilities
- **H2 Database** - In-memory database for repository tests

## Running Tests

### Run All Tests
```bash
mvn test
```

### Run Specific Test Classes
```bash
mvn test -Dtest=InvoiceTest
mvn test -Dtest=InvoiceServiceImplTest
mvn test -Dtest=InvoiceRepositoryTest
```

### Run Test Suite
```bash
mvn test -Dtest=InvoiceModuleTestSuite
```

### Run Tests with Coverage
```bash
mvn test jacoco:report
```

## Test Coverage

The test suite provides comprehensive coverage for:

### Entity Layer
- ✅ Constructor validation
- ✅ Business logic methods
- ✅ State transitions
- ✅ Enum handling
- ✅ Edge cases

### Repository Layer
- ✅ CRUD operations
- ✅ Custom queries
- ✅ Query parameters
- ✅ Result validation
- ✅ Error handling

### Service Layer
- ✅ Business logic
- ✅ Dependency integration
- ✅ Error handling
- ✅ Transaction management
- ✅ Validation

### PDF Generation
- ✅ PDF creation
- ✅ Content validation
- ✅ Performance testing
- ✅ Error handling
- ✅ Edge cases

### S3 Operations
- ✅ File operations
- ✅ URL generation
- ✅ Error handling
- ✅ Integration testing
- ✅ Edge cases

## Test Categories

### Unit Tests
- Individual component testing
- Mocked dependencies
- Fast execution
- Isolated testing

### Integration Tests
- Component interaction testing
- Real database operations
- End-to-end workflows
- Performance validation

### Edge Case Tests
- Null value handling
- Boundary conditions
- Error scenarios
- Stress testing

## Best Practices

### Test Naming
- Use descriptive test names
- Follow the pattern: `should[ExpectedBehavior]When[StateUnderTest]`
- Use `@DisplayName` for better readability

### Test Organization
- Group related tests using `@Nested` classes
- Separate test categories logically
- Use `@BeforeEach` for test setup

### Assertions
- Use specific assertions
- Test both positive and negative cases
- Validate all relevant properties
- Include error message validation

### Mocking
- Mock external dependencies
- Verify mock interactions
- Use `@Mock` and `@InjectMocks` annotations
- Keep mocks simple and focused

## Maintenance

### Adding New Tests
1. Follow existing naming conventions
2. Use appropriate test categories
3. Include edge cases
4. Update this README if needed

### Updating Tests
1. Maintain backward compatibility
2. Update test data as needed
3. Ensure all tests pass
4. Update documentation

### Test Data
- Use realistic test data
- Include edge cases
- Avoid hardcoded values where possible
- Use builders for complex objects

## Troubleshooting

### Common Issues
1. **Test failures due to missing dependencies**
   - Check test configuration
   - Verify mock setup

2. **Database-related test failures**
   - Ensure H2 database is properly configured
   - Check test data setup

3. **Mock-related issues**
   - Verify mock configuration
   - Check mock interactions

4. **Performance test failures**
   - Adjust timeout values
   - Check system resources

### Debug Tips
- Enable debug logging in test configuration
- Use IDE debugging features
- Check test output for detailed error messages
- Verify test data setup

## Contributing

When contributing to the test suite:
1. Follow existing patterns
2. Add appropriate test coverage
3. Update documentation
4. Ensure all tests pass
5. Review test quality
