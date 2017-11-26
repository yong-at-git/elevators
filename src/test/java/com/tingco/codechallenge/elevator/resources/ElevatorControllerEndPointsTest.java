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

    @Test
    public void ping() {

        Assert.assertEquals("pong", endPoints.ping());

    }

    @Test
    public void waitingRequest_floorOutOfRange() {
        UserWaiting userWaiting = new UserWaiting(900, ElevatorImpl.Direction.UP);
        org.springframework.http.ResponseEntity<WaitingRequestResponse> rideRequest = endPoints.createWaitingRequest(userWaiting);

        Assert.assertEquals(rideRequest.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void waitingRequest_directionMissing() {
        UserWaiting userWaiting = new UserWaiting(3, null);
        org.springframework.http.ResponseEntity<WaitingRequestResponse> rideRequest = endPoints.createWaitingRequest(userWaiting);

        Assert.assertEquals(rideRequest.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void ridingRequest_floorOutOfRange() {
        UserRiding userRiding = new UserRiding(1, 100000);
        org.springframework.http.ResponseEntity<String> rideRequest = endPoints.createRidingRequest(userRiding);

        Assert.assertEquals(rideRequest.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void ridingRequest_ridingElevatorIdMissing() {
        UserRiding userRiding = new UserRiding(null, 2);
        org.springframework.http.ResponseEntity<String> rideRequest = endPoints.createRidingRequest(userRiding);

        Assert.assertEquals(rideRequest.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void ridingRequest_ridingElevatorIdInvalid() {
        UserRiding userRiding = new UserRiding(-1, 2);
        org.springframework.http.ResponseEntity<String> rideRequest = endPoints.createRidingRequest(userRiding);

        Assert.assertEquals(rideRequest.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

}
