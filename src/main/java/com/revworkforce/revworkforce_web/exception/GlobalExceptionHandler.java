package com.revworkforce.revworkforce_web.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Centralized Global Exception Handler for the entire project.
 * This class catches exceptions thrown by any controller (both REST and UI)
 * and returns a structured, uniform JSON error response.
 * Proper use of this class minimizes scattered try-catch blocks in controllers.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles standard, generic generic exceptions.
     * It provides a centralized fallback for all unexpected, unhandled exceptions across the project.
     *
     * @param ex      the exception that was thrown
     * @param request the current web request
     * @return a structured JSON response containing timestamp, status code, error details, and requested path
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex, HttpServletRequest request) {
        logger.error("An unexpected error occurred at path {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        Map<String, Object> errorDetails = buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                ex.getMessage() != null ? ex.getMessage() : "An unexpected central error occurred",
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles IllegalArgumentException specifically.
     * Returns a 400 BAD_REQUEST status rather than a 500 error, indicating the client sent an invalid input.
     *
     * @param ex      the exception that was thrown
     * @param request the current web request
     * @return a structured JSON response indicating a bad request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        logger.warn("Illegal Argument Exception at path {}: {}", request.getRequestURI(), ex.getMessage());

        Map<String, Object> errorDetails = buildErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage() != null ? ex.getMessage() : "Invalid argument provided",
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles RuntimeExceptions which usually represent custom business logic faults in services.
     * Also returns a BAD_REQUEST instead of crashing the server entirely.
     *
     * @param ex      the exception that was thrown
     * @param request the current web request
     * @return a structured JSON response
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        logger.warn("Runtime Exception at path {}: {}", request.getRequestURI(), ex.getMessage());

        Map<String, Object> errorDetails = buildErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage() != null ? ex.getMessage() : "A business logic error occurred",
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    /**
     * Helper method to build a consistent error response body.
     *
     * @param status  the HTTP status integer value
     * @param error   the abbreviated HTTP status descriptive text
     * @param message the detailed error message intended for developers or logs
     * @param path    the specific endpoint where the error occurred
     * @return a mapped object structure for JSON serialization
     */
    private Map<String, Object> buildErrorResponse(int status, String error, String message, String path) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now().toString());
        errorDetails.put("status", status);
        errorDetails.put("error", error);
        errorDetails.put("message", message);
        errorDetails.put("path", path);
        
        // This 'success' key is commonly queried by frontend Javascript handling logic 
        // across the RevWorkforce project APIs instead of just checking HTTP statuses.
        errorDetails.put("success", false);
        return errorDetails;
    }
}
