package com.ecommerce.productorder.invoice;

import com.ecommerce.productorder.invoice.domain.entity.InvoiceTest;
import com.ecommerce.productorder.invoice.domain.repository.InvoiceRepositoryTest;
import com.ecommerce.productorder.invoice.service.impl.InvoiceServiceImplTest;
import com.ecommerce.productorder.invoice.service.impl.PdfGeneratorServiceImplTest;
import com.ecommerce.productorder.invoice.service.impl.S3ServiceImplTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Comprehensive test suite for the Invoice module. Runs all unit tests for the invoice module
 * components.
 */
@Suite
@SuiteDisplayName("Invoice Module Test Suite")
@SelectClasses({
  InvoiceTest.class,
  InvoiceRepositoryTest.class,
  InvoiceServiceImplTest.class,
  PdfGeneratorServiceImplTest.class,
  S3ServiceImplTest.class
})
public class InvoiceModuleTestSuite {}
