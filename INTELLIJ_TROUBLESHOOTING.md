# IntelliJ Compilation Error - Troubleshooting

## Current Error
```
Failed to execute goal maven-compiler-plugin:3.11.0:compile
Compilation failure: An unknown compilation problem occurred
```

## ✅ Terminal Build: WORKING
The terminal build succeeds, so this is IntelliJ-specific.

---

## 🔧 SOLUTION: Step-by-Step Fix

### Step 1: Verify Java in IntelliJ

1. **Go to**: `File → Project Structure` (⌘ + ;)
2. **Check Project Settings → Project**:
   - SDK: Should be **17** (OpenJDK 17.0.16)
   - Language Level: Should be **17**
   
3. **Check Platform Settings → SDKs**:
   - If Java 17 is missing, add it:
     - Click **+** → **JDK**
     - Navigate to: `/opt/homebrew/Cellar/openjdk@17/17.0.16/libexec/openjdk.jdk/Contents/Home`
     - Click **Open**

### Step 2: Configure Maven in IntelliJ

1. **Go to**: `IntelliJ IDEA → Preferences` (⌘ + ,)
2. **Navigate to**: `Build, Execution, Deployment → Build Tools → Maven`
3. **General Settings**:
   - Maven home path: Should be auto-detected or `/opt/homebrew/Cellar/maven/3.9.11/libexec`
   
4. **Click on**: `Runner` (sub-menu under Maven)
5. **Set JRE**: 
   - Select **Use Project JDK** (should show Java 17)
   - OR manually select **17**

6. **VM Options** (add this):
   ```
   -Djdk.lang.Process.launchMechanism=FORK
   ```

### Step 3: Configure Compiler

1. **Go to**: `Preferences → Build, Execution, Deployment → Compiler → Java Compiler`
2. **Set**:
   - Project bytecode version: **17**
   - Per-module bytecode version: All should be **17**
3. **Additional command line parameters**:
   ```
   --release 17
   ```

### Step 4: Reload Maven Project (CRITICAL!)

1. **Open Maven Panel**: View → Tool Windows → Maven
2. **Click** the reload icon (🔄) at top left
3. **Or Right-click** `pom.xml` → Maven → Reload Project
4. **Wait** for dependencies to download (check progress bar at bottom)

### Step 5: Clean IntelliJ Caches

1. **Go to**: `File → Invalidate Caches...`
2. **Check ALL boxes**:
   - ✅ Invalidate and Restart
   - ✅ Clear file system cache and Local History
   - ✅ Clear downloaded shared indexes
   - ✅ Clear VCS Log caches and indexes
3. **Click**: `Invalidate and Restart`

### Step 6: Clean Build Directories

After IntelliJ restarts:

1. **In Terminal** (within IntelliJ or external):
   ```bash
   cd /Users/sushantpandey/product-order-service
   rm -rf target out
   mvn clean
   ```

2. **In IntelliJ Maven Panel**:
   - Click `product-order-service → Lifecycle → clean`
   - Then click `compile`

### Step 7: Rebuild Project

1. **Go to**: `Build → Rebuild Project`
2. **Wait** for build to complete
3. **Check** the Build tab at bottom for errors

---

## 🔍 If Still Failing - Get Detailed Error

### Option A: IntelliJ Build Output

1. **Go to**: `Build → Build Project` (⌘ + F9)
2. **In Build tab** (bottom), look for:
   - Red error messages
   - File paths with compilation errors
   - Specific line numbers
   
3. **Copy the full error** and share it

### Option B: Maven Console Output

1. **Open Maven panel**: View → Tool Windows → Maven
2. **Click toggle** "Always show output" (📋 icon)
3. **Right-click** `product-order-service → Lifecycle → compile`
4. **Run**
5. **Copy full console output**

### Option C: Enable Verbose Logging

1. **Go to**: `Preferences → Maven → Runner`
2. **Add to VM Options**:
   ```
   -X
   ```
3. **Rebuild** and check console for detailed error

---

## 🐛 Common Issues & Solutions

### Issue 1: "Cannot find symbol" errors
**Cause**: Generated sources not in classpath

**Solution**:
1. Right-click `target/generated-sources/protobuf/java`
2. Select **Mark Directory as → Generated Sources Root**
3. Right-click `target/generated-sources/protobuf/grpc-java`
4. Select **Mark Directory as → Generated Sources Root**

### Issue 2: "package does not exist"
**Cause**: Dependencies not downloaded

**Solution**:
1. Maven panel → Right-click project → Maven → Reimport
2. Or delete `~/.m2/repository` and reload

### Issue 3: Multiple Java versions conflict
**Cause**: IntelliJ using different Java than terminal

**Solution**:
1. Check which Java IntelliJ is using:
   - Help → About → Copy to clipboard
   - Look for "Runtime version"
2. Change IntelliJ runtime if needed:
   - Help → Find Action → "Choose Boot Java Runtime for the IDE"
   - Select Java 17

### Issue 4: Lombok not working
**Cause**: Lombok plugin not installed

**Solution**:
1. Preferences → Plugins
2. Search "Lombok"
3. Install and restart

### Issue 5: MapStruct errors
**Cause**: Annotation processing not enabled

**Solution**:
1. Preferences → Build → Compiler → Annotation Processors
2. Check ✅ "Enable annotation processing"
3. Processor path: "Obtain from project classpath"

---

## 🎯 Quick Fix Checklist

Try these in order:

- [ ] Maven panel → Reload Project (🔄)
- [ ] File → Invalidate Caches → Invalidate and Restart
- [ ] Build → Rebuild Project
- [ ] Preferences → Maven → Runner → JRE = Use Project JDK
- [ ] Delete target/ and out/ directories
- [ ] Mark generated-sources as Generated Sources Root
- [ ] Enable annotation processing

---

## 📞 Still Not Working?

If none of the above works, please provide:

1. **Full error message** from Build tab
2. **IntelliJ version**: Help → About
3. **Java version in IntelliJ**: Check in About dialog
4. **Maven output**: From Maven panel console
5. **Screenshot** of error if possible

---

## ✅ Expected Success

After successful build, you should see:

```
[INFO] BUILD SUCCESS
[INFO] Total time: ~6s
```

And in IntelliJ Build tab:
```
Build completed successfully in X s Y ms
```

---

## 🔗 Additional Resources

- Full Java fix guide: `INTELLIJ_JAVA_FIX.md`
- Terminal build works: `mvn clean package -DskipTests`
- If all else fails: Use terminal Maven and IntelliJ just as editor

