package org.nanodb.db;

public class Row {
    public static final int ID_SIZE = 4;
    public static final int COURSE_NAME_SIZE = 32;
    public static final int COURSE_CODE_SIZE = 32;
    public static final int ROW_SIZE = ID_SIZE + COURSE_NAME_SIZE + COURSE_CODE_SIZE;

    public int id;
    public String courseName;
    public String courseCode;

    public Row(int id, String courseName, String courseCode) {
        this.id = id;
        this.courseName = courseName;
        this.courseCode = courseCode;
    }

    @Override
    public String toString() {
        return String.format("(%d, %s, %s)", id, courseName, courseCode);
    }
}