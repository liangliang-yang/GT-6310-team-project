/**
 * Created by Lighterkey on 6/18/2017.
 */

import java.util.*;

/*
The AcademicRecord class is used to represent all grade information for a student
Will be mainly used in the student_report
 */
public class AcademicRecord {
    public String year;
    public String term;
    public int studentID;
    public int courseID;
    public int instructorID;
    public String grade;
    public String comments;

    // constructor for AcademicRecord when student request course
    public AcademicRecord(String year, String term, int studentID, int courseID, int instructorID){
        this.year = year;
        this.term = term;
        this.studentID = studentID;
        this.courseID = courseID;
        this.instructorID = instructorID;
        this.grade = "_";
    }

    public AcademicRecord(String year, String term, int studentID, int courseID, String grade, int instructorID, String comments){
        this.year = year;
        this.term = term;
        this.studentID = studentID;
        this.courseID = courseID;
        this.grade = grade;
        this.instructorID = instructorID;
        this.comments = comments;
    }


}
