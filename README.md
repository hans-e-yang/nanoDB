# nanoDB - List-Based Database Implementation

A lightweight SQL-like database in Java using simple list-based tables instead of B-Trees, with optional file persistence.

## Overview

nanoDB demonstrates core database concepts through a straightforward list-based architecture:

- **ArrayList&lt;Row&gt;** for table storage (instead of B-Trees)
- **SQL-like parsing** and execution
- **File-based persistence** to DB.db using RandomAccessFile and 4KB chunks
- **Condition-based filtering** with AND/OR operators

## Architecture

nanoDB uses simple **ArrayList&lt;Row&gt;** instead of B-Trees for a pragmatic approach:

### List-Based Design

The key design choice is using simple **ArrayList&lt;Row&gt;** for table storage:

- **Linear storage**: Rows kept in memory as Java objects
- **Primary key tracking**: Set&lt;Integer&gt; for uniqueness checks
- **Persistence**: Rows serialized to 4KB disk chunks
- **Perfect for**: Small-to-medium datasets

### Layered Components

\\\
Main (CLI)
  |
StatementParser -> Condition
  |
Executor
  |
Db (Abstract)
   FileBasedDb (persistent)
   SimpleDb (in-memory)
  |
Table (ArrayList&lt;Row&gt;)
  |
Chunker (Abstract)
   FileBasedChunker (RandomAccessFile)
   SimpleMapChunker (HashMap)
\\\

## Key Features

### SQL Commands Supported

- \CREATE TABLE tablename\ - Create table
- \INSERT INTO table id courseName courseCode\ - Insert row
- \SELECT * FROM table [WHERE condition]\ - Query rows
- \UPDATE table SET col=val [WHERE condition]\ - Update rows
- \DELETE FROM table [WHERE condition]\ - Delete rows
- \DROP TABLE tablename\ - Delete table

### Filtering Operators

- **Comparison**: \=\, \<\, \>\
- **Logical**: \nd\, \or\
- **Examples**: \id=5\, \id>10 and courseName=John\, \id=1 or courseCode=CS101\

## Data Format

### Row Structure (68 bytes)

\\\
Offset 0-3: id (int)
Offset 4-35: courseName (32 bytes)
Offset 36-67: courseCode (32 bytes)
\\\

### DB.db File Layout

\\\
Bytes 0-3: Metadata (nextId)
Bytes 4-4099: Chunk 0 (table registry)
Bytes 4100+: Chunks 1+ (table data)
\\\

Each chunk = 4096 bytes with 4-byte count header + row data

## Usage

### Run nanoDB

\\\ash
mvn clean compile
java -cp target/classes org.nanodb.Main
\\\

### Example Session

\\\
db &gt; create table courses
Executed.

db &gt; insert into courses 101 Software-Engineering-Practice CS3343
Executed.

db &gt; select * from courses
(101, Software-Engineering-Practice, CS3343)
Executed.

db &gt; .exit
\\\

## Project Structure

\\\
src/main/java/org/nanodb/
 Main.java
 db/
    Db.java (abstract)
    SimpleDb.java (in-memory)
    FileBasedDb.java (persistent)
    Table.java (ArrayList-based)
    Row.java
 statement/
    StatementParser.java
    Statement.java
    condition/
        Condition.java
        ComparisonCondition.java
        AndCondition.java
        OrCondition.java
 executor/
    Executor.java
 chunker/
     Chunker.java (abstract)
     FileBasedChunker.java
     SimpleMapChunker.java
\\\

## Acknowledgements

- Inspired by [db_tutorial](https://cstack.github.io/db_tutorial/)
- Storage concepts from [SQLite](https://www.sqlite.org/)
