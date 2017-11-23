package com.tingco.codechallenge.elevator.api.states.impl;

import com.tingco.codechallenge.elevator.api.states.ElevatorStateToken;

/**
 * Created by Yong Huang on 2017-11-23.
 */
public class JustArrived extends StoppedForGettingOnOrOff {
    @Override public ElevatorStateToken getToken() {
        return ElevatorStateToken.JUST_ARRIVED;
    }
}
