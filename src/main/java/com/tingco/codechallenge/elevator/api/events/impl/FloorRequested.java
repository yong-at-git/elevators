package com.tingco.codechallenge.elevator.api.events.impl;

import com.tingco.codechallenge.elevator.api.events.EventToken;

/**
 * An event that represents a ride request towards a target floor.
 * <p>
 * Created by Yong Huang on 2017-11-21.
 */
public class FloorRequested extends FloorMovementRequest {
    private int toFloor;

    public FloorRequested(int toFloor) {
        this.toFloor = toFloor;
    }

    @Override
    public int getToFloor() {
        return toFloor;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        FloorRequested that = (FloorRequested) o;

        return toFloor == that.toFloor;
    }

    @Override public int hashCode() {
        return toFloor;
    }

    @Override public String toString() {
        return "FloorRequested{" +
            "toFloor=" + toFloor +
            '}';
    }

    @Override public EventToken getToken() {
        return EventToken.FLOOR_REQUESTED;
    }
}
