package com.tingco.codechallenge.elevator.api;

import com.google.common.collect.Range;
import com.google.common.eventbus.EventBus;
import com.tingco.codechallenge.elevator.api.events.EventFactory;
import com.tingco.codechallenge.elevator.api.exceptions.OutOfFloorRangeException;
import com.tingco.codechallenge.elevator.api.utils.FloorValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by Yong Huang on 2017-11-20.
 */
@Service
public class ElevatorControllerService implements ElevatorController {

    @Autowired
    EventBus eventBus;

    @Autowired
    ElevatorImpl demoElevator;

    @Value("${com.tingco.elevator.floor.bottom}")
    private int bottomFloor;

    @Value("${com.tingco.elevator.floor.top}")
    private int topFloor;

    private List<Elevator> elevators = new ArrayList<>();
    private Queue<Elevator> freeElevators = new LinkedBlockingDeque<>();

    @Override public Elevator requestElevator(int toFloor) {
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

        this.eventBus.post(EventFactory.createFloorRequested(toFloor));
    }

    public void createFloorRequestWithDirectionPreference(int waitingFloor, ElevatorImpl.Direction towards) throws OutOfFloorRangeException {
        FloorValidator.validate(waitingFloor, Range.closed(bottomFloor, topFloor));

        this.eventBus.post(EventFactory.createUserWaiting(waitingFloor, towards));
    }

    public int requestElevatorId(int toFloor) {
        return this.requestElevator(toFloor).getId();
    }
}
