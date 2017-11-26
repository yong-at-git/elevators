package com.tingco.codechallenge.elevator.resources;

import com.tingco.codechallenge.elevator.WaitingRequestResponse;
import com.tingco.codechallenge.elevator.api.ElevatorControllerImpl;
import com.tingco.codechallenge.elevator.api.exceptions.InvalidRidingElevatorIdException;
import com.tingco.codechallenge.elevator.api.exceptions.MissingRidingElevatorException;
import com.tingco.codechallenge.elevator.api.exceptions.MissingWaitingDirectionException;
import com.tingco.codechallenge.elevator.api.exceptions.OutOfFloorRangeException;
import com.tingco.codechallenge.elevator.api.requests.UserRiding;
import com.tingco.codechallenge.elevator.api.requests.UserWaiting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * Rest Resource.
 *
 * @author Sven Wesley
 */
@RestController
@RequestMapping("/rest/v1")
public final class ElevatorControllerEndPoints {
    private static final Logger LOGGER = LogManager.getLogger(ElevatorControllerEndPoints.class);

    @Autowired ElevatorControllerImpl elevatorControllerImpl;

    /**
     * Ping service to test if we are alive.
     *
     * @return String pong
     */
    @RequestMapping(value = "/ping", method = RequestMethod.GET)
    public String ping() {
        return "pong";
    }

    @PostMapping(value = "/requests/waitings")
    public ResponseEntity<WaitingRequestResponse> createRideRequest(@RequestBody UserWaiting userWaiting) {
        LOGGER.info("Receiving request={}", userWaiting);
        WaitingRequestResponse requestResponse = new WaitingRequestResponse();

        try {
            Optional<Integer> allocatedElevatorId = this.elevatorControllerImpl.createUserWaitingRequest(userWaiting);
            allocatedElevatorId.ifPresent(elevatorId -> {
                requestResponse.setMessage("Elevator allocated.");
                requestResponse.setAllocated_elevator_id(elevatorId);
            });

            if (!allocatedElevatorId.isPresent()) {
                requestResponse.setMessage("No available elevator. Please wait.");
            }
        } catch (OutOfFloorRangeException | MissingWaitingDirectionException e) {
            LOGGER.error("Exception on request={}", userWaiting, e);

            requestResponse.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(requestResponse);
        }

        return ResponseEntity.ok().body(requestResponse);
    }

    @PostMapping(value = "/requests/ridings")
    public ResponseEntity<String> createRideRequest(@RequestBody UserRiding userRiding) {
        LOGGER.info("Receiving request={}", userRiding);

        try {
            this.elevatorControllerImpl.createUserRidingRequest(userRiding);
        } catch (OutOfFloorRangeException | MissingRidingElevatorException | InvalidRidingElevatorIdException e) {
            LOGGER.error("Exception on request={}", userRiding, e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        return ResponseEntity.ok().build();
    }
}
