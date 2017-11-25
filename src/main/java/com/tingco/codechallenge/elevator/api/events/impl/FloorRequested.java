package com.tingco.codechallenge.elevator.api.events.impl;

import com.tingco.codechallenge.elevator.api.events.Event;

/**
 * Created by Yong Huang on 2017-11-24.
 */
public abstract class FloorRequested implements Event {
    public abstract int getToFloor();
}
