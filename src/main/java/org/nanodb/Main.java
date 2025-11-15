package org.nanodb;

import org.nanodb.orm.BaseModel;
import org.nanodb.orm.NanoDb;
import org.nanodb.orm.Table;
import org.nanodb.orm.annotations.MaximumLength;

import java.io.IOException;
import java.util.*;

public class Main {
    public static class Todo extends BaseModel {
        @MaximumLength(4)
        private List<String> details = new ArrayList<>();
        private boolean done = false;
        public Todo() {}
        public Todo(String... details) {
            this.details.addAll(Arrays.asList(details));
        }
        public List<String> getDetails() {
            return details;
        }
        public boolean getDone() {
            return done;
        }
        public void toggleDone() {done = !done;}
    }

    static class AppDB extends NanoDb  {
        Table<Todo> todos;
        public AppDB(String filename) throws IOException, IllegalAccessException {
            super(filename);
        }
        public Table<Todo> getTodos() {
            return todos;
        }
    }

    public static void main(String[] args) throws IOException, IllegalAccessException {
        AppDB db = new AppDB("test.ndb");
        db.getTodos().insert(new Todo("Eat", "Sleep", "Dream"));
        List<Todo> todos = db.getTodos().selectAll();
        for (Todo todo : todos) {
            System.out.println(todo.details);
        }
    }
}