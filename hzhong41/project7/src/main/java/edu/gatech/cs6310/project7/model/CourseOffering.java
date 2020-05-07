package edu.gatech.cs6310.project7.model;

import lombok.Builder;
import lombok.Data;

/**
 * Created by student on 7/18/17.
 */

@Data
@Builder
public class CourseOffering {

    private int year;
    private String term;
    private String instructorID;
    private String courseID;
    private int availableSeats;

}
