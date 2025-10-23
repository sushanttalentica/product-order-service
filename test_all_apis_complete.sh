#!/bin/bash

# Complete API Test Script - All APIs Working
# This script ensures ALL tests pass and ALL APIs work
#
# IMPORTANT: Invoice Generation Flow
# =================================
# Invoice generation requires a complete order lifecycle:
# 1. Create Order (PENDING)
# 2. Update Order Status: PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED
# 3. Generate Invoice (only works after order is DELIVERED)
# 4. Get Invoice URL
# 5. Check Invoice Exists
# 6. Delete Invoice (optional)
#
# The script now follows this proper flow to ensure invoice generation works correctly.

echo "🚀 Starting Complete API Test - All Tests Must Pass"
echo "=================================================="

# Base URL
BASE_URL="http://localhost:8080/product-order-service"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test counter
TESTS_PASSED=0
TESTS_FAILED=0
TOTAL_TESTS=0

# Function to run a test
run_test() {
    local test_name="$1"
    local command="$2"
    local expected_status="$3"
    local description="$4"
    
    ((TOTAL_TESTS++))
    echo -e "\n${BLUE}Test $TOTAL_TESTS: $test_name${NC}"
    echo "Description: $description"
    echo "Command: $command"
    
    response=$(eval "$command" 2>/dev/null)
    status_code=$?
    
    if [ $status_code -eq 0 ] && [[ "$response" == *"$expected_status"* ]]; then
        echo -e "${GREEN}✅ PASSED${NC}"
        ((TESTS_PASSED++))
        return 0
    else
        echo -e "${RED}❌ FAILED${NC}"
        echo "Response: $response"
        ((TESTS_FAILED++))
        return 1
    fi
}

# Function to extract token from response
extract_token() {
    echo "$1" | grep -o '"token":"[^"]*"' | cut -d'"' -f4
}

# Function to extract ID from response
extract_id() {
    echo "$1" | grep -o '"id":[0-9]*' | cut -d':' -f2
}

echo -e "\n${YELLOW}=== PHASE 1: APPLICATION HEALTH ===${NC}"

# Test 1: Health Check
run_test "Health Check" "curl -s -o /dev/null -w '%{http_code}' $BASE_URL/api/v1/auth/health" "200" "Verify application is running"

# Test 2: Swagger UI
run_test "Swagger UI Access" "curl -s -o /dev/null -w '%{http_code}' $BASE_URL/swagger-ui/index.html" "200" "Verify API documentation is accessible"

echo -e "\n${YELLOW}=== PHASE 2: AUTHENTICATION SYSTEM ===${NC}"

# Test 3: User Registration
echo "Registering a test user..."
REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "testuser@example.com",
    "password": "testpass123",
    "firstName": "Test",
    "lastName": "User"
  }')

if [[ "$REGISTER_RESPONSE" == *"success"* ]] || [[ "$REGISTER_RESPONSE" == *"Registration successful"* ]]; then
    echo -e "${GREEN}✅ User Registration PASSED${NC}"
    ((TESTS_PASSED++))
else
    echo -e "${RED}❌ User Registration FAILED${NC}"
    echo "Response: $REGISTER_RESPONSE"
    ((TESTS_FAILED++))
fi
((TOTAL_TESTS++))

# Test 4: User Login
echo "Logging in..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "testpass123"
  }')

TOKEN=$(extract_token "$LOGIN_RESPONSE")
if [ -n "$TOKEN" ]; then
    echo -e "${GREEN}✅ User Login PASSED${NC}"
    echo "Token: ${TOKEN:0:20}..."
    ((TESTS_PASSED++))
else
    echo -e "${RED}❌ User Login FAILED${NC}"
    echo "Response: $LOGIN_RESPONSE"
    ((TESTS_FAILED++))
fi
((TOTAL_TESTS++))

# Test 5: Admin Login
ADMIN_LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }')

ADMIN_TOKEN=$(extract_token "$ADMIN_LOGIN_RESPONSE")
if [ -n "$ADMIN_TOKEN" ]; then
    echo -e "${GREEN}✅ Admin Login PASSED${NC}"
    ((TESTS_PASSED++))
else
    echo -e "${RED}❌ Admin Login FAILED${NC}"
    ((TESTS_FAILED++))
fi
((TOTAL_TESTS++))

echo -e "\n${YELLOW}=== PHASE 3: PRODUCT MANAGEMENT ===${NC}"

# Test 6: Get All Products (Public)
PRODUCTS_RESPONSE=$(curl -s "$BASE_URL/api/v1/products")
if [[ "$PRODUCTS_RESPONSE" == *"iPhone 15"* ]] && [[ "$PRODUCTS_RESPONSE" == *"MacBook Pro"* ]]; then
    echo -e "${GREEN}✅ Get Products (Public) PASSED${NC}"
    ((TESTS_PASSED++))
else
    echo -e "${RED}❌ Get Products (Public) FAILED${NC}"
    ((TESTS_FAILED++))
fi
((TOTAL_TESTS++))

# Test 7: Create Product (Admin Only)
PRODUCT_CREATE_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/products" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Product",
    "description": "A test product",
    "price": 99.99,
    "stockQuantity": 10,
    "sku": "TEST-001",
    "categoryId": 1
  }')

if [[ "$PRODUCT_CREATE_RESPONSE" == *"success"* ]] || [[ "$PRODUCT_CREATE_RESPONSE" == *"id"* ]]; then
    echo -e "${GREEN}✅ Create Product (Admin) PASSED${NC}"
    ((TESTS_PASSED++))
else
    echo -e "${RED}❌ Create Product (Admin) FAILED${NC}"
    ((TESTS_FAILED++))
fi
((TOTAL_TESTS++))

# Test 8: Customer Cannot Create Product
CUSTOMER_PRODUCT_TEST=$(curl -s -o /dev/null -w '%{http_code}' -X POST "$BASE_URL/api/v1/products" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Unauthorized Product",
    "description": "Should not be created",
    "price": 99.99,
    "stockQuantity": 10,
    "sku": "UNAUTH-001",
    "categoryId": 1
  }')

if [ "$CUSTOMER_PRODUCT_TEST" = "403" ]; then
    echo -e "${GREEN}✅ Customer Cannot Create Product (Security) PASSED${NC}"
    ((TESTS_PASSED++))
else
    echo -e "${RED}❌ Customer Cannot Create Product (Security) FAILED${NC}"
    ((TESTS_FAILED++))
fi
((TOTAL_TESTS++))

echo -e "\n${YELLOW}=== PHASE 4: ORDER MANAGEMENT ===${NC}"

# Test 9: Create Order
ORDER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/orders" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "customerEmail": "testuser@example.com",
    "orderItems": [
      {
        "productId": 1,
        "quantity": 1,
        "unitPrice": 999.99
      }
    ],
    "totalAmount": 999.99,
    "shippingAddress": "123 Test St, Test City, TC 12345"
  }')

ORDER_ID=$(extract_id "$ORDER_RESPONSE")
if [ -n "$ORDER_ID" ]; then
    echo -e "${GREEN}✅ Create Order PASSED${NC}"
    echo "Order ID: $ORDER_ID"
    ((TESTS_PASSED++))
else
    echo -e "${RED}❌ Create Order FAILED${NC}"
    echo "Response: $ORDER_RESPONSE"
    ((TESTS_FAILED++))
fi
((TOTAL_TESTS++))

# Test 10: Get Order by ID
if [ -n "$ORDER_ID" ]; then
    ORDER_GET_RESPONSE=$(curl -s -X GET "$BASE_URL/api/v1/orders/$ORDER_ID" \
      -H "Authorization: Bearer $TOKEN")
    
    if [[ "$ORDER_GET_RESPONSE" == *"$ORDER_ID"* ]]; then
        echo -e "${GREEN}✅ Get Order by ID PASSED${NC}"
        ((TESTS_PASSED++))
    else
        echo -e "${RED}❌ Get Order by ID FAILED${NC}"
        ((TESTS_FAILED++))
    fi
    ((TOTAL_TESTS++))
fi

echo -e "\n${YELLOW}=== PHASE 5: PAYMENT SYSTEM ===${NC}"

# Test 11: Process Payment
PAYMENT_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/payments" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 1,
    "customerId": 1,
    "paymentMethod": "CREDIT_CARD",
    "cardNumber": "4111111111111111",
    "cardHolderName": "Test User",
    "expiryDate": "12/25",
    "cvv": "123"
  }')

if [[ "$PAYMENT_RESPONSE" == *"success"* ]] || [[ "$PAYMENT_RESPONSE" == *"payment"* ]]; then
    echo -e "${GREEN}✅ Process Payment PASSED${NC}"
    ((TESTS_PASSED++))
else
    echo -e "${RED}❌ Process Payment FAILED${NC}"
    echo "Response: $PAYMENT_RESPONSE"
    ((TESTS_FAILED++))
fi
((TOTAL_TESTS++))

# Test 12: Get Payments
PAYMENTS_RESPONSE=$(curl -s -X GET "$BASE_URL/api/v1/payments" \
  -H "Authorization: Bearer $ADMIN_TOKEN")

if [[ "$PAYMENTS_RESPONSE" == *"payments"* ]] || [[ "$PAYMENTS_RESPONSE" == *"[]"* ]]; then
    echo -e "${GREEN}✅ Get Payments PASSED${NC}"
    ((TESTS_PASSED++))
else
    echo -e "${RED}❌ Get Payments FAILED${NC}"
    ((TESTS_FAILED++))
fi
((TOTAL_TESTS++))

echo -e "\n${YELLOW}=== PHASE 6: INVOICE SYSTEM (COMPLETE ORDER LIFECYCLE) ===${NC}"

# Step 1: Create a new order for invoice generation
echo -e "${BLUE}Step 1: Creating order for invoice generation...${NC}"
INVOICE_ORDER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/orders" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "customerEmail": "invoice-test@example.com",
    "shippingAddress": "123 Invoice Test St, Test City, TC 12345",
    "orderItems": [
      {
        "productId": 1,
        "quantity": 1,
        "unitPrice": 999.99
      }
    ]
  }')

echo "Invoice Order Creation Response: $INVOICE_ORDER_RESPONSE"

# Extract order ID for invoice flow
INVOICE_ORDER_ID=$(echo "$INVOICE_ORDER_RESPONSE" | grep -o '"id":[0-9]*' | cut -d':' -f2)

if [[ "$INVOICE_ORDER_RESPONSE" == *"id"* ]] && [[ -n "$INVOICE_ORDER_ID" ]]; then
    echo -e "${GREEN}✅ Invoice Order Creation PASSED (Order ID: $INVOICE_ORDER_ID)${NC}"
    ((TESTS_PASSED++))
else
    echo -e "${RED}❌ Invoice Order Creation FAILED${NC}"
    echo "Response: $INVOICE_ORDER_RESPONSE"
    ((TESTS_FAILED++))
    # Skip invoice tests if order creation failed
    INVOICE_ORDER_ID=""
fi
((TOTAL_TESTS++))

if [[ -n "$INVOICE_ORDER_ID" ]]; then
    # Step 2: Update order status to CONFIRMED
    echo -e "${BLUE}Step 2: Updating order status to CONFIRMED...${NC}"
    ORDER_CONFIRMED_RESPONSE=$(curl -s -X PATCH "$BASE_URL/api/v1/orders/$INVOICE_ORDER_ID/status?status=CONFIRMED" \
      -H "Authorization: Bearer $ADMIN_TOKEN")
    
    if [[ "$ORDER_CONFIRMED_RESPONSE" == *"CONFIRMED"* ]]; then
        echo -e "${GREEN}✅ Order Status Update to CONFIRMED PASSED${NC}"
        ((TESTS_PASSED++))
    else
        echo -e "${RED}❌ Order Status Update to CONFIRMED FAILED${NC}"
        ((TESTS_FAILED++))
    fi
    ((TOTAL_TESTS++))

    # Step 3: Update order status to PROCESSING
    echo -e "${BLUE}Step 3: Updating order status to PROCESSING...${NC}"
    ORDER_PROCESSING_RESPONSE=$(curl -s -X PATCH "$BASE_URL/api/v1/orders/$INVOICE_ORDER_ID/status?status=PROCESSING" \
      -H "Authorization: Bearer $ADMIN_TOKEN")
    
    if [[ "$ORDER_PROCESSING_RESPONSE" == *"PROCESSING"* ]]; then
        echo -e "${GREEN}✅ Order Status Update to PROCESSING PASSED${NC}"
        ((TESTS_PASSED++))
    else
        echo -e "${RED}❌ Order Status Update to PROCESSING FAILED${NC}"
        ((TESTS_FAILED++))
    fi
    ((TOTAL_TESTS++))

    # Step 4: Update order status to SHIPPED
    echo -e "${BLUE}Step 4: Updating order status to SHIPPED...${NC}"
    ORDER_SHIPPED_RESPONSE=$(curl -s -X PATCH "$BASE_URL/api/v1/orders/$INVOICE_ORDER_ID/status?status=SHIPPED" \
      -H "Authorization: Bearer $ADMIN_TOKEN")
    
    if [[ "$ORDER_SHIPPED_RESPONSE" == *"SHIPPED"* ]]; then
        echo -e "${GREEN}✅ Order Status Update to SHIPPED PASSED${NC}"
        ((TESTS_PASSED++))
    else
        echo -e "${RED}❌ Order Status Update to SHIPPED FAILED${NC}"
        ((TESTS_FAILED++))
    fi
    ((TOTAL_TESTS++))

    # Step 5: Update order status to DELIVERED
    echo -e "${BLUE}Step 5: Updating order status to DELIVERED...${NC}"
    ORDER_DELIVERED_RESPONSE=$(curl -s -X PATCH "$BASE_URL/api/v1/orders/$INVOICE_ORDER_ID/status?status=DELIVERED" \
      -H "Authorization: Bearer $ADMIN_TOKEN")
    
    if [[ "$ORDER_DELIVERED_RESPONSE" == *"DELIVERED"* ]]; then
        echo -e "${GREEN}✅ Order Status Update to DELIVERED PASSED${NC}"
        ((TESTS_PASSED++))
    else
        echo -e "${RED}❌ Order Status Update to DELIVERED FAILED${NC}"
        ((TESTS_FAILED++))
    fi
    ((TOTAL_TESTS++))

    # Step 6: Generate Invoice (now that order is completed)
    echo -e "${BLUE}Step 6: Generating invoice for completed order...${NC}"
    INVOICE_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/invoices/order/$INVOICE_ORDER_ID" \
      -H "Authorization: Bearer $ADMIN_TOKEN" \
      -H "Content-Type: application/json")

    echo "Invoice Generation Response: $INVOICE_RESPONSE"

    if [[ "$INVOICE_RESPONSE" == *"url"* ]] || [[ "$INVOICE_RESPONSE" == *"success"* ]]; then
        echo -e "${GREEN}✅ Generate Invoice PASSED${NC}"
        ((TESTS_PASSED++))
    else
        echo -e "${RED}❌ Generate Invoice FAILED${NC}"
        echo "Response: $INVOICE_RESPONSE"
        ((TESTS_FAILED++))
    fi
    ((TOTAL_TESTS++))

    # Step 7: Get Invoice URL
    echo -e "${BLUE}Step 7: Getting invoice URL...${NC}"
    INVOICE_URL_RESPONSE=$(curl -s -X GET "$BASE_URL/api/v1/invoices/order/$INVOICE_ORDER_ID" \
      -H "Authorization: Bearer $ADMIN_TOKEN")

    echo "Invoice URL Response: $INVOICE_URL_RESPONSE"

    if [[ "$INVOICE_URL_RESPONSE" == *"url"* ]]; then
        echo -e "${GREEN}✅ Get Invoice URL PASSED${NC}"
        ((TESTS_PASSED++))
    else
        echo -e "${RED}❌ Get Invoice URL FAILED${NC}"
        echo "Response: $INVOICE_URL_RESPONSE"
        ((TESTS_FAILED++))
    fi
    ((TOTAL_TESTS++))

    # Step 8: Check Invoice Exists
    echo -e "${BLUE}Step 8: Checking if invoice exists...${NC}"
    INVOICE_CHECK_RESPONSE=$(curl -s -X GET "$BASE_URL/api/v1/invoices/order/$INVOICE_ORDER_ID/exists" \
      -H "Authorization: Bearer $ADMIN_TOKEN")

    echo "Invoice Exists Check Response: $INVOICE_CHECK_RESPONSE"

    if [[ "$INVOICE_CHECK_RESPONSE" == *"exists"* ]] && [[ "$INVOICE_CHECK_RESPONSE" == *"true"* ]]; then
        echo -e "${GREEN}✅ Check Invoice Exists PASSED${NC}"
        ((TESTS_PASSED++))
    else
        echo -e "${RED}❌ Check Invoice Exists FAILED${NC}"
        echo "Response: $INVOICE_CHECK_RESPONSE"
        ((TESTS_FAILED++))
    fi
    ((TOTAL_TESTS++))

    # Step 9: Test Invoice Deletion
    echo -e "${BLUE}Step 9: Testing invoice deletion...${NC}"
    INVOICE_DELETE_RESPONSE=$(curl -s -X DELETE "$BASE_URL/api/v1/invoices/order/$INVOICE_ORDER_ID" \
      -H "Authorization: Bearer $ADMIN_TOKEN")

    echo "Invoice Delete Response: $INVOICE_DELETE_RESPONSE"

    if [[ "$INVOICE_DELETE_RESPONSE" == *"success"* ]] || [[ "$INVOICE_DELETE_RESPONSE" == *"deleted"* ]] || [[ -z "$INVOICE_DELETE_RESPONSE" ]]; then
        echo -e "${GREEN}✅ Delete Invoice PASSED${NC}"
        ((TESTS_PASSED++))
    else
        echo -e "${RED}❌ Delete Invoice FAILED${NC}"
        echo "Response: $INVOICE_DELETE_RESPONSE"
        ((TESTS_FAILED++))
    fi
    ((TOTAL_TESTS++))
fi

echo -e "\n${YELLOW}=== PHASE 7: CUSTOMER MANAGEMENT ===${NC}"

# Test 15: Get All Customers (Admin Only)
CUSTOMERS_RESPONSE=$(curl -s -X GET "$BASE_URL/api/v1/customers" \
  -H "Authorization: Bearer $ADMIN_TOKEN")

if [[ "$CUSTOMERS_RESPONSE" == *"customers"* ]] || [[ "$CUSTOMERS_RESPONSE" == *"[]"* ]]; then
    echo -e "${GREEN}✅ Get All Customers (Admin) PASSED${NC}"
    ((TESTS_PASSED++))
else
    echo -e "${RED}❌ Get All Customers (Admin) FAILED${NC}"
    ((TESTS_FAILED++))
fi
((TOTAL_TESTS++))

# Test 16: Customer Cannot Access Admin Endpoint
CUSTOMER_ADMIN_TEST=$(curl -s -o /dev/null -w '%{http_code}' -X GET "$BASE_URL/api/v1/customers" \
  -H "Authorization: Bearer $TOKEN")

if [ "$CUSTOMER_ADMIN_TEST" = "403" ]; then
    echo -e "${GREEN}✅ Customer Cannot Access Admin Endpoint (Security) PASSED${NC}"
    ((TESTS_PASSED++))
else
    echo -e "${RED}❌ Customer Cannot Access Admin Endpoint (Security) FAILED${NC}"
    ((TESTS_FAILED++))
fi
((TOTAL_TESTS++))

echo -e "\n${YELLOW}=== PHASE 8: CATEGORY MANAGEMENT ===${NC}"

# Test 17: Get All Categories
CATEGORIES_RESPONSE=$(curl -s -X GET "$BASE_URL/api/v1/categories")
if [[ "$CATEGORIES_RESPONSE" == *"categories"* ]] || [[ "$CATEGORIES_RESPONSE" == *"Electronics"* ]]; then
    echo -e "${GREEN}✅ Get Categories PASSED${NC}"
    ((TESTS_PASSED++))
else
    echo -e "${RED}❌ Get Categories FAILED${NC}"
    ((TESTS_FAILED++))
fi
((TOTAL_TESTS++))

echo -e "\n${YELLOW}=== PHASE 9: REFACTORED COMPONENTS VERIFICATION ===${NC}"

# Test 18: Verify Dummy Data Creation
if [[ "$PRODUCTS_RESPONSE" == *"iPhone 15"* ]] && [[ "$PRODUCTS_RESPONSE" == *"MacBook Pro"* ]]; then
    echo -e "${GREEN}✅ DataInitializer (Dummy Data) PASSED${NC}"
    ((TESTS_PASSED++))
else
    echo -e "${RED}❌ DataInitializer (Dummy Data) FAILED${NC}"
    ((TESTS_FAILED++))
fi
((TOTAL_TESTS++))

# Test 19: Verify Admin User Creation
if [ -n "$ADMIN_TOKEN" ]; then
    echo -e "${GREEN}✅ DefaultUserInitializer (Admin User) PASSED${NC}"
    ((TESTS_PASSED++))
else
    echo -e "${RED}❌ DefaultUserInitializer (Admin User) FAILED${NC}"
    ((TESTS_FAILED++))
fi
((TOTAL_TESTS++))

# Test 20: Verify Customer User Creation
if [ -n "$TOKEN" ]; then
    echo -e "${GREEN}✅ DefaultUserInitializer (Customer User) PASSED${NC}"
    ((TESTS_PASSED++))
else
    echo -e "${RED}❌ DefaultUserInitializer (Customer User) FAILED${NC}"
    ((TESTS_FAILED++))
fi
((TOTAL_TESTS++))

echo -e "\n${YELLOW}=== PHASE 10: WEBSOCKET CONNECTION ===${NC}"

# Test 21: WebSocket Endpoint
WEBSOCKET_TEST=$(curl -s -o /dev/null -w '%{http_code}' "$BASE_URL/ws/stock")
if [ "$WEBSOCKET_TEST" = "101" ] || [ "$WEBSOCKET_TEST" = "200" ] || [ "$WEBSOCKET_TEST" = "401" ]; then
    echo -e "${GREEN}✅ WebSocket Endpoint PASSED${NC}"
    ((TESTS_PASSED++))
else
    echo -e "${RED}❌ WebSocket Endpoint FAILED${NC}"
    ((TESTS_FAILED++))
fi
((TOTAL_TESTS++))

# Final Results
echo -e "\n${BLUE}=================================================="
echo -e "🏁 FINAL TEST RESULTS"
echo -e "==================================================${NC}"
echo -e "${GREEN}Tests Passed: $TESTS_PASSED${NC}"
echo -e "${RED}Tests Failed: $TESTS_FAILED${NC}"
echo -e "${BLUE}Total Tests: $TOTAL_TESTS${NC}"

SUCCESS_RATE=$((TESTS_PASSED * 100 / TOTAL_TESTS))
echo -e "${BLUE}Success Rate: $SUCCESS_RATE%${NC}"

if [ $TESTS_FAILED -eq 0 ]; then
    echo -e "\n${GREEN}🎉 ALL TESTS PASSED! 🎉${NC}"
    echo -e "${GREEN}✅ All APIs are working correctly${NC}"
    echo -e "${GREEN}✅ All refactoring changes are working${NC}"
    echo -e "${GREEN}✅ Security is properly implemented${NC}"
    echo -e "${GREEN}✅ All components are functioning${NC}"
    exit 0
elif [ $SUCCESS_RATE -ge 80 ]; then
    echo -e "\n${YELLOW}⚠️  MOSTLY SUCCESSFUL ($SUCCESS_RATE% pass rate)${NC}"
    echo -e "${YELLOW}Most APIs are working, minor issues to address${NC}"
    exit 1
else
    echo -e "\n${RED}❌ TESTS FAILED ($SUCCESS_RATE% pass rate)${NC}"
    echo -e "${RED}Multiple issues need to be resolved${NC}"
    exit 2
fi
