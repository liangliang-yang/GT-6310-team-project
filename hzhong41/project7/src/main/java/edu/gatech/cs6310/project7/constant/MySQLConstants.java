package edu.gatech.cs6310.project7.constant;

/**
 *
 */
public class MySQLConstants {
    // SQL Queries
    public static String SQL_CALL_TRUNCATE_TABLES = "CALL TruncateAllTables()";

    public static String SQL_GET_SEMESTER = "SELECT YEAR, TERM FROM CurrentTerm";
    public static String SQL_TRUNCATE_SEMESTER = "DELETE FROM CurrentTerm WHERE 1 = 1";
    public static String SQL_INSERT_SEMESTER = "INSERT INTO CurrentTerm(TERM, YEAR) VALUES (?, ?)";

    public static String SQL_GET_USER_BY_ID = "SELECT User.UUID, NAME, HOMEADDRESS, PHONENUMBER, UserRole.Role FROM User " +
            "INNER JOIN UserRole " +
            "ON UserRole.UUID = User.UUID " +
            "WHERE User.UUID = ?";
    public static String SQL_GET_ALL_USERS = "SELECT User.UUID, NAME, HOMEADDRESS, PHONENUMBER, UserRole.Role FROM User " +
            "INNER JOIN UserRole " +
            "ON UserRole.UUID = User.UUID ";
    public static String SQL_CREATE_USER = "INSERT INTO User(UUID, NAME, HOMEADDRESS, PHONENUMBER) VALUES (?, ?, ?, ?)";
    public static String SQL_UPDATE_USER = "UPDATE User " +
            "SET NAME = ?, " +
            "HOMEADDRESS = ?, " +
            "PHONENUMBER = ? " +
            "WHERE UUID = ?";



    public static String SQL_UPDATE_COURSETITLE = "UPDATE Course " +
            "SET Course.COURSETITLE = ? " +
            "WHERE Course.COURSEID = ?";

    public static String SQL_CREATE_INSTRUCTOR_STATUS = "INSERT INTO InstructorStatus(INSTRUCTORID, ISHIRED) VALUES (?, ?)";
    public static String SQL_GET_INSTRUCTOR_BY_ID =
            "SELECT USER.UUID AS ID,   " +
                    "USER.NAME AS NAME,   " +
                    "USER.HOMEADDRESS AS ADDRESS,   " +
                    "USER.PHONENUMBER AS NUMBER, " +
                    "ROLE.ROLE AS ROLE, " +
                    "STATUS.ISHIRED AS ISHIRED, " +
                    "C1.COURSEID AS ELIGIBLE_COURSE_ID, " +
                    "C1.COURSETITLE AS ELIGIBLE_COURSE_TITLE, " +
                    "C2.COURSEID AS CURRENT_COURSE_ID, " +
                    "C2.COURSETITLE AS CURRENT_COURSE_TITLE " +
                    "FROM User AS USER  " +
                    "INNER JOIN UserRole AS ROLE " +
                    "     ON USER.UUID = ROLE.UUID " +
                    "INNER JOIN InstructorStatus AS STATUS " +
                    "     ON STATUS.INSTRUCTORID = USER.UUID " +
                    "LEFT JOIN EligibleCourse AS ELIGIBLE " +
                    "     ON ELIGIBLE.INSTRUCTORID = USER.UUID " +
                    "LEFT JOIN Course AS C1 " +
                    "     ON ELIGIBLE.COURSEID = C1.COURSEID " +
                    "LEFT JOIN (SELECT Course.COURSEID, Course.COURSETITLE, INSTRUCTORID " +
                    "     FROM Course " +
                    "INNER JOIN CourseOffering " +
                    "ON Course.courseid = CourseOffering.courseid " +
                    "WHERE CourseOffering.term = (SELECT TERM FROM CurrentTerm LIMIT 1) " +
                    "     AND CourseOffering.year = (SELECT YEAR FROM CurrentTerm LIMIT 1)) AS COURSE_OFFERING " +
                    "     ON COURSE_OFFERING.INSTRUCTORID = USER.UUID " +
                    "LEFT JOIN Course AS C2 " +
                    "     ON COURSE_OFFERING.COURSEID = C2.COURSEID " +
                    "WHERE USER.UUID = ?";

    public static String SQL_UPDATE_INSTRUCTOR_STATUS = "UPDATE InstructorStatus SET ISHIRED = ? WHERE INSTRUCTORID = ?";

    public static String SQL_GET_STUDENT_BY_ID =
        "SELECT USER.UUID AS ID, " +
                "USER.NAME AS NAME, " +
                "USER.HOMEADDRESS AS ADDRESS, " +
                "USER.PHONENUMBER AS NUMBER, " +
                "ROLE.ROLE AS ROLE " +
                "FROM User AS USER " +
                "INNER JOIN UserRole AS ROLE " +
                "     ON USER.UUID = ROLE.UUID " +
                "WHERE USER.UUID = ? " +
                "AND ROLE.ROLE = 'STUDENT'";

    public static String SQL_CREATE_COURSE = "INSERT INTO Course(COURSEID, COURSETITLE) VALUES (?, ?)";
    public static String SQL_GET_COURSE_BY_ID =
            "SELECT C.courseid as COURSEID" +
            "      ,C.coursetitle as COURSETITLE" +
            "      ,PREREQ.COURSEID AS PREREQID" +
            "      ,PREREQ.COURSETITLE AS PREREQTITLE" +
            "      ,GROUP_CONCAT(distinct O.term SEPARATOR ',') as OFFERTERM " +
            "FROM Course AS C " +
            "LEFT JOIN Prerequisite AS P " +
            "ON C.courseid=P.courseid " +
            "LEFT JOIN Course AS PREREQ " +
            "ON PREREQ.COURSEID = P.PRECOURSEID " +
            "LEFT JOIN OfferTerm AS O " +
            "ON O.courseid=C.courseid " +
            "WHERE C.courseid = ? " +
            "GROUP BY C.courseid, C.coursetitle, PREREQ.courseid, PREREQ.coursetitle";

    public static String SQL_GET_PREREQUISITE_BY_ID =
            "SELECT PRECOURSEID FROM Prerequisite WHERE COURSEID = ?";

    public static String SQL_CREATE_COURSE_PREREQ = "INSERT INTO Prerequisite (COURSEID, PRECOURSEID) VALUES (?, ?)";
    public static String SQL_CREATE_OFFER_TERM = "INSERT INTO OfferTerm (COURSEID, TERM) VALUES (?, ?)";

    public static String SQL_CREATE_COURSE_OFFERING = "INSERT INTO CourseOffering (INSTRUCTORID, COURSEID, YEAR, TERM, AVAILABLESEATS) VALUES (?, ?, ?, ?, ?)";
    public static String SQL_GET_CURRENT_COURSE_OFFERINGS =
            "SELECT COURSEID, INSTRUCTORID, YEAR, TERM, AVAILABLESEATS " +
            "FROM CourseOffering " +
                    "WHERE COURSEID = ? " +
                    "AND YEAR = (SELECT YEAR FROM CurrentTerm LIMIT 1) " +
                    "AND TERM = (SELECT TERM FROM CurrentTerm LIMIT 1)";
    public static String SQL_UPDATE_AVAILABLE_SEATS =
            "UPDATE CourseOffering " +
                    "SET AVAILABLESEATS = ? " +
                    "WHERE COURSEID = ? " +
                    "AND INSTRUCTORID = ? " +
                    "AND YEAR = ? " +
                    "AND TERM = ?";
    public static String SQL_DROP_COURSE_OFFERING =
            "DELETE FROM CourseOffering " +
                    "WHERE COURSEID = ? " +
                    "AND INSTRUCTORID = ? " +
                    "AND YEAR = ? " +
                    "AND TERM = ?";
    public static String SQL_GET_STUDENT_CURRENT_COURSES =
            "SELECT COURSE.COURSEID AS COURSEID, COURSE.COURSETITLE AS COURSETITLE " +
                    "FROM Course AS COURSE " +
                    "INNER JOIN AcademicRecord AS RECORD " +
                    "ON RECORD.COURSEID = COURSE.COURSEID " +
                    "WHERE RECORD.STUDENTID = ? " +
                    "AND YEAR = (SELECT YEAR FROM CurrentTerm LIMIT 1) " +
                    "AND TERM = (SELECT TERM FROM CurrentTerm LIMIT 1)";

    public static String SQL_GET_CURRENT_COURSE_REQUESTS =
            "SELECT STUDENTID, COURSEID, YEAR, TERM, STATUS, REQUESTDATE " +
                    "FROM CourseRequest " +
                    "WHERE COURSEID = ? " +
                    "AND YEAR = (SELECT YEAR FROM CurrentTerm LIMIT 1) " +
                    "AND TERM = (SELECT TERM FROM CurrentTerm LIMIT 1)";

    public static String SQL_GET_ALL_COURSES = "SELECT COURSEID, COURSETITLE FROM Course";

    public static String SQL_GET_ALL_STUDENTS =
                    "SELECT USER.UUID AS ID, USER.NAME AS NAME, USER.HOMEADDRESS AS ADDRESS, USER.PHONENUMBER AS NUMBER FROM User AS USER INNER JOIN UserRole AS ROLE ON USER.UUID = ROLE.UUID AND ROLE.ROLE = 'STUDENT'";


    public static String SQL_GET_ALL_INSTRUCTORS =
            "SELECT USER.UUID AS ID, USER.NAME AS NAME, USER.HOMEADDRESS AS ADDRESS, USER.PHONENUMBER AS NUMBER FROM User AS USER INNER JOIN UserRole AS ROLE ON USER.UUID = ROLE.UUID AND ROLE.ROLE = 'INSTRUCTOR'";

    public static String SQL_CREATE_COURSE_REQUEST = "INSERT INTO CourseRequest " +
            "(STUDENTID, COURSEID, YEAR, TERM, STATUS, REQUESTDATE) " +
            "VALUES (?, ?, ?, ?, ?, ?) " +
            "ON DUPLICATE KEY UPDATE " +
            "STATUS = ?, REQUESTDATE = ?";

    public static String SQL_CREATE_WAITLIST = "INSERT INTO WaitList " +
            "(STUDENTID, COURSEID, YEAR, TERM, STATUS, INSERTDATE) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

    public static String SQL_GET_WAITLIST_BY_STUDENTID =
            "SELECT STUDENTID, COURSEID, YEAR, TERM, STATUS, INSERTDATE " +
                    "FROM WaitList " +
                    "WHERE STUDENTID = ? " +
                    "AND STATUS = ?";

    public static String SQL_GET_WAITLIST_BY_COURSEID =
            "SELECT STUDENTID, COURSEID, YEAR, TERM FROM WaitList  WHERE courseid=? AND status='active' ORDER BY insertdate LIMIT 1";

    public static String SQL_GET_COURSE_WAITLIST =
            "SELECT  courseid, COUNT(distinct studentid) as cnt_student_in_waitlist FROM WaitList WHERE STATUS = 'active' GROUP BY courseid " ;

    public static String SQL_UPDATE_WAITLIST_STATUS =
            "UPDATE WaitList SET status='removed' WHERE studentid=? and courseid=? and year=? and term=?";

    public static String SQL_CREATE_USER_ROLE = "INSERT INTO UserRole(UUID, ROLE) VALUES (?, ?)";

    public static String SQL_DELETE_USER_ROLE = "DELETE FROM UserRole " +
                                                "WHERE UUID = ? " +
                                                "AND ROLE = ?";

    public static String SQL_CREATE_ELIGIBLE_COURSE = "INSERT INTO EligibleCourse(INSTRUCTORID, COURSEID) VALUES (?, ?)";


    public static String SQL_DELETE_ELIGIBLE_COURSE = "DELETE FROM EligibleCourse " +
            "WHERE INSTRUCTORID = ? " +
            "AND COURSEID = ?";

    public static String SQL_DELETE_COURSE_PREREQUISITE = "DELETE FROM Prerequisite WHERE COURSEID=? AND PRECOURSEID=?";


    public static String SQL_CREATE_ACADEMIC_RECORD = "INSERT INTO AcademicRecord (STUDENTID, COURSEID, YEAR, TERM, INSTRUCTORID, GRADE, COMMENT) VALUES (?, ?, ?, ?, ?, ?, ?)";

    public static String SQL_GET_ACADEMICRECORD =
            "SELECT STUDENTID, COURSEID, YEAR, TERM, INSTRUCTORID, GRADE, COMMENT " +
                    "FROM AcademicRecord " +
                    "WHERE STUDENTID = ?";

    public static String SQL_GET_MAX_ID_FOR_TABLE = "SELECT %1$s FROM %2$s ORDER BY %1$s DESC LIMIT 1";

    public static String SQL_UPDATE_ACADEMICRECORD ="UPDATE AcademicRecord SET instructorid =? , grade=?, comment=? WHERE courseid=? and studentid=? and year=? and term=? ";

    public static String SQL_GET_COURSE_RECORDS =
            "SELECT STUDENT.UUID AS ID, " +
                    "STUDENT.NAME AS NAME, " +
                    "COURSE.COURSETITLE AS COURSETITLE, " +
                    "COURSE.COURSEID AS COURSEID, " +
                    "AcademicRecord.GRADE AS  GRADE, " +
                    "AcademicRecord.COMMENT AS COMMENT, " +
                    "AcademicRecord.TERM AS TERM, " +
                    "AcademicRecord.YEAR AS YEAR " +
            "FROM AcademicRecord " +
            "INNER JOIN User AS STUDENT ON STUDENT.UUID = AcademicRecord.STUDENTID " +
            "INNER JOIN Course AS COURSE ON COURSE.COURSEID = AcademicRecord.COURSEID " +
            "WHERE AcademicRecord.COURSEID = ? " +
            "AND AcademicRecord.TERM = ? " +
            "AND AcademicRecord.YEAR = ?";

    public static String SQL_GET_WEKA ="SET @sql = NULL; SELECT  GROUP_CONCAT(DISTINCT CONCAT( 'MAX( case when courseid = ', courseid, ' then 1 else 0 end)  AS  ', 'course' , courseid)) INTO @sql FROM CourseRequestWeka;    SET @sql1 = CONCAT('SELECT  ',@sql,' FROM CourseRequestWeka GROUP BY studentid');  PREPARE stmt FROM @sql1; EXECUTE stmt; DEALLOCATE PREPARE stmt;";

    private MySQLConstants() {
        // private constructor for utility classes.
    }
}
