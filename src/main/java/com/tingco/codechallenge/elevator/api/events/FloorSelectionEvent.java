package com.tingco.codechallenge.elevator.api.events;

/**
 * An event that represents a ride request towards a target floor.
 * <p>
 * Created by Yong Huang on 2017-11-21.
 */
public class FloorSelectionEvent {
    private int toFloor;

    public FloorSelectionEvent(int toFloor) {
        this.toFloor = toFloor;
    }

    public int getToFloor() {
        return toFloor;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        FloorSelectionEvent that = (FloorSelectionEvent) o;

        return toFloor == that.toFloor;
    }

    @Override public int hashCode() {
        return toFloor;
    }

    @Override public String toString() {
        return "FloorSelectionEvent{" +
            "toFloor=" + toFloor +
            '}';
    }
}
