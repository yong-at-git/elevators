package com.tingco.codechallenge.elevator.api.validators;

import com.google.common.collect.Range;
import com.tingco.codechallenge.elevator.api.exceptions.MissingRidingElevatorException;
import com.tingco.codechallenge.elevator.api.exceptions.OutOfFloorRangeException;
import com.tingco.codechallenge.elevator.api.requests.UserRiding;

/**
 * Created by Yong Huang on 2017-11-26.
 */
public class RidingRequestValidator {
    private RidingRequestValidator() {
    }

    public static final void validate(UserRiding userRiding, Range<Integer> floorRange) throws OutOfFloorRangeException, MissingRidingElevatorException {
        FloorValidator.validate(userRiding.getToFloor(), floorRange);
        hasRidingElevator(userRiding);
    }

    public static final void hasRidingElevator(UserRiding userRiding) throws MissingRidingElevatorException {
        if (userRiding.getRidingElevatorId() == null) {
            throw new MissingRidingElevatorException("Invalid riding request without elevator id. Original request=" + userRiding);
        }
    }
}
