/**
 * Created by Lighterkey on 6/18/2017.
 */

import java.util.*;

public class CourseOffering {
    public String year;
    public String term;
    public int instructorID;
    public int courseID;
    public int availableSeats = 3;
    public int number_StudentsEnrolled = 0;


    public CourseOffering(String year, String term, int instructorID, int courseID){
        this.year= year;
        this.term = term;
        this.instructorID = instructorID;
        this.courseID = courseID;
    }

    public int validate_CourseOffering(int instructor_ID, int course_ID, List<Instructor> instructorList){
        int valid = 1;

        // valid whether the instructor is hired
        for (Instructor instructor : instructorList){
            if (instructor.uuid == instructor_ID){
                if (instructor.isHired == false){
                    valid = -100;
                    return valid;
                }
            }
        }

        // valid whether the instructor is eligible to teach the course
        for (Instructor instructor : instructorList){
            if (instructor.uuid == instructor_ID){
                if (!instructor.eligibleCourses.contains(course_ID)){
                    valid = -200;
                    return valid;
                }
            }
        }

        // valid whether the instructor is teaching a different course
        for (Instructor instructor : instructorList){
            if (instructor.uuid == instructor_ID){
                if (instructor.courseCurrentTeaching != -1){
                    valid = -300;
                    return valid;
                }
            }
        }

        return valid;
    }

}
