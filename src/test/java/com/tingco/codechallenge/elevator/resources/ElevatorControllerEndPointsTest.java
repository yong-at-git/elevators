package com.tingco.codechallenge.elevator.resources;

import com.tingco.codechallenge.elevator.WaitingRequestResponse;
import com.tingco.codechallenge.elevator.api.ElevatorImpl;
import com.tingco.codechallenge.elevator.api.requests.UserRiding;
import com.tingco.codechallenge.elevator.api.requests.UserWaiting;
import com.tingco.codechallenge.elevator.config.ElevatorApplication;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Boiler plate test class to get up and running with a test faster.
 *
 * @author Sven Wesley
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ElevatorApplication.class)
public class ElevatorControllerEndPointsTest {

    @Autowired
    private ElevatorControllerEndPoints endPoints;

    private int waitingFloor;
    private ElevatorImpl.Direction towards;
    private Integer elevatorId;
    private int ridingToFloor;

    @Test
    public void ping() {

        Assert.assertEquals("pong", endPoints.ping());

    }

    @Test
    public void waitingRequest_floorOutOfRange() {
        waitingFloor = 90000;
        towards = ElevatorImpl.Direction.UP;

        UserWaiting userWaiting = new UserWaiting(waitingFloor, towards);
        org.springframework.http.ResponseEntity<WaitingRequestResponse> rideRequest = endPoints.createWaitingRequest(userWaiting);

        Assert.assertEquals(rideRequest.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void waitingRequest_directionMissing() {
        waitingFloor = 90000;
        towards = null;

        UserWaiting userWaiting = new UserWaiting(waitingFloor, towards);
        org.springframework.http.ResponseEntity<WaitingRequestResponse> rideRequest = endPoints.createWaitingRequest(userWaiting);

        Assert.assertEquals(rideRequest.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void waitingRequest_success() {
        waitingFloor = 4;
        towards = ElevatorImpl.Direction.DOWN;

        UserWaiting userWaiting = new UserWaiting(waitingFloor, towards);
        org.springframework.http.ResponseEntity<WaitingRequestResponse> rideRequest = endPoints.createWaitingRequest(userWaiting);

        Assert.assertEquals(rideRequest.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void ridingRequest_floorOutOfRange() {
        elevatorId = 1;
        ridingToFloor = 100000;

        UserRiding userRiding = new UserRiding(elevatorId, ridingToFloor);
        org.springframework.http.ResponseEntity<String> rideRequest = endPoints.createRidingRequest(userRiding);

        Assert.assertEquals(rideRequest.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void ridingRequest_ridingElevatorIdMissing() {
        elevatorId = null;
        ridingToFloor = 4;

        UserRiding userRiding = new UserRiding(elevatorId, ridingToFloor);
        org.springframework.http.ResponseEntity<String> rideRequest = endPoints.createRidingRequest(userRiding);

        Assert.assertEquals(rideRequest.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void ridingRequest_ridingElevatorIdInvalid() {
        elevatorId = -1;
        ridingToFloor = 4;

        UserRiding userRiding = new UserRiding(elevatorId, ridingToFloor);
        org.springframework.http.ResponseEntity<String> rideRequest = endPoints.createRidingRequest(userRiding);

        Assert.assertEquals(rideRequest.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void ridingRequest_success() {
        elevatorId = 1;
        ridingToFloor = 4;

        UserRiding userRiding = new UserRiding(elevatorId, ridingToFloor);
        org.springframework.http.ResponseEntity<String> rideRequest = endPoints.createRidingRequest(userRiding);

        Assert.assertEquals(rideRequest.getStatusCode(), HttpStatus.OK);
    }

}
