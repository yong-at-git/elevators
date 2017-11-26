package com.tingco.codechallenge.elevator.api;

import com.google.common.collect.Range;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.tingco.codechallenge.elevator.api.events.EventFactory;
import com.tingco.codechallenge.elevator.api.events.impl.FloorRequestedWithDirectionPreference;
import com.tingco.codechallenge.elevator.api.events.impl.NewlyFree;
import com.tingco.codechallenge.elevator.api.exceptions.MissingRidingElevatorException;
import com.tingco.codechallenge.elevator.api.exceptions.MissingWaitingDirectionException;
import com.tingco.codechallenge.elevator.api.exceptions.OutOfFloorRangeException;
import com.tingco.codechallenge.elevator.api.requests.UserRiding;
import com.tingco.codechallenge.elevator.api.requests.UserWaiting;
import com.tingco.codechallenge.elevator.api.utils.ComparatorFactory;
import com.tingco.codechallenge.elevator.api.validators.RidingRequestValidator;
import com.tingco.codechallenge.elevator.api.validators.WaitingRequestValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Yong Huang on 2017-11-20.
 */
@Service
public class ElevatorControllerImpl implements ElevatorController {
    private static final Logger LOGGER = LogManager.getLogger(ElevatorControllerImpl.class);

    @Value("${com.tingco.elevator.numberofelevators}")
    private int numberOfElevators;

    @Autowired
    ElevatorConfiguration elevatorConfiguration;

    @Autowired
    EventBus eventBus;

    // to be initialized before put into service
    private Range<Integer> elevatorFloorRange;

    private List<Elevator> elevators = new ArrayList<>();
    private Queue<Elevator> freeElevators = new LinkedBlockingDeque<>();

    private PriorityQueue<FloorRequestedWithDirectionPreference> upwardsWaitingQueue = new PriorityQueue<>(ComparatorFactory.naturalOrderByFloorNumber());
    private PriorityQueue<FloorRequestedWithDirectionPreference> downwardsWaitingQueue = new PriorityQueue<>(ComparatorFactory.reverseOrderByFloorNumber());
    private Queue<FloorRequestedWithDirectionPreference> queueForAllWaitingRequests = new LinkedBlockingQueue<>();

    @PostConstruct
    void init() {
        for (int id = 1; id <= numberOfElevators; id++) {
            Elevator elevator = new ElevatorImpl(this.eventBus, id, this.elevatorConfiguration);
            elevators.add(elevator);
            freeElevators.offer(elevator);
            this.eventBus.register(elevator);
        }

        this.eventBus.register(this);
        this.elevatorFloorRange = Range.closed(this.elevatorConfiguration.getBottomFloor(), this.elevatorConfiguration.getTopFloor());
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

    public void createUserWaitingRequest(UserWaiting userWaiting)
        throws OutOfFloorRangeException, MissingWaitingDirectionException {
        WaitingRequestValidator.validate(userWaiting, this.elevatorFloorRange);

        final int waitingFloor = userWaiting.getWaitingFloor();
        Elevator allocated = requestElevator(waitingFloor);

        this.eventBus.post(EventFactory.createUserWaiting(allocated.getId(), userWaiting.getTowards(), waitingFloor));
    }

    public void createUserRidingRequest(UserRiding userRiding) throws OutOfFloorRangeException, MissingRidingElevatorException {
        RidingRequestValidator.validate(userRiding, this.elevatorFloorRange);

        final int toFloor = userRiding.getToFloor();
        Elevator allocated = requestElevator(toFloor);
        this.eventBus.post(EventFactory.createFloorRequested(allocated.getId(), toFloor));
    }

    public int requestElevatorId(int toFloor) {
        return this.requestElevator(toFloor).getId();
    }

    @Subscribe
    void onElevatorNewlyFree(NewlyFree newlyFree) {
        LOGGER.info("Elevator={} is newly free.", newlyFree.getElevatorId());
    }
}
