package edu.gatech.cs6310.project7.controller;

import edu.gatech.cs6310.project7.dao.UniversityDataSource;
import edu.gatech.cs6310.project7.model.CommandResult;
import edu.gatech.cs6310.project7.model.CourseOffering;
import edu.gatech.cs6310.project7.service.UniversitySystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by student on 7/18/17.
 */
@RestController
@RequestMapping("/api/course")
public class CourseController {
    private final UniversitySystem system;
    private final UniversityDataSource ds;

    @Autowired
    public CourseController(final UniversitySystem system, final UniversityDataSource ds) {
        this.ds = ds;
        this.system = system;
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ResponseEntity getAllCourses() {

        CommandResult result = system.getAllCourses();

        if (result.getStatus()) {
            return ResponseEntity.ok(result);
        } else if ("No courses found.".equals(result.getMessage())) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }


    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity getCourseDetails(@PathVariable("id") Integer courseId) {
        CommandResult result = system.getCourseById(courseId);
        Map<String, Object> body = new HashMap<>();

        if ("Course not found".equals(result.getMessage())) {
            return ResponseEntity.notFound().build();
        } else if (!result.getStatus()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
        body.put("course", result.getResults().get("course"));

        CommandResult courseOfferingResult = system.getCourseOfferingByCourseId(courseId.toString());

        if (!courseOfferingResult.getStatus()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }

        Integer available = 0;
        List<CourseOffering> courseOfferings = (List<CourseOffering>) courseOfferingResult.getResults().get("courseOfferings");
        for (CourseOffering offering : courseOfferings) {
            available += offering.getAvailableSeats();
        }

        body.put("availableSeats", available);
        return ResponseEntity.ok(body);
    }

    @RequestMapping(value = "/records", method = RequestMethod.POST)
    public ResponseEntity getAcademicRecordsForCourse(@RequestBody CourseOffering request) {
        Integer courseId = Integer.parseInt(request.getCourseID());
        CommandResult result = system.getAcademicRecordsForCourse(courseId, request.getTerm(), request.getYear());
        if (result.getStatus()) {
            return ResponseEntity.ok(result.getResults());
        } else if ("No records found".equals(result.getMessage())) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }

    }

    @RequestMapping(value = "/waitlist/{id}", method = RequestMethod.GET)
    public ResponseEntity getCourseWaitList(@PathVariable("id") String id) throws SQLException {
        Map<String, Integer> waitList = ds.getCourseWaitList();
        return ResponseEntity.ok(waitList.get(id));
    }
}
