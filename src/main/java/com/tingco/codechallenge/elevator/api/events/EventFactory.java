package com.tingco.codechallenge.elevator.api.events;

import com.tingco.codechallenge.elevator.api.ElevatorImpl;
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
import com.tingco.codechallenge.elevator.api.events.impl.NewlyFree;
import com.tingco.codechallenge.elevator.api.events.impl.OpenDoor;
import com.tingco.codechallenge.elevator.api.events.impl.PowerOff;

/**
 * Created by Yong Huang on 2017-11-23.
 */
public class EventFactory {
    private EventFactory() {
        // no-arg constructor for factory
    }

    public static final ArriveFloor createArriveFloor(int elevatorId, int floor) {
        return new ArriveFloor(elevatorId, floor);
    }

    public static final BackToService createBackToService(int elevatorId) {
        return new BackToService(elevatorId);
    }

    public static final CloseDoor createCloseDoor(int elevatorId) {
        return new CloseDoor(elevatorId);
    }

    public static final DoorClosed createDoorClosed(int elevatorId) {
        return new DoorClosed(elevatorId);
    }

    public static final DoorFailure createDoorFailure(int elevatorId) {
        return new DoorFailure(elevatorId);
    }

    public static final OpenDoor createOpenDoor(int elevatorId) {
        return new OpenDoor(elevatorId);
    }

    public static final DoorOpened createDoorOpened(int elevatorId) {
        return new DoorOpened(elevatorId);
    }

    public static final Emergency createEmergency(int elevatorId) {
        return new Emergency(elevatorId);
    }

    public static final FloorRequestedWithNumberPreference createFloorRequested(int elevatorId, int toFloor) {
        return new FloorRequestedWithNumberPreference(toFloor);
    }

    public static final Idle createIdle(int elevatorId) {
        return new Idle(elevatorId);
    }

    public static final Maintain createMaintain(int elevatorId) {
        return new Maintain(elevatorId);
    }

    public static final NewlyFree createNewlyFree(int elevatorId) {
        return new NewlyFree(elevatorId);
    }

    public static final PowerOff createPowerOff(int elevatorId) {
        return new PowerOff(elevatorId);
    }

    public static final FloorRequestedWithDirectionPreference createUserWaiting(int elevatorId, ElevatorImpl.Direction towards, int atFloor) {
        return new FloorRequestedWithDirectionPreference(elevatorId, towards, atFloor);
    }
}
