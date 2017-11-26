package com.tingco.codechallenge.elevator.api.validators;

import com.google.common.collect.Range;
import com.tingco.codechallenge.elevator.api.ElevatorImpl;
import com.tingco.codechallenge.elevator.api.exceptions.MissingWaitingDirectionException;
import com.tingco.codechallenge.elevator.api.exceptions.OutOfFloorRangeException;
import com.tingco.codechallenge.elevator.api.requests.UserWaiting;

/**
 * Created by Yong Huang on 2017-11-26.
 */
public class WaitingRequestValidator {
    private WaitingRequestValidator() {
        // no-arg for util class
    }

    public static final void validate(UserWaiting userWaiting, Range<Integer> floorRange)
        throws OutOfFloorRangeException, MissingWaitingDirectionException {
        FloorValidator.validate(userWaiting.getWaitingFloor(), floorRange);
        hasTowardsDirection(userWaiting);
    }

    public static final void hasTowardsDirection(UserWaiting userWaiting) throws MissingWaitingDirectionException {
        if (userWaiting.getTowards() == null || userWaiting.getTowards() == ElevatorImpl.Direction.NONE) {
            throw new MissingWaitingDirectionException("Invalid waiting request without waiting direction. Original request=" + userWaiting);
        }
    }
}
