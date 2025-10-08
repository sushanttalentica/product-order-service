# IntelliJ Java Version Fix

## Problem
```
Fatal error compiling: java.lang.ExceptionInInitializerError: com.sun.tools.javac.code.TypeTag
```

This error occurs when IntelliJ is using a different Java version than what the project requires.

## Solution: Configure IntelliJ to Use Java 17

### Step 1: Set Project SDK
1. Open IntelliJ IDEA
2. Go to **File → Project Structure** (or press `⌘ + ;` on Mac)
3. Under **Project Settings → Project**:
   - **SDK**: Select **17** (or add it if not present)
   - **Language Level**: Select **17 - Sealed types, always-strict floating-point semantics**
   - Click **Apply**

### Step 2: Set Java SDK Path (If Java 17 Not Listed)
If Java 17 is not in the list:
1. In Project Structure, click **Platform Settings → SDKs**
2. Click **+** (Add New SDK) → **JDK**
3. Navigate to:
   ```
   /opt/homebrew/Cellar/openjdk@17/17.0.16/libexec/openjdk.jdk/Contents/Home
   ```
4. Click **Open**
5. Name it: **openjdk-17**
6. Click **Apply**

### Step 3: Set Module SDK
1. In Project Structure, go to **Project Settings → Modules**
2. Select `product-order-service`
3. Under **Sources** tab:
   - **Language level**: Set to **17**
4. Under **Dependencies** tab:
   - **Module SDK**: Select **17** (or use Project SDK)
5. Click **Apply** → **OK**

### Step 4: Set Maven Runner JDK
1. Go to **IntelliJ IDEA → Preferences** (or `⌘ + ,`)
2. Navigate to **Build, Execution, Deployment → Build Tools → Maven → Runner**
3. **JRE**: Select **Use Project JDK (17)**
4. Click **Apply** → **OK**

### Step 5: Invalidate Caches (Important!)
1. Go to **File → Invalidate Caches...**
2. Check:
   - ✅ **Invalidate and Restart**
   - ✅ **Clear file system cache and Local History**
   - ✅ **Clear downloaded shared indexes**
3. Click **Invalidate and Restart**

### Step 6: Reimport Maven Project
After restart:
1. Right-click on `pom.xml`
2. Select **Maven → Reload Project**
3. Wait for dependencies to download

### Step 7: Verify Build
1. Go to **Build → Build Project** (or `⌘ + F9`)
2. Check **Build** tab at bottom for success

---

## Alternative: Command Line Maven Wrapper

If IntelliJ still has issues, use terminal Maven:

```bash
# Set Java 17
export JAVA_HOME=$(/usr/libexec/java_home -v 17)

# Clean build
cd /Users/sushantpandey/product-order-service
mvn clean package -DskipTests

# Verify
java -jar target/product-order-service-1.0.0.jar
```

---

## Verify Java Configuration

```bash
# Check Java version
java -version
# Should show: openjdk version "17.0.16"

# Check Maven's Java
mvn -version
# Should show: Java version: 17

# Check JAVA_HOME
echo $JAVA_HOME
# Should show: /opt/homebrew/Cellar/openjdk@17/17.0.16/libexec/openjdk.jdk/Contents/Home
```

---

## Common Issues

### Issue 1: Maven using different Java
**Solution**: Set in `~/.mavenrc`:
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
```

### Issue 2: IntelliJ still using wrong JDK
**Solution**: 
1. Quit IntelliJ completely
2. Delete IntelliJ caches:
   ```bash
   rm -rf ~/Library/Caches/JetBrains/IntelliJIdea*
   ```
3. Restart IntelliJ
4. Reimport project

### Issue 3: Multiple Java versions conflict
**Solution**: Explicitly set in terminal:
```bash
# Add to ~/.zshrc
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH="$JAVA_HOME/bin:$PATH"
```

---

## Quick Fix (Most Common)

**Try this first:**

1. **IntelliJ**: `File → Project Structure → Project → SDK = 17`
2. **IntelliJ**: `Preferences → Maven → Runner → JRE = Use Project JDK`
3. **IntelliJ**: `File → Invalidate Caches → Invalidate and Restart`
4. **Right-click `pom.xml`** → `Maven → Reload Project`

**That should fix it! ✅**

---

## Current System Info

Your Java setup (verified):
- **Java Version**: OpenJDK 17.0.16 (Homebrew)
- **Java Home**: `/opt/homebrew/Cellar/openjdk@17/17.0.16/libexec/openjdk.jdk/Contents/Home`
- **Terminal Build**: ✅ Working (122MB JAR created)
- **Issue**: IntelliJ configuration only

---

## Protoc (Protocol Buffers) Fix

If you see errors like:
```
Cannot run program "/path/to/protoc-3.24.4-osx-aarch_64.exe"
Failed to exec spawn helper: pid: XXX, exit code: 1
```

**Solution**: The `pom.xml` has been updated to include os-maven-plugin as a build extension:

```xml
<build>
    <extensions>
        <extension>
            <groupId>kr.motd.maven</groupId>
            <artifactId>os-maven-plugin</artifactId>
            <version>1.7.1</version>
        </extension>
    </extensions>
    ...
</build>
```

This ensures the `${os.detected.classifier}` property is resolved before protobuf plugin execution.

**Additional Fix**: Made all protoc executables in Maven repository executable:
```bash
find ~/.m2/repository -name "protoc-*.exe" -exec chmod +x {} \;
```

---

## After Fix

You should see:
```
[INFO] BUILD SUCCESS
[INFO] Total time: ~7 seconds
```

And be able to run the application directly from IntelliJ.

