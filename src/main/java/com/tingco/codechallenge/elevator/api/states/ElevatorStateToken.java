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
     * Elevator has just arrived at the stopping floor now.
     */
    JUST_ARRIVED,
    /**
     * The door is opening.
     */
    DOOR_OPENING,
    /**
     * The door is completely opened and stand still.
     */
    DOOR_OPENED,
    /**
     * The door is closing.
     */
    DOOR_CLOSING,
    /**
     * The door is completely closed and elevator is ready to move up or down.
     */
    READY_TO_MOVE,
    /**
     * Not moving, without pending request and ready for service;
     */
    MAINTENANCE
}
