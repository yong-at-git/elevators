package com.tingco.codechallenge.elevator.api;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.tingco.codechallenge.elevator.api.events.impl.ArriveFloor;
import com.tingco.codechallenge.elevator.api.events.impl.BackToService;
import com.tingco.codechallenge.elevator.api.events.impl.DoorClosed;
import com.tingco.codechallenge.elevator.api.events.impl.DoorFailure;
import com.tingco.codechallenge.elevator.api.events.impl.DoorInterrupted;
import com.tingco.codechallenge.elevator.api.events.impl.DoorOpen;
import com.tingco.codechallenge.elevator.api.events.impl.DoorOpened;
import com.tingco.codechallenge.elevator.api.events.impl.Emergency;
import com.tingco.codechallenge.elevator.api.events.impl.FloorRequested;
import com.tingco.codechallenge.elevator.api.events.impl.Maintain;
import com.tingco.codechallenge.elevator.api.events.impl.PowerOff;
import com.tingco.codechallenge.elevator.api.events.impl.UserWaiting;
import com.tingco.codechallenge.elevator.api.states.ElevatorState;
import com.tingco.codechallenge.elevator.api.states.StateFactory;
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
    private ElevatorFSM fsm = new ElevatorFSM(this);
    private ElevatorState currentState = StateFactory.createIdle();
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

    ElevatorState getCurrentState() {
        return currentState;
    }

    void setCurrentState(ElevatorState newState) {
        this.currentState = newState;
    }

    void setDirection(Direction direction) {
        this.direction = direction;
    }

    public void setCurrentFloor(int currentFloor) {
        this.currentFloor = currentFloor;
    }

    private Direction getDirectionForRequest(int requestedFloor) {
        if (this.currentFloor == requestedFloor) {
            return Direction.NONE;
        }

        return requestedFloor > this.currentFloor ? Direction.UP : Direction.DOWN;
    }

    @Subscribe
    public void onArrive(ArriveFloor arriveFloor) {
        this.fsm.onArrive(arriveFloor);
    }

    @Subscribe
    public void onBackToService(BackToService backToService) {
        this.fsm.onBackToService(backToService);
    }

    @Subscribe
    public void onDoorClosed(DoorClosed doorClosed) {
        this.fsm.onDoorClosed(doorClosed);

    }

    @Subscribe
    public void onDoorFailure(DoorFailure doorFailure) {
        this.fsm.onDoorFailure(doorFailure);

    }

    @Subscribe
    public void onDoorInterrupted(DoorInterrupted doorInterrupted) {

    }

    @Subscribe
    public void onDoorOpen(DoorOpen doorOpen) {
        this.fsm.onDoorOpen(doorOpen);

    }

    @Subscribe
    public void onDoorOpened(DoorOpened doorOpened) {
        this.fsm.onDoorOpened(doorOpened);

    }

    @Subscribe
    public void onEmergency(Emergency emergencyEvent) {
        LOGGER.info("Receiving {} ", emergencyEvent);
        this.fsm.onEmergency();
    }

    @Subscribe
    private void onFloorRequested(FloorRequested floorRequested) {
        LOGGER.info("Receiving {} ", floorRequested);
        this.fsm.onFloorRequested(floorRequested);
    }

    @Subscribe
    private void onMaintain(Maintain maintain) {
        LOGGER.info("Receiving {}", maintain);
        this.fsm.onMaintain(maintain);
    }

    @Subscribe
    public void onPowerOff(PowerOff powerOff) {
        LOGGER.info("Receiving {} ", powerOff);
        this.fsm.onPowerOff(powerOff);
    }

    @Subscribe
    public void onUserWaitingRequest(UserWaiting userWaiting) {
        LOGGER.info("Receiving {} ", userWaiting);
        this.fsm.onUserWaitingRequest(userWaiting);
    }

    EventBus getEventBus() {
        return eventBus;
    }

    PriorityQueue<Integer> getUpwardsTargetFloors() {
        return upwardsTargetFloors;
    }

    PriorityQueue<Integer> getDownwardsTargetFloors() {
        return downwardsTargetFloors;
    }
}
