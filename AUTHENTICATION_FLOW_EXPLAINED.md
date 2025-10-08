# Authentication Flow - Detailed Explanation

## The Code You Asked About

```java
log.info("Attempting authentication for user: {}", request.getUsername());
Authentication authentication = authenticationManager.authenticate(
    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
);
```

This simple-looking code triggers a **complex chain of events**. Let me explain step-by-step!

---

## 🔐 Complete Authentication Flow

### Step-by-Step Breakdown

```
CLIENT REQUEST
     ↓
[1] AuthController.login()
     ↓
[2] authenticationManager.authenticate()
     ↓
[3] DaoAuthenticationProvider
     ↓
[4] CustomUserDetailsService.loadUserByUsername()
     ↓
[5] CustomerService → Database Query
     ↓
[6] Password Comparison (BCrypt)
     ↓
[7] Authentication Object Created
     ↓
[8] JWT Token Generated
     ↓
[9] Token Returned to Client
```

---

## 📖 Detailed Step-by-Step Explanation

### **STEP 1: Client Sends Login Request**

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

**What happens:**
- Request hits `AuthController.login()`
- Located at: `src/main/java/com/ecommerce/productorder/controller/AuthController.java`

```java
@PostMapping("/login")
public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    log.info("Login attempt for user: {}", request.getUsername());
    
    // THIS IS WHERE YOUR CODE STARTS! 👇
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
    );
    // ...
}
```

---

### **STEP 2: Create Authentication Token (Unauthenticated)**

```java
new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
```

**What this creates:**
```java
UsernamePasswordAuthenticationToken {
    principal: "admin",           // Username from request
    credentials: "admin123",      // Plain text password from request
    authenticated: false,         // Not yet authenticated!
    authorities: []              // No roles yet
}
```

This is just a **container** holding the username and password. It's NOT validated yet!

---

### **STEP 3: AuthenticationManager Processes Token**

```java
authenticationManager.authenticate(token)
```

**What happens:**

The `AuthenticationManager` is configured in `SecurityConfig.java`:

```java
@Bean
public AuthenticationManager authenticationManager() throws Exception {
    return new ProviderManager(authenticationProvider());
}
```

**AuthenticationManager delegates to `DaoAuthenticationProvider`:**

```java
@Bean
public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);      // ← How to load user
    authProvider.setPasswordEncoder(passwordEncoder);            // ← How to verify password
    return authProvider;
}
```

The `DaoAuthenticationProvider` does the actual authentication work!

---

### **STEP 4: DaoAuthenticationProvider - Load User from Database**

**DaoAuthenticationProvider internally calls:**

```java
UserDetails userDetails = userDetailsService.loadUserByUsername("admin");
```

This triggers `CustomUserDetailsService.loadUserByUsername()`:

**Location:** `src/main/java/com/ecommerce/productorder/config/CustomUserDetailsService.java`

```java
@Override
public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    log.info("Loading user details for username: {}", username);
    
    // Load from database via CustomerService
    return customerService.getCustomerEntityByUsername(username)
            .map(this::mapCustomerToUserDetails)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
}
```

---

### **STEP 5: Database Query**

**CustomerService queries the database:**

```sql
SELECT * FROM customers WHERE username = 'admin';
```

**If user exists:**
```java
Customer {
    id: 1,
    username: "admin",
    password: "$2a$10$XYZ...",  // ← BCrypt hashed password!
    role: "ADMIN",
    isActive: true
}
```

**If user NOT exists:**
- Throws `UsernameNotFoundException`
- Login fails immediately ❌

---

### **STEP 6: Convert Customer to UserDetails**

```java
private UserDetails mapCustomerToUserDetails(Customer customer) {
    return new User(
        customer.getUsername(),           // "admin"
        customer.getPassword(),           // "$2a$10$XYZ..." (hashed!)
        customer.getIsActive(),           // true
        true,                             // accountNonExpired
        true,                             // credentialsNonExpired
        !customer.getIsActive().equals(false), // accountNonLocked
        getAuthority(customer.getRole().name())  // ["ROLE_ADMIN"]
    );
}
```

**Returns:**
```java
UserDetails {
    username: "admin",
    password: "$2a$10$XYZ...",  // ← Hashed password from DB
    authorities: ["ROLE_ADMIN"],
    accountNonExpired: true,
    accountNonLocked: true,
    credentialsNonExpired: true,
    enabled: true
}
```

---

### **STEP 7: Password Verification (The Magic!)** 🔑

**DaoAuthenticationProvider now has:**

1. **Plain text password** from request: `"admin123"`
2. **Hashed password** from database: `"$2a$10$XYZ..."`

**It uses PasswordEncoder to compare:**

```java
boolean matches = passwordEncoder.matches(
    "admin123",           // Plain text from request
    "$2a$10$XYZ..."      // Hashed from database
);
```

**How BCrypt works:**

```java
// When user was created (DefaultUserInitializer.java):
String hashedPassword = passwordEncoder.encode("admin123");
// Result: "$2a$10$XYZ..." (different every time due to salt!)

// During login:
passwordEncoder.matches("admin123", "$2a$10$XYZ...")
// Returns: true or false

// BCrypt internally:
// 1. Extracts salt from stored hash
// 2. Hashes the plain password with same salt
// 3. Compares the two hashes
// 4. Returns true if match, false otherwise
```

**If passwords match:** ✅ Continue to Step 8  
**If passwords don't match:** ❌ Throws `BadCredentialsException`

---

### **STEP 8: Create Authenticated Token**

If password matches, `DaoAuthenticationProvider` creates **authenticated token:**

```java
Authentication authentication = new UsernamePasswordAuthenticationToken(
    userDetails,                  // Principal (user info)
    null,                        // Credentials (cleared for security!)
    userDetails.getAuthorities() // Authorities (ROLE_ADMIN)
);

authentication.setAuthenticated(true); // ✅ Marked as authenticated!
```

**Returns to AuthController:**
```java
Authentication {
    principal: UserDetails(username="admin", ...),
    credentials: null,           // Password cleared!
    authenticated: true,         // ✅ User is valid!
    authorities: ["ROLE_ADMIN"]
}
```

---

### **STEP 9: Generate JWT Token**

Back in `AuthController.login()`:

```java
// Authentication successful, load full user details
UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());

// Generate JWT token
String token = jwtUtil.generateToken(userDetails);
```

**JwtUtil.generateToken():**

```java
public String generateToken(UserDetails userDetails) {
    return Jwts.builder()
        .setSubject(userDetails.getUsername())           // "admin"
        .setIssuedAt(new Date())                        // Now
        .setExpiration(new Date() + 24 hours)           // Expires in 24h
        .signWith(getSigningKey(), HS256)               // Sign with secret
        .compact();                                     // Create token string
}
```

**Generated token:**
```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTY5ODM2MDA...
```

---

### **STEP 10: Return Token to Client**

```java
return ResponseEntity.ok(AuthResponse.builder()
    .token(token)              // JWT token
    .username("admin")         // Username
    .message("Login successful")
    .build());
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "admin",
  "message": "Login successful"
}
```

---

## 🔍 How Credentials Are Checked

### The Verification Chain:

1. **Username Check:**
   ```java
   customerService.getCustomerEntityByUsername(username)
   ```
   - Queries database: `SELECT * FROM customers WHERE username = ?`
   - If not found → `UsernameNotFoundException` → 401 Unauthorized

2. **Password Check:**
   ```java
   passwordEncoder.matches(plainPassword, hashedPassword)
   ```
   - Uses BCrypt algorithm
   - Compares plain text with hashed password
   - If doesn't match → `BadCredentialsException` → 401 Unauthorized

3. **Account Status Checks:**
   - `isActive` = true? (account enabled)
   - `accountNonExpired` = true?
   - `credentialsNonExpired` = true?
   - `accountNonLocked` = true?
   - If any false → Appropriate exception → 401 Unauthorized

4. **All checks pass:**
   - Create authenticated `Authentication` object
   - Generate JWT token
   - Return to client ✅

---

## 🗄️ Database Structure

### Customer Table (H2 Database):

```sql
CREATE TABLE customers (
    id BIGINT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,      -- BCrypt hash
    email VARCHAR(100) UNIQUE NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    phone VARCHAR(20),
    role VARCHAR(20),                    -- ADMIN, CUSTOMER
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

### Sample Data (from DefaultUserInitializer):

```sql
-- Admin user
INSERT INTO customers VALUES (
    1,
    'admin',
    '$2a$10$...',  -- BCrypt hash of "admin123"
    'admin@example.com',
    'Admin',
    'User',
    NULL,
    'ADMIN',
    true,
    NOW(),
    NOW()
);

-- Customer user
INSERT INTO customers VALUES (
    2,
    'customer',
    '$2a$10$...',  -- BCrypt hash of "customer123"
    'customer@example.com',
    'Test',
    'Customer',
    NULL,
    'CUSTOMER',
    true,
    NOW(),
    NOW()
);
```

---

## 🔐 Security Mechanisms

### 1. Password Hashing (BCrypt)

**Why BCrypt?**
- **Slow by design** - Prevents brute force attacks
- **Salted** - Different hash for same password
- **Adaptive** - Can increase complexity over time

**Example:**
```java
// Same password, different hashes!
encode("admin123") → "$2a$10$ABC..."
encode("admin123") → "$2a$10$XYZ..."  // Different due to random salt

// But both verify correctly:
matches("admin123", "$2a$10$ABC...") → true
matches("admin123", "$2a$10$XYZ...") → true
```

### 2. JWT Token

**Token Structure:**
```
Header.Payload.Signature

eyJhbGciOiJIUzI1NiJ9            // Header: {"alg":"HS256"}
.
eyJzdWIiOiJhZG1pbiIsImlhdCI6... // Payload: {"sub":"admin","iat":...}
.
SflKxwRJSMeKKF2QT4fwpMeJf36P... // Signature (signed with secret key)
```

**Token contains:**
- Username (subject)
- Issued at time
- Expiration time (24 hours)
- Signed with secret key (prevents tampering)

### 3. Spring Security Context

After authentication, Spring Security stores the user in **SecurityContext**:

```java
SecurityContextHolder.getContext().setAuthentication(authentication);
```

This allows any controller method to access current user:

```java
@GetMapping("/profile")
public UserProfile getProfile() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String username = auth.getName();  // Current logged-in user
    // ...
}
```

---

## 🔄 Complete Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│  1. CLIENT SENDS LOGIN REQUEST                                      │
│     POST /api/v1/auth/login                                         │
│     { "username": "admin", "password": "admin123" }                 │
└─────────────────────────────────────────────────────────────────────┘
                             ↓
┌─────────────────────────────────────────────────────────────────────┐
│  2. AUTHCONTROLLER RECEIVES REQUEST                                 │
│     • Logs: "Login attempt for user: admin"                         │
│     • Creates UsernamePasswordAuthenticationToken                   │
│     • Token contains: username + plain password                     │
└─────────────────────────────────────────────────────────────────────┘
                             ↓
┌─────────────────────────────────────────────────────────────────────┐
│  3. AUTHENTICATIONMANAGER DELEGATES TO PROVIDER                     │
│     • AuthenticationManager is just a coordinator                   │
│     • Delegates to DaoAuthenticationProvider                        │
│     • DaoAuthenticationProvider does the real work                  │
└─────────────────────────────────────────────────────────────────────┘
                             ↓
┌─────────────────────────────────────────────────────────────────────┐
│  4. DAOAUTHENTICATIONPROVIDER LOADS USER                            │
│     • Calls: userDetailsService.loadUserByUsername("admin")         │
│     • This triggers CustomUserDetailsService                        │
└─────────────────────────────────────────────────────────────────────┘
                             ↓
┌─────────────────────────────────────────────────────────────────────┐
│  5. CUSTOMUSERDETAILSSERVICE QUERIES DATABASE                       │
│     • Calls: customerService.getCustomerEntityByUsername("admin")   │
│     • CustomerService queries database                              │
│     • SQL: SELECT * FROM customers WHERE username = 'admin'         │
└─────────────────────────────────────────────────────────────────────┘
                             ↓
┌─────────────────────────────────────────────────────────────────────┐
│  6A. IF USER NOT FOUND                                              │
│     • Database returns: no rows                                     │
│     • Throws: UsernameNotFoundException                             │
│     • Login fails with 401 ❌                                       │
└─────────────────────────────────────────────────────────────────────┘

                             OR ↓

┌─────────────────────────────────────────────────────────────────────┐
│  6B. IF USER FOUND                                                  │
│     • Database returns Customer entity:                             │
│       {                                                             │
│         username: "admin",                                          │
│         password: "$2a$10$XYZ...",  ← BCrypt hashed!                │
│         role: "ADMIN",                                              │
│         isActive: true                                              │
│       }                                                             │
│     • Convert to UserDetails object                                 │
└─────────────────────────────────────────────────────────────────────┘
                             ↓
┌─────────────────────────────────────────────────────────────────────┐
│  7. CONVERT CUSTOMER TO USERDETAILS                                 │
│     mapCustomerToUserDetails(customer) creates:                     │
│     UserDetails {                                                   │
│         username: "admin",                                          │
│         password: "$2a$10$XYZ...",  ← Hashed from DB                │
│         authorities: ["ROLE_ADMIN"],                                │
│         enabled: true                                               │
│     }                                                               │
└─────────────────────────────────────────────────────────────────────┘
                             ↓
┌─────────────────────────────────────────────────────────────────────┐
│  8. DAOAUTHENTICATIONPROVIDER VERIFIES PASSWORD                     │
│     passwordEncoder.matches(                                        │
│         "admin123",         // ← Plain text from request            │
│         "$2a$10$XYZ..."     // ← Hashed from database               │
│     )                                                               │
│                                                                     │
│     BCrypt Process:                                                 │
│     1. Extract salt from stored hash: "$2a$10$..."                  │
│     2. Hash plain password with same salt                           │
│     3. Compare: new hash vs stored hash                             │
│     4. Return true/false                                            │
└─────────────────────────────────────────────────────────────────────┘
                             ↓
┌─────────────────────────────────────────────────────────────────────┐
│  9A. IF PASSWORDS DON'T MATCH                                       │
│     • passwordEncoder.matches() returns false                       │
│     • Throws: BadCredentialsException                               │
│     • Login fails with 401 ❌                                       │
└─────────────────────────────────────────────────────────────────────┘

                             OR ↓

┌─────────────────────────────────────────────────────────────────────┐
│  9B. IF PASSWORDS MATCH ✅                                          │
│     • passwordEncoder.matches() returns true                        │
│     • Check account status (enabled, non-locked, etc.)              │
│     • All checks pass!                                              │
│     • Create AUTHENTICATED token:                                   │
│       Authentication {                                              │
│         principal: UserDetails,                                     │
│         credentials: null,  ← Cleared for security!                 │
│         authenticated: true,  ← ✅ User is valid!                   │
│         authorities: ["ROLE_ADMIN"]                                 │
│       }                                                             │
└─────────────────────────────────────────────────────────────────────┘
                             ↓
┌─────────────────────────────────────────────────────────────────────┐
│  10. RETURN TO AUTHCONTROLLER                                       │
│     • authenticationManager.authenticate() returns successfully     │
│     • No exception thrown = authentication success!                 │
└─────────────────────────────────────────────────────────────────────┘
                             ↓
┌─────────────────────────────────────────────────────────────────────┐
│  11. GENERATE JWT TOKEN                                             │
│     jwtUtil.generateToken(userDetails) creates:                     │
│                                                                     │
│     JWT {                                                           │
│       header: { "alg": "HS256", "typ": "JWT" },                     │
│       payload: {                                                    │
│         "sub": "admin",          // Username                        │
│         "iat": 1698360000,       // Issued at timestamp             │
│         "exp": 1698446400        // Expires in 24h                  │
│       },                                                            │
│       signature: "..."           // Signed with secret key          │
│     }                                                               │
│                                                                     │
│     Token: "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI..."   │
└─────────────────────────────────────────────────────────────────────┘
                             ↓
┌─────────────────────────────────────────────────────────────────────┐
│  12. RETURN TOKEN TO CLIENT                                         │
│     HTTP 200 OK                                                     │
│     {                                                               │
│       "token": "eyJhbGciOiJIUzI1NiJ9...",                           │
│       "username": "admin",                                          │
│       "message": "Login successful"                                 │
│     }                                                               │
└─────────────────────────────────────────────────────────────────────┘
                             ↓
┌─────────────────────────────────────────────────────────────────────┐
│  13. CLIENT USES TOKEN FOR SUBSEQUENT REQUESTS                      │
│     Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...                   │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 🎯 Key Components Involved

### 1. **AuthController** (`AuthController.java`)
- **Role**: Entry point for login request
- **Action**: Calls authenticationManager

### 2. **AuthenticationManager** (`SecurityConfig.java`)
- **Role**: Coordinator/Manager
- **Action**: Delegates to authentication provider

### 3. **DaoAuthenticationProvider** (`SecurityConfig.java`)
- **Role**: Does the actual authentication
- **Action**: Loads user + verifies password

### 4. **CustomUserDetailsService** (`CustomUserDetailsService.java`)
- **Role**: Loads user from database
- **Action**: Queries Customer entity

### 5. **CustomerService** (`CustomerServiceImpl.java`)
- **Role**: Database access layer
- **Action**: JPA repository query

### 6. **PasswordEncoder** (`PasswordEncoderConfig.java`)
- **Role**: Password verification
- **Action**: BCrypt hashing and comparison

### 7. **JwtUtil** (`JwtUtil.java`)
- **Role**: JWT token generation/validation
- **Action**: Creates signed JWT tokens

---

## 🔐 Security Features

### Protection Against:

1. **SQL Injection** ✅
   - JPA parameterized queries
   - No raw SQL

2. **Password Storage** ✅
   - Never store plain text
   - BCrypt hashing with salt

3. **Brute Force** ✅
   - BCrypt is slow by design
   - Can add rate limiting

4. **Token Tampering** ✅
   - JWT signed with secret key
   - Any modification invalidates signature

5. **Token Theft** ⚠️
   - Use HTTPS in production
   - Store token securely (not localStorage)
   - Short expiration time (24h)

---

## 💡 What Makes This Secure?

### 1. **Never Compare Plain Passwords**
```java
// ❌ NEVER do this:
if (plainPassword.equals(dbPassword))

// ✅ Always use:
passwordEncoder.matches(plainPassword, hashedPassword)
```

### 2. **Password Never Sent Over Network (After Login)**
- Login: Send password once → Get token
- Subsequent requests: Send token only
- Password never leaves client again

### 3. **Credentials Cleared After Authentication**
```java
authentication.setCredentials(null);  // Password cleared from memory!
```

### 4. **Stateless Authentication**
- No sessions stored on server
- Token contains all necessary info
- Scales horizontally easily

---

## 🧪 Testing Authentication

### Valid Login:
```bash
curl -X POST http://localhost:8080/product-order-service/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Response:
# {
#   "token": "eyJhbGci...",
#   "username": "admin",
#   "message": "Login successful"
# }
```

### Invalid Username:
```bash
curl -X POST http://localhost:8080/product-order-service/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"nonexistent","password":"admin123"}'

# Response:
# {
#   "message": "Invalid credentials"
# }
# (UserNotFoundException caught)
```

### Invalid Password:
```bash
curl -X POST http://localhost:8080/product-order-service/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"wrongpassword"}'

# Response:
# {
#   "message": "Invalid credentials"
# }
# (BadCredentialsException caught)
```

---

## 📊 Summary

### The One-Liner Explanation:

```java
authenticationManager.authenticate(token)
```

**Does this:**
1. Loads user from database by username
2. Compares provided password (plain) with stored password (hashed) using BCrypt
3. Checks account status (active, non-locked, etc.)
4. If all pass → Returns authenticated `Authentication` object
5. If any fail → Throws exception (UsernameNotFoundException, BadCredentialsException, etc.)

### The Magic is in:

- **DaoAuthenticationProvider** - orchestrates the process
- **CustomUserDetailsService** - loads user from DB
- **PasswordEncoder** - securely compares passwords
- **Database** - source of truth for credentials

---

## 🎓 Design Patterns Used

1. **Strategy Pattern**
   - Different authentication strategies (DAO, LDAP, OAuth, etc.)
   - We use DaoAuthenticationProvider

2. **Template Method Pattern**
   - AuthenticationManager defines template
   - Providers implement specific steps

3. **Chain of Responsibility**
   - AuthenticationManager → Provider → UserDetailsService

4. **Factory Pattern**
   - AuthenticationManager factory
   - UserDetails factory

5. **Dependency Injection**
   - All components injected
   - Loose coupling

---

## 🔗 Related Files

- `AuthController.java` - Login endpoint
- `SecurityConfig.java` - Authentication configuration
- `CustomUserDetailsService.java` - User loading logic
- `CustomerService.java` - Database access
- `PasswordEncoderConfig.java` - BCrypt configuration
- `JwtUtil.java` - Token generation
- `JwtRequestFilter.java` - Token validation on subsequent requests

---

This authentication flow is **industry-standard** and follows **Spring Security best practices**! 🚀

