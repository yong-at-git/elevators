package com.tingco.codechallenge.elevator.api;

import com.tingco.codechallenge.elevator.api.events.Event;
import com.tingco.codechallenge.elevator.api.events.EventFactory;
import com.tingco.codechallenge.elevator.api.events.impl.ArriveFloor;
import com.tingco.codechallenge.elevator.api.events.impl.BackToService;
import com.tingco.codechallenge.elevator.api.events.impl.DoorClosed;
import com.tingco.codechallenge.elevator.api.events.impl.DoorFailure;
import com.tingco.codechallenge.elevator.api.events.impl.DoorOpen;
import com.tingco.codechallenge.elevator.api.events.impl.DoorOpened;
import com.tingco.codechallenge.elevator.api.events.impl.FloorMovementRequest;
import com.tingco.codechallenge.elevator.api.events.impl.FloorRequested;
import com.tingco.codechallenge.elevator.api.events.impl.Maintain;
import com.tingco.codechallenge.elevator.api.events.impl.PowerOff;
import com.tingco.codechallenge.elevator.api.events.impl.UserWaiting;
import com.tingco.codechallenge.elevator.api.states.ElevatorState;
import com.tingco.codechallenge.elevator.api.states.StateFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by Yong Huang on 2017-11-24.
 */
class ElevatorFSM {
    private ElevatorImpl elevator;
    private final Logger FSM_LOGGER = LogManager.getLogger(ElevatorFSM.class);

    ElevatorFSM(ElevatorImpl elevator) {
        this.elevator = elevator;
    }

    // These onEvent methods capture event, check condition(i.e. FSM guard), then perform optional actions(e.g. state change)
    void onArrive(ArriveFloor arriveFloor) {
        switch (elevator.getCurrentState().getToken()) {
            case MOVING_UP:
            case MOVING_DOWN:
                updateStatusOnArrive(arriveFloor.getAtFloor());
                break;
            default:
                throwIllegalStateTransitionException(arriveFloor);
        }
    }

    void onBackToService(BackToService backToService) {
        switch (elevator.getCurrentState().getToken()) {
            case MAINTENANCE:
                updateStatusOnBackToService();
                break;
            default:
                throwIllegalStateTransitionException(backToService);
        }
    }

    ElevatorState onDoorClosed(DoorClosed doorClosed) {
        ElevatorState returnState = this.elevator.getCurrentState();

        switch (elevator.getCurrentState().getToken()) {
            case DOOR_OPENING:
                updateStatusOnDoorClosed();
                break;
            default:
                throwIllegalStateTransitionException(doorClosed);
        }

        return returnState;
    }

    void onDoorFailure(DoorFailure doorFailure) {
        switch (elevator.getCurrentState().getToken()) {
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

    void onDoorOpen(DoorOpen doorOpen) {
        switch (elevator.getCurrentState().getToken()) {
            case JUST_ARRIVED:
            case DOOR_OPENING:
            case DOOR_CLOSING:
                updateStatusOnDoorOpen();
                break;
            default:
                throwIllegalStateTransitionException(doorOpen);
        }
    }

    void onDoorOpened(DoorOpened doorOpened) {
        switch (elevator.getCurrentState().getToken()) {
            case DOOR_OPENING:
                updateStatusOnDoorOpened();
                break;
            default:
                throwIllegalStateTransitionException(doorOpened);
        }
    }

    void onEmergency() {
        updateStatusOnEmergency();
    }

    void onFloorRequested(FloorRequested floorRequested) {
        Elevator.Direction towards = handleFloorRequestQueue(floorRequested);
        movingUp(towards);
    }

    void onMaintain(Maintain maintain) {
        switch (elevator.getCurrentState().getToken()) {
            case IDLE:
                updateStatusOnMaintain();
                break;
            default:
                throwIllegalStateTransitionException(maintain);
        }
    }

    public void onPowerOff(PowerOff powerOff) {
        updateStatusOnPowerOff();
    }

    public void onUserWaitingRequest(UserWaiting userWaiting) {
        handleFloorRequestQueue(userWaiting);
        movingUp(userWaiting.getTowards());
    }

    Elevator.Direction handleFloorRequestQueue(FloorMovementRequest floorRequested) {
        int toFloor = floorRequested.getToFloor();
        Elevator.Direction towards = Elevator.Direction.NONE;

        switch (elevator.getCurrentState().getToken()) {
            case IDLE:
                if (toFloor > elevator.currentFloor()) {
                    elevator.getUpwardsTargetFloors().offer(toFloor);
                    towards = Elevator.Direction.UP;
                } else if (toFloor < elevator.currentFloor()) {
                    elevator.getDownwardsTargetFloors().offer(toFloor);
                    towards = Elevator.Direction.DOWN;
                } else {
                    FSM_LOGGER.info("Currently at the requested floor: " + toFloor);
                }
                break;
            case MOVING_UP:
                if (isFloorAlreadyRequested(toFloor)) {
                    FSM_LOGGER.info("Floor {} is already requested.", toFloor);
                } else {
                    if (toFloor > elevator.currentFloor()) {
                        elevator.getUpwardsTargetFloors().offer(toFloor);
                        towards = Elevator.Direction.UP;
                    } else if (toFloor <= elevator.currentFloor()) {
                        elevator.getDownwardsTargetFloors().offer(toFloor);
                        towards = Elevator.Direction.DOWN;
                    }
                }
                break;
            case MOVING_DOWN:
                if (isFloorAlreadyRequested(toFloor)) {
                    FSM_LOGGER.info("Floor {} is already requested.", toFloor);
                } else {
                    if (toFloor >= elevator.currentFloor()) {
                        elevator.getUpwardsTargetFloors().offer(toFloor);
                        towards = Elevator.Direction.UP;
                    } else if (toFloor < elevator.currentFloor()) {
                        elevator.getDownwardsTargetFloors().offer(toFloor);
                        towards = Elevator.Direction.DOWN;
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
                    if (toFloor > elevator.currentFloor()) {
                        elevator.getUpwardsTargetFloors().offer(toFloor);
                        towards = Elevator.Direction.UP;
                    } else if (toFloor < elevator.currentFloor()) {
                        elevator.getDownwardsTargetFloors().offer(toFloor);
                        towards = Elevator.Direction.DOWN;
                    } else {
                        FSM_LOGGER.info("Currently at the requested floor: " + toFloor);
                    }
                }
                break;
            default:
                throwIllegalStateTransitionException(floorRequested);
        }

        return towards;
    }

    void updateStatusOnBackToService() {
        this.elevator.setCurrentState(StateFactory.createIdle());
    }

    void updateStatusOnEmergency() {
        this.elevator.setCurrentState(StateFactory.createMaintenance());
    }

    void updateStatusOnPowerOff() {
        this.elevator.setCurrentState(StateFactory.createMaintenance());
    }

    void moving(Elevator.Direction requestedDirection) {
        switch (requestedDirection) {
            case UP:
                movingUp(requestedDirection);
                break;
            case DOWN:
                movingDown(requestedDirection);
                break;
            default:
                break;
        }
    }

    void movingUp(Elevator.Direction requestDirection) {
        this.elevator.setDirection(requestDirection);
        this.elevator.setCurrentState(StateFactory.createMovingUp());
        doMovingUp();
        stopMotor();
    }

    private void doMovingUp() {
        FSM_LOGGER.info("Moving up.");
        try {
            Thread.sleep(2 * 1000);
        } catch (InterruptedException e) {
            stopMotor();
            FSM_LOGGER.info("Emergency happened.");
            elevator.eventBus.post(EventFactory.createEmergency());
        }
    }

    private void stopMotor() {
        FSM_LOGGER.info("Stopping motor.");
    }

    void movingDown(Elevator.Direction requestDirection) {
        this.elevator.setDirection(Elevator.Direction.DOWN);
        this.elevator.setCurrentState(StateFactory.createMovingDown());
        doMovingDown();
        stopMotor();
    }

    private void doMovingDown() {
        FSM_LOGGER.info("Moving down");
        try {
            Thread.sleep(2 * 1000);
        } catch (InterruptedException e) {
            stopMotor();
            FSM_LOGGER.info("Emergency happened.");
            elevator.eventBus.post(EventFactory.createEmergency());
        }
    }

    void updateStatusOnMaintain() {
        this.elevator.setCurrentState(StateFactory.createMaintenance());
    }

    void updateStatusOnArrive(int arrivedFloor) {
        this.elevator.setCurrentFloor(arrivedFloor);

        if (isFloorAlreadyRequested(arrivedFloor)) {
            this.elevator.setCurrentState(StateFactory.createJustArrived());
        }
        // otherwise just bypass
    }

    private boolean isFloorAlreadyRequested(int arrivedFloor) {
        return this.elevator.getUpwardsTargetFloors().contains(arrivedFloor)
            || this.elevator.getDownwardsTargetFloors().contains(arrivedFloor);
    }

    void updateStatusOnDoorOpen() {
        this.elevator.setCurrentState(StateFactory.createDoorOpening());
    }

    void updateStatusOnDoorFailure() {
        this.elevator.setCurrentState(StateFactory.createMaintenance());
    }

    void updateStatusOnDoorOpened() {
        this.elevator.setCurrentState(StateFactory.createDoorOpened());
    }

    void updateStatusOnDoorClosed() {
        this.elevator.setCurrentState(StateFactory.createReadyToMove());
    }

    private void throwIllegalStateTransitionException(Event event) {
        String exceptionMessage = "Event: " + event.getToken() + " should not happen under state: " + elevator.getCurrentState().getToken();

        FSM_LOGGER.fatal(exceptionMessage);

        throw new IllegalArgumentException(exceptionMessage);
    }

}
