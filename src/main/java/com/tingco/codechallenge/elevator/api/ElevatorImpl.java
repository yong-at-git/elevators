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
import com.tingco.codechallenge.elevator.api.events.impl.DoorOpened;
import com.tingco.codechallenge.elevator.api.events.impl.Emergency;
import com.tingco.codechallenge.elevator.api.events.impl.FloorRequestedWithDirectionPreference;
import com.tingco.codechallenge.elevator.api.events.impl.FloorRequestedWithNumberPreference;
import com.tingco.codechallenge.elevator.api.events.impl.Idle;
import com.tingco.codechallenge.elevator.api.events.impl.Maintain;
import com.tingco.codechallenge.elevator.api.events.impl.OpenDoor;
import com.tingco.codechallenge.elevator.api.events.impl.PowerOff;
import com.tingco.codechallenge.elevator.api.exceptions.OutOfFloorRangeException;
import com.tingco.codechallenge.elevator.api.states.ElevatorState;
import com.tingco.codechallenge.elevator.api.states.ElevatorStateToken;
import com.tingco.codechallenge.elevator.api.states.StateFactory;
import com.tingco.codechallenge.elevator.api.validators.FloorValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by Yong Huang on 2017-11-20.
 */
public class ElevatorImpl implements Elevator {
    private static final Logger LOGGER = LogManager.getLogger(ElevatorImpl.class);

    private volatile Direction direction = Direction.NONE;
    private ElevatorFSM fsm = new ElevatorFSM(this);
    private volatile ElevatorState currentState = StateFactory.createIdle();
    private volatile int currentFloor = 0;
    private int id;
    private EventBus eventBus;
    private ElevatorConfiguration configuration;

    private PriorityBlockingQueue<Integer> upwardsTargetFloors = new PriorityBlockingQueue<>();
    private static final int INIT_CAPACITY = 11; // See PriorityBlockingQueue Javadoc
    private PriorityBlockingQueue<Integer> downwardsTargetFloors = new PriorityBlockingQueue<>(INIT_CAPACITY, Comparator.reverseOrder());

    private ElevatorImpl() {
        // non-arg
    }

    public ElevatorImpl(EventBus eventBus, int id, ElevatorConfiguration configuration) {
        this();
        this.id = id;
        this.eventBus = eventBus;
        this.configuration = configuration;
    }

    /**
     * See also {@link #isOnDownPath()} and {@link #isOnUpPath()}.
     *
     * @return the direction/path of the elevator.
     */
    @Override
    public Direction getDirection() {
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
        this.eventBus.post(EventFactory.createFloorRequested(this.getId(), toFloor));
    }

    @Override public boolean isBusy() {
        return !this.downwardsTargetFloors.isEmpty() || !this.upwardsTargetFloors.isEmpty();
    }

    @Override public int currentFloor() {
        return this.currentFloor;
    }

    /**
     * Check if: 1. an already riding elevator is running upwards or
     * 2. an elevator has been idle and is now requested to move upwards, even though it might need to move down first to pick up the user.
     *
     * @return true if the elevator intends to be on its upwards path; otherwise false.
     */
    public boolean isOnUpPath() {
        return Direction.UP.equals(this.direction);
    }

    /**
     * Check if: 1. an already riding elevator is running downwards or
     * 2. an elevator has been idle and is now requested to move downwards, even though it might need to move up first to pick up the user.
     *
     * @return true if the elevator intends to be on its upwards path; otherwise false.
     */
    public boolean isOnDownPath() {
        return Direction.DOWN.equals(this.direction);
    }

    ElevatorState getCurrentState() {
        return currentState;
    }

    void setCurrentState(ElevatorState newState) {
        LOGGER.info("Elevator={}: Setting state from={} to={}", this.getId(), this.getCurrentState().getToken(), newState.getToken());
        this.currentState = newState;

        if (ElevatorStateToken.IDLE.equals(newState.getToken())) {
            LOGGER.info("Elevator={}: The recorded events={}", this.getId(), this.fsm.getEVENT_LOG());
            this.fsm.getEVENT_LOG().clear();
        }
    }

    void setDirection(Direction direction) {
        this.direction = direction;
    }

    void setCurrentFloor(int newFloor) {
        this.currentFloor = newFloor;
    }

    boolean isFloorAlreadyRequested(int toFloor) {
        return this.upwardsTargetFloors.contains(toFloor) || this.downwardsTargetFloors.contains(toFloor);
    }

    @Subscribe
    public void onArrive(ArriveFloor arriveFloor) {
        if (arriveFloor.getElevatorId() == this.getId()) {
            this.fsm.onArrive(arriveFloor);
        }
    }

    @Subscribe
    public void onBackToService(BackToService backToService) {

        if (backToService.getElevatorId() == this.getId()) {
            this.fsm.onBackToService(backToService);
        }
    }

    @Subscribe
    public void onCloseDoor(CloseDoor closeDoor) {
        if (closeDoor.getElevatorId() == this.getId()) {
            this.fsm.onCloseDoor(closeDoor);
        }
    }

    @Subscribe
    public void onDoorClosed(DoorClosed doorClosed) {
        if (doorClosed.getElevatorId() == this.getId()) {
            this.fsm.onDoorClosed(doorClosed);
        }
    }

    @Subscribe
    public void onDoorFailure(DoorFailure doorFailure) {
        if (doorFailure.getElevatorId() == this.getId()) {
            this.fsm.onDoorFailure(doorFailure);
        }
    }

    @Subscribe
    public void onIdle(Idle idle) {
        if (idle.getElevatorId() == this.getId()) {
            this.fsm.onIdle(idle);
        }
    }

    @Subscribe
    public void onOpenDoor(OpenDoor openDoor) {
        if (openDoor.getElevatorId() == this.getId()) {
            this.fsm.onOpenDoor(openDoor);
        }
    }

    @Subscribe
    public void onDoorOpened(DoorOpened doorOpened) {
        if (doorOpened.getElevatorId() == this.getId()) {
            this.fsm.onDoorOpened(doorOpened);
        }
    }

    @Subscribe
    public void onEmergency(Emergency emergencyEvent) {
        if (emergencyEvent.getElevatorId() == this.getId()) {
            LOGGER.info("Elevator={}: Receiving={} ", this.getId(), emergencyEvent);
            this.fsm.onEmergency();
        }
    }

    @Subscribe
    private void onFloorRequested(FloorRequestedWithNumberPreference floorRequestedWithNumberPreference) throws OutOfFloorRangeException {
        if (floorRequestedWithNumberPreference.getElevatorId() == this.getId()) {
            LOGGER.info("Elevator={}: Receiving {} ", this.getId(), floorRequestedWithNumberPreference);

            int toFloor = floorRequestedWithNumberPreference.getToFloor();
            FloorValidator.validate(toFloor, Range.closed(this.configuration.getBottomFloor(), this.configuration.getTopFloor()));

            this.fsm.onFloorRequested(floorRequestedWithNumberPreference);
        }
    }

    @Subscribe
    private void onMaintain(Maintain maintain) {
        if (maintain.getElevatorId() == this.getId()) {
            LOGGER.info("Elevator={}: Receiving={}", this.getId(), maintain);
            this.fsm.onMaintain(maintain);
        }
    }

    @Subscribe
    public void onPowerOff(PowerOff powerOff) {
        if (powerOff.getElevatorId() == this.getId()) {
            LOGGER.info("Elevator={}: Receiving={} ", this.getId(), powerOff);
            this.fsm.onPowerOff(powerOff);
        }
    }

    @Subscribe
    public void onUserWaitingRequest(FloorRequestedWithDirectionPreference floorRequestedWithDirectionPreference) throws OutOfFloorRangeException {
        if (floorRequestedWithDirectionPreference.getElevatorId() == this.getId()) {
            LOGGER.info("Elevator={}: Receiving={} ", this.getId(), floorRequestedWithDirectionPreference);

            int toFloor = floorRequestedWithDirectionPreference.getToFloor();
            FloorValidator.validate(toFloor, Range.closed(this.configuration.getBottomFloor(), this.configuration.getTopFloor()));

            this.fsm.onUserWaitingRequest(floorRequestedWithDirectionPreference);
        }
    }

    EventBus getEventBus() {
        return eventBus;
    }

    public PriorityBlockingQueue<Integer> getUpwardsTargetFloors() {
        return upwardsTargetFloors;
    }

    public PriorityBlockingQueue<Integer> getDownwardsTargetFloors() {
        return downwardsTargetFloors;
    }

    public ElevatorConfiguration getConfiguration() {
        return configuration;
    }
}
