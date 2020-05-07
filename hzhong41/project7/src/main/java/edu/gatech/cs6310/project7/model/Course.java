package edu.gatech.cs6310.project7.model;

import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by student on 7/18/17.
 */

@Data
@Builder
public class Course {

    private String courseID;
    @NotBlank
    private String courseName;
    @Builder.Default
    private List<String> termsOffered = new ArrayList<>();
    @Builder.Default
    private List<Course> prereqsCourses = new ArrayList<>();

    private Integer waitListed;
    private Integer availableSeats;
}
