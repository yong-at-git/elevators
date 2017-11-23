package com.tingco.codechallenge.elevator.api.states.impl;

import com.tingco.codechallenge.elevator.api.states.ElevatorState;
import com.tingco.codechallenge.elevator.api.states.ElevatorStateToken;

/**
 * Created by Yong Huang on 2017-11-23.
 */
public class Idle implements ElevatorState {
    @Override public ElevatorStateToken getToken() {
        return ElevatorStateToken.IDLE;
    }
}
