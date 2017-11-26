package com.tingco.codechallenge.elevator.api.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Yong Huang on 2017-11-26.
 */
public class UserRiding {
    private Integer ridingElevatorId;
    private int toFloor;

    private UserRiding() {
    }

    public UserRiding(Integer ridingElevatorId, int toFloor) {
        this.ridingElevatorId = ridingElevatorId;
        this.toFloor = toFloor;
    }

    @JsonProperty("to_floor")
    public int getToFloor() {
        return 0;
    }

    @JsonProperty("riding_elevator_id")
    public Integer getRidingElevatorId() {
        return ridingElevatorId;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        UserRiding that = (UserRiding) o;

        if (ridingElevatorId != that.ridingElevatorId)
            return false;
        return toFloor == that.toFloor;
    }

    @Override public int hashCode() {
        int result = ridingElevatorId;
        result = 31 * result + toFloor;
        return result;
    }

    @Override public String toString() {
        return "UserRiding{" +
            "ridingElevatorId=" + ridingElevatorId +
            ", toFloor=" + toFloor +
            '}';
    }
}
