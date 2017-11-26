package com.tingco.codechallenge.elevator.api.utils;

import com.tingco.codechallenge.elevator.api.ElevatorImpl;
import com.tingco.codechallenge.elevator.api.requests.UserWaiting;

import java.util.Comparator;

/**
 * Created by Yong Huang on 2017-11-26.
 */
public class ComparatorFactory {
    private ComparatorFactory() {
        // no-arg for util class
    }

    public static final Comparator<UserWaiting> naturalOrderByFloorNumber() {
        return Comparator.comparingInt(UserWaiting::getWaitingFloor);
    }

    public static final Comparator<UserWaiting> reverseOrderByFloorNumber() {
        return (a, b) -> b.getWaitingFloor() - a.getWaitingFloor();
    }

    public static final Comparator<ElevatorImpl> byAmountOfUpwardsRequestsLowerThanFloor(int floor) {
        return (elevatorA, elevatorB) -> {
            long amountFromA = elevatorA.getUpwardsTargetFloors().stream().filter(requested -> requested < floor).count();
            long amountFromB = elevatorB.getUpwardsTargetFloors().stream().filter(requested -> requested < floor).count();
            return Long.compare(amountFromA, amountFromB);
        };
    }

    public static final Comparator<ElevatorImpl> byAmountOfDownwardsRequestsHigherThanFloor(int floor) {
        return (elevatorA, elevatorB) -> {
            long amountFromA = elevatorA.getUpwardsTargetFloors().stream().filter(requested -> requested > floor).count();
            long amountFromB = elevatorB.getUpwardsTargetFloors().stream().filter(requested -> requested > floor).count();
            return Long.compare(amountFromA, amountFromB);
        };
    }
}
