package com.tingco.codechallenge.elevator.api.events;

/**
 * An event that represents the elevator has just arrived a floor.
 * <p>
 * Created by Yong Huang on 2017-11-21.
 */
public class ArriveFloorEvent implements Event {
    int atFloor;

    public ArriveFloorEvent(int atFloor) {
        this.atFloor = atFloor;
    }

    public int getAtFloor() {
        return atFloor;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ArriveFloorEvent that = (ArriveFloorEvent) o;

        return atFloor == that.atFloor;
    }

    @Override public int hashCode() {
        return atFloor;
    }

    @Override public String toString() {
        return "ArriveFloorEvent{" +
            "atFloor=" + atFloor +
            '}';
    }

    @Override public EventToken getToken() {
        return EventToken.ARRIVE;
    }
}
