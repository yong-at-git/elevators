package com.tingco.codechallenge.elevator.api.events.impl;

import com.tingco.codechallenge.elevator.api.events.Event;
import com.tingco.codechallenge.elevator.api.events.EventToken;

/**
 * Created by Yong Huang on 2017-11-22.
 */
public class Maintain implements Event {
    @Override public EventToken getToken() {
        return EventToken.MAINTENANCE;
    }
}
