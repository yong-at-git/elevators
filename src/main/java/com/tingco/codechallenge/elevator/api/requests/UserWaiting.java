package com.tingco.codechallenge.elevator.api.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tingco.codechallenge.elevator.api.ElevatorImpl;

/**
 * Created by Yong Huang on 2017-11-26.
 */
public class UserWaiting {
    private int waitingFloor;
    private ElevatorImpl.Direction towards;

    public UserWaiting() {
    }

    public UserWaiting(int waitingFloor, ElevatorImpl.Direction towards) {
        this.waitingFloor = waitingFloor;
        this.towards = towards;
    }

    @JsonProperty("waiting_floor")
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

        UserWaiting waiting = (UserWaiting) o;

        if (waitingFloor != waiting.waitingFloor)
            return false;
        return towards == waiting.towards;
    }

    @Override public int hashCode() {
        int result = waitingFloor;
        result = 31 * result + (towards != null ? towards.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return "UserWaiting{" +
            "waitingFloor=" + waitingFloor +
            ", towards=" + towards +
            '}';
    }
}
