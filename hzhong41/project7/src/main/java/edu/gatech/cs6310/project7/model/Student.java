package edu.gatech.cs6310.project7.model;

import lombok.Builder;
import lombok.Data;

import java.util.Collections;

/**
 * Created by student on 7/18/17.
 */

@Data
public class Student extends Person {

    @Builder
    private Student(final String id,
                    final String name,
                    final String address,
                    final String phoneNumber) {
        super(id, name, address, phoneNumber, Collections.singletonList("STUDENT"));
    }
}
