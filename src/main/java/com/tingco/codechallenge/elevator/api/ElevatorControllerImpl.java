package com.tingco.codechallenge.elevator.api;

import com.google.common.collect.Range;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.tingco.codechallenge.elevator.api.events.EventFactory;
import com.tingco.codechallenge.elevator.api.events.impl.NewlyFree;
import com.tingco.codechallenge.elevator.api.exceptions.InvalidRidingElevatorIdException;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

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
    private Set<Integer> validElevatorIds;

    private List<ElevatorImpl> elevators = new ArrayList<>();
    private Queue<ElevatorImpl> freeElevators = new LinkedBlockingDeque<>();

    private LinkedBlockingQueue<UserWaiting> timedQueueForAllWaitingRequests = new LinkedBlockingQueue<>();

    @PostConstruct
    void init() {
        for (int id = 1; id <= numberOfElevators; id++) {
            ElevatorImpl elevator = new ElevatorImpl(this.eventBus, id, this.elevatorConfiguration);
            elevators.add(elevator);
            freeElevators.offer(elevator);
            this.eventBus.register(elevator);
        }

        this.eventBus.register(this);
        this.elevatorFloorRange = Range.closed(this.elevatorConfiguration.getBottomFloor(), this.elevatorConfiguration.getTopFloor());
        this.validElevatorIds = this.elevators.stream().map(Elevator::getId).collect(Collectors.toSet());
    }

    /**
     * Use {@link #createUserRidingRequest(UserRiding)} or {@link #createUserWaitingRequest(UserWaiting)}.
     *
     * @return null
     */
    @Deprecated
    @Override
    public Elevator requestElevator(int toFloor) {
        return null;
    }

    @Override public List<Elevator> getElevators() {
        return new ArrayList<>(this.elevators);
    }

    @Override public void releaseElevator(Elevator elevator) {
        this.freeElevators.offer((ElevatorImpl) elevator);
    }

    /**
     * Process new user waiting request by either : 1. allocating an elevator moving at the same direction as requested but not bypassing the waiting floor yet
     * or 2. enqueuing the request.
     * When there are more than one elevator moving at the requested direction, choose the one that has least request between its latest addressed floor
     * and the waiting floor.
     *
     * @param userWaiting the user waiting request
     * @throws OutOfFloorRangeException         if the requested floor is beyond the closed range: [bottom, top]
     * @throws MissingWaitingDirectionException if the waiting request misses requested direction, e.g. UP, DOWN.
     */
    public Optional<Integer> createUserWaitingRequest(UserWaiting userWaiting)
        throws OutOfFloorRangeException, MissingWaitingDirectionException {
        WaitingRequestValidator.validate(userWaiting, this.elevatorFloorRange);

        if (this.timedQueueForAllWaitingRequests.contains(userWaiting)) {
            LOGGER.info("Waiting request={} already exists.", userWaiting);
            return Optional.empty();
        }

        final int waitingFloor = userWaiting.getWaitingFloor();
        final Elevator.Direction towards = userWaiting.getTowards();

        if (!this.freeElevators.isEmpty()) {
            Elevator freeElevator = this.freeElevators.poll();
            this.eventBus.post(EventFactory.createUserWaiting(freeElevator.getId(), towards, waitingFloor));

            LOGGER.info("Allocating elevator={} to waiting request={}", freeElevator.getId(), userWaiting);
            return Optional.of(freeElevator.getId());
        }

        return tryAllocatingBusyElevatorToRequest(userWaiting);
    }

    /**
     * Delegates the riding request to currently occupied elevator(identified by the elevator id in the request) if the requested floor is not in the elevator's
     * request queue;otherwise ignore the request.
     *
     * @param userRiding an riding request from inside of an elevator.
     * @throws OutOfFloorRangeException         if the requested floor is out of the closed range [bottom, top].
     * @throws MissingRidingElevatorException   if no riding elevator id is provided.
     * @throws InvalidRidingElevatorIdException if the riding elevator id is provided but not a valid one.
     */
    public void createUserRidingRequest(UserRiding userRiding) throws OutOfFloorRangeException, MissingRidingElevatorException,
        InvalidRidingElevatorIdException {
        RidingRequestValidator.validate(userRiding, this.elevatorFloorRange, this.validElevatorIds);

        final int toFloor = userRiding.getToFloor();
        final int ridingElevatorId = userRiding.getRidingElevatorId();

        this.elevators
            .stream()
            .filter(elevator -> elevator.getId() == ridingElevatorId)
            .filter(elevator -> !elevator.isFloorAlreadyRequested(toFloor))//if a floor is already requested, then ignore the request.
            .anyMatch(theRidingElevator -> {
                    LOGGER.info("Sending request riding request={} to elevator={}.", userRiding, theRidingElevator.getId());
                    this.eventBus.post(EventFactory.createFloorRequested(ridingElevatorId, toFloor));
                    return true;
                }
            );
    }

    @Subscribe
    void onElevatorNewlyFree(NewlyFree newlyFree) {
        LOGGER.info("Elevator={} is newly freed.", newlyFree.getElevatorId());

        int freedElevatorId = newlyFree.getElevatorId();

        if (timedQueueForAllWaitingRequests.isEmpty()) {
            LOGGER.info("No pending request. Enqueue the freed elevator={}.", freedElevatorId);

            this.elevators
                .stream()
                .filter(elevator -> elevator.getId() == freedElevatorId)
                .anyMatch(newlyFreedElevator -> this.freeElevators.offer(newlyFreedElevator));
            return;
        }

        UserWaiting longestWaitingRequest = this.timedQueueForAllWaitingRequests.poll();

        LOGGER.info("Allocating free elevator={} to longest waiting request={}.", freedElevatorId, longestWaitingRequest);

        this.eventBus
            .post(EventFactory.createUserWaiting(freedElevatorId, longestWaitingRequest.getTowards(), longestWaitingRequest.getWaitingFloor()));
    }

    @Scheduled(fixedRateString = "${com.tingco.elevator.controller.waiting.queue.scan.interval.in.ms}")
    void processWaitingQueue() {
        if (this.timedQueueForAllWaitingRequests.isEmpty()) {
            return;
        }

        Iterator<UserWaiting> iterator = this.timedQueueForAllWaitingRequests.iterator();

        while (iterator.hasNext()) {
            UserWaiting waitingRequest = iterator.next();
            Optional<Integer> isRequestDelegatedToElevator = tryAllocatingBusyElevatorToRequest(waitingRequest);
            if (isRequestDelegatedToElevator.isPresent()) {
                iterator.remove();
            }
        }
    }

    // return true if manage to delegate the request to an elevator; otherwise false
    private Optional<Integer> tryAllocatingBusyElevatorToRequest(UserWaiting userWaiting) {
        final int waitingFloor = userWaiting.getWaitingFloor();
        Optional delegatedElevatorId = Optional.empty();

        switch (userWaiting.getTowards()) {
            case UP:
                List<ElevatorImpl> lowerFloorElevatorsMovingUpwards = getLowerFloorElevatorsMovingUpwards(waitingFloor);

                if (lowerFloorElevatorsMovingUpwards.isEmpty()) {
                    if (!this.timedQueueForAllWaitingRequests.contains(userWaiting)) {
                        LOGGER.info("Enqueue waiting request={} because of no elevator moving upwards.", userWaiting);
                        // this check will support the scheduled job to reuse this method
                        this.timedQueueForAllWaitingRequests.offer(userWaiting);
                    }
                } else {
                    AllocatedElevator toBePopulatedWithId = new AllocatedElevator();
                    sendUpwardsRequestToBestSuitElevator(waitingFloor, lowerFloorElevatorsMovingUpwards, toBePopulatedWithId);
                    delegatedElevatorId = Optional.of(toBePopulatedWithId.getId());
                }

                break;
            case DOWN:
                List<ElevatorImpl> upperFloorElevatorsMovingDownwards = getUpperFloorElevatorsMovingDownwards(waitingFloor);

                if (upperFloorElevatorsMovingDownwards.isEmpty()) {
                    if (!this.timedQueueForAllWaitingRequests.contains(userWaiting)) {
                        LOGGER.info("Enqueue waiting request={} because of no elevator moving downwards.", userWaiting);
                        // this check will support the scheduled job to reuse this method
                        this.timedQueueForAllWaitingRequests.offer(userWaiting);
                    }
                } else {
                    AllocatedElevator toBePopulatedWithId = new AllocatedElevator();
                    sendDownwardsRequestToBestSuitElevator(waitingFloor, upperFloorElevatorsMovingDownwards, toBePopulatedWithId);
                    delegatedElevatorId = Optional.of(toBePopulatedWithId.getId());
                }

                break;
            default:
                LOGGER.info("Ignore waiting request={}.", userWaiting);
                break;
        }

        return delegatedElevatorId;
    }

    private List<ElevatorImpl> getLowerFloorElevatorsMovingUpwards(final int waitingFloor) {
        return this.elevators
            .stream()
            .filter(elevator -> elevator.currentFloor() < waitingFloor)
            .filter(elevator -> elevator.isOnUpPath())
            .collect(Collectors.toList());
    }

    private void sendUpwardsRequestToBestSuitElevator(final int waitingFloor, List<ElevatorImpl> lowerFloorElevatorsMovingOnUpPath,
        AllocatedElevator toBePopulatedWithId) {
        lowerFloorElevatorsMovingOnUpPath
            .stream()
            .sorted(ComparatorFactory.byAmountOfUpwardsRequestsLowerThanFloor(waitingFloor))
            .findFirst()
            .ifPresent(
                elevator -> {
                    LOGGER.info("Sending upwards waiting request={} to best suit elevator={}.", waitingFloor, elevator.getId());

                    this.eventBus.post(EventFactory.createUserWaiting(elevator.getId(), Elevator.Direction.UP, waitingFloor));
                    toBePopulatedWithId.setId(elevator.getId());
                });
    }

    private List<ElevatorImpl> getUpperFloorElevatorsMovingDownwards(int waitingFloor) {
        return this.elevators
            .stream()
            .filter(elevator -> elevator.currentFloor() > waitingFloor)
            .filter(elevator -> elevator.isOnDownPath())
            .collect(Collectors.toList());
    }

    private void sendDownwardsRequestToBestSuitElevator(final int waitingFloor, List<ElevatorImpl> upperFloorElevatorsMovingOnDownPath,
        AllocatedElevator toBePopulatedWithId) {
        upperFloorElevatorsMovingOnDownPath
            .stream()
            .sorted(ComparatorFactory.byAmountOfDownwardsRequestsHigherThanFloor(waitingFloor))
            .findFirst()
            .ifPresent(
                elevator -> {
                    LOGGER.info("Sending downwards waiting request={} to best suit elevator={}.", waitingFloor, elevator.getId());

                    this.eventBus.post(EventFactory.createUserWaiting(elevator.getId(), Elevator.Direction.DOWN, waitingFloor));
                    toBePopulatedWithId.setId(elevator.getId());
                });
    }

    private class AllocatedElevator {
        private Integer id;

        public void setId(Integer id) {
            this.id = id;
        }

        public Integer getId() {
            return id;
        }
    }
}
