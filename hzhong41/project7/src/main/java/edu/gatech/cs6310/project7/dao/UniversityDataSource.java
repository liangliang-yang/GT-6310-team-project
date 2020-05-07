package edu.gatech.cs6310.project7.dao;

import edu.gatech.cs6310.project7.model.AcademicRecord;
import edu.gatech.cs6310.project7.model.Course;
import edu.gatech.cs6310.project7.model.CourseOffering;
import edu.gatech.cs6310.project7.model.CourseRequest;
import edu.gatech.cs6310.project7.model.Instructor;
import edu.gatech.cs6310.project7.model.Person;
import edu.gatech.cs6310.project7.model.Semester;
import edu.gatech.cs6310.project7.model.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static edu.gatech.cs6310.project7.constant.MySQLConstants.SQL_CALL_TRUNCATE_TABLES;
import static edu.gatech.cs6310.project7.constant.MySQLConstants.SQL_GET_ALL_COURSES;
import static edu.gatech.cs6310.project7.constant.MySQLConstants.SQL_GET_ALL_INSTRUCTORS;
import static edu.gatech.cs6310.project7.constant.MySQLConstants.SQL_GET_ALL_STUDENTS;
import static edu.gatech.cs6310.project7.constant.MySQLConstants.SQL_GET_ALL_USERS;
import static edu.gatech.cs6310.project7.constant.MySQLConstants.SQL_GET_MAX_ID_FOR_TABLE;
import static edu.gatech.cs6310.project7.constant.MySQLConstants.SQL_GET_SEMESTER;
import static edu.gatech.cs6310.project7.constant.MySQLConstants.SQL_TRUNCATE_SEMESTER;
import static edu.gatech.cs6310.project7.dao.DataSourceUtility.addUserRolePS;
import static edu.gatech.cs6310.project7.dao.DataSourceUtility.createAcademicRecordPS;
import static edu.gatech.cs6310.project7.dao.DataSourceUtility.createCourseOfferingPS;
import static edu.gatech.cs6310.project7.dao.DataSourceUtility.createCourseRequestPS;
import static edu.gatech.cs6310.project7.dao.DataSourceUtility.createWaitListPS;
import static edu.gatech.cs6310.project7.dao.DataSourceUtility.deleteEligibleCourse;
import static edu.gatech.cs6310.project7.dao.DataSourceUtility.getAcademicRecordPS;
import static edu.gatech.cs6310.project7.dao.DataSourceUtility.getCourseByIdPS;
import static edu.gatech.cs6310.project7.dao.DataSourceUtility.getCourseRecords;
import static edu.gatech.cs6310.project7.dao.DataSourceUtility.getCourseWaitListPS;
import static edu.gatech.cs6310.project7.dao.DataSourceUtility.getCurrentCourseOfferings;
import static edu.gatech.cs6310.project7.dao.DataSourceUtility.getCurrentCourseRequests;
import static edu.gatech.cs6310.project7.dao.DataSourceUtility.getInstructorByIdPS;
import static edu.gatech.cs6310.project7.dao.DataSourceUtility.getStudentByIdPS;
import static edu.gatech.cs6310.project7.dao.DataSourceUtility.getUserByIdPS;
import static edu.gatech.cs6310.project7.dao.DataSourceUtility.getWaitListbyStudentId;
import static edu.gatech.cs6310.project7.dao.DataSourceUtility.insertCoursePS;
import static edu.gatech.cs6310.project7.dao.DataSourceUtility.insertCoursePrereqPS;
import static edu.gatech.cs6310.project7.dao.DataSourceUtility.insertEligibleCourse;
import static edu.gatech.cs6310.project7.dao.DataSourceUtility.insertInstructorStatus;
import static edu.gatech.cs6310.project7.dao.DataSourceUtility.insertOfferTermPS;
import static edu.gatech.cs6310.project7.dao.DataSourceUtility.insertUserPS;
import static edu.gatech.cs6310.project7.dao.DataSourceUtility.insertUserRolePS;
import static edu.gatech.cs6310.project7.dao.DataSourceUtility.removeCoursePrerequisitePS;
import static edu.gatech.cs6310.project7.dao.DataSourceUtility.removeEligibleCoursePS;
import static edu.gatech.cs6310.project7.dao.DataSourceUtility.removeUserRolePS;
import static edu.gatech.cs6310.project7.dao.DataSourceUtility.updateCourseTitlePS;
import static edu.gatech.cs6310.project7.dao.DataSourceUtility.updateCurrentSemesterPS;
import static edu.gatech.cs6310.project7.dao.DataSourceUtility.updateUserPS;

/**
 * Created by student on 7/18/17.
 */
@Repository
public class UniversityDataSource {

    private DataSource ds;
    private InitialContext ctx;

    @Autowired
    public UniversityDataSource(final InitialContext ctx) {
        this.ctx = ctx;
    }

    @PostConstruct
    public void initialize() throws NamingException {
        ds = (DataSource) ctx.lookup("java:comp/env/jdbc/Project7");
    }

    public void truncateTables() throws SQLException {
        try (Connection conn = ds.getConnection();
            CallableStatement cs = conn.prepareCall(SQL_CALL_TRUNCATE_TABLES)) {
            conn.setAutoCommit(false);
            cs.execute();
            conn.commit();
        }
    }

    public Semester getCurrentSemester() throws SQLException {
        try (
                Connection conn = ds.getConnection();
                PreparedStatement ps = conn.prepareStatement(SQL_GET_SEMESTER);
                ResultSet rs = ps.executeQuery()) {
            if (rs.first()) {
                Semester currentSemester = Semester.builder().build();
                currentSemester.setTerm(rs.getString("TERM"));
                currentSemester.setYear(rs.getInt("YEAR"));
                return currentSemester;
            } else {
                return null;
            }
        }
    }

    public void updateCurrentSemester(Semester semester) throws SQLException {
        try (
                Connection conn = ds.getConnection();
                PreparedStatement truncate = conn.prepareStatement(SQL_TRUNCATE_SEMESTER);
                PreparedStatement update = updateCurrentSemesterPS(conn, semester.getYear(), semester.getTerm())
        ) {
            conn.setAutoCommit(false);
            truncate.executeUpdate();
            update.executeUpdate();
            conn.commit();
        }
    }

    /**
     * Use data in the student object to update the tables in MySQL using SQL queries.
     * @param students the students to create
     */
    public void createStudents(final Student... students) throws SQLException {

        try (Connection conn = ds.getConnection()) {
            for (Student student : students) {
                Integer id = Integer.parseInt(student.getId());
                String name = student.getName();
                String address = student.getAddress();
                String number = student.getPhoneNumber();

                try (PreparedStatement getUserPs = getUserByIdPS(conn, id);
                     ResultSet rs = getUserPs.executeQuery();
                     PreparedStatement userPs = insertUserPS(conn, id, name, address, number);
                     PreparedStatement rolePs = insertUserRolePS(conn, id, "STUDENT")) {

                    conn.setAutoCommit(false);
                    if (!rs.first()) {
                        userPs.execute();
                    }
                    rolePs.execute();
                    conn.commit();
                }
            }
        }
    }

    /**
     * Use a SQL select statement to retrieve a student by ID.
     * @param id The student's ID
     * @return the Student
     */
    public Student getStudentById(String id) throws SQLException {
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = getStudentByIdPS(conn, Integer.parseInt(id));
             ResultSet rs = ps.executeQuery())
        {
            Student student = Student.builder().build();
            Set<String> roles = new HashSet<>();

            if (!rs.first()) {
                return null;
            }

            student.setId(id);
            student.setName(rs.getString("NAME"));
            student.setAddress(rs.getString("ADDRESS"));
            student.setPhoneNumber(rs.getString("NUMBER"));

            do {
                roles.add(rs.getString("ROLE"));
            } while (rs.next());

            student.setRoles(new ArrayList<>(roles));

            return student;
        }
    }

    /**
     * Use SQL to create rows in the MySQL database using info in the instructor object.
     * @param instructors Array list of instructors to add
     */
    public void createInstructors(final Instructor... instructors) throws SQLException {


        try (Connection conn = ds.getConnection()) {
            for (Instructor instructor : instructors) {
                Integer id = Integer.parseInt(instructor.getId());
                String name = instructor.getName();
                String address = instructor.getAddress();
                String number = instructor.getPhoneNumber();
                boolean isHired = instructor.isActive();

                try (PreparedStatement getUserPs = getUserByIdPS(conn, id);
                     ResultSet rs = getUserPs.executeQuery();
                     PreparedStatement userPs = insertUserPS(conn, id, name, address, number);
                     PreparedStatement rolePs = insertUserRolePS(conn, id, "INSTRUCTOR");
                     PreparedStatement statusPs = insertInstructorStatus(conn, id, isHired)) {
                    conn.setAutoCommit(false);

                    if (!rs.next()) {
                        userPs.execute();
                    }
                    rolePs.execute();
                    statusPs.execute();
                    conn.commit();
                }
            }
        }
    }

    /**
     * Execute SQL query to retrieve instructor info from DB and then build Instructor object.
     * @param id Instructor ID
     * @return the Instructor
     */
    public Instructor getInstructorById(String id) throws SQLException {
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = getInstructorByIdPS(conn, Integer.parseInt(id));
             ResultSet rs = ps.executeQuery())
        {
            Instructor instructor = Instructor.builder().build();
            Set<String> roles = new HashSet<>();
            Map<String, Course> eligible = new HashMap<>();

            if (!rs.first()) {
                return null;
            }

            instructor.setId(id);
            instructor.setName(rs.getString("NAME"));
            instructor.setAddress(rs.getString("ADDRESS"));
            instructor.setPhoneNumber(rs.getString("NUMBER"));
            instructor.setActive(rs.getBoolean("ISHIRED"));

            String currentCourseId = rs.getString("CURRENT_COURSE_ID");
            if (currentCourseId == null
                    || currentCourseId.isEmpty()
                    || "null".equals(currentCourseId)) {
                instructor.setCourseCurrentTeaching(null);
            } else {
                Course currentCourse = Course.builder()
                        .courseID(currentCourseId)
                        .courseName(rs.getString("CURRENT_COURSE_TITLE"))
                        .build();
                instructor.setCourseCurrentTeaching(currentCourse);
            }

            do {
                roles.add(rs.getString("ROLE"));
                String courseId = rs.getString("ELIGIBLE_COURSE_ID");
                if (!eligible.containsKey(courseId) && courseId != null) {
                    Course course = Course.builder()
                            .courseID(courseId)
                            .courseName(rs.getString("ELIGIBLE_COURSE_TITLE"))
                            .build();
                    eligible.put(courseId, course);
                }
            } while (rs.next());

            instructor.setRoles(new ArrayList<>(roles));
            List<Course> eligibleCourses = eligible.values().stream()
                    .sorted(Comparator.comparingInt(c -> Integer.parseInt(c.getCourseID())))
                    .collect(Collectors.toList());
            instructor.setEligibleCourses(eligibleCourses);

            return instructor;
        }
    }

    /**
     * Update the InstructorStatus table
     * @param id id of the instructor
     * @param isHired isHired status to set
     */
    public void updateInstructorStatus(String id, boolean isHired) throws SQLException {

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = DataSourceUtility.updateInstructorStatusPS(conn, Integer.parseInt(id), isHired)) {
            conn.setAutoCommit(true);
            ps.execute();
        }
    }

    /**
     * Use SQL to create a new course in the course table.
     * @param courses The courses to create.
     */
    public void createCourses(final Course... courses) throws SQLException {

        try (Connection conn = ds.getConnection()) {
            for (Course course : courses) {
                conn.setAutoCommit(false);
                Integer id = Integer.parseInt(course.getCourseID());
                try (PreparedStatement ps = insertCoursePS(conn, id, course.getCourseName())) {
                    ps.execute();
                }

                for (Course prereq : course.getPrereqsCourses()) {
                    try (PreparedStatement ps = insertCoursePrereqPS(conn, id, Integer.parseInt(prereq.getCourseID()))) {
                        ps.execute();
                    }
                }

                for (String term : course.getTermsOffered()) {
                    try (PreparedStatement ps = insertOfferTermPS(conn, id, term)) {
                        ps.execute();
                    }
                }

                conn.commit();
            }
        }
    }

    public List<Course> getAllCourses() throws SQLException {
        List<Course> courses = new ArrayList<>();
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_GET_ALL_COURSES);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Course course = Course.builder()
                        .courseName(rs.getString("COURSETITLE"))
                        .courseID(rs.getString("COURSEID"))
                        .build();
                courses.add(course);
            }
        }
        return courses;
    }

    public List<Student> getAllStudents() throws SQLException {
        List<Student> students = new ArrayList<>();
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_GET_ALL_STUDENTS);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Student student = Student.builder()
                        .id(rs.getString("ID"))
                        .name(rs.getString("NAME"))
                        .address(rs.getString("ADDRESS"))
                        .phoneNumber(rs.getString("NUMBER"))
                        .build();
                students.add(student);
            }
        }
        return students;
    }

    public List<Instructor> getAllInstructors() throws SQLException {
        List<Instructor> instructors = new ArrayList<>();
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_GET_ALL_INSTRUCTORS);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                if (rs.getInt("ID") <= 0) {
                    continue;
                }
                Instructor instructor = Instructor.builder()
                        .id(rs.getString("ID"))
                        .name(rs.getString("NAME"))
                        .address(rs.getString("ADDRESS"))
                        .phoneNumber(rs.getString("NUMBER"))
                        .build();
                instructors.add(instructor);
            }
        }
        return instructors;
    }

    /**
     * Use SQL to create a CourseOffering in the CourseOffing table.
     * @param courseOffering The course to create.
     */
    public void createCourseOffering(CourseOffering courseOffering) throws SQLException {

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = createCourseOfferingPS(conn, courseOffering)
        ) {
            conn.setAutoCommit(true);
            ps.execute();
        }
    }

    public void createCourseRequest(CourseRequest courseRequest) throws SQLException {

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = createCourseRequestPS(conn, courseRequest)
        ) {
            conn.setAutoCommit(true);
            ps.execute();
        }
    }

    /**
     * Use SQL to get a course by its ID
     * @param courseId the course ID
     * @return the course
     */
    public Course getCourseById(Integer courseId) throws SQLException {

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = getCourseByIdPS(conn, courseId);
             ResultSet rs = ps.executeQuery()) {
            if (!rs.first()) {
                return null;
            }

            String id = rs.getString("COURSEID");
            String title = rs.getString("COURSETITLE");
            List<String> offerTerms = new ArrayList<>();

            String terms = rs.getString("OFFERTERM");
            if (terms != null
                    && !"null".equals(terms)
                    && !terms.isEmpty()) {
                offerTerms = new ArrayList<>(Arrays.asList((rs.getString("OFFERTERM")).split(",")));
            }

            Map<String, Course> prereqs = new HashMap<>();
            do {
                String prereqId = rs.getString("PREREQID");
                if (prereqId != null
                        && !"null".equals(prereqId)
                        && !prereqId.isEmpty()
                        && !prereqs.containsKey(prereqId)) {
                    Course prereq = Course.builder()
                            .courseID(prereqId)
                            .courseName(rs.getString("PREREQTITLE"))
                            .build();
                    prereqs.put(prereqId, prereq);
                }
            } while (rs.next());
            return Course.builder()
                    .courseID(id)
                    .courseName(title)
                    .termsOffered(offerTerms)
                    .prereqsCourses(new ArrayList<>(prereqs.values()))
                    .build();
        }
    }

    public void addEligibleCourse(String instructorId, String... courseIds) throws SQLException {

        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);
            for (String courseId : courseIds) {
                try (PreparedStatement ps = insertEligibleCourse(conn, Integer.parseInt(instructorId), Integer.parseInt(courseId))) {
                    ps.execute();
                }
            }
            conn.commit();
        }
    }

    public void removeEligibleCourse(String instructorId, String... courseIds) throws SQLException {

        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);
            for (String courseId : courseIds) {
                try (PreparedStatement ps = deleteEligibleCourse(conn, Integer.parseInt(instructorId), Integer.parseInt(courseId))) {
                    ps.execute();
                }
            }
            conn.commit();
        }
    }



    /**
     * get list of current course offering, just the courseID list is ok,  will be used in validateCourseRequest()
     */
    public List<CourseOffering> getCurrentCourseOfferingList(String courseId) throws SQLException {
        List<CourseOffering> currentCourseOfferingList = new ArrayList<>();

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = getCurrentCourseOfferings(conn, Integer.parseInt(courseId));
             ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                CourseOffering courseOffering = CourseOffering.builder()
                        .courseID(rs.getString("COURSEID"))
                        .instructorID(rs.getString("INSTRUCTORID"))
                        .availableSeats(rs.getInt("AVAILABLESEATS"))
                        .term(rs.getString("TERM"))
                        .year(rs.getInt("YEAR"))
                        .build();
                currentCourseOfferingList.add(courseOffering);
            }
        }
        return currentCourseOfferingList;
    }

    public void updateAvailableSeats(CourseOffering courseOffering) throws SQLException {
        try (
                Connection conn = ds.getConnection();
                PreparedStatement ps = DataSourceUtility.updateAvailableSeats(conn, courseOffering)
             ) {
                conn.setAutoCommit(true);
                ps.execute();
               }
    }

    public List<CourseRequest> getCurrentCourseRequestsForCourse(Integer courseId) throws SQLException {
        List<CourseRequest> courseRequests = new ArrayList<>();

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = getCurrentCourseRequests(conn, courseId);
             ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                CourseRequest request = CourseRequest.builder()
                        .courseID(rs.getString("COURSEID"))
                        .studentID(rs.getString("STUDENTID"))
                        .term(rs.getString("TERM"))
                        .year(rs.getInt("YEAR"))
                        .status(rs.getString("STATUS"))
                        .requestDate(rs.getTimestamp("REQUESTDATE"))
                        .build();
                courseRequests.add(request);
            }
        }
        return courseRequests;
    }



    /**
     * get waitlist for  a course
     */
    public HashMap<String,Integer> getCourseWaitList() throws SQLException {
        HashMap<String,Integer> coursewaitlist = new HashMap<>();

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = getCourseWaitListPS(conn);
             ResultSet rs = ps.executeQuery()
        ){
            while (rs.next()) {
                coursewaitlist.put(rs.getString("courseid"),rs.getInt("cnt_student_in_waitlist"));
            }
        }
        return coursewaitlist;
    }


    public void createWaitList(CourseRequest courseRequest) throws SQLException {

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = createWaitListPS(conn, courseRequest)
        ) {
            conn.setAutoCommit(true);
            ps.execute();
        }
    }

    public List<CourseRequest> getWaitListByStudentId(String studentId) throws SQLException {

        List<CourseRequest> courseRequests = new ArrayList<>();

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = getWaitListbyStudentId(conn, Integer.parseInt(studentId));
             ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                CourseRequest request = CourseRequest.builder()
                        .courseID(rs.getString("COURSEID"))
                        .studentID(rs.getString("STUDENTID"))
                        .term(rs.getString("TERM"))
                        .year(rs.getInt("YEAR"))
                        .status("-500")
                        .requestDate(rs.getTimestamp("INSERTDATE"))
                        .build();
                courseRequests.add(request);
            }
        }
        return courseRequests;

    }

    //When a course is offered, first check if this course is in waitlist use and assign the seat to waitlist student first
    public void updateWaitListStatus(String courseid) throws SQLException{
        int studentid;
        int year;
        String term;
        try (
                Connection conn = ds.getConnection();
                PreparedStatement ps =DataSourceUtility.getWaitlistByCoureIdPS(conn,Integer.parseInt(courseid));
                ResultSet rs = ps.executeQuery()

        ) {
            if (!rs.next() ) {
                System.out.println("Course not in Waitlist");
            } else {
               studentid=rs.getInt("STUDENTID");
               year=rs.getInt("YEAR");
               term=rs.getString("TERM");

                try (
                        Connection conn2 = ds.getConnection();
                        PreparedStatement ps2 =DataSourceUtility.updateWaitListStatusPS(conn2,studentid,Integer.parseInt(courseid), year,term);
                ){
                    conn2.setAutoCommit(true);
                    ps2.execute();
                }
            }
        }
    }


    public void createOfferTerm(String courseId, String term) throws SQLException {
        Integer course = Integer.parseInt(courseId);

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = insertOfferTermPS(conn, course, term)
        ) {
            conn.setAutoCommit(true);
            ps.execute();
        }
    }

    public Integer getMaxIdForTable(String column, String table) throws SQLException {
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(String.format(SQL_GET_MAX_ID_FOR_TABLE, column, table));
             ResultSet rs = ps.executeQuery()
        ) {
            if (rs.first()) {
                return rs.getInt(column);
            }

            return null;
        }
    }

    public void updateCourseTitle(final Course course) throws SQLException {
        try (
                Connection conn = ds.getConnection();
                PreparedStatement ps = updateCourseTitlePS(conn, course)) {
            conn.setAutoCommit(false);
            ps.executeUpdate();
            conn.commit();
        }
    }

    public void dropCourseOffering(final CourseOffering offering) throws SQLException {
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = DataSourceUtility.dropCourseOffering(conn, offering)) {
            conn.setAutoCommit(true);
            ps.execute();
        }
    }

    public void addUserRole(String userId, String role) throws SQLException {

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = addUserRolePS(conn, Integer.parseInt(userId), role)) {
            conn.setAutoCommit(true);
            ps.execute();
        }
    }

    public void removeUserRole(String userId, String role) throws SQLException {

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = removeUserRolePS(conn, Integer.parseInt(userId), role)) {
            conn.setAutoCommit(true);
            ps.execute();
        }
    }

    public void addEligibleCourse(String instructorId, String courseId) throws SQLException {

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = insertEligibleCourse(conn, Integer.parseInt(instructorId), Integer.parseInt(courseId))) {
            conn.setAutoCommit(true);
            ps.execute();
        }
    }

    public void removeEligibleCourse(String instructorId, String courseId) throws SQLException {

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = removeEligibleCoursePS(conn, Integer.parseInt(instructorId), Integer.parseInt(courseId))) {
            conn.setAutoCommit(true);
            ps.execute();
        }
    }

    public String addCoursePrerequisite(String courseId, String preprequisiteId) throws SQLException {
        Integer courseID = Integer.parseInt(courseId);
        Integer prereqID = Integer.parseInt(preprequisiteId);

        String message = "";

        Course course = getCourseById(courseID);
        Course prereqcourse = getCourseById(prereqID);
        if (course.getPrereqsCourses().contains(preprequisiteId)){
            message = "Error, prerequisite already added!";
            return message;
        }

        if (prereqcourse.getPrereqsCourses().contains(courseId)){
            message = "Error, the course is a prerequiste of the prereq course, can't be a loop!";
            return  message;
        }



        try (Connection conn = ds.getConnection();
             PreparedStatement ps = insertCoursePrereqPS(conn, courseID, prereqID)
        ) {
            conn.setAutoCommit(true);
            ps.execute();
        }

        message = "Prerequiste course add successfully!";
        return message;
    }

    public void removeCoursePrerequisite(String courseId, String precourseId) throws SQLException {

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = removeCoursePrerequisitePS(conn, Integer.parseInt(courseId), Integer.parseInt(precourseId))) {
            conn.setAutoCommit(true);
            ps.execute();
        }
    }



    public void createAcademicRecord(AcademicRecord academicRecord) throws SQLException {

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = createAcademicRecordPS(conn, academicRecord)
        ) {
            conn.setAutoCommit(true);
            ps.execute();
        }
    }

    /**
     *  get academic record by student ID, confused whether we need it by student if
     */
    public List<AcademicRecord> getAcademicRecord(String studentID) throws SQLException {
        List<AcademicRecord> studentAcademicRecordList = new ArrayList<>();

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = getAcademicRecordPS(conn, Integer.parseInt(studentID));
             ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                AcademicRecord academicRecord = AcademicRecord.builder()
                        .studentID(rs.getString("STUDENTID"))
                        .courseID(rs.getString("COURSEID"))
                        .year(rs.getInt("YEAR"))
                        .term(rs.getString("TERM"))
                        .instructorID(rs.getString("INSTRUCTORID"))
                        .grade(rs.getString("GRADE"))
                        .comment(rs.getString("COMMENT"))
                        .build();

                studentAcademicRecordList.add(academicRecord);
            }
        }
        return studentAcademicRecordList;
    }

    /**
     * get HashMap of course & grade for a student, will be used in validateCourseRequest()
     */
    public List<Course> getStudentCurrentCourses(String studentID) throws SQLException {

        List<Course> currentCourses = new ArrayList<>();

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = DataSourceUtility.getStudentCurrentCourses(conn, Integer.parseInt(studentID));
             ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                Course course = Course.builder()
                        .courseID(rs.getString("COURSEID"))
                        .courseName(rs.getString("COURSETITLE"))
                        .build();
                currentCourses.add(course);

            }
        }
        return  currentCourses;
    }




    public void updateAcademicRecord(AcademicRecord academicRecord, String grade, String comment, String instructorId) throws SQLException {
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = DataSourceUtility.updateAcademicRecord(conn, academicRecord, grade, comment, instructorId)
        ) {
            conn.setAutoCommit(true);
            ps.execute();
        }

    }

    public List<AcademicRecord> getAcademicRecordsForCourse(Integer courseId, String term, Integer year) throws SQLException {
        List<AcademicRecord> records = new ArrayList<>();

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = getCourseRecords(conn, courseId, term, year);
             ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                Student student = Student.builder()
                        .id(rs.getString("ID"))
                        .name(rs.getString("NAME"))
                        .build();
                Course course = Course.builder()
                        .courseID(rs.getString("COURSEID"))
                        .courseName(rs.getString("COURSETITLE"))
                        .build();

                AcademicRecord record = AcademicRecord.builder()
                        .student(student)
                        .studentID(rs.getString("ID"))
                        .courseID(rs.getString("COURSEID"))
                        .course(course)
                        .grade(rs.getString("GRADE"))
                        .term(rs.getString("TERM"))
                        .year(rs.getInt("YEAR"))
                        .comment(rs.getString("COMMENT"))
                        .build();
                records.add(record);
            }
        }

        return records;

    }

    public String getWekaData() throws SQLException {
        StringBuilder sb = new StringBuilder();
        try (
         Connection conn = ds.getConnection();
        PreparedStatement ps = conn.prepareStatement(createWekaCourseRequestQuery());
         ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                        int numberOfColumns = rs.getMetaData().getColumnCount();
                       for (int i = 1; i <= numberOfColumns; i++) {
                               sb.append(rs.getString(i));
                           if (i < numberOfColumns) {
                                        sb.append(", ");
                                   }
                            }
                        sb.append('\n');
                    }
            }
        String data = sb.toString();
        return data;
    }

    public List<Person> getUsers() throws Exception {
        Map<String, Person> userMap = new HashMap<>();
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_GET_ALL_USERS);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String id = rs.getString("UUID");
                if (userMap.containsKey(id)) {
                    Person user = userMap.get(id);
                    user.getRoles().add(rs.getString("ROLE"));
                } else {
                    Person user = new Person(
                            id,
                            rs.getString("NAME"),
                            rs.getString("HOMEADDRESS"),
                            rs.getString("PHONENUMBER"),
                            new ArrayList<>(Collections.singletonList(rs.getString("ROLE"))));
                    userMap.put(id, user);
                }
            }
        }

        return new ArrayList<>(userMap.values());
    }

    public void updateUser(Person person) throws SQLException {
        try (Connection conn = ds.getConnection();
            PreparedStatement ps = updateUserPS(conn, person)) {
            conn.setAutoCommit(true);
            ps.execute();
        }
    }
    public String createWekaCourseRequestQuery() throws SQLException {
        List<Integer> courserequest = new ArrayList<>();
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement("select courseid from CourseRequestWeka  group by courseid");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                courserequest.add(rs.getInt("courseid"));
            }
        }
        String SQL_GET_WEKA="SELECT ";
        int i=0;
        for (int courseid : courserequest) {
            if (i==0){
                SQL_GET_WEKA=SQL_GET_WEKA+ "MAX(case when courseid = "+ courseid + " then 1 else 0 end) AS course" +courseid;
                i=i+1;
            } else {
                SQL_GET_WEKA=SQL_GET_WEKA+ " ,MAX(case when courseid = "+ courseid + " then 1 else 0 end) AS course" +courseid;
                i=i+1;
            }
        }
        SQL_GET_WEKA=SQL_GET_WEKA+ " FROM CourseRequestWeka" + " GROUP BY studentid";

        return SQL_GET_WEKA;

    }



}
