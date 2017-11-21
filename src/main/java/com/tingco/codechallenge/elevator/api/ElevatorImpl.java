package com.tingco.codechallenge.elevator.api;

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Created by Yong Huang on 2017-11-20.
 */
public class ElevatorImpl implements Elevator {

    private Direction direction = Direction.NONE;
    private ElevatorState state = ElevatorState.IDLE;
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
        Direction towards = getDirectionForRequest(toFloor);

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
        return !this.downwardsTargetFloors.isEmpty() || !this.upwardsTargetFloors.isEmpty();
    }

    @Override public int currentFloor() {
        return this.currentFloor;
    }

    private Direction getDirectionForRequest(int requestedFloor) {
        if (this.currentFloor == requestedFloor) {
            return Direction.NONE;
        }

        return requestedFloor > this.currentFloor ? Direction.UP : Direction.DOWN;
    }

    private void onPowerOff() {

    }

    private void onEmergency() {

    }

    private void onMaintenanceRequest() {

    }

    private void onMoveRequest() {

    }

    private void onArrive(int floor) {
        switch (state) {
            case MOVING_UP:
                if (floor == upwardsTargetFloors.peek()) {
                    this.state = ElevatorState.STOPPED_FOR_GETTING_ON_OR_OFF;
                }
                break;
            case MOVING_DOWN:
                if (floor == downwardsTargetFloors.peek()) {
                    this.state = ElevatorState.STOPPED_FOR_GETTING_ON_OR_OFF;
                }
                break;
            default:
                break;
        }

    }
}
