package com.tingco.codechallenge.elevator.api.events.impl;

import com.tingco.codechallenge.elevator.api.events.Event;
import com.tingco.codechallenge.elevator.api.events.EventToken;

/**
 * An event that represents the elevator has just arrived a floor.
 * <p>
 * Created by Yong Huang on 2017-11-21.
 */
public class ArriveFloor implements Event {
    int atFloor;

    public ArriveFloor(int atFloor) {
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

        ArriveFloor that = (ArriveFloor) o;

        return atFloor == that.atFloor;
    }

    @Override public int hashCode() {
        return atFloor;
    }

    @Override public String toString() {
        return "ArriveFloor{" +
            "atFloor=" + atFloor +
            '}';
    }

    @Override public EventToken getToken() {
        return EventToken.ARRIVE;
    }
}
