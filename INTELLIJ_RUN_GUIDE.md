# Running Application in IntelliJ IDEA

Complete guide to run and debug the application directly in IntelliJ.

---

## 🔧 Prerequisites (One-Time Setup)

### Step 1: Fix Java Configuration

1. **Set Project SDK**
   - `File` → `Project Structure` (⌘ + ;)
   - `Project` → `SDK`: Select **17**
   - If not available:
     - Click `+` → `Add JDK`
     - Navigate to: `/opt/homebrew/Cellar/openjdk@17/17.0.16/libexec/openjdk.jdk/Contents/Home`
   - Click `Apply`

2. **Set Maven Runner JRE**
   - `Preferences` → `Build, Execution, Deployment` → `Maven` → `Runner`
   - `JRE`: Select **17** (NOT "Use Project JDK")
   - Full path: `/opt/homebrew/Cellar/openjdk@17/17.0.16/libexec/openjdk.jdk/Contents/Home`
   - Click `Apply`

3. **Enable Annotation Processing**
   - `Preferences` → `Compiler` → `Annotation Processors`
   - ✅ Check "Enable annotation processing"
   - `Apply`

4. **Install Lombok Plugin** (if not installed)
   - `Preferences` → `Plugins`
   - Search: "Lombok"
   - Click `Install`
   - Restart IntelliJ

### Step 2: Build the Project

1. **Reload Maven Project**
   - Right-click `pom.xml`
   - Select `Maven` → `Reload Project`
   - Wait for completion

2. **Mark Generated Sources**
   - After build, expand: `target/generated-sources/protobuf/`
   - Right-click `java` folder
   - `Mark Directory as` → `Generated Sources Root` (folder turns blue)
   - Right-click `grpc-java` folder
   - `Mark Directory as` → `Generated Sources Root`

3. **Build Project**
   - `Build` → `Rebuild Project`
   - Wait for success

---

## ▶️ Method 1: Run Main Class (Easiest)

### Setup Run Configuration:

1. **Open Main Class**
   - Navigate to: `src/main/java/com/ecommerce/productorder/ProductOrderServiceApplication.java`

2. **Right-click on the class**
   - Select `Run 'ProductOrderServiceApplication'`
   
   OR
   
   - Click the green ▶️ icon next to `public class ProductOrderServiceApplication`

3. **Application Starts!**
   - Console shows Spring Boot startup logs
   - Look for: "Started ProductOrderServiceApplication in X seconds"

### Access Application:

- **Base URL**: http://localhost:8080/product-order-service
- **Swagger UI**: http://localhost:8080/product-order-service/swagger-ui.html
- **H2 Console**: http://localhost:8080/product-order-service/h2-console
- **Health Check**: http://localhost:8080/product-order-service/actuator/health

---

## ▶️ Method 2: Maven Run Configuration

### Create Custom Run Configuration:

1. **Run → Edit Configurations**

2. **Click + → Maven**

3. **Configure:**
   - **Name**: `Run Application`
   - **Working directory**: `$ProjectFileDir$`
   - **Command line**: `spring-boot:run`
   - **JRE**: Select Java 17

4. **Click Apply → OK**

5. **Run:**
   - Select "Run Application" from dropdown
   - Click ▶️ Run

---

## 🐛 Method 3: Debug Mode

### Run with Breakpoints:

1. **Open any file** (e.g., `ProductServiceImpl.java`)

2. **Set Breakpoints:**
   - Click in the left margin (next to line numbers)
   - Red dot appears

3. **Start Debug:**
   - Right-click `ProductOrderServiceApplication`
   - Select `Debug 'ProductOrderServiceApplication'`
   
   OR
   
   - Click 🐛 Debug icon in toolbar

4. **Make API Calls:**
   - Use Swagger UI or curl
   - Breakpoints will pause execution
   - Inspect variables, step through code

### Debug Controls:

- **F8**: Step Over (next line)
- **F7**: Step Into (enter method)
- **Shift + F8**: Step Out (exit method)
- **F9**: Resume (continue to next breakpoint)
- **⌘ + F8**: Toggle breakpoint

---

## ⚙️ Method 4: Custom Run Configuration with Environment Variables

### For AWS S3 Invoice Generation:

1. **Run → Edit Configurations**

2. **Select** your application configuration (or create new)

3. **Environment Variables:**
   - Click `...` next to "Environment variables"
   - Add:
     ```
     AWS_ACCESS_KEY_ID=your-key
     AWS_SECRET_ACCESS_KEY=your-secret
     AWS_REGION=us-east-1
     AWS_S3_BUCKET_NAME=my-pos-bucket-125
     ```

4. **VM Options** (optional, for more memory):
   ```
   -Xmx2048m -Xms512m
   ```

5. **Apply → OK**

6. **Run/Debug** as normal

---

## 🔄 Hot Reload Development

### Enable Spring Boot DevTools (Already in pom.xml):

1. **Preferences → Build, Execution, Deployment → Compiler**
   - ✅ "Build project automatically"

2. **Preferences → Advanced Settings**
   - ✅ "Allow auto-make to start even if developed application is currently running"

3. **Make Changes to Code**

4. **Build → Build Project** (⌘ + F9)
   - Application automatically restarts with changes!

---

## 🌐 Access Application Endpoints

After starting in IntelliJ:

### Swagger UI (Interactive API Documentation)
```
http://localhost:8080/product-order-service/swagger-ui.html
```

### H2 Database Console
```
URL: http://localhost:8080/product-order-service/h2-console
JDBC URL: jdbc:h2:mem:testdb
Username: sa
Password: (leave empty)
```

### Health Check
```
http://localhost:8080/product-order-service/actuator/health
```

### Actuator Endpoints
```
http://localhost:8080/product-order-service/actuator
```

---

## 📝 Testing APIs from IntelliJ

### Method 1: Use Swagger UI

1. Open: http://localhost:8080/product-order-service/swagger-ui.html
2. Click on any endpoint
3. Click "Try it out"
4. Fill parameters
5. Click "Execute"

### Method 2: IntelliJ HTTP Client

Create file: `test-api.http`

```http
### Login as Admin
POST http://localhost:8080/product-order-service/api/v1/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}

> {%
  client.global.set("admin_token", response.body.token);
%}

### Create Product
POST http://localhost:8080/product-order-service/api/v1/products
Content-Type: application/json
Authorization: Bearer {{admin_token}}

{
  "name": "Test Product",
  "description": "Created from IntelliJ",
  "price": 99.99,
  "stockQuantity": 100,
  "sku": "TEST-001",
  "categoryId": 1
}

### Get All Products
GET http://localhost:8080/product-order-service/api/v1/products
Authorization: Bearer {{admin_token}}
```

Click ▶️ next to each request to execute!

### Method 3: Use Terminal in IntelliJ

Open Terminal tab (bottom) and run curl commands:

```bash
# Login
curl -X POST http://localhost:8080/product-order-service/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

---

## 🛑 Stop Application

### From Run Window:

- Click ⏹️ Stop button (red square) in toolbar
- Or press ⌘ + F2

### Force Stop:

- If application hangs: `Run` → `Stop` → `Force Stop`

---

## 🔍 View Logs in IntelliJ

### Console Output:

- **Run tab** (bottom): Shows application logs in real-time
- **Search**: ⌘ + F to search logs
- **Filter**: Click filter icon to show only errors/warnings

### Log Files:

- Navigate to: `logs/` directory in Project view
- Double-click to open in editor

---

## ⚡ Performance Tips

### Increase IntelliJ Memory:

1. **Help → Edit Custom VM Options**
2. Modify:
   ```
   -Xmx4096m
   -XX:ReservedCodeCacheSize=1024m
   ```
3. Restart IntelliJ

### Exclude Folders from Indexing:

1. Right-click on `target` folder
2. `Mark Directory as` → `Excluded`
3. Same for `node_modules`, `logs` if present

---

## 🚨 Troubleshooting

### Issue 1: "Port 8080 already in use"

**Check what's using port:**
```bash
lsof -i :8080
```

**Kill the process:**
```bash
kill -9 <PID>
```

**Or change port in `application.yml`:**
```yaml
server:
  port: 8081
```

### Issue 2: Build Fails

1. Clean Maven: `Maven panel` → `Lifecycle` → `clean`
2. Reload: Right-click `pom.xml` → `Maven` → `Reload Project`
3. Invalidate caches: `File` → `Invalidate Caches` → `Invalidate and Restart`
4. Rebuild: `Build` → `Rebuild Project`

### Issue 3: Lombok Not Working

- No getters/setters available
- "Cannot find symbol" errors on generated methods

**Fix:**
1. Install Lombok plugin (see Prerequisites)
2. Enable annotation processing (see Prerequisites)
3. Restart IntelliJ

### Issue 4: Can't See Generated Sources

**Fix:**
1. Build project first: `Build` → `Build Project`
2. Mark generated sources (see Prerequisites Step 2)
3. Reload project

### Issue 5: Java Version Mismatch

**Error:** "class file has wrong version"

**Fix:**
1. Check all Java settings point to 17:
   - Project SDK
   - Maven Runner JRE
   - Java Compiler target
2. Rebuild project

---

## 🎯 Recommended Workflow

### Daily Development:

1. **Open IntelliJ** → Open Project
2. **Start Application** → Run `ProductOrderServiceApplication`
3. **Open Swagger** → http://localhost:8080/product-order-service/swagger-ui.html
4. **Edit Code** → IntelliJ editor
5. **Rebuild** → ⌘ + F9 (auto-reloads with DevTools)
6. **Test** → Use Swagger or HTTP Client
7. **Debug** → Set breakpoints, use Debug mode
8. **Stop** → ⏹️ Stop button

---

## 💡 Pro Tips

### 1. Multiple Run Configurations

Create separate configurations for:
- Development (H2 database)
- Local MySQL (if needed)
- With AWS credentials
- Debug mode with specific profiles

### 2. Live Templates

Create shortcuts for common code:
- `psvm` → `public static void main`
- `sout` → `System.out.println`

### 3. Database Tool Window

- `View` → `Tool Windows` → `Database`
- Add H2 connection
- Browse tables while app is running

### 4. REST Client

- Better than Swagger for complex flows
- Can save requests
- Can chain requests with variables

### 5. Git Integration

- `Git` → `Commit` (⌘ + K)
- `Git` → `Push` (⌘ + Shift + K)
- All in IDE, no terminal needed!

---

## 📊 Comparison: IntelliJ vs Docker

| Aspect | IntelliJ | Docker |
|--------|----------|--------|
| **Setup Time** | 5-10 min (one-time) | 2 min |
| **Start Time** | 10-15 sec | 30-40 sec |
| **Hot Reload** | ✅ Yes (DevTools) | ❌ No |
| **Debugging** | ✅ Full IDE support | ⚠️ Remote debug |
| **Performance** | ⚡ Fast (native) | 🐌 Slower (container) |
| **Consistency** | ⚠️ Machine-dependent | ✅ Always same |
| **Learning Curve** | Medium | Easy |

**Recommendation:** 
- Use **IntelliJ** for active development (editing, debugging)
- Use **Docker** for testing full stack or sharing with team

---

## ✅ Success Checklist

Before running in IntelliJ:

- [ ] Java 17 installed
- [ ] IntelliJ Project SDK = 17
- [ ] Maven Runner JRE = 17
- [ ] Lombok plugin installed
- [ ] Annotation processing enabled
- [ ] Maven project reloaded
- [ ] Generated sources marked
- [ ] Build successful

Then:

- [ ] Run `ProductOrderServiceApplication`
- [ ] Application starts without errors
- [ ] Can access http://localhost:8080/product-order-service
- [ ] Swagger UI loads
- [ ] Can login and get JWT token
- [ ] APIs work as expected

---

## 🔗 Related Documentation

- `INTELLIJ_JAVA_FIX.md` - Java configuration troubleshooting
- `INTELLIJ_TROUBLESHOOTING.md` - Build issues and solutions
- `DOCKER_LOCAL_SETUP.md` - Alternative: Run in Docker
- `README.md` - Project overview

---

**Need help?** Check the troubleshooting guides or use Docker for guaranteed success! 🚀

