package edu.gatech.cs6310.project7.model;

import edu.gatech.cs6310.project7.exception.UniversitySystemException;
import lombok.Builder;
import lombok.Data;

/**
 * Represents a semester. Needed to keep track of the current semester across multiple user sessions.
 */
@Data
@Builder
public class Semester {
    private Integer year;
    private String term;

    public Semester(Integer year, String term){
        this.year=year;
        this.term=term;
    }
}
