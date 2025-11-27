A basic database that supports CRUD operations of Structs in a form of Rows, written in Java.

## Quick Start

NanoDB provides two ways to interact with the database:

### CLI (Command-Line Interface)
Execute SQL-like commands interactively or from scripts:

```bash
# Run the CLI
java -jar nanodb-cli.jar

# Example commands
nanodb> CREATE TABLE users (id INT, name VARCHAR, age INT);
nanodb> INSERT INTO users (id, name, age) VALUES (1, 'Alice', 25);
nanodb> INSERT INTO users (id, name) VALUES (2, 'Bob');  -- age defaults to 0
nanodb> INSERT INTO users (id, name, age) VALUES (3, 'Charlie', null);  -- null becomes default
nanodb> SELECT * FROM users WHERE age > 18;
nanodb> UPDATE users SET age = 26 WHERE id = 1;
nanodb> UPDATE users SET name = null WHERE id = 2;  -- sets to default (empty string)
nanodb> DELETE FROM users WHERE age < 18;
```

**For detailed CLI usage, see [CLI_USER_GUIDE.md](CLI_USER_GUIDE.md)**

### ORM (Object-Relational Mapping)
Use type-safe Java objects for database operations:

```java
import org.nanodb.orm.*;
import org.nanodb.internal.condition.*;

// Define your model
public class User extends BaseModel {
    private int id;
    private String name;
    private int age;
    
    public User() {}
    
    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    
    // Setters
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setAge(int age) { this.age = age; }
}

// Create database
public class MyDatabase extends NanoDb {
    public Table<User> users;
    
    public MyDatabase(String filename) throws IOException, IllegalAccessException {
        super(filename);
    }
}

// Use it
try (MyDatabase db = new MyDatabase("myapp.db")) {
    // Insert
    db.users.insert(new User(1, "Alice", 25));

    // Select
    ComparisonCondition condition = new ComparisonCondition("age", ComparisonOperation.GREATER_THAN, 18);
    List<User> adults = db.users.select(condition);

    // Update
    db.users.update(
        new ComparisonCondition("id", ComparisonOperation.EQUAL, 1),
        user -> { user.setAge(26); return user; }
    );

    // Delete
    db.users.delete(new ComparisonCondition("age", ComparisonOperation.LESS_THAN, 18));
}
```

**For detailed ORM usage, see [ORM_USER_GUIDE.md](ORM_USER_GUIDE.md)**

### Building from Source
See [COMPILATION_GUIDE.md](COMPILATION_GUIDE.md) for instructions on compiling both JAR files.

---

## Architecture
The code is seperated into the backend system and frontend interface. 

The backend system does the heavy lifting of the DB functionality, managing CRUD operations of tables and data into file or memory. 

Meanwhile, the frontend provides a user friendly interface. This project has 2 interfaces, an executable with an SQL-like syntax, and a Java ORM library which Java developers can use directly on their code

## Backend
The main backbone of the system consists of the interfaces: DB, Table, and Chunker. 
Each of this classes handle the CRUD operations of increasingly abstract data. 

DB: Mainly concerns on the storage of Table definitions and pointers to the tables. 

Tables: Manages the CRUD of rows, serializing it into bytes and feeding it into the next layer down

Chunker: Interfaces with the underlying storage (memory/file), providing methods to read and write chunks of bytes from it.

We could also make an analogy of this system into code. 
Chunker acts as the malloc

Table acts as a Vector/Collection object

DB acts as the Lexical Environment and holds the mappings of Table name, type, and object, contained inside the environment (whatever environment the chunker is interacting with) 

## Additional important components
To achieve its functionality, the Table class uses 2 crucial components:

1. DataType interface: Represents the data types that are supported by the DB. Implementations should contain functions to serialize and deserialize between bytes (`byte[]`) and the Java Objects they represent.
    - They also support visitors, allowing extensions of functionality.
    - Interestingly, one of the visitors is used to serialize and deserialize the DataType object themselves, which is useful for the Database class implementation.
    
2. Condition interface: Represents a boolean expression as a Tree. Visitor pattern is used to evaluate the overall boolean expression.
   - This enables flexibility to interpret the expression differently when comparing values vs comparing indexes. Note that indexes have not been implemented, but using visitor pattern here greatly eases the process.
   - Originally, the design was planned to accept functions of `Function<Row, boolean>` as the `WHERE` clause, similar to that of .NET's Entity Framework. However, it would be difficult to evaluate indexes this way. It seems EF manages this by accessing the AST generated from the code itself, which seems out of scope for this project. If, by any chance, this feature would like to be added, it most likely would be implemented on the frontend interface, which will translate the Java AST into the backend Condition object boolean expression tree.
    

### Acknowledgements
- Structure inspired by <https://cstack.github.io/db_tutorial/>
