package com.tingco.codechallenge.elevator.api;

import com.google.common.collect.Range;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.tingco.codechallenge.elevator.api.events.EventFactory;
import com.tingco.codechallenge.elevator.api.events.impl.ArriveFloor;
import com.tingco.codechallenge.elevator.api.events.impl.BackToService;
import com.tingco.codechallenge.elevator.api.events.impl.CloseDoor;
import com.tingco.codechallenge.elevator.api.events.impl.DoorClosed;
import com.tingco.codechallenge.elevator.api.events.impl.DoorFailure;
import com.tingco.codechallenge.elevator.api.events.impl.DoorInterrupted;
import com.tingco.codechallenge.elevator.api.events.impl.DoorOpened;
import com.tingco.codechallenge.elevator.api.events.impl.Emergency;
import com.tingco.codechallenge.elevator.api.events.impl.FloorRequestedWithDirectionPreference;
import com.tingco.codechallenge.elevator.api.events.impl.FloorRequestedWithNumberPreference;
import com.tingco.codechallenge.elevator.api.events.impl.Maintain;
import com.tingco.codechallenge.elevator.api.events.impl.OpenDoor;
import com.tingco.codechallenge.elevator.api.events.impl.PowerOff;
import com.tingco.codechallenge.elevator.api.exceptions.OutOfFloorRangeException;
import com.tingco.codechallenge.elevator.api.states.ElevatorState;
import com.tingco.codechallenge.elevator.api.states.ElevatorStateToken;
import com.tingco.codechallenge.elevator.api.states.StateFactory;
import com.tingco.codechallenge.elevator.api.utils.FloorValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${com.tingco.elevator.floor.bottom}")
    private int bottomFloor;

    @Value("${com.tingco.elevator.floor.top}")
    private int topFloor;

    @Value("${com.tingco.elevator.door.opening.duration.in.ms}")
    private int doorOpeningDurationInMs;

    @Value("${com.tingco.elevator.door.opened.waiting.duration.in.ms}")
    private int doorOpenedWaitingDurationInMs;

    @Value("${com.tingco.elevator.door.closing.duration.in.ms}")
    private int doorClosingDurationInMs;

    @Value("${com.tingco.elevator.door.closed.waiting.duration.in.ms}")
    private int doorClosedWaitingDurationInMs;

    @Value("${com.tingco.elevator.move.duration.between.floors.in.ms}")
    private int movingDurationBetweenFloorsInMs;

    @Value("${com.tingco.elevator.waiting.duration.after.notifying.arriving.in.ms}")
    private int waitingDurationAfterNotifyingArrivingInMs;

    private volatile Direction direction = Direction.NONE;
    private ElevatorFSM fsm = new ElevatorFSM(this);
    private volatile ElevatorState currentState = StateFactory.createIdle();
    private volatile int currentFloor = 0;
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
        this.eventBus.post(EventFactory.createFloorRequested(toFloor));
    }

    @Override public boolean isBusy() {
        return !this.downwardsTargetFloors.isEmpty() || !this.upwardsTargetFloors.isEmpty();
    }

    @Override public int currentFloor() {
        return this.currentFloor;
    }

    public boolean isOnUpPath() {
        return Direction.UP.equals(this.direction);
    }

    public boolean isOnDownPath() {
        return Direction.DOWN.equals(this.direction);
    }

    ElevatorState getCurrentState() {
        return currentState;
    }

    void setCurrentState(ElevatorState newState) {
        LOGGER.info("Setting state from: {} to: {}", this.getCurrentState().getToken(), newState.getToken());
        this.currentState = newState;

        if (ElevatorStateToken.IDLE.equals(newState.getToken())) {
            LOGGER.info("The recorded events: {}", this.fsm.getEVENT_LOG());
            this.fsm.getEVENT_LOG().clear();
        }
    }

    void setDirection(Direction direction) {
        this.direction = direction;
    }

    void setCurrentFloor(int newFloor) {
        this.currentFloor = newFloor;
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
    public void onCloseDoor(CloseDoor closeDoor) {
        this.fsm.onCloseDoor(closeDoor);
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
    public void onOpenDoor(OpenDoor openDoor) {
        this.fsm.onOpenDoor(openDoor);

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
    private void onFloorRequested(FloorRequestedWithNumberPreference floorRequestedWithNumberPreference) throws OutOfFloorRangeException {
        LOGGER.info("Receiving {} ", floorRequestedWithNumberPreference);

        int toFloor = floorRequestedWithNumberPreference.getToFloor();
        FloorValidator.validate(toFloor, Range.closed(bottomFloor, topFloor));

        this.fsm.onFloorRequested(floorRequestedWithNumberPreference);
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
    public void onUserWaitingRequest(FloorRequestedWithDirectionPreference floorRequestedWithDirectionPreference) throws OutOfFloorRangeException {
        LOGGER.info("Receiving {} ", floorRequestedWithDirectionPreference);

        int toFloor = floorRequestedWithDirectionPreference.getToFloor();
        FloorValidator.validate(toFloor, Range.closed(bottomFloor, topFloor));

        this.fsm.onUserWaitingRequest(floorRequestedWithDirectionPreference);
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

    int getDoorOpeningDurationInMs() {
        return doorOpeningDurationInMs;
    }

    int getDoorOpenedWaitingDurationInMs() {
        return doorOpenedWaitingDurationInMs;
    }

    int getDoorClosingDurationInMs() {
        return doorClosingDurationInMs;
    }

    int getDoorClosedWaitingDurationInMs() {
        return doorClosedWaitingDurationInMs;
    }

    int getMovingDurationBetweenFloorsInMs() {
        return movingDurationBetweenFloorsInMs;
    }

    int getWaitingDurationAfterNotifyingArrivingInMs() {
        return waitingDurationAfterNotifyingArrivingInMs;
    }
}
