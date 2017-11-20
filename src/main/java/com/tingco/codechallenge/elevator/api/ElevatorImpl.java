package com.tingco.codechallenge.elevator.api;

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Created by Yong Huang on 2017-11-20.
 */
public class ElevatorImpl implements Elevator {

    private Direction direction = Direction.NONE;
    private int currentFloor;
    private int id;

    private PriorityQueue<Integer> upwardsTargetFloors = new PriorityQueue<>();
    private PriorityQueue<Integer> downwardsTargetFloors = new PriorityQueue<>(Comparator.reverseOrder());

    public ElevatorImpl(int id) {
        this.id = id;
    }

    public ElevatorImpl(int currentFloor, int id) {
        this.currentFloor = currentFloor;
        this.id = id;
    }

    @Override public Direction getDirection() {
        return this.direction;
    }

    @Override public int getAddressedFloor() {
        switch (this.direction) {
            case UP:
                return this.upwardsTargetFloors.peek();
            case DOWN:
                return this.downwardsTargetFloors.peek();
            default:
                return this.currentFloor;
        }
    }

    @Override public int getId() {
        return this.id;
    }

    @Override public void moveElevator(int toFloor) {
        // Two events can trigger it:
        // 1. user pressed button from waiting floor
        // 2. user pressed floor button from inside of the elevator
        // TODO: handle the case when user pressed up button but eventually chose to go lower floor
        Direction towards = decideDirection(toFloor);

        if (!isBusy()) {
            this.direction = towards;
        }

        if (Direction.UP.equals(towards)) {
            this.upwardsTargetFloors.offer(toFloor);
        } else if (Direction.DOWN.equals(towards)) {
            this.downwardsTargetFloors.offer(toFloor);
        }
    }

    @Override public boolean isBusy() {
        return Direction.UP.equals(this.direction) || Direction.DOWN.equals(this.direction);
    }

    @Override public int currentFloor() {
        return this.currentFloor;
    }

    private Direction decideDirection(int toFloor) {
        if (this.currentFloor == toFloor) {
            return Direction.NONE;
        }

        return toFloor > this.currentFloor ? Direction.UP : Direction.DOWN;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ElevatorImpl elevator = (ElevatorImpl) o;

        if (currentFloor != elevator.currentFloor)
            return false;
        if (id != elevator.id)
            return false;
        if (direction != elevator.direction)
            return false;
        if (upwardsTargetFloors != null ? !upwardsTargetFloors.equals(elevator.upwardsTargetFloors) : elevator.upwardsTargetFloors != null)
            return false;
        return downwardsTargetFloors != null ? downwardsTargetFloors.equals(elevator.downwardsTargetFloors) : elevator.downwardsTargetFloors == null;
    }

    @Override public int hashCode() {
        int result = direction != null ? direction.hashCode() : 0;
        result = 31 * result + currentFloor;
        result = 31 * result + id;
        result = 31 * result + (upwardsTargetFloors != null ? upwardsTargetFloors.hashCode() : 0);
        result = 31 * result + (downwardsTargetFloors != null ? downwardsTargetFloors.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return "ElevatorImpl{" +
            "direction=" + direction +
            ", currentFloor=" + currentFloor +
            ", id=" + id +
            ", upwardsTargetFloors=" + upwardsTargetFloors +
            ", downwardsTargetFloors=" + downwardsTargetFloors +
            '}';
    }
}
