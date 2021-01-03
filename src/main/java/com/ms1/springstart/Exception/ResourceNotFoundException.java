package com.ms1.springstart.Exception;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

//@Getter
//@ResponseStatus(value = HttpStatus.NOT_FOUND)
@RestControllerAdvice(annotations = RestController.class)
public class ResourceNotFoundException extends ResponseEntityExceptionHandler {

}
