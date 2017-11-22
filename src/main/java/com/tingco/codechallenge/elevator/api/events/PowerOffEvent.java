package com.tingco.codechallenge.elevator.api.events;

/**
 * Created by Yong Huang on 2017-11-21.
 */
public class PowerOffEvent implements Event {
    @Override public String toString() {
        return "PowerOffEvent";
    }

    @Override public EventName getName() {
        return EventName.POWER_OFF;
    }
}
