package com.tingco.codechallenge.elevator;

import com.tingco.codechallenge.elevator.api.ElevatorControllerImpl;
import com.tingco.codechallenge.elevator.api.ElevatorImpl;
import com.tingco.codechallenge.elevator.api.exceptions.InvalidRidingElevatorIdException;
import com.tingco.codechallenge.elevator.api.exceptions.MissingRidingElevatorException;
import com.tingco.codechallenge.elevator.api.exceptions.MissingWaitingDirectionException;
import com.tingco.codechallenge.elevator.api.exceptions.OutOfFloorRangeException;
import com.tingco.codechallenge.elevator.api.requests.UserRiding;
import com.tingco.codechallenge.elevator.api.requests.UserWaiting;
import com.tingco.codechallenge.elevator.api.states.ElevatorStateToken;
import com.tingco.codechallenge.elevator.config.ElevatorApplication;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Optional;
import java.util.concurrent.Callable;

import static org.awaitility.Awaitility.await;

/**
 * Boiler plate test class to get up and running with a test faster.
 *
 * @author Sven Wesley
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ElevatorApplication.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IntegrationTest {
    @Autowired
    ElevatorControllerImpl elevatorController;

    @Test
    public void simulateAnElevatorShaft()
        throws OutOfFloorRangeException, MissingRidingElevatorException, InvalidRidingElevatorIdException, MissingWaitingDirectionException {
    }

    @Test
    public void first_simulate_userRiding()
        throws OutOfFloorRangeException, MissingRidingElevatorException, InvalidRidingElevatorIdException, MissingWaitingDirectionException {

        Integer elevatorInitialFloor = 0;
        Integer ridingElevatorId = 1;
        int ridingFloor = 4;

        ElevatorImpl ridingElevator = getElevator(ridingElevatorId);
        Assert.assertTrue(ridingElevator.currentFloor() == elevatorInitialFloor);

        UserRiding userRiding = new UserRiding(ridingElevatorId, ridingFloor);

        elevatorController.createUserRidingRequest(userRiding);

        await().until(ridingElevatorArrived(userRiding));
    }

    private Callable<Boolean> ridingElevatorArrived(UserRiding userRiding) {
        return () -> {
            ElevatorImpl theElevator = getElevator(userRiding.getRidingElevatorId());

            return theElevator.currentFloor() == userRiding.getToFloor();
        };
    }

    @Test
    public void second_simulate_userWaiting()
        throws OutOfFloorRangeException, MissingRidingElevatorException, InvalidRidingElevatorIdException, MissingWaitingDirectionException {

        int waitingFloor = 2;
        ElevatorImpl.Direction towards = ElevatorImpl.Direction.DOWN;
        UserWaiting userWaiting = new UserWaiting(waitingFloor, towards);

        Optional<Integer> allocatedElevatorId = elevatorController.createUserWaitingRequest(userWaiting);

        allocatedElevatorId.ifPresent(elevatorId -> await().until(elevatorIsArrived(userWaiting, elevatorId)));
    }

    private Callable<Boolean> elevatorIsArrived(UserWaiting userWaiting, int elevatorId) {
        return () -> {
            ElevatorImpl theElevator = getElevator(elevatorId);

            return theElevator.currentFloor() == userWaiting.getWaitingFloor()
                && theElevator.getDirection() == userWaiting.getTowards()
                && theElevator.getCurrentState().getToken().equals(ElevatorStateToken.DOOR_OPENING);
        };
    }

    private ElevatorImpl getElevator(int elevatorId) {
        return elevatorController.getAllElevators()
            .stream()
            .filter(elevator -> elevator.getId() == elevatorId)
            .findFirst()
            .get();
    }

}
