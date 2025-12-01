# NanoDB CLI User Guide

## Overview

NanoDB CLI is an interactive command-line interface for managing a lightweight database system. It provides a SQL-like syntax for creating tables, inserting data, querying records, updating values, and deleting entries.

## Getting Started

### Running the CLI

To start the NanoDB CLI, run the following command from the project root directory:

```bash
java -jar target/nanodb-cli.jar
```

You will see the welcome message and prompt:

```
Welcome to NanoDB CLI!
Type 'help' for available commands, 'exit' to quit.
nanodb>
```

### Exiting the CLI

To exit the CLI, type one of the following commands:
- `exit`
- `quit`
- Press `Ctrl+C`

## Available Commands

### 1. CREATE TABLE

Creates a new table with the specified schema.

**Syntax:**
```sql
CREATE TABLE <table_name> (<column_name> <data_type>, ...)
```

**Supported Data Types:**
- `int` - Integer values
- `varchar(n)` - Variable-length string with maximum length n (n must be positive)
- `date` - Date values

**Examples:**
```sql
CREATE TABLE students (id int, name varchar(50), email varchar(100))
CREATE TABLE courses (courseId int, courseName varchar(100), startDate date)
CREATE TABLE users (userId int, username varchar(30), age int)
```

**Success Output:**
```
Table 'students' created
```

**Notes:**
- Table names and column names are case-sensitive
- Column definitions are required; creating a table without columns will result in a syntax error
- Varchar length must be a positive integer

---

### 2. DROP TABLE

Deletes an existing table and all its data.

**Syntax:**
```sql
DROP TABLE <table_name>
```

**Examples:**
```sql
DROP TABLE students
DROP TABLE old_records
```

**Success Output:**
```
Table 'students' dropped
```

**Notes:**
- The table must exist, or an error will be returned
- All data in the table will be permanently deleted

---

### 3. INSERT

Inserts a new row into a table.

**Syntax:**
```sql
INSERT INTO <table_name> (<column1>, <column2>, ...) VALUES (<value1>, <value2>, ...)
```

**Examples:**
```sql
INSERT INTO students (id, name, email) VALUES (1, 'John Doe', 'john@example.com')
INSERT INTO students (id, name, email) VALUES (2, John, john@test.com)
INSERT INTO courses (courseId, courseName, startDate) VALUES (101, 'Database Systems', '2025-01-15')

-- Insert with NULL values
INSERT INTO students (id, name, email) VALUES (3, 'Jane', null)
INSERT INTO students (id, name) VALUES (4, 'Bob')  -- email will be empty
```

**Notes:**
- Values can be quoted with single quotes (`'value'`) or unquoted
- Use quotes for values containing spaces or special characters
- The number of columns must match the number of values
- Values are type-checked against the table schema
- Date format: `YYYY-MM-DD`
- **NULL values**: Use the keyword `null` (case-insensitive) to insert a null value
- **Partial inserts**: You can omit columns; missing columns are stored with default values:
  - `varchar`: empty string (`""`)
  - `int`: `0`
  - `date`: Unix epoch (`1970-01-01`)

---

### 4. SELECT

Retrieves rows from a table.

**Syntax:**
```sql
SELECT <columns> FROM <table_name> [WHERE <condition>] [LIMIT <n>]
```

**Examples:**
```sql
-- Select all columns from all rows
SELECT * FROM students

-- Select specific columns
SELECT id, name FROM students

-- Select with WHERE clause
SELECT * FROM students WHERE id = 1
SELECT * FROM students WHERE name = 'John Doe'
SELECT name, email FROM students WHERE id > 5

-- Select with LIMIT
SELECT * FROM students LIMIT 10
SELECT * FROM students WHERE age > 18 LIMIT 5

-- Complex WHERE conditions
SELECT * FROM students WHERE id > 1 AND age < 25
SELECT * FROM students WHERE name = 'Alice' OR name = 'Bob'
SELECT * FROM students WHERE id >= 10 AND id <= 20
SELECT * FROM students WHERE email LIKE '%@example.com'
```

**Comparison Operators:**
- `=` - Equal to
- `!=` - Not equal to
- `>` - Greater than
- `<` - Less than
- `>=` - Greater than or equal to
- `<=` - Less than or equal to
- `LIKE` - Pattern matching (supports SQL wildcards)

**Logical Operators:**
- `AND` - Both conditions must be true
- `OR` - At least one condition must be true

**Notes:**
- Use `*` to select all columns
- WHERE clause is optional; omitting it selects all rows
- LIMIT restricts the number of rows returned
- Conditions can be combined with AND/OR operators

---

### 5. UPDATE

Modifies existing rows in a table.

**Syntax:**
```sql
UPDATE <table_name> SET <column1> = <value1>, <column2> = <value2>, ... [WHERE <condition>] [LIMIT <n>]
```

**Examples:**
```sql
-- Update all rows
UPDATE students SET age = 21

-- Update with WHERE clause
UPDATE students SET name = 'Jane Doe' WHERE id = 1
UPDATE students SET email = 'newemail@example.com' WHERE name = 'John'

-- Update multiple columns
UPDATE students SET name = 'Alice Smith', age = 22 WHERE id = 3

-- Update with LIMIT
UPDATE students SET age = 20 WHERE age < 20 LIMIT 10

-- Complex WHERE conditions
UPDATE students SET email = 'updated@test.com' WHERE id > 5 AND age < 25
UPDATE students SET name = 'Anonymous' WHERE email LIKE '%@temp.com'

-- Update with NULL value (sets to default value)
UPDATE students SET email = null WHERE id = 5
```

**Notes:**
- Without a WHERE clause, all rows will be updated
- Multiple columns can be updated in a single statement
- LIMIT restricts the number of rows updated
- Values are type-checked against the table schema
- **NULL values**: Use the keyword `null` (case-insensitive) to set a column to its default value

---

### 6. DELETE

Removes rows from a table.

**Syntax:**
```sql
DELETE FROM <table_name> [WHERE <condition>] [LIMIT <n>]
```

**Examples:**
```sql
-- Delete all rows
DELETE FROM students

-- Delete with WHERE clause
DELETE FROM students WHERE id = 1
DELETE FROM students WHERE age < 18

-- Delete with LIMIT
DELETE FROM students LIMIT 5
DELETE FROM students WHERE status = 'inactive' LIMIT 10

-- Complex WHERE conditions
DELETE FROM students WHERE id > 100 AND status = 'deleted'
DELETE FROM students WHERE email LIKE '%@spam.com'
```

**Notes:**
- Without a WHERE clause, all rows will be deleted (use with caution!)
- LIMIT restricts the number of rows deleted
- The table structure remains intact; use DROP TABLE to remove the table entirely

---

### 7. HELP

Displays available commands and their syntax.

**Syntax:**
```
help
```

**Output:**
```
Available commands:
  CREATE TABLE <name> (<column> <type>, ...)
  DROP TABLE <name>
  INSERT INTO <name> (<columns>) VALUES (<values>)
  SELECT <columns> FROM <name> [WHERE <condition>] [LIMIT <n>]
  UPDATE <name> SET <column>=<value> [WHERE <condition>] [LIMIT <n>]
  DELETE FROM <name> [WHERE <condition>] [LIMIT <n>]
  HELP - Show this help message
  EXIT/QUIT - Exit the CLI
```

---

## Complete Usage Example

Here's a complete example session demonstrating all major features:

```sql
-- Create a students table
CREATE TABLE students (id int, name varchar(50), email varchar(100), age int)

-- Insert some records
INSERT INTO students (id, name, email, age) VALUES (1, 'Alice Johnson', 'alice@university.edu', 20)
INSERT INTO students (id, name, email, age) VALUES (2, 'Bob Smith', 'bob@university.edu', 22)
INSERT INTO students (id, name, email, age) VALUES (3, 'Charlie Brown', 'charlie@university.edu', 19)
INSERT INTO students (id, name, email, age) VALUES (4, 'Diana Prince', 'diana@university.edu', 21)

-- Query all students
SELECT * FROM students

-- Query specific students
SELECT name, email FROM students WHERE age >= 20

-- Update a student's information
UPDATE students SET email = 'alice.j@university.edu' WHERE id = 1

-- Update multiple students
UPDATE students SET age = 21 WHERE age < 21 LIMIT 2

-- Delete a student
DELETE FROM students WHERE id = 3

-- Verify the changes
SELECT * FROM students

-- Drop the table when done
DROP TABLE students
```

---

## Error Handling

The CLI provides feedback for common errors:

- **Syntax Error**: Invalid SQL syntax or missing required elements
- **Table Not Found**: Attempting to access a non-existent table
- **Data Type Mismatch**: Inserting or comparing incompatible data types
- **Unrecognized Statement**: Command not supported by NanoDB

Example error scenarios:
```sql
-- Syntax error: no column definitions
CREATE TABLE invalid

-- Table not found error
SELECT * FROM nonexistent_table

-- Data type mismatch: inserting text into an int column
INSERT INTO students (id, name) VALUES ('abc', 'John')
```

---

## Tips and Best Practices

1. **Always define column schemas**: Tables require column definitions during creation
2. **Use quotes for string values with spaces**: `'John Doe'` instead of `John Doe`
3. **Be careful with UPDATE/DELETE without WHERE**: These operations affect all rows
4. **Use LIMIT to test queries**: Test your WHERE conditions with LIMIT before running on all rows
5. **Table and column names are case-sensitive**: `Students` and `students` are different tables
6. **Date format is strict**: Always use `YYYY-MM-DD` format for date values

---

## Database Persistence

NanoDB stores data in a file-based chunker system. The database state persists across CLI sessions, so your tables and data will be available when you restart the application.

---

## Limitations

- No support for JOIN operations between tables
- No support for aggregate functions (COUNT, SUM, AVG, etc.)
- No support for GROUP BY or ORDER BY clauses
- No support for ALTER TABLE (modify table schema)
- No support for constraints (PRIMARY KEY, FOREIGN KEY, UNIQUE, etc.)
- No support for indexes
- LIKE operator supports basic pattern matching but may have limited wildcard support

---

## Getting Help

If you need assistance while using the CLI:
1. Type `help` to see available commands
2. Refer to this user guide for detailed syntax and examples
3. Check error messages for specific issues with your queries

---

*NanoDB CLI - A lightweight database management system for educational purposes*
