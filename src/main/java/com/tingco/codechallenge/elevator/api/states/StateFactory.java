package com.tingco.codechallenge.elevator.api.states;

import com.tingco.codechallenge.elevator.api.states.impl.DoorClosing;
import com.tingco.codechallenge.elevator.api.states.impl.DoorOpened;
import com.tingco.codechallenge.elevator.api.states.impl.DoorOpening;
import com.tingco.codechallenge.elevator.api.states.impl.JustArrived;
import com.tingco.codechallenge.elevator.api.states.impl.Maintenance;
import com.tingco.codechallenge.elevator.api.states.impl.MovingDown;
import com.tingco.codechallenge.elevator.api.states.impl.MovingUp;
import com.tingco.codechallenge.elevator.api.states.impl.ReadyToMove;

/**
 * Created by Yong Huang on 2017-11-23.
 */
public class StateFactory {
    private StateFactory() {
        // no arg constructor of factory
    }

    public static DoorClosing createDoorClosing() {
        return new DoorClosing();
    }

    public static DoorOpened createDoorOpened() {
        return new DoorOpened();
    }

    public static DoorOpening createDoorOpening() {
        return new DoorOpening();
    }

    public static JustArrived createJustArrived() {
        return new JustArrived();
    }

    public static Maintenance createMaintenance() {
        return new Maintenance();
    }

    public static MovingUp createMovingUp() {
        return new MovingUp();
    }

    public static MovingDown createMovingDown() {
        return new MovingDown();
    }

    public static ReadyToMove createReadyToMove() {
        return new ReadyToMove();
    }
}
