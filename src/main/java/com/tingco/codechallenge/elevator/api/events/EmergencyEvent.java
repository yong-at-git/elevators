package com.tingco.codechallenge.elevator.api.events;

/**
 * Created by Yong Huang on 2017-11-21.
 */
public class EmergencyEvent implements Event {

    @Override public String toString() {
        return "EmergencyEvent";
    }

    @Override public EventName getName() {
        return EventName.EMERGENCY;
    }
}
