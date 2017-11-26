package com.tingco.codechallenge.elevator.api.exceptions;

/**
 * Created by Yong Huang on 2017-11-26.
 */
public class MissingRidingElevatorException extends Exception {
    public MissingRidingElevatorException(String message) {
        super(message);
    }
}
