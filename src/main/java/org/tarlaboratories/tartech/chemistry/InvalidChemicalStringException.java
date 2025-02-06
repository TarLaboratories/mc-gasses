package org.tarlaboratories.tartech.chemistry;

public class InvalidChemicalStringException extends RuntimeException {
    public InvalidChemicalStringException(String message) {
        super("Error parsing chemical: " + message);
    }
}
