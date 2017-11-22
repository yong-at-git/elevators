package com.tingco.codechallenge.elevator.api;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.tingco.codechallenge.elevator.api.events.EmergencyEvent;
import com.tingco.codechallenge.elevator.api.events.FloorSelectionEvent;
import com.tingco.codechallenge.elevator.api.events.PowerOffEvent;
import com.tingco.codechallenge.elevator.api.events.UserWaitingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Created by Yong Huang on 2017-11-20.
 */
@Component
public class ElevatorImpl implements Elevator {
    private static final Logger LOGGER = LogManager.getLogger(ElevatorImpl.class);

    @Autowired
    EventBus eventBus;

    private Direction direction = Direction.NONE;
    private ElevatorState state = ElevatorState.IDLE;
    private int currentFloor;
    private int id;

    private PriorityQueue<Integer> upwardsTargetFloors = new PriorityQueue<>();
    private PriorityQueue<Integer> downwardsTargetFloors = new PriorityQueue<>(Comparator.reverseOrder());

    @PostConstruct
    public void init() {
        this.eventBus.register(this);
    }

    private ElevatorImpl() {
        // non-arg for Spring
    }

    public ElevatorImpl(int id) {
        this();
        this.id = id;
    }

    public ElevatorImpl(int currentFloor, int id) {
        this(id);
        this.currentFloor = currentFloor;
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

    @Subscribe
    public void onPowerOff(PowerOffEvent powerOffEvent) {
        LOGGER.info("Receiving {} ", powerOffEvent);
    }

    @Subscribe
    public void onEmergency(EmergencyEvent emergencyEvent) {
        LOGGER.info("Receiving {} ", emergencyEvent);
    }

    private void onMaintenanceRequest() {

    }

    @Subscribe
    public void onUserWaitingRequest(UserWaitingEvent userWaitingEvent) {
        LOGGER.info("Receiving {} ", userWaitingEvent);
    }

    @Subscribe
    private void onFloorSelectionRequest(FloorSelectionEvent floorSelectionEvent) {
        LOGGER.info("Receiving {} ", floorSelectionEvent);
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
