package com.tingco.codechallenge.elevator.api.validators;

import com.google.common.collect.Range;
import com.tingco.codechallenge.elevator.api.exceptions.InvalidRidingElevatorIdException;
import com.tingco.codechallenge.elevator.api.exceptions.MissingRidingElevatorException;
import com.tingco.codechallenge.elevator.api.exceptions.OutOfFloorRangeException;
import com.tingco.codechallenge.elevator.api.requests.UserRiding;

import java.util.Set;

/**
 * Created by Yong Huang on 2017-11-26.
 */
public class RidingRequestValidator {
    private RidingRequestValidator() {
    }

    public static final void validate(UserRiding userRiding, Range<Integer> floorRange, Set<Integer> validElevatorIds)
        throws OutOfFloorRangeException, MissingRidingElevatorException, InvalidRidingElevatorIdException {
        FloorValidator.validate(userRiding.getToFloor(), floorRange);
        hasRidingElevator(userRiding);
        isElevatorIdValid(userRiding, validElevatorIds);
    }

    public static final void hasRidingElevator(UserRiding userRiding) throws MissingRidingElevatorException {
        if (userRiding.getRidingElevatorId() == null) {
            throw new MissingRidingElevatorException("Invalid riding request without elevator id. Original request=" + userRiding);
        }
    }

    public static final void isElevatorIdValid(UserRiding userRiding, Set<Integer> validElevatorIds) throws InvalidRidingElevatorIdException {
        if (!validElevatorIds.contains(userRiding.getRidingElevatorId())) {
            throw new InvalidRidingElevatorIdException("Invalid riding elevator id=" + userRiding.getRidingElevatorId());
        }
    }
}
