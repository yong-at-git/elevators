package com.tingco.codechallenge.elevator.api.events;

/**
 * Created by Yong Huang on 2017-11-22.
 */
public class MaintenanceEvent implements Event {
    @Override public EventToken getToken() {
        return EventToken.MAINTENANCE;
    }
}
