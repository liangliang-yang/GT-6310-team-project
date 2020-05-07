/**
 * Created by Lighterkey on 6/15/2017.
 */

import java.util.*;

public class Course {

    public int courseID;
    public String courseName;
    public List<String> termsOffered;
    public List<Integer> prereqsCourses;
    //public String courseDescription; //may not be useful now//

    /* constructor */
    public Course(int courseID, String courseName, List<String> termsOffered, List<Integer> prereqsCourses){
        this.courseID = courseID;
        this.courseName = courseName;
        //this.courseDescription = courseDescription;
        this.termsOffered = termsOffered;
        this.prereqsCourses = prereqsCourses;
    }

    /* constructor with less info*/
    public Course(int courseID, String courseName){
        this.courseID = courseID;
        this.courseName = courseName;
        this.termsOffered = new ArrayList<String>();
        this.prereqsCourses= new ArrayList<Integer>();
    }

}
