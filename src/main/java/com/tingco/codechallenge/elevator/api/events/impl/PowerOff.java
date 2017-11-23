package com.tingco.codechallenge.elevator.api.events.impl;

import com.tingco.codechallenge.elevator.api.events.Event;
import com.tingco.codechallenge.elevator.api.events.EventToken;

/**
 * Created by Yong Huang on 2017-11-21.
 */
public class PowerOff implements Event {
    @Override public String toString() {
        return "PowerOff";
    }

    @Override public EventToken getToken() {
        return EventToken.POWER_OFF;
    }
}
