import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Created by laura-jane on 2/7/18.
 */

class Vehicle {

    private int VEHICLE_ID;
    private LocationPoint DEPOT_LOCATION;
    private int MAX_ROUTE_DURATION; // the maximum route duration
    private int MAX_SEATS; // defines the max number of requests the vehicle can hold
    private double SPEED; // speed should be in distance per min
    private int[] WEIGHTS = {0,0,0,0};

    private HashMap<Integer, Request> requests = new HashMap<>();
    private HashMap<Integer, Request> deliveredRequests = new HashMap<>();

    private ArrayList<String> transactions = new ArrayList<>();

    Route route;
    private LocationPoint location;
    private LocationPoint nextLocation;

    Vehicle (Defaults defaults){
        // todo implement better method for request IDs that cannot cause duplicates
        this.VEHICLE_ID = (int)(Math.random() * 899 + 100); // generates a number between 100 - 999
        this.MAX_SEATS = defaults.getMaxSeats();
        this.MAX_ROUTE_DURATION = defaults.getMaxRouteLength();
        this.SPEED = defaults.getVehicleDefaultSpeed();
        this.DEPOT_LOCATION = defaults.getDepotLocation();

        this.WEIGHTS[0] = defaults.getDriveTimeWeight();
        this.WEIGHTS[1] = defaults.getRouteDurationWeight();
        this.WEIGHTS[2] = defaults.getPackageRideTimeViolationWeight();
        this.WEIGHTS[3] = defaults.getRouteDurationViolationWeight();

        route = new Route();
        location = DEPOT_LOCATION; // starting location is the depot

    }

    // --------------------------------------------
    //             GET / SET METHODS
    // --------------------------------------------

    // get methods
    int getID(){ return VEHICLE_ID; }
    int getMaxSeats(){ return MAX_SEATS; }
    int getMaxRouteDuration(){ return MAX_ROUTE_DURATION; }
    HashMap<Integer, Request> getRequests(){ return requests; }
    LocationPoint getLocation() { return location; }
    LocationPoint getNextLocation() { return nextLocation; }

    int getFilledSeats(){
        // TODO: 4/25/18 When SQL has been implemented, replace this with a query for # of requests with 'In Transit' status.

        int filledSeats = 0;
        for (Request r : requests.values()){
            if(r.getStatus().equals("In Transit")){
                filledSeats++;
            }
        }
        return filledSeats;
    }

    // set methods
    void setVehicleID( int VEHICLE_ID ){ this.VEHICLE_ID = VEHICLE_ID; }
    void setMaxSeats(int MAX_SEATS){
        this.MAX_SEATS = MAX_SEATS;
    }
    void setMaxDuration(int MAX_ROUTE_DURATION){
        this.MAX_ROUTE_DURATION = MAX_ROUTE_DURATION;
    }
    void setLocation(LocationPoint location) { this.location = location; }
    void setNextLocation(LocationPoint nextLocation) { this.nextLocation = nextLocation; }

    // --------------------------------------------
    //             METHODS
    // --------------------------------------------

    void updateLocationAtTime(LocalDateTime currentTime){

        LocalDateTime arrivalTime = LocalDateTime.now();

        // if no next request exists
        if (route.schedule.higherKey(currentTime) == null){
            nextLocation = DEPOT_LOCATION;
            arrivalTime = currentTime.plusSeconds((long)location.timeTo(DEPOT_LOCATION, SPEED));
        }
        else {

            Request nextRequest = route.schedule.higherEntry(currentTime).getValue();

            switch (nextRequest.getStatus()) {
                case "Unserviced":
                    nextLocation = nextRequest.getPickUpLoc();
                    arrivalTime = nextRequest.getScheduledPickUp();
                    break;
                case "In Transit":
                    nextLocation = nextRequest.getDropOffLoc();
                    arrivalTime = nextRequest.getScheduledDropOff();
                    break;
                default:
                    System.out.println("Error: next location is from request #" + nextRequest.getID()
                            + ", which has already been delivered.");
                    break;
            }
        }
        location = location.getIntermediaryPoint(nextLocation, currentTime, arrivalTime, SPEED);

    }

    void addRequest(Request request){
        request.setStatus("Unserviced");
        requests.put(request.getID(), request);
        transactions.add(formatTimeStamp(request.getPickUpTime()) + "\t" + "Request #" + request.getID() + " was added");

    }

    void removeRequest(Request request, LocalDateTime time){
        if (request.getStatus().equals("Unserviced")){
            requests.remove(request.getID(), request);
        }
        else {
            System.out.println("Error: Request " + request.getID() + " could not be removed from " +  this.VEHICLE_ID +
                    "at time " + formatTimeStamp(time) + " because it is being serviced." );
            // time should be changed to LocalDateTime.now() for real world applications
        }
    }

    void updateRequests(LocalDateTime currentTime){
        // Updates Request statuses & times. If currentTime is after an event on the schedule, the program assumes the event
        // took place. In the final implementation, this can be replaced by Web Service requests, where drivers update the
        // status of requests.

        if(route.schedule != null){

            // If the vehicle is not scheduled to handle any requests, start heading for the depot.
            // This is to send vehicles home at the end of their shift,
            // and to prevent vehicles from lingering in remote areas of the city.

            if (route.schedule.isEmpty()){
                nextLocation = DEPOT_LOCATION;
            }
            else {

                SortedMap<LocalDateTime, Request> requestsToUpdate = route.schedule.headMap(currentTime);
                HashMap<LocalDateTime, Request> requestsToRemove = new HashMap<>();

                for (LocalDateTime time : requestsToUpdate.keySet()){
                    Request request = route.schedule.get(time);

                    if (request.getStatus().equals("In Transit")){
                        dropOffRequest(request, time);
                    }
                    else if (request.getStatus().equals("Unserviced")){
                        pickUpRequest(request, time);
                    }

                    requestsToRemove.put(time, request);
                }

                for (LocalDateTime time : requestsToRemove.keySet()){
                    // Removes performed events from the schedule
                    route.schedule.remove(time, requestsToRemove.get(time));
                }
            }
        }
    }

    private void pickUpRequest(Request request, LocalDateTime time){
        // time should be changed to LocalDateTime.now() for real world applications
        // TODO: 4/13/18 update clock to account for time to pick up

        if (request.getStatus().equals("Unserviced")){

            request.setPickUpTime(time);
            request.setStatus("In Transit");
            transactions.add(formatTimeStamp(time) + "\t" + "Request #" + request.getID() + " was picked up");
            System.out.printf("(%s)     Request #%03d is in transit.\n", formatTimeStamp(time), request.getID());

        }
        else {
            System.out.println("Error: Request " + request.getID() + " was not picked up at time " + formatTimeStamp(time) +
                    ". It was not on the list of unserviced requests for vehicle " + this.VEHICLE_ID + ".");
        }
    }

    private void dropOffRequest(Request request, LocalDateTime time){
        // time should be changed to LocalDateTime.now() for real world applications
        // TODO: 4/13/18 update clock to account for time to drop off

        if (request.getStatus().equals("In Transit")){

            requests.remove(request.getID(), request);
            deliveredRequests.put(request.getID(), request);

            request.setDropOffTime(time);
            request.setStatus("Delivered");
            transactions.add(formatTimeStamp(time) + "\t" + "Request #" + request.getID() + " was dropped off");
            System.out.printf("(%s)     Request #%03d has been delivered.\n", formatTimeStamp(time), request.getID());

        }
        else {
            System.out.println("Error: Request " + request.getID() + " was not dropped off at time " + formatTimeStamp(time) +
                    ". It was not on the list of in transit requests for vehicle " + this.VEHICLE_ID + ".");
        }
    }

    // --------------------------------------------
    //             PRINT METHODS
    // --------------------------------------------

    void printTransactions(){
        System.out.print("TRANSACTIONS FOR VEHICLE #" + this.getID() + "\n------------------------------");
        for (String s : transactions) {
            System.out.println(s);
        }
    }

    void printStatuses(HashMap<Integer, Request> requests){
        System.out.println("\nRequest Statuses\n" + "------------------------------" );
        for (Integer i : requests.keySet()){
            System.out.println("Request #" + i + ": " + requests.get(i).getStatus() );
        }
    }

    private String formatTimeStamp(LocalDateTime time){
        return time.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    void printDeliveredRequests (){
        if(!deliveredRequests.isEmpty()) {
            System.out.println("\nDelivered Requests\n" + "------------------------------" );
            for (Request r : deliveredRequests.values()){
                System.out.println("Request #" + r.getID());
            }
        }
    }


    // --------------------------------------------
    //
    //             ROUTE CLASS
    //
    // --------------------------------------------


    // Each vehicle has a route
    class Route {

        double routeDuration;
        LocalDateTime routeStartTime;
        Request firstRequest;
        private TreeMap <LocalDateTime, Request> schedule = new TreeMap<>();

        LocalDateTime currentTime;
        LocationPoint currentLocation;

        HashMap<Integer, Request> unscheduledRequests;
        HashMap<Integer, Request> scheduledPickUps;
        HashMap<Integer, Request> scheduledDropoffs;

        Route(){

            this.routeDuration = 0;
            this.currentTime = LocalDateTime.now();
            this.currentLocation = DEPOT_LOCATION;

            this.unscheduledRequests = new HashMap<>();
            this.scheduledPickUps = new HashMap<>();
            this.scheduledDropoffs = new HashMap<>();

        }

        void setRoute(LocalDateTime currentTime){ // currentTime = LocalDateTime.now(); in a real world application

            schedule.clear();

            currentLocation = location;

            unscheduledRequests = new HashMap<>();
            scheduledPickUps = new HashMap<>();
            scheduledDropoffs = new HashMap<>();

            for (Integer id : requests.keySet()){

                switch (requests.get(id).getStatus()){
                    case "Unserviced":
                        unscheduledRequests.put(id, requests.get(id));
                        break;

                    case "In Transit":
                        scheduledPickUps.put(id, requests.get(id));
                        break;

                    default:
                        System.out.println("Error: request #" + id + " was skipped with status " + requests.get(id).getStatus());
                }
            }

            // If no requests have been picked up or serviced, find the first request for the day, based on earliest pickup
            if(scheduledPickUps.isEmpty() && scheduledDropoffs.isEmpty() && deliveredRequests.isEmpty()){

                LocalDateTime earliestPickup = LocalDateTime.MAX; // latest date and time supported by LocalDateTime

                for (Request request: unscheduledRequests.values()){
                    if(request.getPickUpTime().isBefore(earliestPickup)){
                        earliestPickup = request.getPickUpTime();
                        firstRequest = request;
                    }
                }

                // System.out.println("The first request is request #" + firstRequest.getID());

                routeStartTime = currentTime;
                nextLocation = firstRequest.getPickUpLoc();

                currentTime = currentTime.plusSeconds((long)DEPOT_LOCATION.timeTo(nextLocation, SPEED));
                currentLocation = nextLocation;

                schedulePickUp(firstRequest, currentTime);

            }

            // while there are still requests to schedule

//            int breakCount = 0;

            while (!unscheduledRequests.isEmpty() || !scheduledPickUps.isEmpty()){

//                if (breakCount > 5){
//                    break;
//                }

                TreeMap<Double, Request> nearestFour = getNearestFour(currentLocation, currentTime);
                Request nextRequest = findCheapestMove(nearestFour.values());

                // if statement determines if the pick up or drop off location should be used

                if (unscheduledRequests.containsValue(nextRequest)){
                    nextLocation = nextRequest.getPickUpLoc();
                    currentTime = currentTime.plusSeconds((long)currentLocation.timeTo(nextLocation, SPEED));
                    schedulePickUp(nextRequest, currentTime);
                }
                else if (scheduledPickUps.containsValue(nextRequest)){
                    nextLocation = nextRequest.getDropOffLoc();
                    currentTime = currentTime.plusSeconds((long)currentLocation.timeTo(nextLocation, SPEED));
                    scheduleDropOff(nextRequest, currentTime);
                } else {
                    System.out.println("Error: Next Location was not set at time " + formatTimeStamp(currentTime) + " for Request "
                            + nextRequest.getID() + "." );
                }

                currentLocation = nextLocation;

//                breakCount++;
            }

            routeDuration = ChronoUnit.SECONDS.between(routeStartTime, currentTime)/60.0;

        }


        // separation calculation with weights to reduce separation when time window violations are occurring
        double getSeparation(LocationPoint startingPoint, LocationPoint endingPoint,
                             LocalDateTime currentTime, LocalDateTime dropOffTime){

            double separation;
            double travelTime;
            LocalDateTime arrivalTime;

            travelTime = startingPoint.timeTo(endingPoint, SPEED) / 60.0; // add time
            arrivalTime = currentTime.plusSeconds((long)travelTime);

            if(arrivalTime.isAfter(dropOffTime)){
                // violation case
                separation = WEIGHTS[0] * travelTime - WEIGHTS[2] * (ChronoUnit.MINUTES.between(arrivalTime, dropOffTime));
            }
            else {
                // non-violation case. Packages which have more time until drop off are given lower priority
                separation = WEIGHTS[0] * travelTime + ChronoUnit.MINUTES.between(arrivalTime, dropOffTime);
            }

            return separation;
        }

        // separation calculation with weights to reduce separation when time window violations are occurring
        double getCost(LocationPoint startingPoint, LocationPoint endingPoint,
                             LocalDateTime currentTime, LocalDateTime dropOffTime){

            double cost;
            double travelTime;
            LocalDateTime arrivalTime;

            travelTime = startingPoint.timeTo(endingPoint, SPEED); // add time
            arrivalTime = currentTime.plusSeconds((long)travelTime);

            if(arrivalTime.isAfter(dropOffTime)){
                // violation case
                cost = WEIGHTS[0] * travelTime + WEIGHTS[2] * (ChronoUnit.MINUTES.between(arrivalTime, dropOffTime));
            }
            else {
                // non-violation case
                cost = WEIGHTS[0] * travelTime - ChronoUnit.MINUTES.between(arrivalTime, dropOffTime);
            }

            return cost;
        }


        TreeMap<Double, Request> getNearestFour(LocationPoint currentLocation, LocalDateTime currentTime,
                                                HashMap<Integer, Request> requestsToPickUp, HashMap<Integer, Request> requestsToDropOff){

            TreeMap<Double, Request> nearestFour = new TreeMap<>();
            TreeMap<Double, Request> separationTree = new TreeMap<>();

            for (Request dropoff : requestsToDropOff.values()){
                // check possible drop off locations first
                separationTree.put(
                        getSeparation(currentLocation, dropoff.getDropOffLoc(), currentTime, dropoff.getDropOffTime()), dropoff);
            }

            // the number of requests en route cannot exceed the vehicle's available space
            if (scheduledPickUps.size() < MAX_SEATS){
                for (Request pickup : requestsToPickUp.values()){
                    separationTree.put(
                            getSeparation(currentLocation, pickup.getPickUpLoc(), currentTime, pickup.getDropOffTime()), pickup);
                }
            }

            for (int i = 0; i < 4; i++){

                if(separationTree.isEmpty()){ break; }

                Double nearest = separationTree.firstKey();
                nearestFour.put(nearest, separationTree.get(nearest));
                separationTree.remove(nearest);
            }

            return nearestFour;
        }

        TreeMap<Double, Request> getNearestFour(LocationPoint currentLocation, LocalDateTime currentTime){
            // Overloaded method with default values for optional inputs
            return getNearestFour(currentLocation, currentTime, unscheduledRequests, scheduledPickUps);
        }

        Request findCheapestMove(Collection<Request> nearestFour){
            TreeMap<Double, Request> moveCosts = new TreeMap<>();
            // establish local variables

            for (Request request: nearestFour){

                LocalDateTime time = currentTime;
                LocationPoint location = currentLocation; // default to avoid null error
                LocationPoint nextLocation;

                HashMap<Integer, Request> requestsToPickUp = new HashMap<>(unscheduledRequests);
                HashMap<Integer, Request> requestsToDropOff = new HashMap<>(scheduledPickUps);
                HashMap<Integer, Request> dropOffs = new HashMap<>(scheduledDropoffs);

                double cost = 0;

                // if statement determines if the pick up or drop off location should be used
                if (requestsToPickUp.containsKey(request.getID())){
                    nextLocation = request.getPickUpLoc();

                    requestsToPickUp.remove(request.getID(), request);
                    requestsToDropOff.put(request.getID(), request);

                }
                else {
                    nextLocation = request.getDropOffLoc();

                    requestsToDropOff.remove(request.getID(), request);
                    dropOffs.put(request.getID(), request);
                }

                // add separation to cost
                cost += getSeparation(location, nextLocation, time, request.getDropOffTime());

                time = time.plusSeconds((long)location.timeTo(nextLocation, SPEED));


                // calculate the cost for the 3 next nearest neighbor moves. Add to cost.
                for (int i = 0; i<3; i++){

                    if(requestsToPickUp.isEmpty() && requestsToDropOff.isEmpty()){ break; }

                    // find nearest neighbor
                    TreeMap<Double, Request> separationTree = getNearestFour(location, time, requestsToPickUp, requestsToDropOff);
                    request = separationTree.firstEntry().getValue();

                    if (requestsToPickUp.containsKey(request.getID())){
                        requestsToPickUp.remove(request.getID(), request);
                        requestsToDropOff.put(request.getID(), request);

                        nextLocation = request.getPickUpLoc();
                    }
                    else if (requestsToDropOff.containsKey(request.getID())){
                        requestsToDropOff.remove(request.getID(), request);
                        dropOffs.put(request.getID(), request);

                        nextLocation = request.getDropOffLoc();
                    }

                    time = time.plusSeconds((long)location.timeTo(request.getPickUpLoc(), SPEED));
                    cost += getCost(location, nextLocation, time, request.getDropOffTime());

                }

                moveCosts.put(cost, request);
            }

            return moveCosts.firstEntry().getValue();
        }

        void schedulePickUp(Request request, LocalDateTime time){

            if (unscheduledRequests.containsKey(request.getID())){
                unscheduledRequests.remove(request.getID(), request);
                scheduledPickUps.put(request.getID(), request);
                request.setScheduledPickUp(time);

                schedule.put(time, request);
            }
            else {
                System.out.println("You cannot schedule a pick up for this request. It is not on the list of unscheduled requests.");
            }
        }

        void scheduleDropOff(Request request, LocalDateTime time){

            if (scheduledPickUps.containsKey(request.getID())){
                scheduledPickUps.remove(request.getID(), request);
                scheduledDropoffs.put(request.getID(), request);
                request.setScheduledDropOff(time);

                schedule.put(time, request);
            }
            else {
                System.out.println("You cannot schedule a drop off for this request. It is not on the list of scheduled pick ups.");
            }
        }

        public double getCostofRoute(){

            double totalCost;

            double driveTime; //distance?
            double routeDuration;
            double packageRideTimeViolations = 0;
            double routeDurationViolation = 0;

            // TODO: 4/12/18 fix cost calculation on driveTime

            driveTime = ChronoUnit.SECONDS.between(routeStartTime, currentTime)/60.0; // time actually traveling
            routeDuration = ChronoUnit.SECONDS.between(routeStartTime, currentTime)/60.0;

            for (Request request : requests.values()){
                packageRideTimeViolations += request.calcDropOffWait();
            }

            if (MAX_ROUTE_DURATION < routeDuration){
                routeDurationViolation = routeDuration - MAX_ROUTE_DURATION;
            }

            totalCost = WEIGHTS[0] * driveTime +
                        WEIGHTS[1] * routeDuration +
                        WEIGHTS[2] * packageRideTimeViolations +
                        WEIGHTS[3] * routeDurationViolation;

            return totalCost;
        }

        double getInsertionCost(Request request){

            // vehicles with no requests are treated as new vehicles.

            if (schedule.isEmpty()){
                return (location.distanceTo(request.getPickUpLoc()) + location.distanceTo(request.getDropOffLoc())) / 2.0;
            }

            // insertion cost is calculated as the total distance between the request pick up / drop off locations
            // and the closest stops already in the route.
            // Penalty terms are added for vehicles which have been behind or are close to their route duration limit

            double cost;
            double minPickUpDistance = 999999999;
            double minDropOffDistance = 999999999;
            double packageRideTimeViolations = 0;
            double routeDurationViolation = 0;

            // TODO: 4/13/18 Improve calculation of penalty terms 

            for ( Request r : requests.values()){
                minPickUpDistance = Math.min(minPickUpDistance, request.getPickUpLoc().distanceTo(r.getPickUpLoc()));
                minPickUpDistance = Math.min(minPickUpDistance, request.getPickUpLoc().distanceTo(r.getDropOffLoc()));

                minDropOffDistance = Math.min(minDropOffDistance, request.getDropOffLoc().distanceTo(r.getPickUpLoc()));
                minDropOffDistance = Math.min(minDropOffDistance, request.getDropOffLoc().distanceTo(r.getDropOffLoc()));

                packageRideTimeViolations += request.calcDropOffWait();
            }

            if (MAX_ROUTE_DURATION < routeDuration){
                routeDurationViolation = routeDuration - MAX_ROUTE_DURATION;
            }

            cost = minPickUpDistance + minDropOffDistance + packageRideTimeViolations + routeDurationViolation;

            return cost;
        }

        private void printUnscheduledRequests(){
            if(!unscheduledRequests.isEmpty()) {
                System.out.println("\nUnscheduled Requests\n" + "------------------------------" );
                for (Request r : unscheduledRequests.values()){
                    System.out.println("Request #" + r.getID());
                }
            }
        }

        private void printScheduledPickUps(){
            if(!scheduledPickUps.isEmpty()) {
                System.out.println("\nScheduled Pick Up Requests\n" + "------------------------------" );
                for (Request r : scheduledPickUps.values()){
                    System.out.println("Request #" + r.getID());
                }
            }
        }

        private void printScheduledDropOffs(){
            if(!scheduledDropoffs.isEmpty()) {
                System.out.println("\nScheduled Drop Off Requests\n" + "------------------------------" );
                for (Request r : scheduledDropoffs.values()){
                    System.out.println("Request #" + r.getID());
                }
            }
        }

        public String toString(){

            String s = "\nRoute for Vehicle #" + getID() + "\n" + "------------------------------\n";
            s += "Route Start Time: " + formatTimeStamp(routeStartTime) + "\n";
            for (String t: transactions){
                s += t + "\n";
            }
            for (LocalDateTime key : schedule.keySet()){
                s += formatTimeStamp(key) + "\t" + "Request #" + schedule.get(key).getID() + "\n";
            }
            return s;
        }

        public void printRoute(){
            System.out.println(this);
            System.out.printf("Route Duration: %.0f hours %.0f min\n", Math.floor(routeDuration / 60), routeDuration % 60);

            printStatuses(requests);
        }

        public void printSchedule(){
            if(!this.schedule.isEmpty()){
                System.out.println("\nSCHEDULE FOR VEHICLE #" + getID() + "\n------------------------------");

                for (LocalDateTime k : schedule.keySet()){
                    System.out.printf("%8s     Request #%3d     \n", formatTimeStamp(k), schedule.get(k).getID());
                }
            }
        }

    }

}