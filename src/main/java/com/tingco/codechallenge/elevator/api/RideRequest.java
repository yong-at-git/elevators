package com.tingco.codechallenge.elevator.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Yong Huang on 2017-11-25.
 */
public class RideRequest {
    private int toFloor;
    private Elevator.Direction towards = Elevator.Direction.NONE;

    private RideRequest() {
        // for Spring
    }

    public RideRequest(int toFloor) {
        this();
        this.toFloor = toFloor;
    }

    public RideRequest(int toFloor, Elevator.Direction towards) {
        this(toFloor);
        this.towards = (towards == null ? Elevator.Direction.NONE : towards);
    }

    @JsonProperty("to_floor")
    public int getToFloor() {
        return toFloor;
    }

    @JsonProperty("towards")
    public Elevator.Direction getTowards() {
        return towards;
    }
}
