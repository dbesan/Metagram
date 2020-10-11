package org.studyproject.metagram.controller;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class ControllerUtils {
    /**
     * Map<String, String> getErrors(BindingResult bindingResult) returns map with errors in view *error*+Error as key,
     * error message as value
     */
    public static Map<String, String> getErrors(BindingResult bindingResult) {
        Collector<FieldError, ?, Map<String, String>> collector = Collectors
                .toMap(fieldError -> fieldError.getField() + "Error",
                        FieldError::getDefaultMessage);
        return bindingResult.getFieldErrors().stream().collect(collector);
    }
}
