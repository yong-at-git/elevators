package com.tingco.codechallenge.elevator.resources;

import com.tingco.codechallenge.elevator.api.ElevatorControllerImpl;
import com.tingco.codechallenge.elevator.api.RideRequest;
import com.tingco.codechallenge.elevator.api.exceptions.OutOfFloorRangeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rest Resource.
 *
 * @author Sven Wesley
 */
@RestController
@RequestMapping("/rest/v1")
public final class ElevatorControllerEndPoints {
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

    @PostMapping(value = "/requests")
    public ResponseEntity<String> createRideRequest(@RequestBody RideRequest rideRequest) {
        try {
            this.elevatorControllerImpl.processRequest(rideRequest);
        } catch (OutOfFloorRangeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        return ResponseEntity.ok().build();
    }
}
