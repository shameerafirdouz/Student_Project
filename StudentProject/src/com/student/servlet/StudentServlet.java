package com.student.servlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.student.model.Student;
import com.student.service.StudentService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * REST servlet for Student CRUD operations.
 * Mapped to /api/students via @WebServlet annotation.
 *
 * GET    /api/students              -> all students (JSON array)
 * GET    /api/students?rollNumber=X -> single student (JSON object)
 * POST   /api/students              -> add student   (body: JSON)
 * PUT    /api/students?rollNumber=X -> update student (body: JSON)
 * DELETE /api/students?rollNumber=X -> delete student
 */
@WebServlet("/api/students")
public class StudentServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private StudentService service;

    // =========================================================
    // INIT
    // =========================================================

    @Override
    public void init() throws ServletException {
        // Resolve the data file path relative to the deployed webapp.
        // Fallback to local data path if getRealPath returns null.
        String filePath = null;
        if (getServletContext() != null) {
            filePath = getServletContext().getRealPath("/WEB-INF/data/students.txt");
        }

        if (filePath == null) {
            String catalinaBase = System.getProperty("catalina.base");
            if (catalinaBase != null && !catalinaBase.isEmpty()) {
                filePath = catalinaBase + File.separator + "data" + File.separator + "students.txt";
            } else {
                filePath = "data" + File.separator + "students.txt";
            }
        }

        service = new StudentService(filePath);
    }

    // =========================================================
    // GET
    // =========================================================

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        setCorsHeaders(resp);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        PrintWriter out = resp.getWriter();
        String rollParam = req.getParameter("rollNumber");

        if (rollParam == null || rollParam.trim().isEmpty()) {
            // Return all students
            List<Student> students = service.getAllStudents();
            out.print(studentsToJson(students));
        } else {
            // Return single student
            try {
                int roll = Integer.parseInt(rollParam.trim());
                Student student = service.findStudent(roll);

                if (student == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\":\"Student not found\"}");
                } else {
                    out.print(studentToJson(student));
                }
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid roll number format\"}");
            }
        }
    }

    // =========================================================
    // POST — Add student
    // =========================================================

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        setCorsHeaders(resp);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            String body = readBody(req);
            Student student = parseStudent(body, null);
            boolean added = service.addStudent(student);

            if (added) {
                resp.setStatus(HttpServletResponse.SC_CREATED);
                out.print("{\"message\":\"Student added successfully\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                out.print("{\"error\":\"Student already exists or invalid data\"}");
            }

        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"Server error: " + escapeJson(e.getMessage()) + "\"}");
        }
    }

    // =========================================================
    // PUT — Update student
    // =========================================================

    @Override
    protected void doPut(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        setCorsHeaders(resp);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        String rollParam = req.getParameter("rollNumber");

        try {
            String body = readBody(req);
            Student student = parseStudent(body, rollParam);
            boolean updated = service.updateStudent(student);

            if (updated) {
                out.print("{\"message\":\"Student updated successfully\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"Student not found\"}");
            }

        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"Server error: " + escapeJson(e.getMessage()) + "\"}");
        }
    }

    // =========================================================
    // DELETE — Delete student
    // =========================================================

    @Override
    protected void doDelete(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        setCorsHeaders(resp);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        String rollParam = req.getParameter("rollNumber");

        if (rollParam == null || rollParam.trim().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"rollNumber parameter is required\"}");
            return;
        }

        try {
            int roll = Integer.parseInt(rollParam.trim());
            boolean deleted = service.deleteStudent(roll);

            if (deleted) {
                out.print("{\"message\":\"Student deleted successfully\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"Student not found\"}");
            }

        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid roll number format\"}");
        }
    }

    // =========================================================
    // OPTIONS — CORS preflight
    // =========================================================

    @Override
    protected void doOptions(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {
        setCorsHeaders(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private void setCorsHeaders(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader(
                "Access-Control-Allow-Methods",
                "GET, POST, PUT, DELETE, OPTIONS"
        );
        resp.setHeader(
                "Access-Control-Allow-Headers",
                "Content-Type"
        );
    }

    private String readBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    /**
     * Parses a Student object from JSON string, using defaultRollNumber if not found in JSON.
     */
    private Student parseStudent(String json, String defaultRollParam) {
        if (json == null || json.trim().isEmpty()) {
            throw new IllegalArgumentException("Request body is empty");
        }

        String rollStr = extractOptionalValue(json, "rollNumber");
        if (rollStr == null || rollStr.isEmpty()) {
            rollStr = defaultRollParam;
        }
        if (rollStr == null || rollStr.trim().isEmpty()) {
            throw new IllegalArgumentException("rollNumber is required");
        }

        int rollNumber;
        try {
            rollNumber = Integer.parseInt(rollStr.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid roll number format");
        }

        String name = extractValue(json, "name").trim();
        String branch = extractValue(json, "branch").trim();

        String marksStr = extractValue(json, "marks").trim();
        double marks;
        try {
            marks = Double.parseDouble(marksStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid marks format");
        }

        return new Student(rollNumber, name, branch, marks);
    }

    private String extractOptionalValue(String json, String key) {
        try {
            return extractValue(json, key);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Extracts the value for a given key from a simple flat JSON string.
     */
    private String extractValue(String json, String key) {
        Pattern pattern = Pattern.compile(
                "\"" + Pattern.quote(key) + "\"\\s*:\\s*(?:\"([^\"]*)\"|([^,}\\]\\s]+))"
        );
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            if (matcher.group(1) != null) {
                return matcher.group(1)
                        .replace("\\\"", "\"")
                        .replace("\\\\", "\\")
                        .replace("\\n", "\n")
                        .replace("\\r", "\r")
                        .replace("\\t", "\t");
            } else if (matcher.group(2) != null) {
                return matcher.group(2).trim();
            }
        }
        throw new IllegalArgumentException("Missing field: " + key);
    }

    // =========================================================
    // JSON SERIALIZATION
    // =========================================================

    private String studentToJson(Student s) {
        return "{"
                + "\"rollNumber\":" + s.getRollNumber() + ","
                + "\"name\":\""    + escapeJson(s.getName())   + "\","
                + "\"branch\":\""  + escapeJson(s.getBranch()) + "\","
                + "\"marks\":"     + s.getMarks()
                + "}";
    }

    private String studentsToJson(List<Student> students) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < students.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(studentToJson(students.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"",  "\\\"")
                .replace("\n",  "\\n")
                .replace("\r",  "\\r")
                .replace("\t",  "\\t");
    }
}