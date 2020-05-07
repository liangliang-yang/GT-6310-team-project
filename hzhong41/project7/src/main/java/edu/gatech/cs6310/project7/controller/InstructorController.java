package edu.gatech.cs6310.project7.controller;

import edu.gatech.cs6310.project7.model.AcademicRecord;
import edu.gatech.cs6310.project7.model.CommandResult;
import edu.gatech.cs6310.project7.model.CourseOffering;
import edu.gatech.cs6310.project7.model.Instructor;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by student on 7/18/17.
 */
@Slf4j
@RestController
@RequestMapping("/api/instructor")
public class InstructorController {

    private final UniversitySystem system;

    @Autowired
    public InstructorController(final UniversitySystem system) {
        this.system = system;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity getInstructorById(@PathVariable("id") final String id) {
        try {
            CommandResult result = system.getInstructor(id);
            if (!result.getStatus()) {
                if (result.getMessage().equals("Instructor not found.")) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                } else {
                    log.error("An error occurred while getting Instructor {}", id);
                    log.error(result.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("An error occurred while getting Instructor {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ResponseEntity getAllInstructors() {
        CommandResult result = system.getAllInstructors();
        if (result.getStatus()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @RequestMapping(value = "/hire", method = RequestMethod.POST)
    public ResponseEntity setInstructorHireStatus(@RequestBody Instructor instructor) {

        try {
            CommandResult result = system.updateInstructorStatus(instructor.getId(), instructor.isActive());
            if (!result.getStatus()) {
                return ResponseEntity.badRequest().body(result);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("An error occurred while hiring Instructor {}", instructor.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @RequestMapping(value = "/teach", method = RequestMethod.POST)
    public ResponseEntity teachCourse(@RequestBody CourseOffering courseOffering) {
        try {
            log.info("Teach Course request received {}", courseOffering);
            String instructorId = courseOffering.getInstructorID();
            String courseId = courseOffering.getCourseID();
            CommandResult request = system.createCourseOffering(instructorId, courseId);

            if (!request.getStatus()) {
                log.error("Teach Course request rejected: {}", request.getMessage());
                return ResponseEntity.badRequest().body(request);
            }

            return ResponseEntity.ok(courseOffering);
        } catch (Exception e) {
            log.error("An error occurred while requesting to teach a course", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @RequestMapping(value = "/unassign", method = RequestMethod.POST)
    public ResponseEntity unassignInstructor(@RequestBody CourseOffering courseOffering) {
        CommandResult result = system.dropCourseOffering(courseOffering.getInstructorID());
        if (result.getStatus()) {
            return ResponseEntity.ok().build();
        } else if ("Instructor is not eligible to drop the course.".equals(result.getMessage())) {
            return ResponseEntity.badRequest().body(result);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    @RequestMapping(value = "/eligibleCourses", method = RequestMethod.POST)
    public ResponseEntity updateEligibleCourses(@RequestBody Instructor instructor) {
        CommandResult result = system.updateEligibleCourses(instructor);
        if (result.getStatus()) {
            return ResponseEntity.ok().body(result.getResults());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    @RequestMapping(value = "/assignGrade", method = RequestMethod.POST)
    public ResponseEntity assignGrade(@RequestBody List<AcademicRecord> records) {
        log.info("Request received to assign grades: {}", records);
        List<String> errors = new ArrayList<>();
        for (AcademicRecord record : records) {
            CommandResult result = system.assignGrade(record);
            if (!result.getStatus()) {
                errors.add(record.getStudentID());
            }
        }
        if (errors.isEmpty()) {
            return ResponseEntity.ok(records);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errors);
        }
    }
}
