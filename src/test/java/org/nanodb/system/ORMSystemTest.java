package org.nanodb.system;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.nanodb.orm.*;
import org.nanodb.orm.annotations.MaximumLength;
import org.nanodb.internal.condition.*;

import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * System tests for ORM usage scenarios
 */
@DisplayName("ORM System Tests")
public class ORMSystemTest {

    @TempDir
    Path tempDir;

    public static class User extends BaseModel {
        public int userId;
        @MaximumLength(50)
        public String username;
        @MaximumLength(100)
        public String email;
        public Date createdAt;
    }

    public static class Post extends BaseModel {
        public int postId;
        public int authorId;
        @MaximumLength(200)
        public String title;
        @MaximumLength(1000)
        public String content;
        public Date publishedAt;
    }

    static class BlogDb extends NanoDb {
        public Table<User> users;
        public Table<Post> posts;

        BlogDb(String filename) throws Exception {
            super(filename);
        }
    }

    @Test
    @DisplayName("Complete blog application workflow")
    void testBlogWorkflow() throws Exception {
        String dbFile = tempDir.resolve("blog.db").toString();
        BlogDb blog = new BlogDb(dbFile);

        // Create users
        User alice = new User();
        alice.userId = 1;
        alice.username = "alice";
        alice.email = "alice@example.com";
        alice.createdAt = new Date();

        User bob = new User();
        bob.userId = 2;
        bob.username = "bob";
        bob.email = "bob@example.com";
        bob.createdAt = new Date();

        blog.users.insert(List.of(alice, bob));

        // Create posts
        Post post1 = new Post();
        post1.postId = 101;
        post1.authorId = 1;
        post1.title = "Alice's First Post";
        post1.content = "Hello World from Alice";
        post1.publishedAt = new Date();

        Post post2 = new Post();
        post2.postId = 102;
        post2.authorId = 2;
        post2.title = "Bob's Introduction";
        post2.content = "Hi everyone, I'm Bob";
        post2.publishedAt = new Date();

        blog.posts.insert(List.of(post1, post2));

        // Query Alice's posts
        List<Post> alicePosts = blog.posts.select(
                new ComparisonCondition("authorId", ComparisonOperation.EQUAL, 1),
                10);

        assertEquals(1, alicePosts.size());
        assertEquals("Alice's First Post", alicePosts.get(0).title);

        // Update post
        blog.posts.update(
                new ComparisonCondition("postId", ComparisonOperation.EQUAL, 101),
                post -> {
                    post.content = "Updated content from Alice";
                    return post;
                },
                1);

        // Verify update
        List<Post> updated = blog.posts.select(
                new ComparisonCondition("postId", ComparisonOperation.EQUAL, 101),
                1);
        assertEquals("Updated content from Alice", updated.get(0).content);

        // Delete Bob's post
        blog.posts.delete(
                new ComparisonCondition("authorId", ComparisonOperation.EQUAL, 2),
                10);

        assertEquals(1, blog.posts.selectAll().size());
        assertEquals(2, blog.users.selectAll().size());

        blog.close();
    }

    @Test
    @DisplayName("Empty result set: Query non-existent user")
    void testEmptyResultSet() throws Exception {
        String dbFile = tempDir.resolve("empty.db").toString();
        try (BlogDb blog = new BlogDb(dbFile)) {
            List<User> users = blog.users.select(new ComparisonCondition("userId", ComparisonOperation.EQUAL, 999), 10);
            assertTrue(users.isEmpty());
        }
    }

    @Test
    @DisplayName("Update/delete with no match: Should not change anything")
    void testNoMatchUpdateDelete() throws Exception {
        String dbFile = tempDir.resolve("nomatch.db").toString();
        try (BlogDb blog = new BlogDb(dbFile)) {
            User user = new User();
            user.userId = 40;
            user.username = "nomatch";
            user.email = "nomatch@example.com";
            user.createdAt = new Date();
            blog.users.insert(List.of(user));
            int updated = blog.users.update(new ComparisonCondition("userId", ComparisonOperation.EQUAL, 999), u -> { u.username = "changed"; return u; }, 1);
            assertEquals(0, updated);
            int deleted = blog.users.delete(new ComparisonCondition("userId", ComparisonOperation.EQUAL, 999), 1);
            assertEquals(0, deleted);
            List<User> users = blog.users.selectAll();
            assertEquals(1, users.size());
            assertEquals("nomatch", users.get(0).username);
        }
    }

    }