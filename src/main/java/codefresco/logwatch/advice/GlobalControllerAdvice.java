package codefresco.logwatch.advice;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.ConstraintViolationException;

@ControllerAdvice
public class GlobalControllerAdvice {
  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  public String handleConstraintViolationException(ConstraintViolationException ex) {
    return "Invalid request parameters";
  }
}