package com.tingco.codechallenge.elevator.api.states;

/**
 * Created by Yong Huang on 2017-11-21.
 */
public enum ElevatorStateToken {
    /**
     * Not moving, without pending request and ready for service.
     */
    IDLE,
    /**
     * At upwards moving.
     */
    MOVING_UP,
    /**
     * At downwards moving.
     */
    MOVING_DOWN,
    /**
     * Paused at floor for user to get on or off
     */
    STOPPED_FOR_GETTING_ON_OR_OFF,
    /**
     * Not moving, without pending request and ready for service;
     */
    MAINTENANCE
}
