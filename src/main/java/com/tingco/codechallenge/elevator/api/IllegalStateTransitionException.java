package com.tingco.codechallenge.elevator.api;

/**
 * Created by Yong Huang on 2017-11-23.
 */
public class IllegalStateTransitionException extends RuntimeException {
    public IllegalStateTransitionException(String message) {
        super(message);
    }
}
