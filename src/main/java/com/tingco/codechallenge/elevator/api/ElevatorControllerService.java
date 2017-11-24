package com.tingco.codechallenge.elevator.api;

import com.google.common.eventbus.EventBus;
import org.springframework.beans.factory.annotation.Autowired;
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

    public void demoEventHandling(int toFloor) {
        demoElevator.moveElevator(toFloor);
    }

    public int requestElevatorId(int toFloor) {
        return this.requestElevator(toFloor).getId();
    }
}
