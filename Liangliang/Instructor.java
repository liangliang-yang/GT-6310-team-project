/**
 * Created by Lighterkey on 6/15/2017.
 */

import java.util.*;

public class Instructor{

    public int uuid;
    public String name;
    public String address;
    public String phoneNumber;

    // provides info which courses can be taught by the instructor, from eligible.csv
    public List<Integer> eligibleCourses = new ArrayList<Integer>();

    public Boolean isHired = false;
    public int courseCurrentTeaching = -1; // -1 means at beginning there is no course




    /* constructor for Instructor */
    public Instructor(int uuid, String name, String address, String phoneNumber){
        this.uuid = uuid;
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        //this.isHired = false;
        //this.eligibleCourses = new ArrayList<Integer>();
    }

    public Instructor(int uuid){
        this.uuid = uuid;
        this.name = null;
        this.address = null;
        this.phoneNumber = null;
        //this.isHired = false;
        //this.eligibleCourses = new ArrayList<Integer>();
    }

    public void TeachCourse(int courseID){
        this.courseCurrentTeaching = courseID;
    }

    public void assign_grade_withComments(String year, String term, int studentID, int courseID, String grade, int instructorID, String comments, List<AcademicRecord> academicRecordList){
        for (AcademicRecord academicRecord: academicRecordList){
            if(academicRecord.year.equals(year) && academicRecord.term.equals(term) && academicRecord.studentID == studentID && academicRecord.courseID == courseID){
                academicRecord.instructorID = instructorID;
                academicRecord.grade = grade;
                academicRecord.comments = comments;
                //return academicRecord;
            }
        }
        //AcademicRecord newAcademicRecord = new AcademicRecord(year, term, studentID, courseID, grade, instructorID, comments);
        //return  newAcademicRecord;
    }

    public void assign_grade_withoutComments(String year, String term, int studentID, int courseID, String grade, int instructorID, List<AcademicRecord> academicRecordList){
        for (AcademicRecord academicRecord: academicRecordList){
            if(academicRecord.year.equals(year) && academicRecord.term.equals(term) && academicRecord.studentID == studentID && academicRecord.courseID == courseID){
                academicRecord.instructorID = instructorID;
                academicRecord.grade = grade;

            }
        }
    }

}
