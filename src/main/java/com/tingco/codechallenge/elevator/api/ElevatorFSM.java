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
import com.tingco.codechallenge.elevator.api.states.StateFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by Yong Huang on 2017-11-24.
 */
class ElevatorFSM {
    private final ElevatorImpl elevator;
    private static final Logger LOGGER = LogManager.getLogger(ElevatorFSM.class);

    ElevatorFSM(ElevatorImpl elevator) {
        this.elevator = elevator;
    }

    /*
     * These onEvent methods capture event, check condition(i.e. FSM guard),
     * then delegate status update tasks to the update methods.
     */
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

    void onDoorClosed(DoorClosed doorClosed) {
        switch (elevator.getCurrentState().getToken()) {
            case DOOR_OPENING:
                updateStatusOnDoorClosed();
                break;
            default:
                throwIllegalStateTransitionException(doorClosed);
        }
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
        switch (elevator.getCurrentState().getToken()) {
            case IDLE:
            case MOVING_UP:
            case MOVING_DOWN:
            case JUST_ARRIVED:
            case DOOR_OPENING:
            case DOOR_OPENED:
            case DOOR_CLOSING:
            case READY_TO_MOVE:
                updateStatusOnFloorRequested(floorRequested);
                break;
            default:
                throwIllegalStateTransitionException(floorRequested);
        }
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

    void onPowerOff(PowerOff powerOff) {
        updateStatusOnPowerOff();
    }

    void onUserWaitingRequest(UserWaiting userWaiting) {
        switch (elevator.getCurrentState().getToken()) {
            case IDLE:
            case MOVING_UP:
            case MOVING_DOWN:
            case JUST_ARRIVED:
            case DOOR_OPENING:
            case DOOR_OPENED:
            case DOOR_CLOSING:
            case READY_TO_MOVE:
                updateStatusOnUserWaitingRequest(userWaiting);
                break;
            default:
                throwIllegalStateTransitionException(userWaiting);
        }
    }

    private void updateStatusOnFloorRequested(FloorRequested floorRequested) {
        Elevator.Direction towards = handleFloorRequestQueue(floorRequested);
        moving(towards);
    }

    private void updateStatusOnUserWaitingRequest(UserWaiting userWaiting) {
        handleFloorRequestQueue(userWaiting);
        moving(userWaiting.getTowards());
    }

    private Elevator.Direction handleFloorRequestQueue(FloorMovementRequest floorRequested) {
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
                    LOGGER.info("Currently at the requested floor: " + toFloor);
                }
                break;
            case MOVING_UP:
                if (isFloorAlreadyRequested(toFloor)) {
                    LOGGER.info("Floor {} is already requested.", toFloor);
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
                    LOGGER.info("Floor {} is already requested.", toFloor);
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
                    LOGGER.info("Floor {} is already requested.", toFloor);
                } else {
                    if (toFloor > elevator.currentFloor()) {
                        elevator.getUpwardsTargetFloors().offer(toFloor);
                        towards = Elevator.Direction.UP;
                    } else if (toFloor < elevator.currentFloor()) {
                        elevator.getDownwardsTargetFloors().offer(toFloor);
                        towards = Elevator.Direction.DOWN;
                    } else {
                        LOGGER.info("Currently at the requested floor: " + toFloor);
                    }
                }
                break;
            default:
                throwIllegalStateTransitionException(floorRequested);
        }

        return towards;
    }

    private void updateStatusOnBackToService() {
        this.elevator.setCurrentState(StateFactory.createIdle());
    }

    private void updateStatusOnEmergency() {
        this.elevator.setCurrentState(StateFactory.createMaintenance());
    }

    private void updateStatusOnPowerOff() {
        this.elevator.setCurrentState(StateFactory.createMaintenance());
    }

    private void moving(Elevator.Direction requestedDirection) {
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

    private void movingUp(Elevator.Direction requestDirection) {
        LOGGER.info("Moving down upon requested direction: {}", requestDirection);

        this.elevator.setDirection(requestDirection);
        this.elevator.setCurrentState(StateFactory.createMovingUp());
        doMovingUp();
        stopMotor();
    }

    private void doMovingUp() {
        LOGGER.info("Moving up.");
        try {
            Thread.sleep(2 * 1000);
        } catch (InterruptedException e) {
            stopMotor();
            LOGGER.info("Emergency happened.");
            elevator.eventBus.post(EventFactory.createEmergency());
        }
    }

    private void stopMotor() {
        LOGGER.info("Stopping motor.");
    }

    private void movingDown(Elevator.Direction requestDirection) {
        LOGGER.info("Moving down upon requested direction: {}", requestDirection);

        this.elevator.setDirection(requestDirection);
        this.elevator.setCurrentState(StateFactory.createMovingDown());
        doMovingDown();
        stopMotor();
    }

    private void doMovingDown() {
        LOGGER.info("Moving down");
        try {
            Thread.sleep(2 * 1000);
        } catch (InterruptedException e) {
            stopMotor();
            LOGGER.info("Emergency happened.");
            elevator.eventBus.post(EventFactory.createEmergency());
        }
    }

    private void updateStatusOnMaintain() {
        this.elevator.setCurrentState(StateFactory.createMaintenance());
    }

    private void updateStatusOnArrive(int arrivedFloor) {
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

    private void updateStatusOnDoorOpen() {
        this.elevator.setCurrentState(StateFactory.createDoorOpening());
    }

    private void updateStatusOnDoorFailure() {
        this.elevator.setCurrentState(StateFactory.createMaintenance());
    }

    private void updateStatusOnDoorOpened() {
        this.elevator.setCurrentState(StateFactory.createDoorOpened());
    }

    private void updateStatusOnDoorClosed() {
        this.elevator.setCurrentState(StateFactory.createReadyToMove());

        if (this.elevator.getDownwardsTargetFloors().isEmpty() && this.elevator.getUpwardsTargetFloors().isEmpty()) {
            // none request left, going idle
            this.elevator.setCurrentState(StateFactory.createIdle());
            this.elevator.setDirection(Elevator.Direction.NONE);
        } else if (this.elevator.getDownwardsTargetFloors().isEmpty()) {
            // only upwards request, going up
            this.elevator.setCurrentState(StateFactory.createMovingUp());
            this.elevator.setDirection(Elevator.Direction.UP);
        } else if (this.elevator.getUpwardsTargetFloors().isEmpty()) {
            // only downwards request, going down
            this.elevator.setCurrentState(StateFactory.createMovingDown());
            this.elevator.setDirection(Elevator.Direction.DOWN);
        } else {
            // still have both upwards and downwards requests
            if (this.elevator.isOnUpPath()) {
                // keep moving up
                this.elevator.setCurrentState(StateFactory.createMovingUp());
            } else {
                // keep moving down
                this.elevator.setCurrentState(StateFactory.createMovingDown());
            }
        }
    }

    private void throwIllegalStateTransitionException(Event event) {
        String exceptionMessage = "Event: " + event.getToken() + " should not happen under state: " + elevator.getCurrentState().getToken();

        LOGGER.fatal(exceptionMessage);
        throw new IllegalArgumentException(exceptionMessage);
    }
}
