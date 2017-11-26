package com.tingco.codechallenge.elevator.api.utils;

import com.tingco.codechallenge.elevator.api.events.impl.FloorRequestedWithDirectionPreference;

import java.util.Comparator;

/**
 * Created by Yong Huang on 2017-11-26.
 */
public class ComparatorFactory {
    private ComparatorFactory() {
        // no-arg for util class
    }

    public static final Comparator<FloorRequestedWithDirectionPreference> naturalOrderByFloorNumber() {
        return Comparator.comparingInt(FloorRequestedWithDirectionPreference::getToFloor);
    }

    public static final Comparator<FloorRequestedWithDirectionPreference> reverseOrderByFloorNumber() {
        return (a, b) -> b.getToFloor() - a.getToFloor();
    }
}
