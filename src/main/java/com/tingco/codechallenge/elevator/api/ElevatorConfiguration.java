package com.tingco.codechallenge.elevator.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Created by Yong Huang on 2017-11-25.
 */

@Component
public class ElevatorConfiguration {
    @Value("${com.tingco.elevator.floor.bottom}")
    private int bottomFloor;

    @Value("${com.tingco.elevator.floor.top}")
    private int topFloor;

    @Value("${com.tingco.elevator.door.opening.duration.in.ms}")
    private int doorOpeningDurationInMs;

    @Value("${com.tingco.elevator.door.opened.waiting.duration.in.ms}")
    private int doorOpenedWaitingDurationInMs;

    @Value("${com.tingco.elevator.door.closing.duration.in.ms}")
    private int doorClosingDurationInMs;

    @Value("${com.tingco.elevator.door.closed.waiting.duration.in.ms}")
    private int doorClosedWaitingDurationInMs;

    @Value("${com.tingco.elevator.move.duration.between.floors.in.ms}")
    private int movingDurationBetweenFloorsInMs;

    @Value("${com.tingco.elevator.waiting.duration.after.notifying.arriving.in.ms}")
    private int waitingDurationAfterNotifyingArrivingInMs;

    private ElevatorConfiguration() {
        // no-arg for Spring
    }

    public int getBottomFloor() {
        return bottomFloor;
    }

    public int getTopFloor() {
        return topFloor;
    }

    public int getDoorOpeningDurationInMs() {
        return doorOpeningDurationInMs;
    }

    public int getDoorOpenedWaitingDurationInMs() {
        return doorOpenedWaitingDurationInMs;
    }

    public int getDoorClosingDurationInMs() {
        return doorClosingDurationInMs;
    }

    public int getDoorClosedWaitingDurationInMs() {
        return doorClosedWaitingDurationInMs;
    }

    public int getMovingDurationBetweenFloorsInMs() {
        return movingDurationBetweenFloorsInMs;
    }

    public int getWaitingDurationAfterNotifyingArrivingInMs() {
        return waitingDurationAfterNotifyingArrivingInMs;
    }

}
