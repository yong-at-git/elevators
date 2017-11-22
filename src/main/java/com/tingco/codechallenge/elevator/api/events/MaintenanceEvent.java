package com.tingco.codechallenge.elevator.api.events;

/**
 * Created by Yong Huang on 2017-11-22.
 */
public class MaintenanceEvent implements Event {
    @Override public EventName getName() {
        return EventName.MAINTENANCE;
    }
}
