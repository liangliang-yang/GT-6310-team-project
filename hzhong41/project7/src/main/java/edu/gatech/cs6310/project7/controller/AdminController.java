package edu.gatech.cs6310.project7.controller;

import edu.gatech.cs6310.project7.dao.UniversityDataSource;
import edu.gatech.cs6310.project7.model.CommandResult;
import edu.gatech.cs6310.project7.model.Course;
import edu.gatech.cs6310.project7.model.Instructor;
import edu.gatech.cs6310.project7.model.Person;
import edu.gatech.cs6310.project7.model.Student;
import edu.gatech.cs6310.project7.service.UniversitySystem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller to handle all requests from the Admin page.
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UniversitySystem system;
    private final UniversityDataSource ds;

    @Autowired
    public AdminController(final UniversitySystem system, final UniversityDataSource ds) {
        this.ds = ds;
        this.system = system;
    }

    @RequestMapping(value = "/initialize", method = RequestMethod.POST)
    public ResponseEntity initializeData() {
        CommandResult result = system.initializeData();
        if (!result.getStatus()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(result.getMessage());
        }

        return ResponseEntity.ok(result.getMessage());
    }

    @RequestMapping(value = "/term", method = RequestMethod.GET)
    public ResponseEntity getCurrentTerm() {
        CommandResult result = system.getCurrentSemester();

        if (!result.getStatus()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(result.getMessage());
        }
        return ResponseEntity.ok(result.getResults());
    }

    @RequestMapping(value = "/term/next", method = RequestMethod.POST)
    public ResponseEntity startNextTerm() {
        CommandResult result = system.goToNextSemester();

        if (!result.getStatus()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(result.getMessage());
        }
        return ResponseEntity.ok(result.getResults());
    }

    @RequestMapping(value = "/user/register", method = RequestMethod.POST)
    public ResponseEntity registerUser(@RequestBody @Valid Person person) {
        CommandResult result = null;
        if (person.getRoles().contains("INSTRUCTOR")) {
            Instructor instructor = Instructor.builder()
                    .id(person.getId())
                    .name(person.getName())
                    .address(person.getAddress())
                    .phoneNumber(person.getPhoneNumber())
                    .isActive(false)
                    .build();
            try {
                result = system.addInstructor(instructor);
            } catch (Exception e) {
                log.error("Error occurred while registering instructor.", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            if (person.getRoles().contains("STUDENT")) {
                system.addUserRole(((Person) result.getResults().get("user")).getId(), "STUDENT");
            }
        } else if (person.getRoles().contains("STUDENT")) {
            try {
                Student s = Student.builder()
                        .name(person.getName())
                        .address(person.getAddress())
                        .phoneNumber(person.getPhoneNumber())
                        .build();
                result = system.addStudent(s);
            } catch (Exception e) {
                log.error("Error occurred while registering student.", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(result.getResults().get("user"));
    }

    @RequestMapping(value = "/course/register", method = RequestMethod.POST)
    public ResponseEntity registerCourse(@RequestBody @Valid Course course) {
        CommandResult result;
        try {
            result = system.addCourse(course);
        } catch (Exception e) {
            log.error("Error occurred while registering course.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        Course resultCourse = (Course) result.getResults().get("course");
        Map<String, Object> body = new HashMap<>();
        body.put("result", result);
        body.put("location", "/course.html?id=" + resultCourse.getCourseID());
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @RequestMapping(value = "/user/update", method = RequestMethod.POST)
    public ResponseEntity updateUser(@RequestBody @Valid Person person) throws Exception {
        CommandResult result = system.updateUser(person);
        return ResponseEntity.status(HttpStatus.OK).body(result.getResults().get("user"));
    }

    @RequestMapping(value = "/course/update", method = RequestMethod.POST)
    public ResponseEntity updateCourses(@RequestBody Course course) throws SQLException {
        ds.updateCourseTitle(course);
        for (Course prereq : course.getPrereqsCourses()) {
            String[] tokens = prereq.getCourseID().split("-");
            String id = tokens[0];
            String action = tokens[1];
            if ("ADD".equals(action)) {
                ds.addCoursePrerequisite(course.getCourseID(), id);
            } else if ("REMOVE".equals(action)) {
                ds.removeCoursePrerequisite(course.getCourseID(), id);
            }
        }
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/user/list", method = RequestMethod.GET)
    public ResponseEntity getUserList() throws Exception {

        CommandResult result = system.getUsers();
        return ResponseEntity.ok(result.getResults().get("users"));
    }
}
