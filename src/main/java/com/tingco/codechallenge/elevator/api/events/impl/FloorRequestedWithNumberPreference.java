package com.tingco.codechallenge.elevator.api.events.impl;

import com.tingco.codechallenge.elevator.api.events.EventToken;

/**
 * An event that represents a ride request towards a target floor.
 * <p>
 * Created by Yong Huang on 2017-11-21.
 */
public class FloorRequestedWithNumberPreference extends FloorRequested {
    private int toFloor;
    private int receiverElevatorId;

    public FloorRequestedWithNumberPreference(int receiverElevatorId, int toFloor) {
        this.toFloor = toFloor;
        this.receiverElevatorId = receiverElevatorId;
    }

    @Override
    public int getToFloor() {
        return toFloor;
    }

    @Override public EventToken getToken() {
        return EventToken.FLOOR_REQUESTED;
    }

    @Override public int getElevatorId() {
        return receiverElevatorId;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        FloorRequestedWithNumberPreference that = (FloorRequestedWithNumberPreference) o;

        if (toFloor != that.toFloor)
            return false;
        return receiverElevatorId == that.receiverElevatorId;
    }

    @Override public int hashCode() {
        int result = toFloor;
        result = 31 * result + receiverElevatorId;
        return result;
    }

    @Override public String toString() {
        return "FloorRequestedWithNumberPreference{" +
            "toFloor=" + toFloor +
            ", receiverElevatorId=" + receiverElevatorId +
            '}';
    }
}
