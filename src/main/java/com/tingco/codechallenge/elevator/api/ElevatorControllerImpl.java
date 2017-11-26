package com.tingco.codechallenge.elevator.api;

import com.google.common.collect.Range;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.tingco.codechallenge.elevator.api.events.EventFactory;
import com.tingco.codechallenge.elevator.api.events.impl.NewlyFree;
import com.tingco.codechallenge.elevator.api.exceptions.OutOfFloorRangeException;
import com.tingco.codechallenge.elevator.api.validators.FloorValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by Yong Huang on 2017-11-20.
 */
@Service
public class ElevatorControllerImpl implements ElevatorController {
    private static final Logger LOGGER = LogManager.getLogger(ElevatorControllerImpl.class);

    @Autowired
    EventBus eventBus;

    private ElevatorImpl elevator;

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

    @Value("${com.tingco.elevator.numberofelevators}")
    private int numberOfElevators;

    private List<Elevator> elevators = new ArrayList<>();
    private Queue<Elevator> freeElevators = new LinkedBlockingDeque<>();

    @PostConstruct
    void init() {
        for (int id = 1; id <= numberOfElevators; id++) {
            elevator = new ElevatorImpl(this.eventBus, id, createElevatorConfiguration());
            elevators.add(elevator);
            freeElevators.offer(elevator);
            this.eventBus.register(elevator);
        }

        this.eventBus.register(this);
    }

    @Override public Elevator requestElevator(int toFloor) {
        if (!this.freeElevators.isEmpty()) {
            Elevator freeElevator = this.freeElevators.poll();
            // TODO: post event to the allocated elevator
            return freeElevator;
        }

        // TODO: design the algorithms here

        return this.freeElevators.poll();
    }

    @Override public List<Elevator> getElevators() {
        return this.elevators;
    }

    @Override public void releaseElevator(Elevator elevator) {
        this.freeElevators.offer(elevator);
    }

    public void createFloorRequestWithNumberPreference(int toFloor) throws OutOfFloorRangeException {
        FloorValidator.validate(toFloor, Range.closed(bottomFloor, topFloor));

        Elevator allocated = requestElevator(toFloor);

        this.eventBus.post(EventFactory.createFloorRequested(allocated.getId(), toFloor));
    }

    public void createFloorRequestWithDirectionPreference(int waitingFloor, ElevatorImpl.Direction towards) throws OutOfFloorRangeException {
        FloorValidator.validate(waitingFloor, Range.closed(bottomFloor, topFloor));

        Elevator allocated = requestElevator(waitingFloor);

        this.eventBus.post(EventFactory.createUserWaiting(allocated.getId(), towards, waitingFloor));
    }

    public int requestElevatorId(int toFloor) {
        return this.requestElevator(toFloor).getId();
    }

    @Subscribe
    void onElevatorNewlyFree(NewlyFree newlyFree) {
        LOGGER.info("Elevator: {} is newly free.", newlyFree.getElevatorId());
    }

    private ElevatorConfiguration createElevatorConfiguration() {
        return new ElevatorConfiguration.Builder()
            .withBottomFloor(bottomFloor)
            .withTopFloor(topFloor)
            .withDoorClosingDurationInMs(doorClosingDurationInMs)
            .withDoorClosedWaitingDurationInMs(doorClosedWaitingDurationInMs)
            .withDoorOpeningDurationInMs(doorOpeningDurationInMs)
            .withDoorOpenedWaitingDurationInMs(doorOpenedWaitingDurationInMs)
            .withMovingDurationBetweenFloorsInMs(movingDurationBetweenFloorsInMs)
            .withWaitingDurationAfterNotifyingArrivingInMs(waitingDurationAfterNotifyingArrivingInMs)
            .build();
    }
}
