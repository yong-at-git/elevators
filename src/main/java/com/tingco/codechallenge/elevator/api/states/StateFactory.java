package com.tingco.codechallenge.elevator.api.states;

import com.tingco.codechallenge.elevator.api.states.impl.DoorClosing;
import com.tingco.codechallenge.elevator.api.states.impl.DoorOpened;
import com.tingco.codechallenge.elevator.api.states.impl.DoorOpening;
import com.tingco.codechallenge.elevator.api.states.impl.Idle;
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

    public static final DoorClosing createDoorClosing() {
        return new DoorClosing();
    }

    public static final DoorOpened createDoorOpened() {
        return new DoorOpened();
    }

    public static final DoorOpening createDoorOpening() {
        return new DoorOpening();
    }

    public static final Idle createIdle() {
        return new Idle();
    }

    public static final JustArrived createJustArrived() {
        return new JustArrived();
    }

    public static final Maintenance createMaintenance() {
        return new Maintenance();
    }

    public static final MovingUp createMovingUp() {
        return new MovingUp();
    }

    public static final MovingDown createMovingDown() {
        return new MovingDown();
    }

    public static final ReadyToMove createReadyToMove() {
        return new ReadyToMove();
    }
}
