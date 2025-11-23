# NanoDB Compilation Guide

## Prerequisites

Before compiling NanoDB, ensure you have the following installed:

### Required Software

1. **Java Development Kit (JDK) 21 or higher**
   - Download from: [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) or [OpenJDK](https://openjdk.org/)
   - Verify installation:
     ```bash
     java -version
     javac -version
     ```
   - Both commands should show version 21 or higher

2. **Apache Maven 3.6 or higher**
   - Download from: [Maven Official Website](https://maven.apache.org/download.cgi)
   - Verify installation:
     ```bash
     mvn -version
     ```

### Optional Tools

- **Git**: For cloning the repository
- **IDE**: IntelliJ IDEA, Eclipse, or VS Code with Java extensions (for development)

---

## Project Structure

```
nanoDB/
├── pom.xml                    # Maven build configuration
├── src/
│   ├── main/
│   │   └── java/
│   │       └── org/nanodb/    # Main source code
│   └── test/
│       └── java/
│           └── org/nanodb/    # Test source code
├── target/                    # Build output directory (generated)
│   ├── classes/               # Compiled .class files
│   ├── test-classes/          # Compiled test .class files
│   └── nanodb-cli.jar         # Executable JAR file (after build)
├── README.md
├── CLI_USER_GUIDE.md
└── COMPILATION_GUIDE.md       # This file
```

---

## Compilation Methods

### Method 1: Build Executable JAR (Recommended)

This method creates a standalone executable JAR file with all dependencies included.

**Command:**
```bash
mvn clean package
```

**What it does:**
1. Cleans previous build artifacts (`target/` directory)
2. Compiles main source code
3. Compiles test source code
4. Runs all unit tests
5. Creates JAR files:
   - `target/nanodb-1.0-SNAPSHOT.jar` (basic JAR)
   - `target/nanodb-cli.jar` (CLI executable with dependencies)
   - `target/nanodb-orm-lib.jar` (ORM library with dependencies)

**Output:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: ~10 seconds
```

**Running the application:**
```bash
java -jar target/nanodb-cli.jar
```

---

### Method 2: Build Without Running Tests

If you want to skip test execution (faster build):

**Command:**
```bash
mvn clean package -DskipTests
```

**Note:** This still compiles tests but doesn't execute them.

---

### Method 3: Build Without Compiling Tests

If you want to skip test compilation entirely (fastest build):

**Command (PowerShell):**
```powershell
mvn clean package "-Dmaven.test.skip=true"
```

**Command (Linux/macOS/Git Bash):**
```bash
mvn clean package -Dmaven.test.skip=true
```

**Note:** The quotes are required in PowerShell to prevent incorrect argument parsing.

---

### Method 4: Compile Only (No JAR)

To compile source code without creating JAR files:

**Command:**
```bash
mvn clean compile
```

**Output:** Compiled `.class` files in `target/classes/`

---

### Method 5: Run Tests Only

To run tests without building JAR:

**Command:**
```bash
mvn test
```

**Output:** Test results and reports in `target/surefire-reports/`

---

## Build Phases Explained

Maven executes the following phases in order when you run `mvn clean package`:

| Phase | Description |
|-------|-------------|
| **clean** | Deletes the `target/` directory |
| **validate** | Validates project structure |
| **compile** | Compiles main source code (`src/main/java/`) |
| **test-compile** | Compiles test source code (`src/test/java/`) |
| **test** | Runs unit tests using JUnit 5 |
| **package** | Creates JAR files |
| **assembly** | Creates fat JAR with dependencies |

---

## Build Configuration Details

The build is configured in `pom.xml` with the following key settings:

### Compiler Settings
- **Java Version**: 21
- **Source Encoding**: UTF-8
- **Compiler Plugin**: maven-compiler-plugin 3.11.0

### JAR Plugin Configuration
- **Main Class**: `org.nanodb.Main`
- **Plugin**: maven-jar-plugin 3.3.0
- Creates basic JAR with manifest

### Assembly Plugin Configuration
- **Main Class**: `org.nanodb.Main`
- **Plugin**: maven-assembly-plugin 3.6.0
- Creates fat JAR with all dependencies included
- **Output**: `target/nanodb-cli.jar` (CLI executable) and `target/nanodb-orm-lib.jar` (ORM library)

### Dependencies
- **JUnit 5.10.0**: For unit testing (test scope only)

---

## Troubleshooting

### Common Issues

#### 1. Java Version Mismatch

**Error:**
```
[ERROR] invalid target release: 21
```

**Solution:** 
- Install JDK 21 or higher
- Set `JAVA_HOME` environment variable to JDK 21 installation directory
- Verify with `java -version`

---

#### 2. Maven Not Found

**Error:**
```
'mvn' is not recognized as an internal or external command
```

**Solution:**
- Install Apache Maven
- Add Maven's `bin` directory to your system PATH
- Restart your terminal/command prompt
- Verify with `mvn -version`

---

#### 3. Build Fails Due to Test Errors

**Error:**
```
[ERROR] Tests run: X, Failures: Y, Errors: Z
[INFO] BUILD FAILURE
```

**Solution:**
- Review test output in `target/surefire-reports/`
- Fix failing tests or skip tests temporarily:
  ```bash
  mvn clean package -DskipTests
  ```

---

#### 4. Out of Memory During Build

**Error:**
```
java.lang.OutOfMemoryError: Java heap space
```

**Solution:**
Set Maven memory options:
```bash
# Windows PowerShell
$env:MAVEN_OPTS="-Xmx1024m"
mvn clean package

# Linux/macOS
export MAVEN_OPTS="-Xmx1024m"
mvn clean package
```

---

#### 5. PowerShell Parameter Parsing Error

**Error:**
```
[ERROR] Unknown lifecycle phase ".test.skip=true"
```

**Solution:**
Use quotes around parameters in PowerShell:
```powershell
mvn clean package "-Dmaven.test.skip=true"
```

---

## Advanced Build Options

### Clean Build (Remove All Artifacts)

```bash
mvn clean
```

Deletes the entire `target/` directory.

---

### Verbose Output

```bash
mvn clean package -X
```

Shows detailed debug information during build.

---

### Offline Mode

```bash
mvn clean package -o
```

Uses only local repository (no internet connection required).

---

### Update Dependencies

```bash
mvn clean package -U
```

Forces Maven to update all dependencies from remote repositories.

---

### Build Specific Module

If working with a multi-module project:
```bash
mvn clean package -pl <module-name>
```

---

## Continuous Integration

For CI/CD pipelines, use:

```bash
mvn clean verify
```

This runs all tests and verifies the build without installing to local repository.

---

## IDE Integration

### IntelliJ IDEA

1. **Import Project**: File → Open → Select `pom.xml`
2. **Build**: Build → Build Project (Ctrl+F9)
3. **Run**: Right-click `Main.java` → Run 'Main.main()'
4. **Package**: Maven tool window → Lifecycle → package (double-click)

### Eclipse

1. **Import Project**: File → Import → Maven → Existing Maven Projects
2. **Build**: Project → Build Project
3. **Run**: Right-click project → Run As → Java Application → Select Main
4. **Package**: Right-click `pom.xml` → Run As → Maven build → Goals: `clean package`

### VS Code

1. **Open Folder**: Open the `nanoDB` directory
2. **Install Extensions**: Java Extension Pack, Maven for Java
3. **Build**: Terminal → Run Task → `mvn package`
4. **Run**: Press F5 or use Run/Debug panel

---

## Build Artifacts

After a successful build, the `target/` directory contains:

```
target/
├── classes/                           # Compiled main classes
│   └── org/nanodb/                    # Package structure
├── test-classes/                      # Compiled test classes
├── nanodb-1.0-SNAPSHOT.jar            # Basic JAR (without dependencies)
├── nanodb-cli.jar                     # CLI executable JAR (with dependencies) ★
├── nanodb-orm-lib.jar                 # ORM library JAR (with dependencies)
├── maven-archiver/                    # Maven metadata
├── maven-status/                      # Compilation status
└── surefire-reports/                  # Test reports
    ├── TEST-*.xml                     # XML test results
    └── *.txt                          # Text test results
```

**Main Outputs:** 
- `target/nanodb-cli.jar` - CLI application to run interactively
- `target/nanodb-orm-lib.jar` - Library for use in other Java projects

---

## Quick Reference

| Task | Command |
|------|---------|
| Full build with tests | `mvn clean package` |
| Build without running tests | `mvn clean package -DskipTests` |
| Build without compiling tests | `mvn clean package "-Dmaven.test.skip=true"` (PowerShell) |
| Compile only | `mvn clean compile` |
| Run tests only | `mvn test` |
| Clean build artifacts | `mvn clean` |
| Run the application | `java -jar target/nanodb-cli.jar` |

---

## Next Steps

After successful compilation:

1. **Run the CLI**: 
   ```bash
   java -jar target/nanodb-cli.jar
   ```

2. **Read the User Guide**: See `CLI_USER_GUIDE.md` for usage instructions

3. **Run Tests**: 
   ```bash
   mvn test
   ```

4. **Development**: Import the project into your IDE for development

---

## Additional Resources

- [Maven Official Documentation](https://maven.apache.org/guides/index.html)
- [Java 21 Documentation](https://docs.oracle.com/en/java/javase/21/)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- Project README: `README.md`
- CLI User Guide: `CLI_USER_GUIDE.md`

---

*NanoDB - A lightweight database management system for educational purposes*
