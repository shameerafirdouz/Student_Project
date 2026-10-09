package com.student.model;

public class Student {

    private int rollNumber;
    private String name;
    private String branch;
    private double marks;

    public Student() {
    }

    public Student(int rollNumber, String name, String branch, double marks) {
        this.rollNumber = rollNumber;
        this.name = name;
        this.branch = branch;
        this.marks = marks;
    }

    public int getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(int rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public double getMarks() {
        return marks;
    }

    public void setMarks(double marks) {
        this.marks = marks;
    }

    @Override
    public String toString() {
        return "Student{" +
                "rollNumber=" + rollNumber +
                ", name='" + name + '\'' +
                ", branch='" + branch + '\'' +
                ", marks=" + marks +
                '}';
    }
}