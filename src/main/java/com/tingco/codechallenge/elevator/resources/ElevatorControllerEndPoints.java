package com.tingco.codechallenge.elevator.resources;

import com.tingco.codechallenge.elevator.api.ElevatorControllerService;
import com.tingco.codechallenge.elevator.api.ElevatorImpl;
import com.tingco.codechallenge.elevator.api.exceptions.OutOfFloorRangeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rest Resource.
 *
 * @author Sven Wesley
 */
@RestController
@RequestMapping("/rest/v1")
public final class ElevatorControllerEndPoints {
    @Autowired ElevatorControllerService elevatorControllerService;

    /**
     * Ping service to test if we are alive.
     *
     * @return String pong
     */
    @RequestMapping(value = "/ping", method = RequestMethod.GET)
    public String ping() {
        return "pong";
    }

    @GetMapping(value = "/ride")
    public int requestElevator(@RequestParam("towards") ElevatorImpl.Direction towards, @RequestParam("waiting_floor") int waiting_floor) {
        ElevatorImpl.Direction d = ElevatorImpl.Direction.DOWN;
        return this.elevatorControllerService.requestElevatorId(waiting_floor);
    }

    @PostMapping(value = "/ride/{to_floor}")
    public ResponseEntity<String> createFloorRequestWithNumberPreference(@PathVariable("to_floor") int toFloor) {
        try {
            this.elevatorControllerService.createFloorRequestWithNumberPreference(toFloor);
        } catch (OutOfFloorRangeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/waiting/{waiting_floor}/{towards}")
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<String> createFloorRequestWithDirectionPreference(
        @PathVariable("waiting_floor") int waitingFloor,
        @PathVariable("towards") ElevatorImpl.Direction towards) {
        try {
            this.elevatorControllerService.createFloorRequestWithDirectionPreference(waitingFloor, towards);
        } catch (OutOfFloorRangeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        return ResponseEntity.ok().build();
    }
}
