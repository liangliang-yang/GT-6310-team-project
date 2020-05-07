package edu.gatech.cs6310.project7.service;

import edu.gatech.cs6310.project7.dao.UniversityDataSource;
import edu.gatech.cs6310.project7.exception.UniversitySystemException;
import edu.gatech.cs6310.project7.model.AcademicRecord;
import edu.gatech.cs6310.project7.model.CommandResult;
import edu.gatech.cs6310.project7.model.Course;
import edu.gatech.cs6310.project7.model.CourseOffering;
import edu.gatech.cs6310.project7.model.CourseRequest;
import edu.gatech.cs6310.project7.model.Instructor;
import edu.gatech.cs6310.project7.model.Person;
import edu.gatech.cs6310.project7.model.Semester;
import edu.gatech.cs6310.project7.model.Student;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import weka.associations.Apriori;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;
import weka.filters.unsupervised.attribute.RemoveType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Created by student on 7/18/17.
 */
@Slf4j
@Service
public class UniversitySystem {

    private UniversityDataSource dataSource;

    @Autowired
    public UniversitySystem(final UniversityDataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Truncates all tables and then initializes database from files. Sets the current semester to Summer 2017.
     */
    public CommandResult initializeData() {
        CommandResult result;

        try {
            dataSource.truncateTables();
            Semester semester = Semester.builder().term("Summer").year(2017).build();
            dataSource.updateCurrentSemester(semester);

            Instructor emptyInstructor = Instructor.builder()
                    .id("0")
                    .isActive(false)
                    .name("SYSTEM")
                    .address("NA")
                    .phoneNumber("NA")
                    .build();
            dataSource.createInstructors(emptyInstructor);
            parseStudentInfo();
            parseInstructorInfo();
            parseCourseInfo();
            parseEligibleCourseInfo();
            parsePrereqsCourseInfo();
            parseTermInfo();

            result = CommandResult.builder()
                    .status(true)
                    .message("Data has been successfully initialized.")
                    .build();

        } catch (SQLException | UncheckedIOException e) {
            log.error("Error occurred while initializing data.", e);
            result = CommandResult.builder()
                    .status(false)
                    .results(Collections.singletonMap("exception", e))
                    .message(e.getMessage())
                    .build();
        }
        return result;
    }

    /**
     * Get the Current Semester from the CurrentTerm table
     */
    public CommandResult getCurrentSemester() {
        CommandResult result;
        try {
            result = CommandResult.builder()
                    .status(true)
                    .results(Collections.singletonMap("semester", dataSource.getCurrentSemester()))
                    .build();
        } catch (SQLException e) {
            log.error("Exception while getting current semester.", e);
            result = CommandResult.builder()
                    .status(false)
                    .results(Collections.singletonMap("exception", e))
                    .message(e.getMessage())
                    .build();
        }
        return result;
    }

    /**
     * Perform actions to start the next term and update the CurrentTerm table.
     */
    public CommandResult goToNextSemester() {
        try {
            Semester currentSemester = dataSource.getCurrentSemester();
            switch (currentSemester.getTerm()) {
                case "Fall":
                    currentSemester.setTerm("Winter");
                    break;
                case "Winter":
                    currentSemester.setTerm("Spring");
                    break;
                case "Spring":
                    currentSemester.setTerm("Summer");
                    break;
                case "Summer":
                    currentSemester.setTerm("Fall");
                    currentSemester.setYear(currentSemester.getYear() + 1);
                    break;
            }

            dataSource.updateCurrentSemester(currentSemester);

            return CommandResult.builder()
                    .status(true)
                    .results(Collections.singletonMap("semester", currentSemester))
                    .message("Next term start: " + currentSemester.getTerm() + " " + currentSemester.getYear())
                    .build();
        } catch (SQLException e) {
            log.error("Exception while starting next semester.", e);
            return CommandResult.builder()
                    .status(false)
                    .results(Collections.singletonMap("exception", e))
                    .message(e.getMessage())
                    .build();
        }

    }

    public CommandResult addUserRole(String userId, String role) {
        try {
            dataSource.addUserRole(userId, role);
            return CommandResult.builder()
                    .status(true)
                    .message(userId + " is now a " + role)
                    .build();
        } catch (SQLException e) {
            log.error("Exception while adding user role", e);
            return CommandResult.builder()
                    .status(false)
                    .message(e.getMessage())
                    .results(Collections.singletonMap("error", e))
                    .build();
        }
    }

    public CommandResult removeUserRole(String userId, String role) {
        try {
            dataSource.removeUserRole(userId, role);
            return CommandResult.builder()
                    .status(true)
                    .message(role + " role removed for " + userId)
                    .build();
        } catch (SQLException e) {
            log.error("Exception while removing user role", e);
            return CommandResult.builder()
                    .status(false)
                    .message(e.getMessage())
                    .results(Collections.singletonMap("error", e))
                    .build();
        }
    }

    public CommandResult addStudent(Person student){
        try {
            if (student.getId() != null) {
                Student duplicate = dataSource.getStudentById(student.getId());
                if (duplicate != null) {
                    return CommandResult.builder()
                            .status(false)
                            .message("# ERROR: Duplicate ID found.")
                            .results(Collections.singletonMap("user", duplicate))
                            .build();
                }
            } else {
                Integer id = dataSource.getMaxIdForTable("UUID", "User") + 1;
                student.setId(id.toString());
            }

            dataSource.createStudents((Student) student);
            return CommandResult.builder()
                    .status(true)
                    .message("Student with ID " +student.getId() +" is successfully added")
                    .results(Collections.singletonMap("student", student))
                    .build();
        } catch (SQLException e) {
            log.error("Exception while adding student.", e);
            return CommandResult.builder()
                    .status(false)
                    .message(e.getMessage())
                    .build();
        }
    }

    public CommandResult addCourse(Course course){
        try {
            if (course.getCourseID() != null) {
                Course duplicate = dataSource.getCourseById(Integer.parseInt(course.getCourseID()));
                // 1. valid whether the student exists
                if(duplicate != null){
                    return CommandResult.builder()
                            .status(false)
                            .message("# ERROR: Duplicate ID found.")
                            .results(Collections.singletonMap("course", duplicate))
                            .build();
                }
            } else {
                Integer id = dataSource.getMaxIdForTable("COURSEID", "Course") + 1;
                course.setCourseID(id.toString());
            }

            dataSource.createCourses(course);
            return CommandResult.builder()
                    .message("Course with ID " +course.getCourseID() +" is successfully added")
                    .results(Collections.singletonMap("course", course))
                    .status(true)
                    .build();
        } catch (SQLException e) {
            log.error("Exception while adding course.", e);
            return CommandResult.builder()
                    .status(false)
                    .message(e.getMessage())
                    .build();
        }
    }

    public CommandResult getAllCourses() {
        try {
            List<Course> courses = dataSource.getAllCourses();
            if (courses.isEmpty()) {
                return CommandResult.builder()
                        .status(false)
                        .message("No courses found.")
                        .results(Collections.singletonMap("courses", courses))
                        .build();
            }

            courses = courses.stream()
                    .sorted(Comparator.comparingInt(c -> Integer.parseInt(c.getCourseID())))
                    .collect(Collectors.toList());
            return CommandResult.builder()
                    .status(true)
                    .results(Collections.singletonMap("courses", courses))
                    .build();
        } catch (SQLException e) {
            log.error("Exception while getting all courses.", e);
            return CommandResult.builder()
                    .status(false)
                    .message(e.getMessage())
                    .results(Collections.singletonMap("error", e))
                    .build();
        }
    }

    public CommandResult getAllInstructors() {
        try {
            List<Instructor> instructors = dataSource.getAllInstructors();
            if (instructors.isEmpty()) {
                return CommandResult.builder()
                        .status(false)
                        .message("No instructors found.")
                        .results(Collections.singletonMap("instructors", instructors))
                        .build();
            }

            instructors = instructors.stream()
                    .sorted(Comparator.comparingInt(i -> Integer.parseInt(i.getId())))
                    .collect(Collectors.toList());

            return CommandResult.builder()
                    .status(true)
                    .results(Collections.singletonMap("instructors", instructors))
                    .build();
        } catch (SQLException e) {
            log.error("Exception while getting all instructors.", e);
            return CommandResult.builder()
                    .status(false)
                    .message(e.getMessage())
                    .results(Collections.singletonMap("error", e))
                    .build();
        }
    }

    public CommandResult getAllStudents() {
        try {
            List<Student> students = dataSource.getAllStudents();
            if (students.isEmpty()) {
                return CommandResult.builder()
                        .status(false)
                        .message("No students found.")
                        .results(Collections.singletonMap("students", students))
                        .build();
            }

            students = students.stream().sorted(Comparator.comparingInt(s -> Integer.parseInt(s.getId()))).collect(Collectors.toList());

            return CommandResult.builder()
                    .status(true)
                    .results(Collections.singletonMap("students", students))
                    .build();
        } catch (SQLException e) {
            log.error("Exception while getting all students.", e);
            return CommandResult.builder()
                    .status(false)
                    .message(e.getMessage())
                    .results(Collections.singletonMap("error", e))
                    .build();
        }
    }

    public CommandResult getCourseById(Integer id) {
        try {
            Course course = dataSource.getCourseById(id);
            if (course == null) {
                return CommandResult.builder()
                        .status(false)
                        .message("Course not found")
                        .build();
            }
            return CommandResult.builder()
                    .status(true)
                    .results(Collections.singletonMap("course", course))
                    .build();
        } catch (Exception e) {
            log.error("Error getting course {}", id, e);
            return CommandResult.builder()
                    .message(e.getMessage())
                    .status(false)
                    .build();
        }
    }

    public CommandResult getStudentById(String id) {
        try {
            CommandResult commandResult = CommandResult.builder().build();
            String message = "";
            Student student = dataSource.getStudentById(id);
            if (student == null) {
                commandResult.setStatus(false);
                message = "# ERROR: Student does not exist";
                commandResult.setMessage(message);
            } else {
                Map<String, Object> results = new HashMap<>();

                List<AcademicRecord> records = dataSource.getAcademicRecord(id).stream()
                        .sorted((r1, r2) -> {
                            int yearDiff = r2.getYear() - r1.getYear();
                            if (yearDiff != 0) {
                                return yearDiff;
                            }

                            int r1Term = "Fall".equals(r1.getTerm()) ? 1
                                    : "Winter".equals(r1.getTerm()) ? 2
                                    : "Spring".equals(r1.getTerm()) ? 3
                                    : "Summer".equals(r1.getTerm()) ? 4
                                    : 0;
                            int r2Term = "Fall".equals(r2.getTerm()) ? 1
                                    : "Winter".equals(r2.getTerm()) ? 2
                                    : "Spring".equals(r2.getTerm()) ? 3
                                    : "Summer".equals(r2.getTerm()) ? 4
                                    : 0;
                            return r2Term - r1Term;
                        }).collect(Collectors.toList());

                List<Course> currentCourses = dataSource.getStudentCurrentCourses(id).stream()
                        .sorted(Comparator.comparing(Course::getCourseID))
                        .collect(Collectors.toList());

                results.put("academicRecord", records);
                results.put("student", student);
                results.put("currentCourses", currentCourses);


                commandResult.setStatus(true);
                commandResult.setResults(results);
            }
            return commandResult;
        } catch (SQLException e) {
            log.error("Error getting student {}", id, e);
            return CommandResult.builder()
                    .message(e.getMessage())
                    .status(false)
                    .build();
        }
    }

    public CommandResult getInstructor(String id) {
        CommandResult commandResult = CommandResult.builder().build();
        String message;
        try {
            Instructor instructor = dataSource.getInstructorById(id);
            if(instructor==null){
                commandResult.setStatus(false);
                message = "# ERROR: Instructor does not exist";
                commandResult.setMessage(message);
            }

            else {
                commandResult.setStatus(true);
                commandResult.setResults((Collections.singletonMap(id, instructor)));
            }
            return commandResult;
        } catch (SQLException e) {
            log.error("Exception while getting instructor.", e);
            return CommandResult.builder()
                    .status(false)
                    .message(e.getMessage())
                    .build();
        }

    }

    public CommandResult getCourseWaitList(String courseId) {
        CommandResult commandResult = CommandResult.builder().build();
        String message = "";
//        List<CourseRequest> courseWaitList = dataSource.getCourseWaitList(courseID);
        List<CourseRequest> courseWaitList = Collections.emptyList();
        if(courseWaitList.size()==0){
            commandResult.setStatus(false);
            message = "# ERROR: Course does not have waitlist";
            commandResult.setMessage(message);
        }

        else {
            commandResult.setStatus(true);
            commandResult.setResults((Collections.singletonMap("courseID", courseWaitList)));
        }
        return commandResult;
    }

    public CommandResult addInstructor(Instructor instructor){
        try {
            if (instructor.getId() != null) {
                Instructor duplicate = dataSource.getInstructorById(instructor.getId());
                // 1. valid whether the student exists
                if(duplicate != null){
                    return CommandResult.builder()
                            .status(false)
                            .message("# ERROR: Duplicate ID found.")
                            .results(Collections.singletonMap("user", duplicate))
                            .build();
                }
            } else {
                Integer id = dataSource.getMaxIdForTable("UUID", "User") + 1;
                instructor.setId(id.toString());
            }

            dataSource.createInstructors(instructor);

            return CommandResult.builder()
                    .status(true)
                    .message("Instructor with ID " +instructor.getId() +" is successfully added")
                    .results(Collections.singletonMap("user", instructor))
                    .build();

        } catch (SQLException e) {
            log.error("Exception while adding instructor.", e);
            return CommandResult.builder()
                    .status(false)
                    .message(e.getMessage())
                    .build();
        }

    }



    public CommandResult updateInstructorStatus(String id, boolean isHired) {
        try {
            Instructor instructor = dataSource.getInstructorById(id);
            if(instructor == null){
                return CommandResult.builder()
                        .status(false)
                        .message("# ERROR: Instructor does not exist")
                        .build();
            }

            // If instructor is going to be on leave and leave is not allowed, do not allow instructor to take leave.
            if (!isHired && !validDropCourse(instructor)) {
                return CommandResult.builder()
                        .status(false)
                        .message("Instructor is ineligible to take leave.")
                        .build();
            }

            dataSource.updateInstructorStatus(id, isHired);
            return CommandResult.builder()
                    .status(true)
                    .message("Instructor status updated.")
                    .build();
        } catch (SQLException e) {
            log.error("Exception while getting instructor.", e);
            return CommandResult.builder()
                    .status(false)
                    .message(e.getMessage())
                    .build();
        }
    }

    public CommandResult updateEligibleCourses(Instructor instructor) {
        try {
            Instructor currentInstructor = dataSource.getInstructorById(instructor.getId());

            List<String> updatedIds = instructor.getEligibleCourses().stream()
                    .map(Course::getCourseID)
                    .collect(Collectors.toList());
            List<String> currentIds = currentInstructor.getEligibleCourses().stream()
                    .map(Course::getCourseID)
                    .collect(Collectors.toList());

            List<String> newCourses = updatedIds.stream()
                    .filter(courseId -> !currentIds.contains(courseId))
                    .collect(Collectors.toList());
            List<String> deletedCourses = currentIds.stream()
                    .filter(courseId -> !updatedIds.contains(courseId))
                    .collect(Collectors.toList());

            dataSource.addEligibleCourse(instructor.getId(), newCourses.toArray(new String[newCourses.size()]));
            dataSource.removeEligibleCourse(instructor.getId(), deletedCourses.toArray(new String[deletedCourses.size()]));

            return CommandResult.builder()
                    .status(true)
                    .message("Successfully updated eligible courses")
                    .build();
        } catch (Exception e) {
            log.error("Error occurred while updating eligible courses.", e);
            return CommandResult.builder()
                    .status(false)
                    .message(e.getMessage())
                    .build();
        }
    }

    public CommandResult assignGrade(AcademicRecord academicRecord) {

        try {
            CommandResult commandResult = CommandResult.builder().build();


            dataSource.updateAcademicRecord(academicRecord, academicRecord.getGrade(), academicRecord.getComment(), academicRecord.getInstructorID());
            commandResult.setStatus(true);
            commandResult.setResults(Collections.singletonMap("record", academicRecord));
            return commandResult;
        } catch (SQLException e) {
            log.error("Error occurred while assigning grade.", e);
            return CommandResult.builder()
                    .status(false)
                    .message(e.getMessage())
                    .build();
        }
    }

    public CommandResult createCourseOffering(String instructorID, String courseID){
        Semester currentSemester;

        try {
            currentSemester = dataSource.getCurrentSemester();
            CommandResult commandResult = CommandResult.builder().build();

            CourseOffering courseOffering = CourseOffering.builder()
                    .year(currentSemester.getYear())
                    .term(currentSemester.getTerm())
                    .instructorID(instructorID)
                    .courseID(courseID)
                    .availableSeats(3)
                    .build();

            Instructor instructor = dataSource.getInstructorById(instructorID);
            List<String> eligibleCourses = instructor.getEligibleCourses().stream()
                    .map(Course::getCourseID)
                    .collect(Collectors.toList());
            String errorMessage;

            // 1. valid whether the instructor is hired
            if(!instructor.isActive()){
                commandResult.setStatus(false);
                errorMessage = "# ERROR: instructor is not working";
                commandResult.setMessage(errorMessage);
                return commandResult;

            }

            // 2. valid whether the instructor is eligible to teach the course
            else if(!eligibleCourses.contains(courseID)){
                commandResult.setStatus(false);
                errorMessage = "# ERROR: instructor is not eligible to teach this course";
                commandResult.setMessage(errorMessage);
                return commandResult;

            }

            // 3. valid whether the instructor is already teaching a different course
            else if(instructor.getCourseCurrentTeaching() != null){
                commandResult.setStatus(false);
                errorMessage = "# ERROR: instructor is already teaching a different course";
                commandResult.setMessage(errorMessage);
                return commandResult;

            }

            // 4. request successful
            else{
                dataSource.createCourseOffering(courseOffering);

                int seats = 3;
                for (int i=0; i<seats; i++){
                    dataSource.updateWaitListStatus(courseID);
                }



                while (courseOffering.getAvailableSeats() > 0 && updateFromWaitList(courseID)) {
                    courseOffering.setAvailableSeats(courseOffering.getAvailableSeats() - 1);
                }

                commandResult.setStatus(true);
                commandResult.setResults(Collections.singletonMap("courseOffering", courseOffering));

                return commandResult;
            }
        } catch (SQLException e) {
            log.error("Error while validating teach request.", e);
            return CommandResult.builder()
                    .status(false)
                    .results(Collections.singletonMap("exception", e))
                    .message(e.getMessage())
                    .build();
        }

    }

    public CommandResult getCourseOfferingByCourseId(String courseId) {
        try {
            List<CourseOffering> offerings = dataSource.getCurrentCourseOfferingList(courseId);
            return CommandResult.builder()
                    .status(true)
                    .results(Collections.singletonMap("courseOfferings", offerings))
                    .build();
        } catch (Exception e) {
            log.error("An error occurred while getting course offerings for " + courseId, e);
            return CommandResult.builder()
                    .status(false)
                    .message(e.getMessage())
                    .results(Collections.singletonMap("error", e))
                    .build();
        }
    }

    public CommandResult dropCourseOffering(String instructorId) {
        try {
            Instructor instructor = dataSource.getInstructorById(instructorId);

            if (validDropCourse(instructor)) {
                return CommandResult.builder()
                        .status(true)
                        .message("Course Offering dropped successfully.")
                        .build();
            } else {
                return CommandResult.builder()
                        .status(false)
                        .message("Instructor is not eligible to drop the course.")
                        .build();
            }
        } catch (Exception e) {
            log.error("An error occurred whiled attempting to drop course offering.", e);
            return CommandResult.builder()
                    .status(false)
                    .message(e.getMessage())
                    .results(Collections.singletonMap("error", e))
                    .build();
        }
    }

    /**
     * The validateCourseRequest() function is used to validate course enrollment request by student
     */
    public CommandResult validateCourseRequest(CourseRequest courseRequest){

        try {
            CourseRequest duplicateRequest = dataSource.getCurrentCourseRequestsForCourse(Integer.parseInt(courseRequest.getCourseID()))
                    .stream()
                    .filter(r -> r.getStudentID().equals(courseRequest.getStudentID()))
                    .findFirst()
                    .orElse(null);

            CourseRequest request = duplicateRequest == null ? courseRequest : duplicateRequest;

            if (request.getStatus() != null
                && Integer.parseInt(request.getStatus()) > 0) {
                return CommandResult.builder()
                        .message("Student is already enrolled.")
                        .results(Collections.singletonMap("request", request))
                        .build();
            }

            String studentID = request.getStudentID();
            String courseID = request.getCourseID();
            request.setRequestDate(Timestamp.from(Instant.now(Clock.systemUTC())));

            Integer status;
            CommandResult commandResult = CommandResult.builder()
                    .results(Collections.singletonMap("request", request))
                    .build();

            // 1. validate whether the course has been passed or not before, or already enrolled or not
            List<AcademicRecord> studentRecords = dataSource.getAcademicRecord(studentID);
            Course course = dataSource.getCourseById(Integer.parseInt(courseID));

            String errorMessage;

            for(AcademicRecord studentRecord : studentRecords){
                if(studentRecord.getCourseID().equals(courseID)){
                    String grade = studentRecord.getGrade();
                    // (1.1). check if the student has already passed the course
                    if (grade.equals("A") || grade.equals("B") || grade .equals("C")){
                        status = -100;
                        request.setStatus(status);
                        dataSource.createCourseRequest(request);
                        commandResult.setStatus(false);
                        errorMessage = "# not enrolled: course already passed before";
                        commandResult.setMessage(errorMessage);
                        return commandResult;
                    }

                    // (1.2) check if the student has already enrolled or not
                    else if (grade.equals("_")){
                        status = -200;
                        request.setStatus(status);
                        dataSource.createCourseRequest(request);
                        commandResult.setStatus(false);
                        errorMessage = "# student already enrolled in course";
                        commandResult.setMessage(errorMessage);
                        return commandResult;
                    }
                }
            }


            // 2. may need to check for the terms.csv, for termsOffered of the course, not clear yet


            // 3. validate the course prerequisites
            List<String> passingGrades = Arrays.asList("A", "B", "C");

            for(Course prereqsCourse : course.getPrereqsCourses()) {
                List<AcademicRecord> records = studentRecords.stream()
                        .filter(record -> record.getCourseID().equals(prereqsCourse.getCourseID())
                                && passingGrades.contains(record.getGrade()))
                        .collect(Collectors.toList());
                if (records.isEmpty()) {
                    status = -300;
                    request.setStatus(status);
                    dataSource.createCourseRequest(request);
                    commandResult.setStatus(false);
                    errorMessage = "# not enrolled: missing prerequisites";
                    commandResult.setMessage(errorMessage);
                    return commandResult;
                }
            }


            // 4. validate whether the course has been signed up by an instructor / current offering
            List<CourseOffering> currentCourseOfferingList;
            try {
                currentCourseOfferingList = dataSource.getCurrentCourseOfferingList(courseID);
            } catch (SQLException e) {
                return CommandResult.builder()
                        .status(false)
                        .message("Error occurred while updating available seats.")
                        .results(Collections.singletonMap("error", e.getMessage()))
                        .build();
            }

            if(currentCourseOfferingList == null || currentCourseOfferingList.isEmpty()){
                boolean hasWaitList = false;
                List<CourseRequest> studentWaitList = dataSource.getWaitListByStudentId(studentID);
                if (studentWaitList != null && !studentWaitList.isEmpty()) {
                    for (CourseRequest waitlist : studentWaitList) {
                        if (courseID.equals(waitlist.getCourseID())) {
                            hasWaitList = true;
                            break;
                        }
                    }
                }

                if (!hasWaitList) {
                    dataSource.createWaitList(courseRequest);
                }
                status = -400;
                request.setStatus(status);
                dataSource.createCourseRequest(request);
                commandResult.setStatus(false);
                errorMessage = "# not enrolled: no available seats";
                commandResult.setMessage(errorMessage);
                return commandResult;
            }


            // 5. validate available seats, as discussed by piazza @167, same output as before
            // https://piazza.com/class/j2pi7gp4xe35ql?cid=167
            // need to consider the case that there are multiple instructors teach the same course in a semester
            int availableSeats = 0;

            for (CourseOffering courseOffering : currentCourseOfferingList) {
                availableSeats += courseOffering.getAvailableSeats();
            }

            if(availableSeats <= 0){
                boolean hasWaitList = false;
                List<CourseRequest> studentWaitList = dataSource.getWaitListByStudentId(studentID);
                if (studentWaitList != null && !studentWaitList.isEmpty()) {
                    for (CourseRequest waitlist : studentWaitList) {
                        if (courseID.equals(waitlist.getCourseID())) {
                            hasWaitList = true;
                            break;
                        }
                    }
                }

                if (!hasWaitList) {
                    dataSource.createWaitList(courseRequest);
                }

                status = -500;
                request.setStatus(status);
                dataSource.createCourseRequest(request);
                dataSource.createWaitList(request);
                commandResult.setStatus(false);
                errorMessage = "# not enrolled: no available seats";
                commandResult.setMessage(errorMessage);
                return commandResult;
            }

            // course is not full, can be enrolled
            else{
                Optional<CourseOffering> optional = currentCourseOfferingList.stream()
                        .max(Comparator.comparingInt(CourseOffering::getAvailableSeats));
                CourseOffering courseOffering = null;

                // If max not returned, get the first non-zero course offering
                if (!optional.isPresent()) {
                    for (CourseOffering co : currentCourseOfferingList) {
                        if (co.getAvailableSeats() > 0) {
                            courseOffering = co;
                            break;
                        }
                    }
                } else {
                    courseOffering = optional.get();
                }

                if (courseOffering == null || courseOffering.getAvailableSeats() < 1) {
                    boolean hasWaitList = false;
                    List<CourseRequest> studentWaitList = dataSource.getWaitListByStudentId(studentID);
                    if (studentWaitList != null && !studentWaitList.isEmpty()) {
                        for (CourseRequest waitlist : studentWaitList) {
                            if (courseID.equals(waitlist.getCourseID())) {
                                hasWaitList = true;
                                break;
                            }
                        }
                    }

                    if (!hasWaitList) {
                        dataSource.createWaitList(courseRequest);
                    }
                    status = -500;
                    request.setStatus(status);
                    dataSource.createCourseRequest(request);
                    return CommandResult.builder()
                            .status(false)
                            .message("# not enrolled: no available seats")
                            .results(Collections.singletonMap("request", request))
                            .build();
                }

                // Remove one seat from the selected offering
                courseOffering.setAvailableSeats(courseOffering.getAvailableSeats() - 1);

                // (5.2). create and pass the successfull CourseRequest to the data source
                // here use the related courseoffering instructorID as status, for later reference
                status = Integer.valueOf(courseOffering.getInstructorID());
                request.setStatus(status);
                try {
                    dataSource.createCourseRequest(request);

                    AcademicRecord record = AcademicRecord.builder()
                            .courseID(courseID)
                            .studentID(studentID)
                            .term(request.getTerm())
                            .year(request.getYear())
                            .instructorID(courseOffering.getInstructorID())
                            .grade("_")
                            .build();
                    dataSource.createAcademicRecord(record);
                    dataSource.updateAvailableSeats(courseOffering);
                } catch (SQLException e) {
                    return CommandResult.builder()
                            .status(false)
                            .message("Error occurred while updating available seats.")
                            .results(Collections.singletonMap("error", e.getMessage()))
                            .build();
                }
                commandResult.setStatus(true);
                return commandResult;
            }
        } catch (SQLException e) {
            log.error("Error while requesting course.", e);
            return CommandResult.builder()
                    .status(false)
                    .results(Collections.singletonMap("exception", e))
                    .message(e.getMessage())
                    .build();
        }
    }

    public CommandResult studentDropCourse(AcademicRecord record) {
        try {
            dataSource.updateAcademicRecord(record, "I", "", "0");
            CourseOffering offering = dataSource.getCurrentCourseOfferingList(record.getCourseID())
                    .stream()
                    .min(Comparator.comparingInt(CourseOffering::getAvailableSeats))
                    .orElseThrow(() -> new SQLException("Unable to find course offering with least available seats"));
            if (!updateFromWaitList(offering.getCourseID())) {
                offering.setAvailableSeats(offering.getAvailableSeats() + 1);
                dataSource.updateAvailableSeats(offering);
            }

            return CommandResult.builder()
                    .status(true)
                    .message("Course successfully dropped.")
                    .build();

        } catch (SQLException e) {
            log.error("Error while dropping course.", e);
            return CommandResult.builder()
                    .status(false)
                    .results(Collections.singletonMap("exception", e))
                    .message(e.getMessage())
                    .build();
        }
    }

    public CommandResult getAcademicRecordsForCourse(Integer courseId, String term, Integer year) {
        try {
            List<AcademicRecord> records = dataSource.getAcademicRecordsForCourse(courseId, term, year)
                    .stream()
                    .sorted(Comparator.comparing(AcademicRecord::getStudentID))
                    .collect(Collectors.toList());
            if (records.isEmpty()) {
                return CommandResult.builder()
                        .status(false)
                        .message("No records found")
                        .build();
            }

            return CommandResult.builder()
                    .status(true)
                    .results(Collections.singletonMap("records", records))
                    .build();
        } catch (SQLException e) {
            log.error("Error while getting academic records for course.", e);
            return CommandResult.builder()
                    .status(false)
                    .results(Collections.singletonMap("exception", e))
                    .message(e.getMessage())
                    .build();
        }
    }
    /* ******************************************************/
    /*              Private Methods                         */
    /* ******************************************************/
    private boolean updateFromWaitList(String courseId) {
        List<CourseRequest> waitList = Collections.emptyList();
        if (waitList != null && !waitList.isEmpty()) {
            for (CourseRequest request : waitList) {
                CommandResult result = validateCourseRequest(request);
                if (result.getStatus()) {
                    // Successfully updated from wait list.
                    return true;
                }
            }
        }
        return false;
    }

    private  ArrayList<String[]> readCsvFile(String fileName) {
        ClassLoader loader = getClass().getClassLoader();

        ArrayList<String[]> fileContent = new ArrayList<>();
        try (InputStream is = loader.getResourceAsStream("data/" + fileName);
             InputStreamReader reader = new InputStreamReader(is);
             BufferedReader br = new BufferedReader(reader)){

            String line = br.readLine();
            while (line != null) {
                fileContent.add(line.split(","));
                line = br.readLine(); // read a new line
            }
            br.close();
        } catch (IOException e) {
             log.error("IOException while reading " + fileName, e);
             throw new UncheckedIOException(e);
        }
        return fileContent;
    }

    private  void  parseTermInfo() throws SQLException {
        String termCSVFile = "terms.csv";
        ArrayList<String[]> termInfoList = readCsvFile(termCSVFile);
        // 2. parse terms info
        for (String[] termInfo : termInfoList) {
            String termCourseID = termInfo[0];
            String term = termInfo[1];

            final Course course = dataSource.getCourseById(Integer.parseInt(termCourseID));
            course.getTermsOffered().add(term);
            log.info("{}: {} is offered during {}", course.getCourseID(), course.getCourseName(), term);
            dataSource.createOfferTerm(termCourseID, term);
        }
    }

    private  void parseStudentInfo() throws SQLException {

        String studentCSVFile = "students.csv";
        ArrayList<String[]> studentInfoList = readCsvFile(studentCSVFile);
        Student[] students = new Student[studentInfoList.size()];
        int count = 0;

        for (String[] studentInfo : studentInfoList) {
            String studentID = studentInfo[0];
            String name = studentInfo[1];
            String address = studentInfo[2];
            String phoneNumber = studentInfo[3];

            log.info("Adding student {}", name);
            students[count] = Student.builder()
                    .id(studentID)
                    .name(name)
                    .address(address)
                    .phoneNumber(phoneNumber)
                    .build();
            count++;
        }

        dataSource.createStudents(students);
    }

    private  void parseInstructorInfo() throws SQLException {
        String instructorCSVFile = "instructors.csv";
        ArrayList<String[]> instructorInfoList = readCsvFile(instructorCSVFile);
        Instructor[] instructors = new Instructor[instructorInfoList.size()];
        int count = 0;

        for (String[] instructor_Info : instructorInfoList) {
            String instructorID = instructor_Info[0];
            String name = instructor_Info[1];
            String address = instructor_Info[2];
            String phoneNumber = instructor_Info[3];

            log.info("Adding instructor {}", name);
            instructors[count] = Instructor.builder()
                    .id(instructorID)
                    .name(name)
                    .address(address)
                    .phoneNumber(phoneNumber)
                    .build();
            count++;
        }

        dataSource.createInstructors(instructors);
    }

    private  void parseCourseInfo() throws SQLException {
        String course_CSV_File = "courses.csv";

        ArrayList<String[]> courseInfoList = readCsvFile(course_CSV_File);
        Course[] courses = new Course[courseInfoList.size()];
        int count = 0;
        //  1. parse basic course info
        for (String[] course_Info : courseInfoList) {
            String courseID = course_Info[0];
            String courseName = course_Info[1];

            Course newCourse = Course.builder()
                    .courseID(courseID)
                    .courseName(courseName)
                    .build();
            log.info("Adding course {}", newCourse);
            courses[count] = newCourse;
            count++;
        }
        dataSource.createCourses(courses);
    }

    private void parseEligibleCourseInfo() throws SQLException  {
        String eligibleCSVFile = "eligible.csv";
        ArrayList<String[]> eligibleInfoList = readCsvFile(eligibleCSVFile);

        for (String[] eligible_Info : eligibleInfoList) {
            String instructorId = eligible_Info[0];
            String courseId = eligible_Info[1];

            Instructor instructor = dataSource.getInstructorById(instructorId);
            Course course = dataSource.getCourseById(Integer.parseInt(courseId));

            if (instructor == null) {
                throw new UniversitySystemException("Instructor not found while adding eligible course: " + instructorId);
            }

            if (course == null) {
                throw new UniversitySystemException("Course not found while adding eligible course: " + courseId);
            }
            log.info("{} is eligible to teach {}", instructor, course);
            dataSource.addEligibleCourse(instructor.getId(), course.getCourseID());
        }
    }

    private void parsePrereqsCourseInfo() throws SQLException {
        String prereqCSVFile = "prereqs.csv";
        ArrayList<String[]> prereqInfoList = readCsvFile(prereqCSVFile);
        for (String[] prereqInfo : prereqInfoList) {
            String prereqCourseID = prereqInfo[0];
            String mainCourseID = prereqInfo[1];

            final Course course = dataSource.getCourseById(Integer.parseInt(mainCourseID));
            final Course prereq = dataSource.getCourseById(Integer.parseInt(prereqCourseID));

            if (course == null) {
                throw new UniversitySystemException("Course not found while adding prerequisite: " + mainCourseID);
            }

            if (prereq == null) {
                throw new UniversitySystemException("Prerequisite not found while adding prerequisite: " + prereqCourseID);
            }

            log.info("{} requires {}", course, prereq);
            dataSource.addCoursePrerequisite(mainCourseID, prereqCourseID);
        }
    }

    private boolean validDropCourse(Instructor instructor) throws SQLException {
        Course currentCourse = instructor.getCourseCurrentTeaching();

        if (currentCourse == null) {
            return true;
        }

        // Get Fulfilled CourseRequests for the currentCourse for the currentYear and currentTerm
        // Get all course offerings for the course
        // If number of courseRequests + displaced > remainingSeats, return false
        List<CourseRequest> courseRequests = dataSource.getCurrentCourseRequestsForCourse(Integer.parseInt(currentCourse.getCourseID()));
        List<CourseOffering> courseOfferings = dataSource.getCurrentCourseOfferingList(currentCourse.getCourseID());

        Integer requests = courseRequests.stream().filter(req -> Integer.parseInt(req.getStatus()) > 0)
                .collect(Collectors.toList()).size();
        Integer remainingSeats = 0;
        Integer displacedSeats = 0;

        List<CourseOffering> remainingOfferings = new ArrayList<>();
        CourseOffering droppedOffering = null;

        for (CourseOffering offering : courseOfferings) {
            if (offering.getInstructorID().equals(instructor.getId())) {
                displacedSeats += (3 - offering.getAvailableSeats());
                droppedOffering = offering;
            } else {
                remainingOfferings.add(offering);
                remainingSeats += offering.getAvailableSeats();
            }
        }

        // No students will be displaced. No need for reseating. Drop course.
        if (displacedSeats == 0) {
            dataSource.dropCourseOffering(droppedOffering);
            return true;
        }

        // Total students exceed remaining seats available. Prevent drop.
        if ((requests + displacedSeats) > remainingSeats) {
            return false;
        }

        boolean allSeatsReplaced = false;

        // Place students in other Course Offerings.
        for (CourseOffering offering : remainingOfferings) {
            Integer postCapacity = offering.getAvailableSeats() - displacedSeats;
            if (postCapacity >= 0) {
                offering.setAvailableSeats(postCapacity);
                dataSource.updateAvailableSeats(offering);
                allSeatsReplaced = true;
                break;
            } else {
                displacedSeats = Math.abs(postCapacity);
            }
        }

        // Drop Instructor's Course Offering
        if (allSeatsReplaced) {
            dataSource.dropCourseOffering(droppedOffering);
            return true;
        }
        return false;
    }

    public CommandResult generateWekaTable(){
        CommandResult result;
        try {
            String data = dataSource.getWekaData();
            result = CommandResult.builder()
                    .status(true)
                    .message(data)
                    .build();

        } catch (SQLException e) {
            e.printStackTrace();
            log.error("Error occurred while generating weka table.", e);
            result = CommandResult.builder()
                    .status(false)
                    .results(Collections.singletonMap("exception", e))
                    .message(e.getMessage())
                    .build();
        }
        return  result;
    }


    public CommandResult wekaAnalysis() throws Exception {

        CommandResult result;
        ClassLoader classLoader = getClass().getClassLoader();

        try (InputStream is = classLoader.getResourceAsStream("weka/weka.csv")) {
            CSVLoader loader = new CSVLoader();
            loader.setSource(is);
            Instances dataset = loader.getDataSet();
            RemoveType removeType=new RemoveType();
            NumericToNominal numericToNominal=new NumericToNominal();
            removeType.setInputFormat(dataset);
            numericToNominal.setInputFormat(dataset);
            dataset= Filter.useFilter(dataset,removeType);
            dataset= Filter.useFilter(dataset,numericToNominal);
            Apriori model=new Apriori();
            model.buildAssociations(dataset);
            System.out.println(model.toString());
            result = CommandResult.builder()
                    .status(true)
                    .message("Weka analysis has been successfully done.")
                    .results(Collections.singletonMap("model", model.toString()))
                    .build();

            return  result;
        }
    }

    public CommandResult getUsers() throws Exception {
        List<Person> people = dataSource.getUsers().stream()
                .sorted(Comparator.comparingInt(u -> Integer.parseInt(u.getId())))
                .filter(u -> Integer.parseInt(u.getId()) > 0)
                .collect(Collectors.toList());
        return CommandResult.builder()
                .results(Collections.singletonMap("users", people))
                .status(true)
                .build();
    }

    public CommandResult updateUser(Person user) throws Exception {
        dataSource.updateUser(user);
        return CommandResult.builder()
                .results(Collections.singletonMap("user", user))
                .status(true)
                .build();
    }
}
