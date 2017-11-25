package com.tingco.codechallenge.elevator.api.events.impl;

import com.tingco.codechallenge.elevator.api.events.Event;
import com.tingco.codechallenge.elevator.api.events.EventToken;

/**
 * Created by Yong Huang on 2017-11-22.
 */
public class BackToService implements Event {
    private int receiverElevatorId;

    public BackToService(int receiverElevatorId) {
        this.receiverElevatorId = receiverElevatorId;
    }

    @Override public EventToken getToken() {
        return EventToken.BACK_TO_SERVICE;
    }

    @Override public int getReceiverElevatorId() {
        return receiverElevatorId;
    }
}
