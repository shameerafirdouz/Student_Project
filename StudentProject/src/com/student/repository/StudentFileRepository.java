package com.student.repository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.student.model.Student;

/**
 * File-based repository for Student records.
 *
 * - Thread-safe synchronized access.
 * - Explicit UTF-8 encoding across all reads and writes.
 * - Auto-creates parent directories and file if they do not exist.
 * - addStudent returns boolean indicating write success.
 * - Resilient line-by-line parsing skipping bad entries.
 */
public class StudentFileRepository {

    private final String filePath;

    public StudentFileRepository(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            this.filePath = "data" + File.separator + "students.txt";
        } else {
            this.filePath = filePath;
        }
        ensureFileExists();
    }

    /**
     * Ensures that the data file and its parent directories exist.
     */
    private synchronized void ensureFileExists() {
        try {
            File file = new File(this.filePath);
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            System.err.println("Notice: Could not initialize data file: " + e.getMessage());
        }
    }

    // =========================================================
    // GET ALL
    // =========================================================

    public synchronized List<Student> getAllStudents() {
        List<Student> students = new ArrayList<>();
        File file = new File(filePath);

        if (!file.exists()) {
            return students;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] data = line.trim().split("\\|");
                if (data.length != 4) {
                    System.out.println("Skipping malformed line: " + line);
                    continue;
                }

                try {
                    int rollNumber = Integer.parseInt(data[0].trim());
                    String name = data[1].trim();
                    String branch = data[2].trim();
                    double marks = Double.parseDouble(data[3].trim());

                    students.add(new Student(rollNumber, name, branch, marks));
                } catch (NumberFormatException e) {
                    System.out.println("Skipping line with invalid numbers: " + line);
                }
            }

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }

        return students;
    }

    // =========================================================
    // ADD
    // =========================================================

    public synchronized boolean addStudent(Student student) {
        ensureFileExists();
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(filePath, true), StandardCharsets.UTF_8))) {

            writer.write(
                    student.getRollNumber() + "|"
                    + student.getName()     + "|"
                    + student.getBranch()   + "|"
                    + student.getMarks()
            );
            writer.newLine();
            return true;

        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
            return false;
        }
    }

    // =========================================================
    // FIND
    // =========================================================

    public synchronized Student findStudent(int rollNumber) {
        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] data = line.trim().split("\\|");
                if (data.length != 4) {
                    continue;
                }

                try {
                    int currentRoll = Integer.parseInt(data[0].trim());
                    if (currentRoll == rollNumber) {
                        String name   = data[1].trim();
                        String branch = data[2].trim();
                        double marks  = Double.parseDouble(data[3].trim());

                        return new Student(currentRoll, name, branch, marks);
                    }
                } catch (NumberFormatException e) {
                    // Skip invalid line
                }
            }

        } catch (IOException e) {
            System.err.println("Error searching file: " + e.getMessage());
        }

        return null;
    }

    // =========================================================
    // UPDATE
    // =========================================================

    public synchronized boolean updateStudent(Student updatedStudent) {
        List<Student> students = getAllStudents();
        boolean found = false;

        for (int i = 0; i < students.size(); i++) {
            if (students.get(i).getRollNumber() == updatedStudent.getRollNumber()) {
                students.set(i, updatedStudent);
                found = true;
                break;
            }
        }

        if (!found) {
            return false;
        }

        return writeAllStudents(students);
    }

    // =========================================================
    // DELETE
    // =========================================================

    public synchronized boolean deleteStudent(int rollNumber) {
        List<Student> students = getAllStudents();
        boolean removed = false;

        for (int i = 0; i < students.size(); i++) {
            if (students.get(i).getRollNumber() == rollNumber) {
                students.remove(i);
                removed = true;
                break;
            }
        }

        if (!removed) {
            return false;
        }

        return writeAllStudents(students);
    }

    // =========================================================
    // PRIVATE HELPERS
    // =========================================================

    private boolean writeAllStudents(List<Student> students) {
        ensureFileExists();
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8))) {

            for (Student student : students) {
                writer.write(
                        student.getRollNumber() + "|"
                        + student.getName()     + "|"
                        + student.getBranch()   + "|"
                        + student.getMarks()
                );
                writer.newLine();
            }

            return true;

        } catch (IOException e) {
            System.err.println("Error writing file: " + e.getMessage());
            return false;
        }
    }
}