package edu.gatech.cs6310.project7.exception;

/**
 * Generic runtime except while performing business operation.
 */
public class UniversitySystemException extends RuntimeException {

    public UniversitySystemException(final String message) {
        super(message);
    }
}
