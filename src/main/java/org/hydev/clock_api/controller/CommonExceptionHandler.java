package org.hydev.clock_api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class CommonExceptionHandler {
    @ExceptionHandler(ConstraintViolationException.class)
    // ConstraintViolationException will be wrapped to TransactionSystemException!
    // https://stackoverflow.com/questions/45070642/springboot-doesnt-handle-org-hibernate-exception-constraintviolationexception
    private ResponseEntity<List<String>> handleConstraintViolationException(ConstraintViolationException cve) {
        List<String> errorMessages = cve.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage).sorted()
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(errorMessages);
    }
}
