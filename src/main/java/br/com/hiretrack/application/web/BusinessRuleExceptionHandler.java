package br.com.hiretrack.application.web;

import br.com.hiretrack.application.domain.BusinessRuleException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class BusinessRuleExceptionHandler {

    @ExceptionHandler(BusinessRuleException.class)
    ProblemDetail handle(BusinessRuleException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }
}
