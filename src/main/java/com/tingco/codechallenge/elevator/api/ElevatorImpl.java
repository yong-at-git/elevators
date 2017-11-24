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

    private class ElevatorFSM {
        private final Logger FSM_LOGGER = LogManager.getLogger(ElevatorFSM.class);

        // These onEvent methods capture event, check condition(i.e. FSM guard), then perform optional actions(e.g. state change)
        public void onArrive(ArriveFloor arriveFloor) {
            switch (currentState.getToken()) {
                case MOVING_UP:
                case MOVING_DOWN:
                    updateStatusOnArrive(arriveFloor.getAtFloor());
                    break;
                default:
                    throwIllegalStateTransitionException(arriveFloor);
            }
        }

        public void onBackToService(BackToService backToService) {
            switch (currentState.getToken()) {
                case MAINTENANCE:
                    updateStatusOnBackToService();
                    break;
                default:
                    throwIllegalStateTransitionException(backToService);
            }
        }

        public void onDoorClosed(DoorClosed doorClosed) {
            switch (currentState.getToken()) {
                case DOOR_OPENING:
                    updateStatusOnDoorClosed();
                    break;
                default:
                    throwIllegalStateTransitionException(doorClosed);
            }
        }

        public void onDoorFailure(DoorFailure doorFailure) {
            switch (currentState.getToken()) {
                case JUST_ARRIVED:
                case DOOR_OPENING:
                case DOOR_OPENED:
                case DOOR_CLOSING:
                    updateStatusOnDoorFailure();
                    break;
                default:
                    throwIllegalStateTransitionException(doorFailure);
            }
        }

        public void onDoorOpen(DoorOpen doorOpen) {
            switch (currentState.getToken()) {
                case JUST_ARRIVED:
                case DOOR_OPENING:
                case DOOR_CLOSING:
                    updateStatusOnDoorOpen();
                    break;
                default:
                    throwIllegalStateTransitionException(doorOpen);
            }
        }

        public void onDoorOpened(DoorOpened doorOpened) {
            switch (currentState.getToken()) {
                case DOOR_OPENING:
                    updateStatusOnDoorOpened();
                    break;
                default:
                    throwIllegalStateTransitionException(doorOpened);
            }
        }

        public void onEmergency() {
            updateStatusOnEmergency();
        }

        private void onFloorRequested(FloorRequested floorRequested) {
            int toFloor = floorRequested.getToFloor();

            switch (currentState.getToken()) {
                case IDLE:
                    if (toFloor > currentFloor) {
                        upwardsTargetFloors.offer(toFloor);
                        toMovingUp();
                    } else if (toFloor < currentFloor) {
                        downwardsTargetFloors.offer(toFloor);
                        toMovingDown();
                    } else {
                        FSM_LOGGER.info("Currently at the requested floor: " + toFloor);
                    }
                    break;
                case MOVING_UP:
                    if (isFloorAlreadyRequested(toFloor)) {
                        FSM_LOGGER.info("Floor {} is already requested.", toFloor);
                    } else {
                        if (toFloor > currentFloor) {
                            upwardsTargetFloors.offer(toFloor);
                        } else if (toFloor <= currentFloor) {
                            downwardsTargetFloors.offer(toFloor);
                        }
                    }
                    break;
                case MOVING_DOWN:
                    if (isFloorAlreadyRequested(toFloor)) {
                        FSM_LOGGER.info("Floor {} is already requested.", toFloor);
                    } else {
                        if (toFloor >= currentFloor) {
                            upwardsTargetFloors.offer(toFloor);
                        } else if (toFloor < currentFloor) {
                            downwardsTargetFloors.offer(toFloor);
                        }
                    }
                    break;
                case JUST_ARRIVED:
                case DOOR_OPENING:
                case DOOR_OPENED:
                case DOOR_CLOSING:
                case READY_TO_MOVE:
                    if (isFloorAlreadyRequested(toFloor)) {
                        FSM_LOGGER.info("Floor {} is already requested.", toFloor);
                    } else {
                        if (toFloor > currentFloor) {
                            upwardsTargetFloors.offer(toFloor);
                        } else if (toFloor < currentFloor) {
                            downwardsTargetFloors.offer(toFloor);
                        } else {
                            FSM_LOGGER.info("Currently at the requested floor: " + toFloor);
                        }
                    }
                    break;
                default:
                    throwIllegalStateTransitionException(floorRequested);
            }
        }

        private void onMaintain(Maintain maintain) {
            switch (currentState.getToken()) {
                case IDLE:
                    updateStatusOnMaintain();
                    break;
            }
        }

        public void onPowerOff(PowerOff powerOff) {
            updateStatusOnMaintain();
        }

        public void onUserWaitingRequest(UserWaiting userWaiting) {

        }

        void updateStatusOnBackToService() {
            currentState = StateFactory.createIdle();
        }

        void updateStatusOnEmergency() {
            currentState = StateFactory.createMaintenance();
        }

        void toMovingUp() {
            direction = Direction.UP;
            currentState = StateFactory.createMovingUp();
            // doMovingUp()
            // stopMotor()
        }

        void toMovingDown() {
            direction = Direction.DOWN;
            currentState = StateFactory.createMovingDown();
            // doMovingDown()
            // stopMotor()
        }

        void updateStatusOnMaintain() {
            currentState = StateFactory.createMaintenance();
        }

        void updateStatusOnArrive(int arrivedFloor) {

            currentFloor = arrivedFloor;

            if (isFloorAlreadyRequested(arrivedFloor)) {
                currentState = StateFactory.createJustArrived();
            }

            // otherwise just bypass
        }

        private boolean isFloorAlreadyRequested(int arrivedFloor) {
            return upwardsTargetFloors.contains(arrivedFloor) || downwardsTargetFloors.contains(arrivedFloor);
        }

        void updateStatusOnDoorOpen() {
            currentState = StateFactory.createDoorOpening();
        }

        void updateStatusOnDoorFailure() {
            currentState = StateFactory.createMaintenance();
        }

        void updateStatusOnDoorOpened() {
            currentState = StateFactory.createDoorOpened();
        }

        void updateStatusOnDoorClosed() {
            currentState = StateFactory.createReadyToMove();
        }

        void toDoorClosing() {
            currentState = StateFactory.createDoorClosing();
        }

        void toReadyToMove() {
            currentState = StateFactory.createReadyToMove();
        }

        private void throwIllegalStateTransitionException(Event event) {
            String exceptionMessage = "Event: " + event.getToken() + " should not happen under state: " + currentState.getToken();

            FSM_LOGGER.fatal(exceptionMessage);

            throw new IllegalArgumentException(exceptionMessage);
        }

    }
}
