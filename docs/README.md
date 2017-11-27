First of all, huge thanks for this great project, which makes me revisit FSM in details, first time use
Guava EventBus and use Availability for asynchronous integration testing.

Below is an outline of the solution. Thanks for your time in advance.

# Modelling
    1. The elevator is modelled with FSM (finite-state-machine).
    2. The elevator controller is modelled with help from its activity diagram.
    Please refer to the FSM and activity diagrams (PDF in docs folder) for details.

# Design
    1. The `ElevatorControllerEndPoints` class is the REST interface of the service.
    2. The ElevatorControllerImpl class validates the requests and delegates valid ones to elevators via event bus.
    3. The ElevatorImpl class listens to request events via event bus and forwards events to FSM.
    4. The ElevatorFSM class acts on events by checking state transition condition(i.e. guard) and performing state changes.

# Implementation
    1. Rely on event publish-subscribe for request delegation and FSM state transition;
    2. Use Spring scheduling task for processing user waiting queue;
    3. Event classes and state classes are created for FSM.
    4. Two REST API endpoints:
        4.1 User waiting
            URL: http://localhost:8080/rest/v1/requests/waitings
            Method: POST
            Input example: {"waiting_floor":4, "towards":"DOWN"}
            Output example: {"message":"Elevator allocated.","allocated_elevator_id":5}

        4.2 User riding
            URL: http://localhost:8080/rest/v1/requests/ridings
            Method: POST
            Input example: {"riding_elevator_id":2, "to_floor":-1}
            Output example: 200 OK with empty body
    5. Use concurrent queues for handling FIFO entries, e.g. FSM event logs.

# Auto-Testing Code
    Basic negative and positive endpoint test cases are added to ElevatorControllerEndPointsTest.
    Two simulation integration test cases are added to IntegrationTest, using Availability library.

# TODO
    1. Handle approaching target floor, e.g. slowing down while approaching a floor.
    2. Document the APIs with Swagger.
    3. More asynchronous integration testing with Availability.

# Miscellaneous
    Updated Maven dependency of Availability and re-organized the pom file.
    Updated some Spring annotations, e.g. using SpringBootApplication.
    Use log4j2 for logging.
    It seems initially the InteliJ project file from the zip file sets Java level to 1.3.

# How to verify
    The service logs each event and state transitions. It also keeps a queue of event tokens,
    which will all be de-queued and printed out when the state changes to idle again.
    An example is like:
        Elevator=2: The recorded events=[FLOOR_REQUESTED, MOVING_DOWN, ARRIVE, OPEN_DOOR, DOOR_OPENED, CLOSE_DOOR, DOOR_CLOSED, IDLE]

    Below are some example requests and corresponding outputs:

    1. A user is waiting at floor 4 and wants to go downstairs. Run below command from terminal, assuming the service is running locally.
        Command:
        curl -H "Content-Type: application/json" -X POST -d '{"waiting_floor":4, "towards":"DOWN"}' http://localhost:8080/rest/v1/requests/waitings

        Output:

        INFO  com.tingco.codechallenge.elevator.resources.ElevatorControllerEndPoints:47 - Receiving request=UserWaiting{waitingFloor=4, towards=DOWN}
        INFO  com.tingco.codechallenge.elevator.api.ElevatorImpl:251 - Elevator=1: Receiving=FloorRequestedWithDirectionPreference{waitingFloor=4, towards=DOWN}
        INFO  com.tingco.codechallenge.elevator.api.ElevatorControllerImpl:124 - Allocating elevator=1 to waiting request=UserWaiting{waitingFloor=4, towards=DOWN}
        INFO  com.tingco.codechallenge.elevator.api.ElevatorFSM:228 - Elevator=1: Floor requested at floor=4 and with preferred direction=DOWN
        INFO  com.tingco.codechallenge.elevator.api.ElevatorFSM:387 - Elevator=1: Starting motor.
        INFO  com.tingco.codechallenge.elevator.api.ElevatorFSM:323 - Elevator=1: Moving up upon requested direction=UP
        INFO  com.tingco.codechallenge.elevator.api.ElevatorImpl:126 - Elevator=1: Setting state from=IDLE to=MOVING_UP
        INFO  com.tingco.codechallenge.elevator.api.ElevatorFSM:400 - Elevator=1: Arriving floor=1
        INFO  com.tingco.codechallenge.elevator.api.ElevatorFSM:400 - Elevator=1: Arriving floor=2
        INFO  com.tingco.codechallenge.elevator.api.ElevatorFSM:400 - Elevator=1: Arriving floor=3
        INFO  com.tingco.codechallenge.elevator.api.ElevatorFSM:400 - Elevator=1: Arriving floor=4
        INFO  com.tingco.codechallenge.elevator.api.ElevatorFSM:392 - Elevator=1: Stopping motor.
        INFO  com.tingco.codechallenge.elevator.api.ElevatorImpl:126 - Elevator=1: Setting state from=MOVING_UP to=JUST_ARRIVED
        INFO  com.tingco.codechallenge.elevator.api.ElevatorImpl:126 - Elevator=1: Setting state from=JUST_ARRIVED to=DOOR_OPENING
        INFO  com.tingco.codechallenge.elevator.api.ElevatorFSM:432 - Elevator=1: Opening door.
        INFO  com.tingco.codechallenge.elevator.api.ElevatorImpl:126 - Elevator=1: Setting state from=DOOR_OPENING to=DOOR_OPENED
        INFO  com.tingco.codechallenge.elevator.api.ElevatorFSM:450 - Elevator=1: Door opened. Waiting for user to get off and on.
        INFO  com.tingco.codechallenge.elevator.api.ElevatorImpl:126 - Elevator=1: Setting state from=DOOR_OPENED to=DOOR_CLOSING
        INFO  com.tingco.codechallenge.elevator.api.ElevatorFSM:201 - Elevator=1: Closing door.
        INFO  com.tingco.codechallenge.elevator.api.ElevatorFSM:462 - Elevator=1: Door closed. Waiting for floor request.
        INFO  com.tingco.codechallenge.elevator.api.ElevatorFSM:471 - Elevator=1: No request. Going idle.
        INFO  com.tingco.codechallenge.elevator.api.ElevatorImpl:126 - Elevator=1: Setting state from=DOOR_CLOSING to=IDLE
        INFO  com.tingco.codechallenge.elevator.api.ElevatorImpl:130 - Elevator=1: The recorded events=[USER_WAITING, MOVING_UP, ARRIVE, ARRIVE, ARRIVE, ARRIVE, OPEN_DOOR, DOOR_OPENED, CLOSE_DOOR, DOOR_CLOSED, IDLE]
        INFO  com.tingco.codechallenge.elevator.api.ElevatorControllerImpl:161 - Elevator=1 is newly freed.
        INFO  com.tingco.codechallenge.elevator.api.ElevatorControllerImpl:166 - No pending request. Enqueue the freed elevator=1.

    2. A user is inside of elevator 2 and wants to go to floor -1.
        Command:
        curl -H "Content-Type: application/json" -X POST -d '{"riding_elevator_id":2, "to_floor":-1}' http://localhost:8080/rest/v1/requests/ridings

        Output:

        INFO  com.tingco.codechallenge.elevator.resources.ElevatorControllerEndPoints:72 - Receiving request=UserRiding{ridingElevatorId=2, toFloor=-1}
        INFO  com.tingco.codechallenge.elevator.api.ElevatorControllerImpl:152 - Sending request riding request=UserRiding{ridingElevatorId=2, toFloor=-1} to elevator=2.
        INFO  com.tingco.codechallenge.elevator.api.ElevatorImpl:223 - Elevator=2: Receiving FloorRequestedWithNumberPreference{toFloor=-1, receiverElevatorId=2}
        INFO  com.tingco.codechallenge.elevator.api.ElevatorFSM:387 - Elevator=2: Starting motor.
        INFO  com.tingco.codechallenge.elevator.api.ElevatorFSM:330 - Elevator=2: Moving down upon requested direction=DOWN
        INFO  com.tingco.codechallenge.elevator.api.ElevatorImpl:126 - Elevator=2: Setting state from=IDLE to=MOVING_DOWN
        INFO  com.tingco.codechallenge.elevator.api.ElevatorFSM:400 - Elevator=2: Arriving floor=-1
        INFO  com.tingco.codechallenge.elevator.api.ElevatorFSM:392 - Elevator=2: Stopping motor.
        INFO  com.tingco.codechallenge.elevator.api.ElevatorImpl:126 - Elevator=2: Setting state from=MOVING_DOWN to=JUST_ARRIVED
        INFO  com.tingco.codechallenge.elevator.api.ElevatorImpl:126 - Elevator=2: Setting state from=JUST_ARRIVED to=DOOR_OPENING
        INFO  com.tingco.codechallenge.elevator.api.ElevatorFSM:432 - Elevator=2: Opening door.
        INFO  com.tingco.codechallenge.elevator.api.ElevatorImpl:126 - Elevator=2: Setting state from=DOOR_OPENING to=DOOR_OPENED
        INFO  com.tingco.codechallenge.elevator.api.ElevatorFSM:450 - Elevator=2: Door opened. Waiting for user to get off and on.
        INFO  com.tingco.codechallenge.elevator.api.ElevatorImpl:126 - Elevator=2: Setting state from=DOOR_OPENED to=DOOR_CLOSING
        INFO  com.tingco.codechallenge.elevator.api.ElevatorFSM:201 - Elevator=2: Closing door.
        INFO  com.tingco.codechallenge.elevator.api.ElevatorFSM:462 - Elevator=2: Door closed. Waiting for floor request.
        INFO  com.tingco.codechallenge.elevator.api.ElevatorFSM:471 - Elevator=2: No request. Going idle.
        INFO  com.tingco.codechallenge.elevator.api.ElevatorImpl:126 - Elevator=2: Setting state from=DOOR_CLOSING to=IDLE
        INFO  com.tingco.codechallenge.elevator.api.ElevatorImpl:130 - Elevator=2: The recorded events=[FLOOR_REQUESTED, MOVING_DOWN, ARRIVE, OPEN_DOOR, DOOR_OPENED, CLOSE_DOOR, DOOR_CLOSED, IDLE]
        INFO  com.tingco.codechallenge.elevator.api.ElevatorControllerImpl:161 - Elevator=2 is newly freed.
        INFO  com.tingco.codechallenge.elevator.api.ElevatorControllerImpl:166 - No pending request. Enqueue the freed elevator=2.

    3. A user is waiting at floor 4 and wants to go downstairs. But all elevators are busy and any downwards going elevator is currently below floor 4.
        Command:
        curl -H "Content-Type: application/json" -X POST -d '{"waiting_floor":4, "towards":"DOWN"}' http://localhost:8080/rest/v1/requests/waitings

        Output:

        INFO  com.tingco.codechallenge.elevator.resources.ElevatorControllerEndPoints:47 - Receiving request=UserWaiting{waitingFloor=4, towards=DOWN}
        INFO  com.tingco.codechallenge.elevator.api.ElevatorControllerImpl:227 - Enqueue waiting request=UserWaiting{waitingFloor=4, towards=DOWN} because of no elevator moving downwards.