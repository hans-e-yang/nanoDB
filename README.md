A basic database implementation in Java, hopefully.

# Code Architecture
The code will be seperated into the 'frontend' and 'backend'
Frontend: Code that interacts with user, either through CLI or by exposing a facade pattern for a better DX
Backend: Actual code that implements the DB

## Backend
-- Still planning --
DB: 
  - Primitive DB functions
    - select(String tablename, String[] rows, Condition conditions)
    - insert
    - delete
    - ...
    - create table
    - drop table
  - This layer will understand the types of the  
    

B-Tree Manager (Name to be determined): Represents multiple tables (The runtime that maps variable names ('table names') into values ('the datastructures that represent the table'))
  - Maps table name into the table itself
  - I think this is the lowest level that we can consider as the db, we give DB as a facade? for a more `SQL` like interface.)
  - In this interface, the types of the tables are unknown, so we return values as byte[][] where each record is a byte[].
      - However, it must know how long one record is
  

B-Tree: Represents one table (The data structure)

Chunker: Acts like the malloc of our DB, it assigns chunks of 4kbs into datastructures that requests it.
  - We can also implement caching in this layer

## How the database will be stored in the file
Assume file db.db, split into 4kb chunks
Chunker will handle mapping ID into the chunks
|    Chunk 1         |   Chunk 2            |    Chunk 3          |
+--------------------+----------------------+---------------------+-----
|                    |                      |                     |
+--------------------+----------------------+---------------------+-----

B-tree will manage on the chunk level, each chunk will have some metadata for the b-tree, such as:
- Initialized: boolean
- isLeaf: boolean
The rest of the chunk will be reserved for data or indexes
+------------------------------------------+
| Initialized | isLeaf | Data              |
+------------------------------------------+
Each b-tree will need to know the key and the size of each record, then, it can create the indexes of (previous_key, chunk_id)[]

B-tree Manager will store metadata on the names of each b-trees, by creating a b-tree in the first chunk itself.
So there will be one b-tree dedicated to keep the metadata.
Each record will be (String tablename, int table_metadata_id, int table_data_id)
- table_metadata_id will point to a chunk that holds the table metadata for upper layers to use (namely to store column definitions)
- table_data_id will point to the root of the b-tree that represents the table


### Acknowledgements
- Structure inspired by <https://cstack.github.io/db_tutorial/>
