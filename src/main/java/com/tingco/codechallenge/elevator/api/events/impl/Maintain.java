package com.tingco.codechallenge.elevator.api.events.impl;

import com.tingco.codechallenge.elevator.api.events.Event;
import com.tingco.codechallenge.elevator.api.events.EventToken;

/**
 * Created by Yong Huang on 2017-11-22.
 */
public class Maintain implements Event {
    private int receiverElevatorId;

    public Maintain(int receiverElevatorId) {
        this.receiverElevatorId = receiverElevatorId;
    }

    @Override public EventToken getToken() {
        return EventToken.MAINTAIN;
    }

    @Override public int getElevatorId() {
        return receiverElevatorId;
    }
}
