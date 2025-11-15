A basic database that supports CRUD operations of Structs as Rows, written in Java. 

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
