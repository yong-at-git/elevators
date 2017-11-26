package com.tingco.codechallenge.elevator.api.events.impl;

import com.tingco.codechallenge.elevator.api.events.Event;
import com.tingco.codechallenge.elevator.api.events.EventToken;

/**
 * Created by Yong Huang on 2017-11-26.
 */
public class NewlyFree implements Event {
    private int elevatorId;

    public NewlyFree(int elevatorId) {
        this.elevatorId = elevatorId;
    }

    @Override public EventToken getToken() {
        return null;
    }

    @Override public int getElevatorId() {
        return this.elevatorId;
    }
}
