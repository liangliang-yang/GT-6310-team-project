package edu.gatech.cs6310.project7.dao;

import edu.gatech.cs6310.project7.model.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import static edu.gatech.cs6310.project7.constant.MySQLConstants.*;

/**
 *
 */
class DataSourceUtility {

    static PreparedStatement updateCurrentSemesterPS(Connection conn, Integer year, String term) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(SQL_INSERT_SEMESTER);
        ps.setString(1, term);
        ps.setInt(2, year);
        return ps;
    }

    static PreparedStatement insertUserPS(Connection conn,
                                          Integer id,
                                          String name,
                                          String address,
                                          String phoneNumber) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(SQL_CREATE_USER);
        ps.setInt(1, id);
        ps.setString(2, name);
        ps.setString(3, address);
        ps.setString(4, phoneNumber);
        return ps;
    }

    static PreparedStatement getUserByIdPS(Connection conn, Integer id) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(SQL_GET_USER_BY_ID);
        ps.setInt(1, id);
        return ps;
    }

    static PreparedStatement updateUserPS(Connection conn, Person user) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_USER);
        ps.setString(1, user.getName());
        ps.setString(2, user.getAddress());
        ps.setString(3, user.getPhoneNumber());
        ps.setInt(4, Integer.parseInt(user.getId()));
        return ps;
    }



    static PreparedStatement updateCourseTitlePS(Connection conn, Course course) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_COURSETITLE);
        ps.setString(1, course.getCourseName());
        ps.setInt(2, Integer.parseInt(course.getCourseID()));
        return ps;
    }

    static PreparedStatement insertUserRolePS(Connection conn, Integer userId, String role) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(SQL_CREATE_USER_ROLE);
        ps.setInt(1, userId);
        ps.setString(2, role);

        return ps;
    }

    static PreparedStatement insertInstructorStatus(Connection conn,
                                                    Integer instructorId,
                                                    boolean isHired) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(SQL_CREATE_INSTRUCTOR_STATUS);
        ps.setInt(1, instructorId);
        ps.setBoolean(2, isHired);
        return ps;
    }

    static PreparedStatement insertEligibleCourse(Connection conn, Integer instructorId, Integer courseId) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(SQL_CREATE_ELIGIBLE_COURSE);
        ps.setInt(1, instructorId);
        ps.setInt(2, courseId);
        return ps;
    }

    static PreparedStatement deleteEligibleCourse(Connection conn, Integer instructorId, Integer courseId) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(SQL_DELETE_ELIGIBLE_COURSE);
        ps.setInt(1, instructorId);
        ps.setInt(2, courseId);
        return ps;
    }

    static PreparedStatement getInstructorByIdPS(Connection conn, Integer id) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(SQL_GET_INSTRUCTOR_BY_ID);
        ps.setInt(1, id);
        return ps;
    }

    static PreparedStatement getStudentByIdPS(Connection conn, Integer id) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(SQL_GET_STUDENT_BY_ID);
        ps.setInt(1, id);
        return ps;
    }

    static PreparedStatement insertCoursePS(Connection conn, Integer id, String title) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(SQL_CREATE_COURSE);
        ps.setInt(1, id);
        ps.setString(2, title);
        return ps;
    }

    static PreparedStatement getCourseByIdPS(Connection conn, Integer id) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(SQL_GET_COURSE_BY_ID);
        ps.setInt(1, id);
        return ps;
    }

    static PreparedStatement insertCoursePrereqPS(Connection conn, Integer courseId, Integer prereqId) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(SQL_CREATE_COURSE_PREREQ);
        ps.setInt(1, courseId);
        ps.setInt(2, prereqId);
        return ps;
    }

    static PreparedStatement insertOfferTermPS(Connection conn, Integer courseId, String term) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(SQL_CREATE_OFFER_TERM);
        ps.setInt(1, courseId);
        ps.setString(2, term);
        return ps;
    }

    static PreparedStatement updateInstructorStatusPS(Connection conn, Integer id, boolean isHired) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_INSTRUCTOR_STATUS);
        ps.setBoolean(1, isHired);
        ps.setInt(2, id);
        return ps;
    }

    static PreparedStatement createCourseOfferingPS(Connection conn, CourseOffering courseOffering) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(SQL_CREATE_COURSE_OFFERING);
        ps.setInt(1, Integer.parseInt(courseOffering.getInstructorID()));
        ps.setInt(2, Integer.parseInt(courseOffering.getCourseID()));
        ps.setInt(3, courseOffering.getYear());
        ps.setString(4, courseOffering.getTerm());
        ps.setInt(5, courseOffering.getAvailableSeats());
        return ps;
    }

    static PreparedStatement createCourseRequestPS(Connection conn, CourseRequest courseRequest) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(SQL_CREATE_COURSE_REQUEST);
        ps.setInt(1, Integer.parseInt(courseRequest.getStudentID()));
        ps.setInt(2, Integer.parseInt(courseRequest.getCourseID()));
        ps.setInt(3, courseRequest.getYear());
        ps.setString(4, courseRequest.getTerm());
        ps.setString(5, courseRequest.getStatus());
        ps.setTimestamp(6, courseRequest.getRequestDate());
        ps.setString(7, courseRequest.getStatus());
        ps.setTimestamp(8, courseRequest.getRequestDate());
        return ps;
    }

    static PreparedStatement createWaitListPS(Connection conn, CourseRequest courseRequest) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(SQL_CREATE_WAITLIST);
        ps.setInt(1, Integer.parseInt(courseRequest.getStudentID()));
        ps.setInt(2, Integer.parseInt(courseRequest.getCourseID()));
        ps.setInt(3, courseRequest.getYear());
        ps.setString(4, courseRequest.getTerm());
        ps.setString(5, "active");
        ps.setTimestamp(6, courseRequest.getRequestDate());
        return ps;
    }

    static PreparedStatement getCourseWaitListPS(Connection conn) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(SQL_GET_COURSE_WAITLIST);
        return ps;
    }

    static PreparedStatement getWaitListbyStudentId(Connection conn, Integer studentId) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(SQL_GET_WAITLIST_BY_STUDENTID);
        ps.setInt(1, studentId);
        ps.setString(2, "active");
        return ps;
    }

    static PreparedStatement getCurrentCourseOfferings(Connection conn, Integer courseId) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(SQL_GET_CURRENT_COURSE_OFFERINGS);
        ps.setInt(1, courseId);
        return ps;
    }

    static PreparedStatement updateAvailableSeats(Connection conn, CourseOffering courseOffering) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_AVAILABLE_SEATS);
        ps.setInt(1, courseOffering.getAvailableSeats());
        ps.setInt(2, Integer.parseInt(courseOffering.getCourseID()));
        ps.setInt(3, Integer.parseInt(courseOffering.getInstructorID()));
        ps.setInt(4, courseOffering.getYear());
        ps.setString(5, courseOffering.getTerm());
        return ps;
    }

    static PreparedStatement dropCourseOffering(Connection conn, CourseOffering offering) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(SQL_DROP_COURSE_OFFERING);
        ps.setInt(1,Integer.parseInt(offering.getCourseID()));
        ps.setInt(2, Integer.parseInt(offering.getInstructorID()));
        ps.setInt(3, offering.getYear());
        ps.setString(4, offering.getTerm());
        return ps;
    }

    static PreparedStatement getCurrentCourseRequests(Connection conn, Integer courseId) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(SQL_GET_CURRENT_COURSE_REQUESTS);
        ps.setInt(1, courseId);
        return ps;
    }

    static PreparedStatement addUserRolePS(Connection conn, int userId, String role) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(SQL_CREATE_USER_ROLE);
        ps.setInt(1, userId);
        ps.setString(2, role);
        return ps;
    }

    static PreparedStatement removeUserRolePS(Connection conn, int userId, String role) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(SQL_DELETE_USER_ROLE);
        ps.setInt(1, userId);
        ps.setString(2, role);
        return ps;
    }

    static PreparedStatement removeEligibleCoursePS(Connection conn, int instructorId, int courseId) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(SQL_DELETE_ELIGIBLE_COURSE);
        ps.setInt(1, instructorId);
        ps.setInt(2, courseId);
        return ps;
    }

    static PreparedStatement removeCoursePrerequisitePS(Connection conn, int courseId, int precourseId) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(SQL_DELETE_COURSE_PREREQUISITE);
        ps.setInt(1, courseId);
        ps.setInt(2, precourseId);
        return ps;
    }

    static PreparedStatement createAcademicRecordPS(Connection conn, AcademicRecord academicRecord) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(SQL_CREATE_ACADEMIC_RECORD);
        ps.setInt(1, Integer.parseInt(academicRecord.getStudentID()));
        ps.setInt(2, Integer.parseInt(academicRecord.getCourseID()));
        ps.setInt(3, academicRecord.getYear());
        ps.setString(4, academicRecord.getTerm());
        ps.setInt(5,  Integer.parseInt(academicRecord.getInstructorID()));
        ps.setString(6, academicRecord.getGrade());
        ps.setString(7, academicRecord.getComment());
        return ps;
    }

    static PreparedStatement getAcademicRecordPS(Connection conn, Integer studentId) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(SQL_GET_ACADEMICRECORD);
        ps.setInt(1, studentId);
        return ps;
    }

    static PreparedStatement updateAcademicRecord(Connection conn, AcademicRecord academicRecord, String grade, String comment, String instructorId) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_ACADEMICRECORD);
        if (instructorId != null) {
            ps.setInt(1, Integer.parseInt(instructorId));
        } else {
            ps.setNull(1, Types.INTEGER);
        }
        ps.setString(2, grade);
        ps.setString(3,comment);
        ps.setInt(4,Integer.parseInt(academicRecord.getCourseID()));
        ps.setInt(5,Integer.parseInt(academicRecord.getStudentID()));
        ps.setInt(6,academicRecord.getYear());
        ps.setString(7,academicRecord.getTerm());
        return ps;
    }

    static PreparedStatement getStudentCurrentCourses(Connection conn, Integer studentId) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(SQL_GET_STUDENT_CURRENT_COURSES);
        ps.setInt(1, studentId);
        return ps;
    }

    static PreparedStatement getCourseRecords(Connection conn, Integer courseId, String term, Integer year) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(SQL_GET_COURSE_RECORDS);
        ps.setInt(1, courseId);
        ps.setString(2, term);
        ps.setInt(3, year);
        return ps;
    }

    static PreparedStatement getWaitlistByCoureIdPS(Connection conn, Integer courseId) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(SQL_GET_WAITLIST_BY_COURSEID);
        ps.setInt(1, courseId);
        return ps;
    }

    static PreparedStatement updateWaitListStatusPS(Connection conn, Integer studentid, Integer courseid, Integer year, String term) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_WAITLIST_STATUS);
        ps.setInt(1,studentid);
        ps.setInt(2,courseid);
        ps.setInt(3,year);
        ps.setString(4,term);

        return ps;
    }
}
