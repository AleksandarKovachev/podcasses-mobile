package com.podcasses.model.response;

/**
 * Created by aleksandar.kovachev.
 */
public class FieldErrorResponse {

    private String field;

    private String error;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
    
}
