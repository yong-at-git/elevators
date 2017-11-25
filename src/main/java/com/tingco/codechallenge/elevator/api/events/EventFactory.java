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
import com.tingco.codechallenge.elevator.api.events.impl.Maintain;
import com.tingco.codechallenge.elevator.api.events.impl.OpenDoor;
import com.tingco.codechallenge.elevator.api.events.impl.PowerOff;

/**
 * Created by Yong Huang on 2017-11-23.
 */
public class EventFactory {
    private EventFactory() {
        // no-arg constructor for factory
    }

    public static final ArriveFloor createArriveFloor(int receiverElevatorId, int floor) {
        return new ArriveFloor(receiverElevatorId, floor);
    }

    public static final BackToService createBackToService(int receiverElevatorId) {
        return new BackToService(receiverElevatorId);
    }

    public static final CloseDoor createCloseDoor(int receiverElevatorId) {
        return new CloseDoor(receiverElevatorId);
    }

    public static final DoorClosed createDoorClosed(int receiverElevatorId) {
        return new DoorClosed(receiverElevatorId);
    }

    public static final DoorFailure createDoorFailure(int receiverElevatorId) {
        return new DoorFailure(receiverElevatorId);
    }

    public static final OpenDoor createOpenDoor(int receiverElevatorId) {
        return new OpenDoor(receiverElevatorId);
    }

    public static final DoorOpened createDoorOpened(int receiverElevatorId) {
        return new DoorOpened(receiverElevatorId);
    }

    public static final Emergency createEmergency(int receiverElevatorId) {
        return new Emergency(receiverElevatorId);
    }

    public static final FloorRequestedWithNumberPreference createFloorRequested(int receiverElevatorId, int toFloor) {
        return new FloorRequestedWithNumberPreference(toFloor);
    }

    public static final Maintain createMaintain(int receiverElevatorId) {
        return new Maintain(receiverElevatorId);
    }

    public static final PowerOff createPowerOff(int receiverElevatorId) {
        return new PowerOff(receiverElevatorId);
    }

    public static final FloorRequestedWithDirectionPreference createUserWaiting(int receiverElevatorId, ElevatorImpl.Direction towards, int atFloor) {
        return new FloorRequestedWithDirectionPreference(receiverElevatorId, towards, atFloor);
    }
}
