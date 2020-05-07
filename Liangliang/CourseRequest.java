/**
 * Created by Lighterkey on 6/18/2017.
 */

import java.util.*;


public class CourseRequest {

    public String year;
    public String term;
    public int studentID;
    public int courseID;

    public int requestResult = 0;


    public CourseRequest(String year, String term){
        this.year= year;
        this.term = term;
    }

    public CourseRequest(String year, String term, int studentID, int courseID){
        this.year= year;
        this.term = term;
        this.studentID = studentID;
        this.courseID = courseID;
    }

    // here validateRequest will return negative value for invalid request, or the instructor ID for valid request, so that the student can know which session he/she will enroll
    // also the instructor is important for update/create a new academic record with "_" grade
    // if return 0, means some other error
    public int validateRequest(String year, String term, int studentID, int courseID,  List<Student> studentList, List<Integer> current_CourseOffering_List, List<Course> courseList, List<CourseOffering> courseOfferingList){
        int valid = 0;

        Map<Integer, String> takenCourse_withGrade = new HashMap<Integer, String>();

        // 1. validate whether the course has been passed or not before, or already enrolled or not
        for (Student student : studentList){
            if (student.uuid == studentID){
                takenCourse_withGrade = student.takenCourse_withGrade;
                if (takenCourse_withGrade.containsKey(courseID)){
                    String grade = student.takenCourse_withGrade.get(courseID);
                    if (grade.equals("A") || grade.equals("B") || grade .equals("C")){
                        valid = -100;
                        return valid;
                    }
                    if (grade.equals("_")){
                        valid = -200;
                        return valid;
                    }
                }
            }
        }

        // 2. skip the check for terms.csv

        // 3. validate prerequisite
        int findCourse = -1;
        for (Course course : courseList){
            if (course.courseID == courseID){
                findCourse = 1;
                List<Integer> prereqsCourses = course.prereqsCourses;
                for (Integer prereqsCourse : prereqsCourses){
                    if (! takenCourse_withGrade.containsKey(prereqsCourse)){
                        valid = -300;
                        return valid;
                    }
                    else{
                        String grade = takenCourse_withGrade.get(prereqsCourse);
                        if (!grade.equals("A") && !grade.equals("B") && !grade.equals("C") ){
                            valid = -300;
                            return valid;
                        }
                    }
                }
            }
        }


        // 3-1. if course not find
        if (findCourse == -1){
            valid = -1000;
            return  valid;
        }

        // 4. validate whether the course has been signed up by an instructor / current offering
        if ( !current_CourseOffering_List.contains(courseID) ){
            valid = -400;
            return valid;
        }

        // 5. validate available seats, as discussed by piazza @167, same output as before
        // https://piazza.com/class/j2pi7gp4xe35ql?cid=167
        // need to consider the case that there are multiple instructors teach the same course in a semester
        int seats_Available = -1;
        for (CourseOffering courseOffering : courseOfferingList){
            if (courseOffering.year.equals(year) && courseOffering.term.equals(term) && courseOffering.courseID == courseID){
                // find such a course
                if(courseOffering.number_StudentsEnrolled < courseOffering.availableSeats){
                    // course is not full, can be enrolled
                    seats_Available = 1;
                    courseOffering.number_StudentsEnrolled += 1;
                    valid = courseOffering.instructorID;
                    return valid;
                }
            }
        }
        // if all courses are full
        if(seats_Available == -1){
            valid = -400;
            return valid;
        }

        // other special case



        return valid;
    }

}
