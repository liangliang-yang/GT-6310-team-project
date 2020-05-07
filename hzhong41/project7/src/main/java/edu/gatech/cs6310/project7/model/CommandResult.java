package edu.gatech.cs6310.project7.model;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * <p>
 *     Model for CommandResult. Some commands are expected to return a list &mdash; like course_record &mdash;
 *     while others may simply return a message. This class gives the Command interface a single class to return and
 *     extend.
 *
 *     If a command needs a special type of result, it can extend this class. Otherwise, it can just return SUCCESS
 *     or FAIL. In case of fail, it should also have an error message.
 * </p>
 *
 * <p>
 *     The @Data annotation creates setters and getters. It also overrides the equals, hash, and toString functions.
 * </p>
 */
@Data
public class CommandResult {
    private Boolean status;
    private Map<String, Object> results;
    private String message;

    public CommandResult() {

    }

    @Builder
    public CommandResult(boolean status, Map<String,Object> results, String message) {
        this.status = status;
        this.results = results;
        this.message = message;
    }

}
