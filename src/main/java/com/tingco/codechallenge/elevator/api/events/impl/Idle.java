package com.tingco.codechallenge.elevator.api.events.impl;

import com.tingco.codechallenge.elevator.api.events.Event;
import com.tingco.codechallenge.elevator.api.events.EventToken;

/**
 * Created by Yong Huang on 2017-11-26.
 */
public class Idle implements Event {
    private int receiverElevatorId;

    public Idle(int receiverElevatorId) {
        this.receiverElevatorId = receiverElevatorId;
    }

    @Override public EventToken getToken() {
        return EventToken.IDLE;
    }

    @Override public int getElevatorId() {
        return this.receiverElevatorId;
    }
}
