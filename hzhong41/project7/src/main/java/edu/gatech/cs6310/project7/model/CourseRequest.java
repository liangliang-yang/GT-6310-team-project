package edu.gatech.cs6310.project7.model;

import lombok.Builder;
import lombok.Data;

import java.sql.Date;
import java.sql.Timestamp;

/**
 *
 */
@Data
@Builder
public class CourseRequest {
    private int year;
    private String term;
    private String studentID;
    private String courseID;
    private String status; // status such as "course not find" can be represented as "CNF"
    private Timestamp requestDate;

    public void setStatus(Integer status) {
        this.status = Integer.toString(status);
    }
}
