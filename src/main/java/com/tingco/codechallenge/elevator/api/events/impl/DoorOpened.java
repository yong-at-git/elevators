package com.tingco.codechallenge.elevator.api.events.impl;

import com.tingco.codechallenge.elevator.api.events.Event;
import com.tingco.codechallenge.elevator.api.events.EventToken;

/**
 * Created by Yong Huang on 2017-11-23.
 */
public class DoorOpened implements Event {
    @Override public EventToken getToken() {
        return EventToken.DOOR_OPENED;
    }
}
