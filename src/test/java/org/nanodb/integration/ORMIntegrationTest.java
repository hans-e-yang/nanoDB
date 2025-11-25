package org.nanodb.integration;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.nanodb.orm.NanoDb;
import org.nanodb.orm.Table;
import org.nanodb.orm.BaseModel;
import org.nanodb.internal.condition.*;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for ORM functionality
 * Tests model-to-table mapping and operations
 */
@DisplayName("ORM Integration Tests")
public class ORMIntegrationTest {
    
    @TempDir
    Path tempDir;
    
    public static class Student extends BaseModel {
        public int id;
        public String name;
        public int grade;
    }
    
    public static class Course extends BaseModel {
        public int courseId;
        public String courseName;
        public int credits;
    }
    
    static class SchoolDb extends NanoDb {
        public Table<Student> students;
        public Table<Course> courses;
        
        SchoolDb(String filename) throws Exception {
            super(filename);
        }
    }
    
    @Test
    @DisplayName("ORM handles complete entity lifecycle")
    void testEntityLifecycle() throws Exception {
        String dbFile = tempDir.resolve("school.db").toString();
        SchoolDb db = new SchoolDb(dbFile);
        
        // Create
        Student alice = new Student();
        alice.id = 1;
        alice.name = "Alice";
        alice.grade = 95;
        
        db.students.insert(alice);
        
        // Read
        List<Student> results = db.students.selectAll();
        assertEquals(1, results.size());
        assertEquals("Alice", results.get(0).name);
        
        // Update
        db.students.update(
            new ComparisonCondition("id", ComparisonOperation.EQUAL, 1),
            student -> {
                student.grade = 98;
                return student;
            },
            1
        );
        
        results = db.students.selectAll();
        assertEquals(98, results.get(0).grade);
        
        // Delete
        db.students.delete(
            new ComparisonCondition("id", ComparisonOperation.EQUAL, 1),
            1
        );
        
        assertEquals(0, db.students.selectAll().size());
    }
    
    @Test
    @DisplayName("Multiple models interact correctly in same database")
    void testMultipleModels() throws Exception {
        String dbFile = tempDir.resolve("multi_model.db").toString();
        SchoolDb db = new SchoolDb(dbFile);
        
        // Add students
        Student s1 = new Student();
        s1.id = 1;
        s1.name = "Alice";
        s1.grade = 90;
        
        Student s2 = new Student();
        s2.id = 2;
        s2.name = "Bob";
        s2.grade = 85;
        
        db.students.insert(List.of(s1, s2));
        
        // Add courses
        Course c1 = new Course();
        c1.courseId = 101;
        c1.courseName = "Database Systems";
        c1.credits = 4;
        
        db.courses.insert(c1);
        
        // Verify independence
        assertEquals(2, db.students.selectAll().size());
        assertEquals(1, db.courses.selectAll().size());
        
        // Delete from one table shouldn't affect the other
        db.students.delete(
            new ComparisonCondition("id", ComparisonOperation.EQUAL, 1),
            1
        );
        
        assertEquals(1, db.students.selectAll().size());
        assertEquals(1, db.courses.selectAll().size());
    }
    
    @Test
    @DisplayName("ORM persists data across database reopening")
    void testORMPersistence() throws Exception {
        String dbFile = tempDir.resolve("persist.db").toString();
        
        // First session
        {
            SchoolDb db = new SchoolDb(dbFile);
            Student s = new Student();
            s.id = 100;
            s.name = "Persistent";
            s.grade = 100;
            db.students.insert(s);
        }
        
        // Second session
        {
            SchoolDb db = new SchoolDb(dbFile);
            List<Student> students = db.students.selectAll();
            assertEquals(1, students.size());
            assertEquals("Persistent", students.get(0).name);
            assertEquals(100, students.get(0).grade);
        }
    }
}