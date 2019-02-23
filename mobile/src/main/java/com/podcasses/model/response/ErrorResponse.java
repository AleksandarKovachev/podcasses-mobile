package com.podcasses.model.response;

import java.util.List;

/**
 * Created by aleksandar.kovachev.
 */
public class ErrorResponse {

    private String code;

    private String message;

    private List<String> details;

    private List<FieldErrorResponse> fieldErrors;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getDetails() {
        return details;
    }

    public void setDetails(List<String> details) {
        this.details = details;
    }

    public List<FieldErrorResponse> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(List<FieldErrorResponse> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }

}
