package com.tingco.codechallenge.elevator.api.events.impl;

import com.tingco.codechallenge.elevator.api.events.Event;
import com.tingco.codechallenge.elevator.api.events.EventToken;

/**
 * Created by Yong Huang on 2017-11-23.
 */
public class DoorOpened implements Event {
    private int receiverElevatorId;

    public DoorOpened(int receiverElevatorId) {
        this.receiverElevatorId = receiverElevatorId;
    }

    @Override public EventToken getToken() {
        return EventToken.DOOR_OPENED;
    }

    @Override public int getElevatorId() {
        return receiverElevatorId;
    }
}
