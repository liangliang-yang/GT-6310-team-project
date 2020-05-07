/**
 * Created by Lighterkey on 6/17/2017.
 */

import java.io.*;
import  java.util.*;

public class MainSystem {


    /* 1. First, we need to create some array list to store those information about those classes */
    public static List<Student> studentList = new ArrayList<Student>();
    public static List<Instructor> instructorList = new ArrayList<Instructor>();
    public static List<Course> courseList = new ArrayList<Course>();

    public static List<CourseOffering> courseOfferingList = new ArrayList<CourseOffering>();
    // A list used to store and update current course offered in the current term
    public static List<Integer> current_CourseOffering_List = new ArrayList<Integer>();

    public static List<CourseRequest> courseRequestList = new ArrayList<CourseRequest>();

    public static List<AcademicRecord> academicRecordList = new ArrayList<AcademicRecord>();

    // reference:
    // 1. https://www.mkyong.com/java/how-to-read-and-parse-csv-file-in-java/
    // 2. https://www.javainterviewpoint.com/how-to-read-and-parse-csv-file-in-java/
    public static List<String[]> read_CSV_File(String fileName) {
        List<String[]> fileContent = new ArrayList<String[]>();


        try {
            File f = new File(fileName);
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = br.readLine();
            while (line != null) {
                fileContent.add(line.split(","));
                line = br.readLine(); // read a new line
            }
            br.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileContent;

    }

    /* read the student.csv file and parse all necessary data */
    public static void parse_Student_Info() {
        String student_CSV_File = "students.csv";
        List<String[]> student_Info_List = read_CSV_File(student_CSV_File);
        for (String[] student_Info : student_Info_List) {
            int uuid = Integer.parseInt(student_Info[0]);
            String name = student_Info[1];
            String address = student_Info[2];
            String phoneNumber = student_Info[3];

            Student newStudent = new Student(uuid, name, address, phoneNumber);
            studentList.add(newStudent);
        }

    }


    public static void parse_Instructor_Info() {
        String instructor_CSV_File = "instructors.csv";
        String eligible_CSV_File = "eligible.csv";

        List<String[]> instructor_Info_List = read_CSV_File(instructor_CSV_File);
        List<String[]> eligible_Info_List = read_CSV_File(eligible_CSV_File);

        // 1. parse basic instructor info
        for (String[] instructor_Info : instructor_Info_List) {
            int uuid = Integer.parseInt(instructor_Info[0]);
            String name = instructor_Info[1];
            String address = instructor_Info[2];
            String phoneNumber = instructor_Info[3];

            Instructor newInstructor = new Instructor(uuid, name, address, phoneNumber);
            instructorList.add(newInstructor);
        }

        // 2. parse eligible info
        for (String[] eligible_Info : eligible_Info_List) {
            int eligible_instructorID = Integer.parseInt(eligible_Info[0]);
            int eligible_courseID = Integer.parseInt(eligible_Info[1]);
            for (Instructor instructor : instructorList) {
                if (instructor.uuid == eligible_instructorID) {
                    instructor.eligibleCourses.add(eligible_courseID);
                    break;
                }

            }
        }

    }

    public static void parse_Course_Info() {
        String course_CSV_File = "courses.csv";
        String term_CSV_File = "terms.csv";
        String prereq_CSV_File = "prereqs.csv";

        List<String[]> course_Info_List = read_CSV_File(course_CSV_File);
        List<String[]> term_Info_List = read_CSV_File(term_CSV_File);
        List<String[]> prereq_Info_List = read_CSV_File(prereq_CSV_File);

        //  1. parse basic course info
        for (String[] course_Info : course_Info_List) {
            int courseID = Integer.parseInt(course_Info[0]);
            String courseName = course_Info[1];

            Course newCourse = new Course(courseID, courseName);
            courseList.add(newCourse);
        }

        // 2. parse terms info
        for (String[] term_Info : term_Info_List) {
            int term_courseID = Integer.parseInt(term_Info[0]);
            String term = term_Info[1];
            for (Course course : courseList) {
                if (course.courseID == term_courseID) {
                    course.termsOffered.add(term);
                    break;
                }
            }
        }


        // 3. parse prereq info
        for (String[] prereq_Info : prereq_Info_List) {
            int prereq_courseID = Integer.parseInt(prereq_Info[0]);
            int main_courseID = Integer.parseInt(prereq_Info[1]);

            for (Course course : courseList) {
                if (course.courseID == main_courseID) {
                    course.prereqsCourses.add(prereq_courseID);
                    break;
                }
            }
        }
    }

    public static List<String[]> parse_Action_Info() {
        String action_CSV_File = "actions.csv";
        List<String[]> action_Info_List = read_CSV_File(action_CSV_File);
        return action_Info_List;
    }

    public static void main(String[] args) {

        parse_Student_Info();

        parse_Instructor_Info();

        parse_Course_Info();

        List<String[]> action_Info_List = parse_Action_Info();

        // need to parse information about year and term
        String year = "";
        String term = "";
        String[] termArray = {"Fall", "Winter", "Spring", "Summer"};
        int termIndex = 0;
        int yearIndex = 0;

        for (String[] action : action_Info_List) {

//            // first, just output the command line as required
//            StringBuilder builder = new StringBuilder();
//            for (String value : action){
//                builder.append(value);
//            }
//            String actionText = builder.toString();
//            System.out.println(actionText);


            // first, just output the command line as required
            // in java 8, we don't need StringBuilder
            // https://stackoverflow.com/questions/6622974/convert-string-to-comma-separated-string-in-java
            String actionText = String.join(",", action);
            System.out.println(actionText);

            if (action[0].equals("start_sim")){
                // parse info of year

                year = action[1];
                yearIndex = Integer.parseInt(year);
                term = termArray[termIndex];
                System.out.println("# begin " + term + "_" + year + " term");

            }

            else if (action[0].equals("next_term")){
                // every term, need to clean all instructor.courseCurrentTeaching data
                for (Instructor instructor : instructorList){
                    instructor.courseCurrentTeaching = -1;
                }

                // also every term, need to clean all current_CourseOffering_List data
                current_CourseOffering_List = new ArrayList<Integer>();


                termIndex += 1;
                yearIndex += termIndex/4;
                termIndex = termIndex%4;
                year = String.valueOf(yearIndex);
                term = termArray[termIndex%4];
                System.out.println("# begin " + term + "_" + year + " term");
            }

            else if (action[0].equals("stop_sim")){
                //term = termArray[termIndex];
                System.out.println("# end " + term + "_" + year + " term");
            }

            else if (action[0].equals("hire")){
                int instructorID = Integer.parseInt(action[1]);
                for (Instructor instructor : instructorList){
                    if(instructor.uuid == instructorID){
                        instructor.isHired = true;
                        System.out.println("# instructor " + instructorID + " now hired");
                        break;
                    }
                }
            }

            else if (action[0].equals("take_leave")){
                int instructorID = Integer.parseInt(action[1]);
                for (Instructor instructor : instructorList){
                    if(instructor.uuid == instructorID){
                        instructor.isHired = false;
                        System.out.println("# instructor " + instructorID + " now on leave");
                        break;
                    }
                }
            }


            // command for teach course
            else if (action[0].equals("teach_course")){
                int instructorID = Integer.parseInt(action[1]);
                int courseID = Integer.parseInt(action[2]);
                CourseOffering newCourseOffering = new CourseOffering(year, term, instructorID, courseID);

                int validValue = newCourseOffering.validate_CourseOffering(instructorID, courseID, instructorList);

                if(validValue == -100){
                    System.out.println("# ERROR: instructor is not working");
                }
                else if(validValue == -200){
                    System.out.println("# ERROR: instructor is not eligible to teach this course");
                }
                else if(validValue == -300){
                    System.out.println("# ERROR: instructor is already teaching a different course");
                }
                else {
                    current_CourseOffering_List.add(courseID);
                    courseOfferingList.add(newCourseOffering);

                    for (Instructor instructor : instructorList) {
                        if (instructor.uuid == instructorID) {
                            instructor.TeachCourse(courseID);
                            System.out.println("# instructor " + instructorID + " is assigned to course " + courseID);
                        }

                    }
                }
            }

            // command for request course
            else if (action[0].equals("request_course")){
                int studentID = Integer.parseInt(action[1]);
                int courseID = Integer.parseInt(action[2]);
                //CourseRequest newCourseRequest = new CourseRequest(year, term, studentID, courseID);
                CourseRequest newCourseRequest = new CourseRequest(year, term);

                // call Student.RequestCourse() function
                int findStudent = -1;
                for (Student student : studentList){
                    if(student.uuid == studentID){
                        findStudent = 1;
                        newCourseRequest = student.RequestCourse(year, term, courseID);
                        break;
                    }
                }
                // if studentID not find
                if(findStudent == -1){
                    System.out.println("# ERROR: student ID does not exist");
                }

                int validValue = newCourseRequest.validateRequest(year, term, studentID, courseID, studentList, current_CourseOffering_List, courseList, courseOfferingList);
                newCourseRequest.requestResult = validValue;
                courseRequestList.add(newCourseRequest);

                if(validValue == -100){
                    System.out.println("# not enrolled: course already passed before");
                }
                else if(validValue == -200){
                    System.out.println("# student already enrolled in course");
                }

                else if(validValue == -300){
                    System.out.println("# not enrolled: missing prerequisites");
                }

                else if(validValue == -400){
                    System.out.println("# not enrolled: no available seats");
                }

                else if (validValue == -1000){
                    System.out.println("# ERROR: course ID does not exist");
                }

                else{
                    System.out.println("# enrolled");

                    // after success, first need to init/assign grade as "_"
                    for (Student student : studentList) {
                        if (studentID == student.uuid) {
                            student.takenCourse_withGrade.put(courseID, "_");
                            break;
                        }
                    }

                    // note here the return value for a valid request is the instructor ID
                    int instructorID = validValue;

                    // add new academic record with "_" grade
                    AcademicRecord newAcademicRecord = new AcademicRecord(year, term, studentID, courseID, instructorID);
                    academicRecordList.add(newAcademicRecord);

                }

            }

            else if (action[0].equals("instructor_report")) {
                int instructorID = Integer.parseInt(action[1]);
                String instructorName = "";
                int courseID_CurrentTeaching = -1;
                String courseName = "";
                for(Instructor instructor: instructorList){
                    if(instructor.uuid == instructorID){
                        instructorName = instructor.name;
                        courseID_CurrentTeaching = instructor.courseCurrentTeaching;
                        break;
                    }
                }

                for(Course course : courseList){
                    if(course.courseID == courseID_CurrentTeaching){
                        courseName = course.courseName;
                        break;
                    }
                }

                System.out.println("# instructor, " + instructorName);
                if (courseID_CurrentTeaching > 0){
                    System.out.println("# " + courseID_CurrentTeaching + ", " + courseName);
                }
            }

            else if (action[0].equals("student_report")) {
                int studentID = Integer.parseInt(action[1]);
                String studentName = "";
                String studentAddress = "";
                String studentPhoneNumber = "";
                List<AcademicRecord> student_academicRecordList = new ArrayList<AcademicRecord>();

                for(Student student: studentList){
                    if(student.uuid == studentID){
                        studentName = student.name;
                        studentAddress = student.address;
                        studentPhoneNumber = student.phoneNumber;
                        break;
                    }
                }

                for(AcademicRecord academicRecord : academicRecordList){
                    if(studentID == academicRecord.studentID){
                        student_academicRecordList.add(academicRecord);
                    }
                }

                System.out.println("# student, " + studentName);
                System.out.println(studentID + ": " + studentName + ", " + studentAddress + ", " + studentPhoneNumber);

                for (AcademicRecord academicRecord : student_academicRecordList){
                    if(academicRecord.comments != null){
                        System.out.println(academicRecord.courseID + ", " + academicRecord.grade + ", " + academicRecord.term + "_" + academicRecord.year + ", " + academicRecord.instructorID
                                + ", " + academicRecord.comments);
                    }
                    else{
                        System.out.println(academicRecord.courseID + ", " + academicRecord.grade + ", " + academicRecord.term + "_" + academicRecord.year + ", " + academicRecord.instructorID);
                    }

                }
            }


            else if (action[0].equals("assign_grade")) {


                int studentID = Integer.parseInt(action[1]);
                int courseID = Integer.parseInt(action[2]);
                String grade = action[3];
                int instructorID = Integer.parseInt(action[4]);

                // this is the instructor who will assign the grade, may not exactly the instructor who teach the course
                Instructor newInstructor = new Instructor(instructorID);
                if (action.length == 5) {
                   newInstructor.assign_grade_withoutComments(year, term, studentID, courseID, grade, instructorID, academicRecordList);
                } else {
                    String comments = action[5];
                    newInstructor.assign_grade_withComments(year, term, studentID, courseID, grade, instructorID, comments, academicRecordList);
                }

                System.out.println("# grade recorded");

                // after store academicRecord, we can also update student's record
                for (Student student : studentList) {
                    if (student.uuid == studentID) {
                        student.takenCourse_withGrade.put(courseID, grade);
                        break;
                    }
                }

            }


        }


    }

}
