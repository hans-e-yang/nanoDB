# NanoDB ORM User Guide

## Overview

NanoDB ORM (Object-Relational Mapping) is a Java library that provides a type-safe, object-oriented interface for database operations. It allows you to work with Java objects instead of writing SQL statements, while automatically handling database schema management and migrations.

## Key Features

- **Type-Safe Database Operations**: Work with strongly-typed Java objects
- **Automatic Schema Management**: Tables are created and updated automatically based on your model classes
- **Auto-Migrations**: Schema changes are detected and applied automatically
- **Reflection-Based Mapping**: Minimal configuration required
- **Field-Level Control**: Annotations to customize behavior

---

## Getting Started

### Prerequisites

- Java 21 or higher
- NanoDB ORM library JAR (`nanodb-orm-lib.jar`)

### Adding to Your Project

Include the NanoDB ORM library in your project's classpath or add it as a Maven dependency:

```xml
<dependency>
    <groupId>org.nanodb</groupId>
    <artifactId>nanodb</artifactId>
    <version>1.0-SNAPSHOT</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/path/to/nanodb-orm-lib.jar</systemPath>
</dependency>
```

---

## Core Concepts

### 1. BaseModel

All model classes must extend `BaseModel`. This abstract class serves as a marker to distinguish database models from regular Java classes.

```java
import org.nanodb.orm.BaseModel;

public class User extends BaseModel {
    // Your fields here
}
```

### 2. NanoDb

The `NanoDb` class is the main entry point for your database. It implements `AutoCloseable`, so you should use try-with-resources or call `close()` when done. You create a subclass that defines your database schema by declaring `Table<T>` fields.

```java
import org.nanodb.orm.NanoDb;
import org.nanodb.orm.Table;

public class MyDatabase extends NanoDb {
    public Table<User> users;
    public Table<Product> products;

    public MyDatabase(String filename) throws IOException, IllegalAccessException {
        super(filename);
    }
}
```

### 3. Table

The `Table<T>` class provides methods for CRUD operations on your models.

---

## Creating Models

### Basic Model

```java
import org.nanodb.orm.BaseModel;

public class Student extends BaseModel {
    private int id;
    private String name;
    private String email;
    private int age;

    // Empty constructor is REQUIRED
    public Student() {
    }

    public Student(int id, String name, String email, int age) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.age = age;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public int getAge() { return age; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setAge(int age) { this.age = age; }
}
```

**Important Requirements:**
- Must extend `BaseModel`
- Must have a public empty (no-argument) constructor
- Fields should be private with public getters and setters for automatic mapping

### Supported Field Types

NanoDB ORM supports the following Java types:

| Java Type | Database Type | Notes |
|-----------|---------------|-------|
| `int` | Int32Type | 32-bit integer |
| `boolean` | BooleanType | true/false |
| `String` | VarCharType | Variable-length string (max 255 chars by default) |
| `byte[]` | BytesType | Binary data |
| `Date` | DateType | Java Date object |
| `List<T>` | ArrayType | Lists of supported types |
| Custom BaseModel | StructType | Nested models |

### Annotations

#### @Ignore

Exclude a field from database mapping:

```java
import org.nanodb.orm.annotations.Ignore;

public class User extends BaseModel {
    private int id;
    private String username;
    
    @Ignore
    private transient String tempPassword; // Won't be stored in database
    
    public User() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
```

#### @MaximumLength

Set maximum length for lists and strings:

```java
import org.nanodb.orm.annotations.MaximumLength;
import java.util.List;

public class Task extends BaseModel {
    private int id;
    
    @MaximumLength(10)
    private List<String> tags; // Maximum 10 tags
    
    public Task() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}
```

Default maximum length is 64 if not specified.

### Nested Models

You can embed one model inside another:

```java
public class Address extends BaseModel {
    private String street;
    private String city;
    private String zipCode;
    
    public Address() {}

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }
}

public class Person extends BaseModel {
    private int id;
    private String name;
    private Address address; // Nested model
    
    public Person() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }
}
```

---

## Database Operations

### Creating a Database Instance

```java
import org.nanodb.orm.NanoDb;
import org.nanodb.orm.Table;
import java.io.IOException;

public class AppDatabase extends NanoDb {
    public Table<Student> students;
    public Table<Course> courses;

    public AppDatabase(String filename) throws IOException, IllegalAccessException {
        super(filename);
    }
}

// Usage
public class Main {
    public static void main(String[] args) throws Exception {
        try (AppDatabase db = new AppDatabase("myapp.db")) {
            // Database is ready to use!
            // ... perform operations
        } // Database is automatically closed here
    }
}
```

**What happens automatically:**
1. Database file is created (if it doesn't exist)
2. Tables are created for each `Table<T>` field
3. Schema is validated against existing tables
4. Outdated tables are dropped and recreated
5. Tables not defined in the class are removed

### Insert Operations

#### Insert Single Record

```java
Student alice = new Student(1, "Alice", "alice@example.com", 20);
db.students.insert(alice);
```

#### Insert Multiple Records

```java
List<Student> students = Arrays.asList(
    new Student(1, "Alice", "alice@example.com", 20),
    new Student(2, "Bob", "bob@example.com", 22),
    new Student(3, "Charlie", "charlie@example.com", 19)
);
db.students.insert(students);
```

#### Handling Null Fields

When inserting records with null fields, NanoDB automatically converts them to default values:

```java
Student student = new Student();
student.id = 1;
student.name = "John";
// email is null, age is 0 (default for int)
db.students.insert(student);
// email will be stored as empty string "", age as 0
```

**Default values for null fields:**
- `String`: empty string (`""`)
- `int`: `0`
- `boolean`: `false`
- `Date`: Unix epoch (`1970-01-01`)
- `byte[]`: empty array
- `List<T>`: empty list

### Select Operations

#### Select All Records

```java
List<Student> allStudents = db.students.selectAll();
for (Student s : allStudents) {
    System.out.println(s.getName());
}
```

#### Select with Condition

```java
import org.nanodb.internal.condition.ComparisonCondition;
import org.nanodb.internal.condition.ComparisonOperation;

// Select students where age >= 20
ComparisonCondition condition = new ComparisonCondition("age", ComparisonOperation.GREATER_EQUALS, 20);
List<Student> adults = db.students.select(condition);
```

#### Select with Limit

```java
// Get first 5 students where age >= 18
ComparisonCondition condition = new ComparisonCondition("age", ComparisonOperation.GREATER_EQUALS, 18);
List<Student> limitedResults = db.students.select(condition, 5);
```

### Update Operations

#### Update with Condition

```java
import org.nanodb.internal.condition.ComparisonCondition;
import org.nanodb.internal.condition.ComparisonOperation;

// Update email for student with id = 1
ComparisonCondition condition = new ComparisonCondition("id", ComparisonOperation.EQUAL, 1);
int updated = db.students.update(
    condition,
    student -> {
        student.setEmail("newemail@example.com");
        return student;
    }
);
System.out.println("Updated " + updated + " record(s)");
```

#### Update with Limit

```java
// Update first 10 students with age < 18
ComparisonCondition condition = new ComparisonCondition("age", ComparisonOperation.LESS_THAN, 18);
int updated = db.students.update(
    condition,
    student -> {
        student.setAge(18);
        return student;
    },
    10  // Limit to 10 records
);
```

#### Update All Records

```java
import org.nanodb.internal.condition.BooleanCondition;

// Increment everyone's age
int updated = db.students.update(
    BooleanCondition.TRUE,
    student -> {
        student.setAge(student.getAge() + 1);
        return student;
    }
);
```

**Note on null values in updates:** When updating a record, if you set a field to `null`, it will be automatically converted to its default value (see Insert Operations for the list of defaults).

### Delete Operations

#### Delete with Condition

```java
// Delete students with age < 18
ComparisonCondition condition = new ComparisonCondition("age", ComparisonOperation.LESS_THAN, 18);
int deleted = db.students.delete(condition);
System.out.println("Deleted " + deleted + " record(s)");
```

#### Delete with Limit

```java
// Delete first 5 inactive students
ComparisonCondition condition = new ComparisonCondition("active", ComparisonOperation.EQUAL, false);
int deleted = db.students.delete(condition, 5);
```

#### Delete All Records

```java
// Clear the entire table
db.students.clear();
```

---

## Working with Conditions

### Comparison Operations

```java
import org.nanodb.internal.condition.ComparisonCondition;
import org.nanodb.internal.condition.ComparisonOperation;

// Equal
new ComparisonCondition("name", ComparisonOperation.EQUAL, "Alice");

// Not equal
new ComparisonCondition("status", ComparisonOperation.NOT_EQUALS, "inactive");

// Greater than
new ComparisonCondition("age", ComparisonOperation.GREATER_THAN, 18);

// Less than
new ComparisonCondition("score", ComparisonOperation.LESS_THAN, 100);

// Greater than or equal
new ComparisonCondition("age", ComparisonOperation.GREATER_EQUALS, 21);

// Less than or equal
new ComparisonCondition("price", ComparisonOperation.LESS_EQUALS, 50);

// Pattern matching (LIKE)
new ComparisonCondition("email", ComparisonOperation.LIKE, "%@example.com");
```

### Logical Operators

#### AND Condition

```java
import org.nanodb.internal.condition.AndCondition;

ComparisonCondition ageCondition = new ComparisonCondition("age", ComparisonOperation.GREATER_EQUALS, 18);
ComparisonCondition statusCondition = new ComparisonCondition("status", ComparisonOperation.EQUAL, "active");

AndCondition combined = new AndCondition(ageCondition, statusCondition);
List<Student> results = db.students.select(combined);
```

#### OR Condition

```java
import org.nanodb.internal.condition.OrCondition;

ComparisonCondition freshmen = new ComparisonCondition("year", ComparisonOperation.EQUAL, 1);
ComparisonCondition sophomores = new ComparisonCondition("year", ComparisonOperation.EQUAL, 2);

OrCondition either = new OrCondition(freshmen, sophomores);
List<Student> results = db.students.select(either);
```

#### Complex Conditions

```java
// (age >= 18 AND status = 'active') OR (premium = true)
ComparisonCondition ageCheck = new ComparisonCondition("age", ComparisonOperation.GREATER_EQUALS, 18);
ComparisonCondition statusCheck = new ComparisonCondition("status", ComparisonOperation.EQUAL, "active");
ComparisonCondition premiumCheck = new ComparisonCondition("premium", ComparisonOperation.EQUAL, true);

AndCondition baseUsers = new AndCondition(ageCheck, statusCheck);
OrCondition allEligible = new OrCondition(baseUsers, premiumCheck);

List<Student> results = db.students.select(allEligible);
```

### Always True Condition

```java
import org.nanodb.internal.condition.BooleanCondition;

// Select all records
List<Student> all = db.students.select(BooleanCondition.TRUE);

// Update all records
db.students.update(BooleanCondition.TRUE, student -> {
    student.setVerified(true);
    return student;
});
```

---

## Complete Example

```java
import org.nanodb.orm.BaseModel;
import org.nanodb.orm.NanoDb;
import org.nanodb.orm.Table;
import org.nanodb.orm.annotations.Ignore;
import org.nanodb.orm.annotations.MaximumLength;
import org.nanodb.internal.condition.*;

import java.io.IOException;
import java.util.List;

// Define Models
class User extends BaseModel {
    private int id;
    private String username;
    private String email;
    private int age;
    private boolean active;
    
    @MaximumLength(5)
    private List<String> roles;
    
    @Ignore
    private String sessionToken;
    
    public User() {}
    
    public User(int id, String username, String email, int age) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.age = age;
        this.active = true;
    }

    // Getters
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public int getAge() { return age; }
    public boolean isActive() { return active; }
    public List<String> getRoles() { return roles; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setAge(int age) { this.age = age; }
    public void setActive(boolean active) { this.active = active; }
    public void setRoles(List<String> roles) { this.roles = roles; }
}

class Post extends BaseModel {
    private int id;
    private int userId;
    private String title;
    private String content;
    
    public Post() {}
    
    public Post(int id, int userId, String title, String content) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.content = content;
    }

    // Getters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
}

// Define Database
class BlogDatabase extends NanoDb {
    public Table<User> users;
    public Table<Post> posts;
    
    public BlogDatabase(String filename) throws IOException, IllegalAccessException {
        super(filename);
    }
}

// Main Application
public class BlogApp {
    public static void main(String[] args) throws Exception {
        // Initialize database with try-with-resources
        try (BlogDatabase db = new BlogDatabase("blog.db")) {
            // Create users
            User alice = new User(1, "alice", "alice@blog.com", 25);
            User bob = new User(2, "bob", "bob@blog.com", 30);
            db.users.insert(alice);
            db.users.insert(bob);
            
            // Create posts
            Post post1 = new Post(1, 1, "Hello World", "My first post!");
            Post post2 = new Post(2, 1, "Java Tips", "Learn Java ORM...");
            db.posts.insert(List.of(post1, post2));
            
            // Query active users
            ComparisonCondition activeCondition = new ComparisonCondition("active", ComparisonOperation.EQUAL, true);
            List<User> activeUsers = db.users.select(activeCondition);
            System.out.println("Active users: " + activeUsers.size());
            
            // Update user age
            ComparisonCondition userCondition = new ComparisonCondition("id", ComparisonOperation.EQUAL, 1);
            db.users.update(userCondition, user -> {
                user.setAge(26);
                return user;
            });
            
            // Find posts by user
            ComparisonCondition postCondition = new ComparisonCondition("userId", ComparisonOperation.EQUAL, 1);
            List<Post> alicePosts = db.posts.select(postCondition);
            System.out.println("Alice's posts: " + alicePosts.size());
            
            // Delete inactive users
            ComparisonCondition inactiveCondition = new ComparisonCondition("active", ComparisonOperation.EQUAL, false);
            int deleted = db.users.delete(inactiveCondition);
            System.out.println("Deleted users: " + deleted);
            
            System.out.println("Blog application completed!");
        } // Database automatically closed here
    }
}
```

---

## Advanced Features

### Automatic Schema Migrations

When you modify your model classes, NanoDB ORM automatically detects changes:

```java
// Initial model
public class Product extends BaseModel {
    public int id;
    public String name;
    public Product() {}
}

// Modified model (added price field)
public class Product extends BaseModel {
    public int id;
    public String name;
    public double price;  // New field
    public Product() {}
}
```

**What happens:**
1. NanoDB detects schema mismatch on next database initialization
2. Old table is dropped
3. New table is created with updated schema
4. ⚠️ **Warning: Existing data is lost during migration**

### Database File Management

The database is stored in a single binary file:

```java
// Specify custom location
AppDatabase db = new AppDatabase("/path/to/data/app.db");

// Relative path
AppDatabase db = new AppDatabase("data/app.db");

// Current directory
AppDatabase db = new AppDatabase("app.db");
```

**File Characteristics:**
- Binary format (not human-readable)
- Single file per database
- Maximum size: ~1GB (configurable)
- Persistent across application restarts

---

## Best Practices

### 1. Always Provide Empty Constructors

```java
// ✅ Good
public class User extends BaseModel {
    private int id;
    private String name;
    
    public User() {}
    public User(int id, String name) { this.id = id; this.name = name; }
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

// ❌ Bad - will cause runtime errors
public class User extends BaseModel {
    public User(int id, String name) { /* ... */ }
    // Missing empty constructor!
}
```

### 2. Use Private Fields with Public Getters/Setters

```java
// ✅ Good - private fields with public getters/setters are persisted
public class User extends BaseModel {
    private int id;
    private String name;
    
    public User() {}
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

// ⚠️ Private fields without getters/setters won't be persisted
public class User extends BaseModel {
    private int id;  // Won't be saved - no getter/setter
    private String name;  // Won't be saved - no getter/setter
}
```

### 3. Mark Transient Fields with @Ignore

```java
public class User extends BaseModel {
    private int id;
    private String username;
    
    @Ignore
    private String currentSession;  // Temporary data
    
    @Ignore
    private transient boolean isLoading;  // Runtime state
    
    public User() {}
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
```

### 4. Handle Exceptions Appropriately

```java
try {
    List<User> users = db.users.selectAll();
} catch (MissingWhereConditionException e) {
    // This exception is thrown for certain operations
    // that require WHERE clauses in strict mode
    System.err.println("WHERE clause required");
}
```

### 5. Close Resources Properly

NanoDb implements `AutoCloseable`, so always close the database when done to release file handles:

```java
// ✅ Recommended - use try-with-resources
public static void main(String[] args) {
    try (AppDatabase db = new AppDatabase("app.db")) {
        // ... use database
    } catch (Exception e) {
        e.printStackTrace();
    }
    // Database is automatically closed
}

// ✅ Alternative - manual close
public static void main(String[] args) throws Exception {
    AppDatabase db = new AppDatabase("app.db");
    try {
        // ... use database
    } finally {
        db.close();
    }
}
```

---

## Limitations

1. **No Join Operations**: Each table is queried independently
2. **No Aggregate Functions**: COUNT, SUM, AVG not supported
3. **No Transactions**: Operations are not atomic
4. **No Indexes**: Linear search for all queries
5. **No Foreign Key Constraints**: Referential integrity must be managed manually
6. **Schema Migration Loses Data**: Changing models drops and recreates tables
7. **Limited Query Capabilities**: Only basic WHERE conditions supported

---

## Troubleshooting

### ClassCastException: String cannot be cast to Integer

**Problem:** Condition values should match field types for type safety

```java
// ⚠️ String values work but require parsing
new ComparisonCondition("age", ComparisonOperation.EQUAL, "20");

// ✅ Recommended - use the actual type for type safety
new ComparisonCondition("age", ComparisonOperation.EQUAL, 20);
```

### RuntimeException: BaseModel classes must have an empty constructor

**Problem:** Missing no-argument constructor

**Solution:** Add an empty public constructor:
```java
public class MyModel extends BaseModel {
    public MyModel() {}  // Add this
}
```

### UnsupportedOperationException: Table type must inherit from BaseModel

**Problem:** Table generic type doesn't extend BaseModel

```java
// ❌ Wrong
public class MyDb extends NanoDb {
    public Table<String> data;  // String doesn't extend BaseModel
}

// ✅ Correct
public class MyDb extends NanoDb {
    public Table<MyModel> data;  // MyModel extends BaseModel
}
```

---

## API Reference Summary

### Table<T> Methods

| Method | Description |
|--------|-------------|
| `insert(T value)` | Insert single record |
| `insert(List<T> values)` | Insert multiple records |
| `selectAll()` | Select all records |
| `select(Condition condition)` | Select with filtering |
| `select(Condition condition, int limit)` | Select with limit |
| `update(Condition condition, Function<T, T> fn)` | Update records |
| `update(Condition condition, Function<T, T> fn, int limit)` | Update with limit |
| `delete(Condition condition)` | Delete records |
| `delete(Condition condition, int limit)` | Delete with limit |
| `clear()` | Delete all records |

### Comparison Operations

- `ComparisonOperation.EQUAL`
- `ComparisonOperation.NOT_EQUALS`
- `ComparisonOperation.GREATER_THAN`
- `ComparisonOperation.LESS_THAN`
- `ComparisonOperation.GREATER_EQUALS`
- `ComparisonOperation.LESS_EQUALS`
- `ComparisonOperation.LIKE`

### Annotations

- `@Ignore` - Exclude field from database
- `@MaximumLength(n)` - Set max length for lists/arrays

---

## Additional Resources

- [NanoDB README](README.md) - Project overview
- [CLI User Guide](CLI_USER_GUIDE.md) - Command-line interface documentation
- [Compilation Guide](COMPILATION_GUIDE.md) - Building from source

---

*NanoDB ORM - Type-safe database operations for Java*
