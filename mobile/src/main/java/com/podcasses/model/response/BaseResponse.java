package com.podcasses.model.response;

/**
 * Created by aleksandar.kovachev.
 */
public class BaseResponse {

    private String message;

    private int status;

    public enum Status {

        SUCCESSFUL(0), FAIL(1);

        private int status;

        Status(int status) {
            this.status = status;
        }

        public int getStatus() {
            return status;
        }

    }

}
