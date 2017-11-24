package com.tingco.codechallenge.elevator.api;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.tingco.codechallenge.elevator.api.events.Event;
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
    private ElevatorFSM fsm = new ElevatorFSM();
    private ElevatorState elevatorState = StateFactory.createIdle();
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
        this.fsm.onDoorInterrupted(doorInterrupted);
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
        this.fsm.onEmergency(emergencyEvent);
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

    private class ElevatorFSM {
        private final Logger FSM_LOGGER = LogManager.getLogger(ElevatorFSM.class);

        // These onEvent methods capture event, check condition(i.e. FSM guard), then perform optional actions(e.g. state change)
        public void onArrive(ArriveFloor arriveFloor) {
            switch (elevatorState.getToken()) {
                case MOVING_UP:
                case MOVING_DOWN:
                    updateStatusOnArrive(arriveFloor.getAtFloor());
                    break;
                default:
                    throwIllegalStateTransitionException(arriveFloor);
            }
        }

        public void onBackToService(BackToService backToService) {
            switch (elevatorState.getToken()) {
                case MAINTENANCE:
                    elevatorState = StateFactory.createIdle();
                    break;
                default:
                    throwIllegalStateTransitionException(backToService);
            }
        }

        public void onDoorClosed(DoorClosed doorClosed) {

        }

        public void onDoorFailure(DoorFailure doorFailure) {
        }

        public void onDoorInterrupted(DoorInterrupted doorInterrupted) {
        }

        public void onDoorOpen(DoorOpen doorOpen) {
            switch (elevatorState.getToken()) {
                case JUST_ARRIVED:
                    toDoorOpening();
                    break;
                default:
                    throwIllegalStateTransitionException(doorOpen);
            }
        }

        public void onDoorOpened(DoorOpened doorOpened) {
            switch (elevatorState.getToken()) {
                case DOOR_OPENING:
                    toDoorOpened();
                    break;
            }
        }

        public void onEmergency(Emergency emergency) {
            toMaintenance();
        }

        private void onFloorRequested(FloorRequested floorRequested) {
            switch (elevatorState.getToken()) {
                case MOVING_DOWN:
                case MOVING_UP:
                case IDLE:
                case JUST_ARRIVED:
                case DOOR_OPENED:
                case DOOR_OPENING:
                case DOOR_CLOSING:
                case READY_TO_MOVE:
                    // TODO add to queue
                    break;
            }
        }

        private void onMaintain(Maintain maintain) {
            switch (elevatorState.getToken()) {
                case IDLE:
                    toMaintenance();
                    break;
            }
        }

        public void onPowerOff(PowerOff powerOff) {
            toMaintenance();
        }

        @Subscribe
        public void onUserWaitingRequest(UserWaiting userWaiting) {

        }

        void toIdle() {
            elevatorState = StateFactory.createIdle();
        }

        void toMovingUp() {
            elevatorState = StateFactory.createMovingUp();
        }

        void toMovingDown() {
            elevatorState = StateFactory.createMovingDown();
        }

        void toMaintenance() {
            elevatorState = StateFactory.createMaintenance();
        }

        void updateStatusOnArrive(int arrivedFloor) {

            currentFloor = arrivedFloor;

            boolean floorIsRequested = upwardsTargetFloors.contains(arrivedFloor) || downwardsTargetFloors.contains(arrivedFloor);
            if (floorIsRequested) {
                elevatorState = StateFactory.createJustArrived();
            }

            // otherwise just bypass
        }

        void toDoorOpening() {
            // TODO: openDoor()
            elevatorState = StateFactory.createDoorOpening();
        }

        void toDoorOpened() {
            elevatorState = StateFactory.createDoorOpened();
        }

        void toDoorClosing() {
            elevatorState = StateFactory.createDoorClosing();
        }

        void toReadyToMove() {
            elevatorState = StateFactory.createReadyToMove();
        }

        private void throwIllegalStateTransitionException(Event event) {
            String exceptionMessage = "Event: " + event.getToken() + " should not happen under state: " + elevatorState.getToken();

            FSM_LOGGER.fatal(exceptionMessage);

            throw new IllegalArgumentException(exceptionMessage);
        }

    }
}
