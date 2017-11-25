package com.tingco.codechallenge.elevator.api.exceptions;

/**
 * Created by Yong Huang on 2017-11-25.
 */
public class OutOfFloorRangeException extends Exception {

    public OutOfFloorRangeException(String message) {
        super(message);
    }
}
