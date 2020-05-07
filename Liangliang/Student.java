/**
 * Created by Lighterkey on 6/15/2017.
 */

import java.util.*;

public class Student{

    public int uuid;
    public String name;
    public String address;
    public String phoneNumber;

    // hashmap used to store taken courses, when a course request is passed, init with null value
    // when a grade is assignment, give it a grade
    public Map<Integer, String> takenCourse_withGrade = new HashMap<Integer, String>();


    /* constructor for Student */
    public Student(int uuid, String name, String address, String phoneNumber){
        this.uuid = uuid;
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
    }

    public CourseRequest RequestCourse(String year, String term, int courseID){
        int studentID = this.uuid;
        CourseRequest newCourseRequest = new CourseRequest(year, term, studentID, courseID);
        return newCourseRequest;
    }


}
