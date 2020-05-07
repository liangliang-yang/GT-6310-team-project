package edu.gatech.cs6310.project7.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import java.util.List;

/**
 * Base class for a person
 */
@Data
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Person {
    private String id;
    @NotBlank
    private String name;
    @NotBlank
    private String address;
    @NotBlank
    private String phoneNumber;
    private List<String> roles;

    public Person() {

    }
}
