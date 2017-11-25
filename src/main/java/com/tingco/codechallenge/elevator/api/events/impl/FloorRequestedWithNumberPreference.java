package com.tingco.codechallenge.elevator.api.events.impl;

import com.tingco.codechallenge.elevator.api.events.EventToken;

/**
 * An event that represents a ride request towards a target floor.
 * <p>
 * Created by Yong Huang on 2017-11-21.
 */
public class FloorRequestedWithNumberPreference extends FloorRequested {
    private int toFloor;

    public FloorRequestedWithNumberPreference(int toFloor) {
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

        FloorRequestedWithNumberPreference that = (FloorRequestedWithNumberPreference) o;

        return toFloor == that.toFloor;
    }

    @Override public int hashCode() {
        return toFloor;
    }

    @Override public String toString() {
        return "FloorRequestedWithNumberPreference{" +
            "toFloor=" + toFloor +
            '}';
    }

    @Override public EventToken getToken() {
        return EventToken.FLOOR_REQUESTED;
    }
}
