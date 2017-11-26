package com.tingco.codechallenge.elevator.api.events.impl;

import com.tingco.codechallenge.elevator.api.events.Event;
import com.tingco.codechallenge.elevator.api.events.EventToken;

/**
 * Created by Yong Huang on 2017-11-24.
 */
public class CloseDoor implements Event {
    private int receiverElevatorId;

    public CloseDoor(int receiverElevatorId) {
        this.receiverElevatorId = receiverElevatorId;
    }

    @Override public EventToken getToken() {
        return EventToken.CLOSE_DOOR;
    }

    @Override public int getElevatorId() {
        return receiverElevatorId;
    }
}
