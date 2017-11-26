package com.tingco.codechallenge.elevator.api.exceptions;

/**
 * Created by Yong Huang on 2017-11-26.
 */
public class MissingWaitingDirectionException extends Exception {
    public MissingWaitingDirectionException(String message) {
        super(message);
    }
}
