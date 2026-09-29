package br.com.hiretrack.application.domain;

import com.lib.exception.core.BusinessException;

public class BusinessRuleException extends BusinessException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
