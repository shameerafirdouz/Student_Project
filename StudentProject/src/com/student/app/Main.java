package com.student.app;

import java.util.List;

import com.student.model.Student;
import com.student.service.StudentService;

/**
 * Standalone console application for testing StudentService directly.
 *
 * Uses a path relative to the project root (where Main is typically run from
 * in Eclipse / command-line), separate from the Tomcat-deployed path.
 */
public class Main {

    // Path relative to the project root when run as a standalone app
    private static final String FILE_PATH = "data/students.txt";

    public static void main(String[] args) {

        StudentService service = new StudentService(FILE_PATH);


        System.out.println("===== ADD STUDENT =====");

        Student newStudent = new Student(
                106,
                "Ananya",
                "ECE",
                92.0
        );

        boolean added = service.addStudent(newStudent);

        System.out.println("Added: " + added);


        System.out.println("\n===== ALL STUDENTS =====");

        List<Student> students = service.getAllStudents();

        for (Student student : students) {
            System.out.println(
                    student.getRollNumber() + " | " +
                    student.getName()       + " | " +
                    student.getBranch()     + " | " +
                    student.getMarks()
            );
        }


        System.out.println("\n===== SEARCH STUDENT =====");

        int searchRoll = 102;

        Student foundStudent = service.findStudent(searchRoll);

        if (foundStudent != null) {

            System.out.println("Student found:");

            System.out.println(
                    foundStudent.getRollNumber() + " | " +
                    foundStudent.getName()       + " | " +
                    foundStudent.getBranch()     + " | " +
                    foundStudent.getMarks()
            );

        } else {
            System.out.println("Student not found.");
        }


        System.out.println("\n===== UPDATE STUDENT =====");

        Student updatedStudent = new Student(
                102,
                "Rahul Kumar",
                "CSE-DS",
                95.0
        );

        boolean updated = service.updateStudent(updatedStudent);

        System.out.println("Updated: " + updated);


        System.out.println("\n===== DELETE STUDENT =====");

        int deleteRoll = 106;

        boolean deleted = service.deleteStudent(deleteRoll);

        System.out.println("Deleted: " + deleted);


        System.out.println("\n===== FINAL STUDENT LIST =====");

        students = service.getAllStudents();

        for (Student student : students) {
            System.out.println(
                    student.getRollNumber() + " | " +
                    student.getName()       + " | " +
                    student.getBranch()     + " | " +
                    student.getMarks()
            );
        }
    }
}