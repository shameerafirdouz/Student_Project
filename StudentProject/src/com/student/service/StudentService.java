package com.student.service;

import java.util.List;

import com.student.model.Student;
import com.student.repository.StudentFileRepository;

/**
 * Business logic layer for Student operations.
 * Accepts the data file path so the servlet can inject the
 * correct Tomcat-resolved path, while Main.java can pass
 * its own local path.
 */
public class StudentService {

    private final StudentFileRepository repository;

    public StudentService(String filePath) {
        this.repository = new StudentFileRepository(filePath);
    }

    // =========================================================
    // GET ALL
    // =========================================================

    public List<Student> getAllStudents() {
        return repository.getAllStudents();
    }

    // =========================================================
    // ADD
    // =========================================================

    public boolean addStudent(Student student) {

        if (student == null) {
            System.out.println("Student cannot be null.");
            return false;
        }

        if (student.getRollNumber() <= 0) {
            System.out.println("Roll number must be positive.");
            return false;
        }

        if (student.getName() == null
                || student.getName().trim().isEmpty()) {
            System.out.println("Name cannot be empty.");
            return false;
        }

        if (student.getName().contains("|")) {
            System.out.println("Name cannot contain the '|' character.");
            return false;
        }

        if (student.getBranch() == null
                || student.getBranch().trim().isEmpty()) {
            System.out.println("Branch cannot be empty.");
            return false;
        }

        if (student.getBranch().contains("|")) {
            System.out.println("Branch cannot contain the '|' character.");
            return false;
        }

        if (student.getMarks() < 0 || student.getMarks() > 100) {
            System.out.println("Marks must be between 0 and 100.");
            return false;
        }

        // Duplicate check (single file read only)
        Student existing = repository.findStudent(
                student.getRollNumber()
        );

        if (existing != null) {
            System.out.println(
                    "Student with roll number "
                    + student.getRollNumber()
                    + " already exists."
            );
            return false;
        }

        return repository.addStudent(student);
    }

    // =========================================================
    // FIND
    // =========================================================

    public Student findStudent(int rollNumber) {

        if (rollNumber <= 0) {
            System.out.println("Roll number must be positive.");
            return null;
        }

        return repository.findStudent(rollNumber);
    }

    // =========================================================
    // UPDATE
    // =========================================================

    public boolean updateStudent(Student student) {

        if (student == null) {
            System.out.println("Student cannot be null.");
            return false;
        }

        if (student.getRollNumber() <= 0) {
            System.out.println("Roll number must be positive.");
            return false;
        }

        if (student.getName() == null
                || student.getName().trim().isEmpty()) {
            System.out.println("Name cannot be empty.");
            return false;
        }

        if (student.getName().contains("|")) {
            System.out.println("Name cannot contain the '|' character.");
            return false;
        }

        if (student.getBranch() == null
                || student.getBranch().trim().isEmpty()) {
            System.out.println("Branch cannot be empty.");
            return false;
        }

        if (student.getBranch().contains("|")) {
            System.out.println("Branch cannot contain the '|' character.");
            return false;
        }

        if (student.getMarks() < 0 || student.getMarks() > 100) {
            System.out.println("Marks must be between 0 and 100.");
            return false;
        }

        return repository.updateStudent(student);
    }

    // =========================================================
    // DELETE
    // =========================================================

    public boolean deleteStudent(int rollNumber) {

        if (rollNumber <= 0) {
            System.out.println("Roll number must be positive.");
            return false;
        }

        return repository.deleteStudent(rollNumber);
    }
}