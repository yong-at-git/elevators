package com.tingco.codechallenge.elevator.api;

/**
 * Created by Yong Huang on 2017-11-25.
 */
public class ElevatorConfiguration {
    private int bottomFloor;

    private int topFloor;

    private int doorOpeningDurationInMs;

    private int doorOpenedWaitingDurationInMs;

    private int doorClosingDurationInMs;

    private int doorClosedWaitingDurationInMs;

    private int movingDurationBetweenFloorsInMs;

    private int waitingDurationAfterNotifyingArrivingInMs;

    private ElevatorConfiguration(Builder builder) {
        this.doorOpenedWaitingDurationInMs = builder.doorOpenedWaitingDurationInMs;
        this.movingDurationBetweenFloorsInMs = builder.movingDurationBetweenFloorsInMs;
        this.doorOpeningDurationInMs = builder.doorOpeningDurationInMs;
        this.doorClosedWaitingDurationInMs = builder.doorClosedWaitingDurationInMs;
        this.waitingDurationAfterNotifyingArrivingInMs = builder.waitingDurationAfterNotifyingArrivingInMs;
        this.bottomFloor = builder.bottomFloor;
        this.topFloor = builder.topFloor;
        this.doorClosingDurationInMs = builder.doorClosingDurationInMs;
    }

    public int getBottomFloor() {
        return bottomFloor;
    }

    public int getTopFloor() {
        return topFloor;
    }

    public int getDoorOpeningDurationInMs() {
        return doorOpeningDurationInMs;
    }

    public int getDoorOpenedWaitingDurationInMs() {
        return doorOpenedWaitingDurationInMs;
    }

    public int getDoorClosingDurationInMs() {
        return doorClosingDurationInMs;
    }

    public int getDoorClosedWaitingDurationInMs() {
        return doorClosedWaitingDurationInMs;
    }

    public int getMovingDurationBetweenFloorsInMs() {
        return movingDurationBetweenFloorsInMs;
    }

    public int getWaitingDurationAfterNotifyingArrivingInMs() {
        return waitingDurationAfterNotifyingArrivingInMs;
    }

    public static final class Builder {
        private int bottomFloor;
        private int topFloor;
        private int doorOpeningDurationInMs;
        private int doorOpenedWaitingDurationInMs;
        private int doorClosingDurationInMs;
        private int doorClosedWaitingDurationInMs;
        private int movingDurationBetweenFloorsInMs;
        private int waitingDurationAfterNotifyingArrivingInMs;

        public Builder() {
            // no-arg
        }

        public Builder withBottomFloor(int bottomFloor) {
            this.bottomFloor = bottomFloor;
            return this;
        }

        public Builder withTopFloor(int topFloor) {
            this.topFloor = topFloor;
            return this;
        }

        public Builder withDoorOpeningDurationInMs(int doorOpeningDurationInMs) {
            this.doorOpeningDurationInMs = doorOpeningDurationInMs;
            return this;
        }

        public Builder withDoorOpenedWaitingDurationInMs(int doorOpenedWaitingDurationInMs) {
            this.doorOpenedWaitingDurationInMs = doorOpenedWaitingDurationInMs;
            return this;
        }

        public Builder withDoorClosingDurationInMs(int doorClosingDurationInMs) {
            this.doorClosingDurationInMs = doorClosingDurationInMs;
            return this;
        }

        public Builder withDoorClosedWaitingDurationInMs(int doorClosedWaitingDurationInMs) {
            this.doorClosedWaitingDurationInMs = doorClosedWaitingDurationInMs;
            return this;
        }

        public Builder withMovingDurationBetweenFloorsInMs(int movingDurationBetweenFloorsInMs) {
            this.movingDurationBetweenFloorsInMs = movingDurationBetweenFloorsInMs;
            return this;
        }

        public Builder withWaitingDurationAfterNotifyingArrivingInMs(int waitingDurationAfterNotifyingArrivingInMs) {
            this.waitingDurationAfterNotifyingArrivingInMs = waitingDurationAfterNotifyingArrivingInMs;
            return this;
        }

        public ElevatorConfiguration build() {
            return new ElevatorConfiguration(this);
        }
    }
}
