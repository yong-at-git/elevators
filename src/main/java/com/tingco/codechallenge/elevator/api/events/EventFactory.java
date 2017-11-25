package com.tingco.codechallenge.elevator.api.events;

import com.tingco.codechallenge.elevator.api.ElevatorImpl;
import com.tingco.codechallenge.elevator.api.events.impl.ArriveFloor;
import com.tingco.codechallenge.elevator.api.events.impl.BackToService;
import com.tingco.codechallenge.elevator.api.events.impl.CloseDoor;
import com.tingco.codechallenge.elevator.api.events.impl.DoorClosed;
import com.tingco.codechallenge.elevator.api.events.impl.DoorFailure;
import com.tingco.codechallenge.elevator.api.events.impl.DoorInterrupted;
import com.tingco.codechallenge.elevator.api.events.impl.FloorRequestedWithNumberPreference;
import com.tingco.codechallenge.elevator.api.events.impl.OpenDoor;
import com.tingco.codechallenge.elevator.api.events.impl.DoorOpened;
import com.tingco.codechallenge.elevator.api.events.impl.Emergency;
import com.tingco.codechallenge.elevator.api.events.impl.Maintain;
import com.tingco.codechallenge.elevator.api.events.impl.PowerOff;
import com.tingco.codechallenge.elevator.api.events.impl.FloorRequestedWithDirectionPreference;

/**
 * Created by Yong Huang on 2017-11-23.
 */
public class EventFactory {
    private EventFactory() {
        // no-arg constructor for factory
    }

    public static final ArriveFloor createArriveFloor(int floor) {
        return new ArriveFloor(floor);
    }

    public static final BackToService createBackToService() {
        return new BackToService();
    }

    public static final CloseDoor createCloseDoor(){
        return new CloseDoor();
    }

    public static final DoorClosed createDoorClosed() {
        return new DoorClosed();
    }

    public static final DoorFailure createDoorFailure() {
        return new DoorFailure();
    }

    public static final DoorInterrupted createDoorInterrupted() {
        return new DoorInterrupted();
    }

    public static final OpenDoor createOpenDoor() {
        return new OpenDoor();
    }

    public static final DoorOpened createDoorOpened() {
        return new DoorOpened();
    }

    public static final Emergency createEmergency() {
        return new Emergency();
    }

    public static final FloorRequestedWithNumberPreference createFloorRequested(int toFloor) {
        return new FloorRequestedWithNumberPreference(toFloor);
    }

    public static final Maintain createMaintain() {
        return new Maintain();
    }

    public static final PowerOff createPowerOff() {
        return new PowerOff();
    }

    public static final FloorRequestedWithDirectionPreference createUserWaiting(int atFloor, ElevatorImpl.Direction towards) {
        return new FloorRequestedWithDirectionPreference(atFloor, towards);
    }
}
