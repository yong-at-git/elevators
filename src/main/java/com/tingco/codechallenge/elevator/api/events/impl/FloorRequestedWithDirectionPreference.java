package com.tingco.codechallenge.elevator.api.events.impl;

import com.tingco.codechallenge.elevator.api.ElevatorImpl;
import com.tingco.codechallenge.elevator.api.events.EventToken;

import java.util.Objects;

/**
 * An event that represents a user is waiting and requesting an elevator.
 * <p>
 * Created by Yong Huang on 2017-11-21.
 */
public class FloorRequestedWithDirectionPreference extends FloorRequested {
    private int waitingFloor;
    private ElevatorImpl.Direction towards;
    private int receiverElevatorId;

    public FloorRequestedWithDirectionPreference(int receiverElevatorId, ElevatorImpl.Direction towards, int waitingFloor) {
        this.waitingFloor = waitingFloor;
        this.towards = towards;
        this.receiverElevatorId = receiverElevatorId;
    }

    public FloorRequestedWithDirectionPreference(int waitingFloor, ElevatorImpl.Direction towards) {
        Objects.requireNonNull(towards);

        this.waitingFloor = waitingFloor;
        this.towards = towards;
    }

    public int getWaitingFloor() {
        return waitingFloor;
    }

    public ElevatorImpl.Direction getTowards() {
        return towards;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        FloorRequestedWithDirectionPreference that = (FloorRequestedWithDirectionPreference) o;

        if (waitingFloor != that.waitingFloor)
            return false;
        return towards == that.towards;
    }

    @Override public int hashCode() {
        int result = waitingFloor;
        result = 31 * result + (towards != null ? towards.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return "FloorRequestedWithDirectionPreference{" +
            "waitingFloor=" + waitingFloor +
            ", towards=" + towards +
            '}';
    }

    @Override public EventToken getToken() {
        return EventToken.USER_WAITING;
    }

    @Override public int getToFloor() {
        return waitingFloor;
    }

    @Override public int getElevatorId() {
        return receiverElevatorId;
    }
}
