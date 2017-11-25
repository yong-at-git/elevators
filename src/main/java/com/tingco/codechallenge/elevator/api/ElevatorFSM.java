package com.tingco.codechallenge.elevator.api;

import com.tingco.codechallenge.elevator.api.events.Event;
import com.tingco.codechallenge.elevator.api.events.EventFactory;
import com.tingco.codechallenge.elevator.api.events.EventToken;
import com.tingco.codechallenge.elevator.api.events.impl.ArriveFloor;
import com.tingco.codechallenge.elevator.api.events.impl.BackToService;
import com.tingco.codechallenge.elevator.api.events.impl.CloseDoor;
import com.tingco.codechallenge.elevator.api.events.impl.DoorClosed;
import com.tingco.codechallenge.elevator.api.events.impl.DoorFailure;
import com.tingco.codechallenge.elevator.api.events.impl.DoorOpened;
import com.tingco.codechallenge.elevator.api.events.impl.FloorRequested;
import com.tingco.codechallenge.elevator.api.events.impl.FloorRequestedWithDirectionPreference;
import com.tingco.codechallenge.elevator.api.events.impl.FloorRequestedWithNumberPreference;
import com.tingco.codechallenge.elevator.api.events.impl.Maintain;
import com.tingco.codechallenge.elevator.api.events.impl.OpenDoor;
import com.tingco.codechallenge.elevator.api.events.impl.PowerOff;
import com.tingco.codechallenge.elevator.api.states.ElevatorStateToken;
import com.tingco.codechallenge.elevator.api.states.StateFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * This FSM class simulates the behaviors of an elevator, based on its current state and the triggering event.
 * It performs guard check upon receiving events, e.g., if it's valid case that an event happens at current state.
 * When the guard check gives positive result, it then performs state transition.
 * <p>
 * Created by Yong Huang on 2017-11-24.
 */
class ElevatorFSM {
    private final ElevatorImpl elevator;
    private static final Logger LOGGER = LogManager.getLogger(ElevatorFSM.class);
    private volatile boolean isMotorRunning = false;
    private final Queue<EventToken> EVENT_LOG = new ConcurrentLinkedDeque<>();

    ElevatorFSM(ElevatorImpl elevator) {
        this.elevator = elevator;
    }

    /*
     * These onEvent methods capture event, check condition(i.e. FSM guard),
     * then delegate state transition tasks to the updateStatus methods.
     */
    void onArrive(ArriveFloor arriveFloor) {
        switch (elevator.getCurrentState().getToken()) {
            case MOVING_UP:
            case MOVING_DOWN:
                EVENT_LOG.offer(arriveFloor.getToken());
                updateStatusOnArrive(arriveFloor.getAtFloor());
                break;
            default:
                throwIllegalStateTransitionException(arriveFloor);
        }
    }

    void onBackToService(BackToService backToService) {
        switch (elevator.getCurrentState().getToken()) {
            case MAINTENANCE:
                EVENT_LOG.offer(backToService.getToken());
                updateStatusOnBackToService();
                break;
            default:
                throwIllegalStateTransitionException(backToService);
        }
    }

    void onCloseDoor(CloseDoor closeDoor) {
        switch (elevator.getCurrentState().getToken()) {
            case DOOR_OPENED:
                EVENT_LOG.offer(closeDoor.getToken());
                updateStatusOnCloseDoor();
                break;
            default:
                throwIllegalStateTransitionException(closeDoor);
        }
    }

    void onDoorClosed(DoorClosed doorClosed) {
        switch (elevator.getCurrentState().getToken()) {
            case DOOR_CLOSING:
                EVENT_LOG.offer(doorClosed.getToken());
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
                EVENT_LOG.offer(doorFailure.getToken());
                updateStatusOnDoorFailure();
                break;
            default:
                throwIllegalStateTransitionException(doorFailure);
        }
    }

    void onOpenDoor(OpenDoor openDoor) {
        switch (elevator.getCurrentState().getToken()) {
            case JUST_ARRIVED:
            case DOOR_OPENING:
            case DOOR_CLOSING:
                EVENT_LOG.offer(openDoor.getToken());
                updateStatusOnOpenDoor();
                break;
            default:
                throwIllegalStateTransitionException(openDoor);
        }
    }

    void onDoorOpened(DoorOpened doorOpened) {
        switch (elevator.getCurrentState().getToken()) {
            case DOOR_OPENING:
                EVENT_LOG.offer(doorOpened.getToken());
                updateStatusOnDoorOpened();
                break;
            default:
                throwIllegalStateTransitionException(doorOpened);
        }
    }

    void onEmergency() {
        updateStatusOnEmergency();
    }

    void onFloorRequested(FloorRequestedWithNumberPreference floorRequestedWithNumberPreference) {
        switch (elevator.getCurrentState().getToken()) {
            case IDLE:
            case MOVING_UP:
            case MOVING_DOWN:
            case JUST_ARRIVED:
            case DOOR_OPENING:
            case DOOR_OPENED:
            case DOOR_CLOSING:
                EVENT_LOG.offer(floorRequestedWithNumberPreference.getToken());
                updateStatusOnFloorRequested(floorRequestedWithNumberPreference);
                break;
            default:
                throwIllegalStateTransitionException(floorRequestedWithNumberPreference);
        }
    }

    void onMaintain(Maintain maintain) {
        switch (elevator.getCurrentState().getToken()) {
            case IDLE:
                EVENT_LOG.offer(maintain.getToken());
                updateStatusOnMaintain();
                break;
            default:
                throwIllegalStateTransitionException(maintain);
        }
    }

    void onPowerOff(PowerOff powerOff) {
        EVENT_LOG.offer(powerOff.getToken());
        updateStatusOnPowerOff();
    }

    void onUserWaitingRequest(FloorRequestedWithDirectionPreference floorRequestedWithDirectionPreference) {
        switch (elevator.getCurrentState().getToken()) {
            case IDLE:
            case MOVING_UP:
            case MOVING_DOWN:
            case JUST_ARRIVED:
            case DOOR_OPENING:
            case DOOR_OPENED:
            case DOOR_CLOSING:
                EVENT_LOG.offer(floorRequestedWithDirectionPreference.getToken());
                updateStatusOnUserWaitingRequest(floorRequestedWithDirectionPreference);
                break;
            default:
                throwIllegalStateTransitionException(floorRequestedWithDirectionPreference);
        }
    }

    private void updateStatusOnCloseDoor() {
        this.elevator.setCurrentState(StateFactory.createDoorClosing());

        try {
            LOGGER.info("Closing door.");
            Thread.sleep(this.elevator.getConfiguration().getDoorClosingDurationInMs());
        } catch (InterruptedException e) {
            LOGGER.info("Emergency happened.");
            elevator.getEventBus().post(EventFactory.createEmergency(this.elevator.getId()));
        }

        this.elevator.getEventBus().post(EventFactory.createDoorClosed(this.elevator.getId()));
    }

    private void updateStatusOnFloorRequested(FloorRequestedWithNumberPreference floorRequestedWithNumberPreference) {
        Elevator.Direction towards = handleFloorRequestQueue(floorRequestedWithNumberPreference);

        if (ElevatorStateToken.IDLE.equals(this.elevator.getCurrentState().getToken())) {
            // only move while idle, otherwise leave the request queue to be handled.
            this.elevator.setDirection(towards);
            move(towards);
        }
    }

    private void updateStatusOnUserWaitingRequest(FloorRequestedWithDirectionPreference floorRequestedWithDirectionPreference) {
        Elevator.Direction towards = handleFloorRequestQueue(floorRequestedWithDirectionPreference);

        if (ElevatorStateToken.IDLE.equals(this.elevator.getCurrentState().getToken())) {
            // only move while idle, otherwise leave the request queue to be handled.
            // move towards preferred direction.
            Elevator.Direction preferredDirection = floorRequestedWithDirectionPreference.getTowards();
            LOGGER.info(
                "Floor requested at floor {} and with preferred direction: {}",
                floorRequestedWithDirectionPreference.getWaitingFloor(),
                preferredDirection);

            this.elevator.setDirection(preferredDirection);
            move(towards);
        }
    }

    // Update request queues and return the move direction, which is done by comparing current and requested floor numbers.
    // The caller of this method could ignore the returned direction, if the request has preferred direction.
    private Elevator.Direction handleFloorRequestQueue(FloorRequested floorRequested) {
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

    private void move(Elevator.Direction requestedDirection) {
        switch (requestedDirection) {
            case UP:
                startMotor();
                LOGGER.info("Moving up upon requested direction: {}", requestedDirection);

                this.elevator.setCurrentState(StateFactory.createMovingUp());
                doMovingUp();
                break;
            case DOWN:
                startMotor();
                LOGGER.info("Moving down upon requested direction: {}", requestedDirection);

                this.elevator.setCurrentState(StateFactory.createMovingDown());
                doMovingDown();
                break;
            default:
                LOGGER.info("No movement on direction: {}", requestedDirection);
                break;
        }
    }

    private void doMovingUp() {
        EVENT_LOG.offer(EventToken.MOVING_UP);

        try {
            while (isMotorRunning) {
                Thread.sleep(this.elevator.getConfiguration().getMovingDurationBetweenFloorsInMs());

                int currentFloor = this.elevator.currentFloor();
                int arrivingFloor = currentFloor + 1;
                this.elevator.getEventBus().post(EventFactory.createArriveFloor(this.elevator.getId(), arrivingFloor));

                Thread.sleep(this.elevator.getConfiguration().getWaitingDurationAfterNotifyingArrivingInMs());
            }
        } catch (InterruptedException e) {
            stopMotor();
            LOGGER.info("Emergency happened.");
            elevator.getEventBus().post(EventFactory.createEmergency(this.elevator.getId()));
        }
    }

    private void doMovingDown() {
        EVENT_LOG.offer(EventToken.MOVING_DOWN);

        try {
            while (isMotorRunning) {
                Thread.sleep(this.elevator.getConfiguration().getMovingDurationBetweenFloorsInMs());

                int currentFloor = this.elevator.currentFloor();
                int arrivingFloor = currentFloor - 1;
                this.elevator.getEventBus().post(EventFactory.createArriveFloor(this.elevator.getId(), arrivingFloor));

                Thread.sleep(this.elevator.getConfiguration().getWaitingDurationAfterNotifyingArrivingInMs());
            }

        } catch (InterruptedException e) {
            stopMotor();
            LOGGER.info("Emergency happened.");
            elevator.getEventBus().post(EventFactory.createEmergency(this.elevator.getId()));
        }
    }

    private void startMotor() {
        this.isMotorRunning = true;
        LOGGER.info("Starting motor.");
    }

    private void stopMotor() {
        this.isMotorRunning = false;
        LOGGER.info("Stopping motor.");
    }

    private void updateStatusOnMaintain() {
        this.elevator.setCurrentState(StateFactory.createMaintenance());
    }

    private void updateStatusOnArrive(int arrivedFloor) {
        LOGGER.info("Arriving floor: {}", arrivedFloor);
        this.elevator.setCurrentFloor(arrivedFloor);

        if (!isFloorAlreadyRequested(arrivedFloor)) {
            // bypass unrequested floor
            return;
        }

        stopMotor();
        this.elevator.setCurrentState(StateFactory.createJustArrived());
        if (this.elevator.isOnUpPath()) {
            if (this.elevator.getUpwardsTargetFloors().isEmpty()) {
                // user waiting downstairs and wants to go upstairs
                this.elevator.getDownwardsTargetFloors().poll();
            } else {
                this.elevator.getUpwardsTargetFloors().poll();
            }
        } else {
            if (this.elevator.getDownwardsTargetFloors().isEmpty()) {
                // user waiting upstairs and wants to go downstairs
                this.elevator.getUpwardsTargetFloors().poll();
            } else {
                this.elevator.getDownwardsTargetFloors().poll();
            }
        }
        this.elevator.getEventBus().post(EventFactory.createOpenDoor(this.elevator.getId()));
    }

    private boolean isFloorAlreadyRequested(int arrivedFloor) {
        return this.elevator.getUpwardsTargetFloors().contains(arrivedFloor)
            || this.elevator.getDownwardsTargetFloors().contains(arrivedFloor);
    }

    private void updateStatusOnOpenDoor() {
        this.elevator.setCurrentState(StateFactory.createDoorOpening());

        try {
            LOGGER.info("Opening door.");
            Thread.sleep(this.elevator.getConfiguration().getDoorOpeningDurationInMs());
        } catch (InterruptedException e) {
            LOGGER.info("Emergency happened.");
            elevator.getEventBus().post(EventFactory.createEmergency(this.elevator.getId()));
        }

        this.elevator.getEventBus().post(EventFactory.createDoorOpened(this.elevator.getId()));
    }

    private void updateStatusOnDoorFailure() {
        this.elevator.setCurrentState(StateFactory.createMaintenance());
    }

    private void updateStatusOnDoorOpened() {
        this.elevator.setCurrentState(StateFactory.createDoorOpened());

        try {
            LOGGER.info("Door opened. Waiting for user to get off and on.");
            Thread.sleep(this.elevator.getConfiguration().getDoorOpenedWaitingDurationInMs());
        } catch (InterruptedException e) {
            LOGGER.info("Emergency happened.");
            elevator.getEventBus().post(EventFactory.createEmergency(this.elevator.getId()));
        }

        this.elevator.getEventBus().post(EventFactory.createCloseDoor(this.elevator.getId()));
    }

    private void updateStatusOnDoorClosed() {
        if (this.elevator.getDownwardsTargetFloors().isEmpty() && this.elevator.getUpwardsTargetFloors().isEmpty()) {
            LOGGER.info("Door Closed. No remaining request. Going idle.");
            this.elevator.setCurrentState(StateFactory.createIdle());
            this.elevator.setDirection(Elevator.Direction.NONE);
        } else if (this.elevator.getDownwardsTargetFloors().isEmpty()) {
            LOGGER.info("Door Closed. No downwards request left. Going up.");
            int toFloor = this.elevator.getUpwardsTargetFloors().peek();
            move(Elevator.Direction.UP);
        } else if (this.elevator.getUpwardsTargetFloors().isEmpty()) {
            LOGGER.info("Door Closed. No upwards request left. Going down.");
            int toFloor = this.elevator.getDownwardsTargetFloors().peek();
            move(Elevator.Direction.DOWN);
        } else {
            if (this.elevator.isOnUpPath()) {
                LOGGER.info("Door Closed. Keep moving up.");
                int toFloor = this.elevator.getUpwardsTargetFloors().peek();
                move(Elevator.Direction.UP);
            } else {
                LOGGER.info("Door Closed. Keep moving down.");
                int toFloor = this.elevator.getDownwardsTargetFloors().peek();
                move(Elevator.Direction.DOWN);
            }
        }
    }

    private void throwIllegalStateTransitionException(Event event) {
        String exceptionMessage = "Event: " + event.getToken() + " should not happen under state: " + elevator.getCurrentState().getToken();
        LOGGER.fatal(exceptionMessage);
        updateStatusOnEmergency();
    }

    Queue<EventToken> getEVENT_LOG() {
        return EVENT_LOG;
    }
}
