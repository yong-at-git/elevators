package com.tingco.codechallenge.elevator.api.exceptions;

/**
 * Created by Yong Huang on 2017-11-23.
 */
public class IllegalStateTransitionException extends Exception {
    public IllegalStateTransitionException(String message) {
        super(message);
    }
}
