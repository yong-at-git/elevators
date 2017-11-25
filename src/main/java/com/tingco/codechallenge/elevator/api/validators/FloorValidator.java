package com.tingco.codechallenge.elevator.api.validators;

import com.google.common.collect.Range;
import com.tingco.codechallenge.elevator.api.exceptions.OutOfFloorRangeException;

/**
 * Created by Yong Huang on 2017-11-25.
 */
public class FloorValidator {
    private FloorValidator() {
        // non-arg constructor for util class
    }

    public static final void validate(int toFloor, Range<Integer> floorRange) throws OutOfFloorRangeException {
        if (!floorRange.contains(toFloor)) {
            String message = "Requested floor: " + toFloor + " is out of floor range: " + floorRange;
            throw new OutOfFloorRangeException(message);
        }
    }
}
