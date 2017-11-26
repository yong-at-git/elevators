package com.tingco.codechallenge.elevator;

/**
 * Created by Yong Huang on 2017-11-26.
 */
public class WaitingRequestResponse {
    private String message;
    private Integer allocated_elevator_id;

    public WaitingRequestResponse() {
    }

    public WaitingRequestResponse(String message, Integer allocated_elevator_id) {
        this.message = message;
        this.allocated_elevator_id = allocated_elevator_id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getAllocated_elevator_id() {
        return allocated_elevator_id;
    }

    public void setAllocated_elevator_id(Integer allocated_elevator_id) {
        this.allocated_elevator_id = allocated_elevator_id;
    }
}
