package com.tingco.codechallenge.elevator.api.events.impl;

import com.tingco.codechallenge.elevator.api.events.Event;
import com.tingco.codechallenge.elevator.api.events.EventToken;

/**
 * Created by Yong Huang on 2017-11-21.
 */
public class Emergency implements Event {
    private int receiverElevatorId;

    public Emergency(int receiverElevatorId) {
        this.receiverElevatorId = receiverElevatorId;
    }

    @Override public String toString() {
        return "Emergency";
    }

    @Override public EventToken getToken() {
        return EventToken.EMERGENCY;
    }

    @Override public int getReceiverElevatorId() {
        return receiverElevatorId;
    }
}
