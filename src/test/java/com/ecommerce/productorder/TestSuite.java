package com.ecommerce.productorder;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Comprehensive test suite for Product Order Service
 * 
 * Design Principles Applied:
 * - Test Suite Organization: Groups all tests into logical suites
 * - Test Coverage: Ensures comprehensive test coverage
 * - Test Categories: Organizes tests by functionality
 * - Test Execution: Provides easy test execution
 * - Test Reporting: Enables detailed test reporting
 * - Test Maintenance: Simplifies test maintenance
 */
@Suite
@SuiteDisplayName("Product Order Service Test Suite")
@SelectPackages({
    "com.ecommerce.productorder.domain.service.impl",
    "com.ecommerce.productorder.controller",
    "com.ecommerce.productorder.payment.service.impl",
    "com.ecommerce.productorder.payment.grpc",
    "com.ecommerce.productorder.service.impl",
    "com.ecommerce.productorder.security",
    "com.ecommerce.productorder.performance"
})
public class TestSuite {
    // This class serves as a test suite container
    // All tests are automatically discovered and executed
}
