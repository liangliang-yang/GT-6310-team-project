package edu.gatech.cs6310.project7.controller;

import edu.gatech.cs6310.project7.dao.UniversityDataSource;
import edu.gatech.cs6310.project7.model.AcademicRecord;
import edu.gatech.cs6310.project7.model.CommandResult;
import edu.gatech.cs6310.project7.model.CourseRequest;
import edu.gatech.cs6310.project7.model.Person;
import edu.gatech.cs6310.project7.service.UniversitySystem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by student on 7/18/17.
 */
@Slf4j
@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final UniversitySystem system;
    private final UniversityDataSource ds;

    @Autowired
    public StudentController(final UniversitySystem system, final UniversityDataSource ds) {
        this.system = system;
        this.ds = ds;
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ResponseEntity getAllStudents() {
        CommandResult result = system.getAllStudents();
        if (result.getStatus()) {
            return ResponseEntity.ok(result);
        } else if ("No students found.".equals(result.getMessage())) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity getStudentById(@PathVariable("id") String id) {
        CommandResult result = system.getStudentById(id);
        if (result.getStatus()) {
            return ResponseEntity.ok(result);
        } else if ("# ERROR: Student does not exist".equals(result.getMessage())) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ResponseEntity registerStudent(@RequestBody Person person) {
        CommandResult result = system.addUserRole(person.getId(), "STUDENT");
        if (result.getStatus()) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }

    @RequestMapping(value = "/unenroll", method = RequestMethod.POST)
    public ResponseEntity unenrollStudent(@RequestBody Person person) {
        CommandResult result = system.removeUserRole(person.getId(), "STUDENT");
        if (result.getStatus()) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }

    @RequestMapping(value = "/requestCourse", method = RequestMethod.POST)
    public ResponseEntity requestCourse(@RequestBody CourseRequest request) {
        CommandResult result = system.validateCourseRequest(request);
        if (result.getStatus()) {
            log.info("Course Request Accepted: {}", result);
            return ResponseEntity.ok(result);
        } else if (result.getResults().get("request") != null) {
            log.error("Request course failed: {}", result.getMessage());
            return ResponseEntity.badRequest().body(result);
        } else {
            log.error("Request course exception: {}", result);
            result.setMessage(result.getMessage());
            //result.setMessage("An error occurred while requesting the course. Please contact an administrator.");
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(result);
        }
    }

    @RequestMapping(value = "/dropCourse", method = RequestMethod.POST)
    public ResponseEntity dropCourse(@RequestBody AcademicRecord record) {
        CommandResult result = system.studentDropCourse(record);
        if (result.getStatus()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    @RequestMapping(value = "/waitlist/{id}", method = RequestMethod.GET)
    public ResponseEntity studentWaitList(@PathVariable("id") String id) throws SQLException {
       List<CourseRequest> waitlist = ds.getWaitListByStudentId(id);
       return ResponseEntity.ok(waitlist);
    }
}
