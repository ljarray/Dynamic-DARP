import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

/**
 * Created by laura-jane on 2/7/18.
 */

class Vehicle {

    private int VEHICLE_ID;
    private LocationPoint DEPOT_LOCATION;
    private int MAX_ROUTE_DURATION; // the maximum route duration
    private int MAX_SEATS; // defines the max number of requests the vehicle can hold
    private double SPEED; // speed should be in distance per min
    private int[] WEIGHTS;

    private ArrayList<Request> requests = new ArrayList<Request>(); // a list to hold the requests
    private ArrayList<Request> servicedRequests = new ArrayList<Request>();
    private ArrayList<Request> inTransitRequests = new ArrayList<Request>();
    private ArrayList<Request> unservicedRequests = new ArrayList<Request>();

    Route route = new Route();
    private LocationPoint location;

//    Vehicle (int VEHICLE_ID, int seats, int duration, double speed, LocationPoint depot_location){
//        this.VEHICLE_ID = VEHICLE_ID;
//        this.MAX_SEATS = seats;
//        this.MAX_ROUTE_DURATION = duration;
//        this.SPEED = SPEED;
//        this.DEPOT_LOCATION = depot_location;
//    }

    Vehicle (int VEHICLE_ID, Defaults defaults){
        this.VEHICLE_ID = VEHICLE_ID;
        this.MAX_SEATS = defaults.getMAX_SEATS();
        this.MAX_ROUTE_DURATION = defaults.getROUTE_LENGTH();
        this.SPEED = defaults.getVEHICLE_DEFAULT_SPEED();
        this.DEPOT_LOCATION = defaults.getDEPOT_LOCATION();

        this.WEIGHTS[0] = defaults.getWEIGHT_DRIVE_TIME();
        this.WEIGHTS[1] = defaults.getWEIGHT_ROUTE_DURATION();
        this.WEIGHTS[2] = defaults.getWEIGHT_PACKAGE_RIDE_TIME_VIOLATION();
        this.WEIGHTS[3] = defaults.getWEIGHT_ROUTE_DURATION_VIOLATION();
    }

    // get methods
    public int getVehicleNum(){ return VEHICLE_ID; }
    public int getMaxSeats(){ return MAX_SEATS; }
    public int getMaxRouteDuration(){ return MAX_ROUTE_DURATION; }

    // set methods
    public void setVehicleNum( int VEHICLE_ID ){ this.VEHICLE_ID = VEHICLE_ID; }
    public void setMaxSeats(int MAX_SEATS){
        this.MAX_SEATS = MAX_SEATS;
    }
    public void setMaxDuration(int MAX_ROUTE_DURATION){
        this.MAX_ROUTE_DURATION = MAX_ROUTE_DURATION;
    }

    public void addRequest(Request request){
        requests.add(request);
        unservicedRequests.add(request);
    }

    public void removeRequest(Request request, LocalDateTime time){
        if (unservicedRequests.contains(request)){
            unservicedRequests.remove(request);
            requests.remove(request);
        }
        else {
            System.out.println("Error: Request " + request.getRequestNum() + " could not be removed from " +  this.VEHICLE_ID +
                    "at time " + formatTimeStamp(time) + ". It was not on the list of unserviced requests for this vehicle." );
            // time should be changed to LocalDateTime.now() for real world applications
        }
    }

    public void pickUpRequest(Request request, LocalDateTime time){
        if (unservicedRequests.contains(request)){
            unservicedRequests.remove(request);
            inTransitRequests.add(request);
        }
        else {
            System.out.println("Error: Request " + request.getRequestNum() + " was not picked up at time " + formatTimeStamp(time) +
                    ". It was not on the list of unserviced requests for vehicle " + this.VEHICLE_ID + ".");
            // time should be changed to LocalDateTime.now() for real world applications
        }
    }

    public void dropOffRequest(Request request, LocalDateTime time){
        if (inTransitRequests.contains(request)){
            inTransitRequests.remove(request);
            servicedRequests.add(request);
        }
        else {
            System.out.println("Error: Request " + request.getRequestNum() + " was not dropped off at time " + formatTimeStamp(time) +
                    ". It was not on the list of in transit requests for vehicle " + this.VEHICLE_ID + ".");
            // time should be changed to LocalDateTime.now() for real world applications
        }
    }

    public String formatTimeStamp(LocalDateTime time){
        return time.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    // Each vehicle has a route
    class Route {

        double routeDuration = 0;
        LocalDateTime routeStartTime;
        Request firstRequest;

        LocalDateTime currentTime;
        LocationPoint currentLocation;
        LocationPoint nextLocation;

        ArrayList<Request> unscheduledRequests;
        ArrayList<Request> scheduledPickUps;
        ArrayList<Request> scheduledDropoffs;

        void setRoute(LocalDateTime now){

            currentTime = now; // = LocalDateTime.now(); in a real world application, for this program, time is given by the handler
            currentLocation = location;

            unscheduledRequests = unservicedRequests;
            scheduledPickUps = inTransitRequests;
            scheduledDropoffs = servicedRequests;

            // If no requests have been picked up or serviced, find the first request for the day, based on earliest pickup
            if(scheduledPickUps.isEmpty() && scheduledDropoffs.isEmpty()){

                LocalDateTime earliestPickup = LocalDateTime.MAX; // latest date and time supported by LocalDateTime

                for (Request request: unscheduledRequests){
                    if(earliestPickup.isAfter(request.getPickUpTime())){
                        earliestPickup = request.getPickUpTime();
                        firstRequest = request;
                    }
                }

                routeStartTime = currentTime;
                nextLocation = firstRequest.getPickUpLoc();

                currentTime.plusSeconds((long)DEPOT_LOCATION.timeTo(nextLocation, SPEED));
                currentLocation = nextLocation;

                schedulePickUp(firstRequest, currentTime);

            }

            // while there are still requests to schedule

            while (!unscheduledRequests.isEmpty() && !scheduledPickUps.isEmpty()){

                TreeMap<Double, Request> nearestFour = getNearestFour(currentLocation, currentTime);
                Request nextRequest = findCheapestMove(nearestFour.values());

                // if statement determines if the pick up or drop off location should be used
                if (unscheduledRequests.contains(nextRequest)){
                    nextLocation = nextRequest.getPickUpLoc();
                    currentTime.plusSeconds((long)currentLocation.timeTo(nextLocation, SPEED));
                    schedulePickUp(nextRequest, currentTime);
                }
                else if (inTransitRequests.contains(nextRequest)){
                    nextLocation = nextRequest.getDropOffLoc();
                    currentTime.plusSeconds((long)currentLocation.timeTo(nextLocation, SPEED));
                    scheduleDropOff(nextRequest, currentTime);
                } else {
                    System.out.println("Error: Next Location was not set at time " + formatTimeStamp(currentTime) + " for Request "
                            + nextRequest.getRequestNum() + "." );
                }

                currentLocation = nextLocation;
            }

            routeDuration = ChronoUnit.MINUTES.between(routeStartTime, currentTime);

        }

        // location only separation - time and distance separation only
        double getSeparation(LocationPoint startingPoint, LocationPoint endingPoint){
            double separation;
            separation = startingPoint.timeTo(endingPoint, SPEED) * 8; // time separation * weight
            //todo make weight defined by Defaults class

            return separation;
        }

        // separation calculation with weights to reduce separation when time window violations are occurring
        double getSeparationWithTimeWindow(LocationPoint startingPoint, LocalDateTime currentTime,
                                           LocationPoint endingPoint, LocalDateTime dropOffTime){

            int WEIGHT_PACKAGE_RIDE_TIME_VIOLATION = 20;
            double separation;
            double distance;
            double travelTime;
            LocalDateTime arrivalTime;

            distance = startingPoint.distanceTo(endingPoint); // calculate distance separation
            travelTime = timeToTravel(distance); // add time
            arrivalTime = currentTime.plusSeconds((long)travelTime);

            if(arrivalTime.isAfter(dropOffTime)){
                separation = distance + travelTime - WEIGHT_PACKAGE_RIDE_TIME_VIOLATION * (ChronoUnit.MINUTES.between(arrivalTime, dropOffTime));
            }
            else {
                separation = distance + travelTime;
            }

            return separation;
        }

        TreeMap<Double, Request> getNearestFour(LocationPoint currentLocation, LocalDateTime currentTime){

            TreeMap<Double, Request> nearestFour = new TreeMap<Double, Request>();
            TreeMap<Double, Request> separationTree = new TreeMap<Double, Request>();

            for (Request dropoff : scheduledPickUps){
                // check possible drop off locations first
                separationTree.put(getSeparationWithTimeWindow(currentLocation, currentTime, dropoff.getDropOffLoc(), dropoff.getDropOffTime()), dropoff);
            }

            // the number of requests en route cannot exceed the vehicle's available space
            if (inTransitRequests.size() < MAX_SEATS){
                for (Request pickup : unscheduledRequests){
                    separationTree.put(getSeparation(currentLocation, pickup.getPickUpLoc()), pickup);
                }
            }

            for (int i = 0; i < 4; i++){
                Double nearest = separationTree.firstKey();
                nearestFour.put(nearest, separationTree.get(nearest));
                separationTree.remove(nearest);
            }

            return nearestFour;
        }

        Request findCheapestMove(Collection<Request> nearestFour){
            TreeMap<Double, Request> moveCosts = new TreeMap<Double, Request>();

            for (Request request: nearestFour){

                LocationPoint moveTo;

                // if statement determines if the pick up or drop off location should be used
                if (unscheduledRequests.contains(request)){
                    moveTo = request.getPickUpLoc();
                }
                else {
                    moveTo = request.getDropOffLoc();
                }

                moveCosts.put(getCostToMove(currentLocation, moveTo), request);
            }

            return moveCosts.firstEntry().getValue();
        }

        double getCostToMove(LocationPoint startingPoint, LocationPoint endingPoint){
            double cost = 0;
            // TODO: 2/12/18 write cost of move calculation
            cost = startingPoint.distanceTo(endingPoint) + startingPoint.timeTo(endingPoint, SPEED);
            return cost;
        }

        double timeToTravel(double distance){
            //returns travel time in seconds
            return distance * 60 / SPEED; // speed should be distance per min
        }

        void schedulePickUp(Request request, LocalDateTime time){
            if (unscheduledRequests.contains(request)){
                unscheduledRequests.remove(request);
                scheduledPickUps.add(request);
                request.setScheduledPickUp(time);
            }
            else {
                System.out.println("You cannot schedule a pick up for this request. It is not on the list of unscheduled requests.");
            }
        }

        void scheduleDropOff(Request request, LocalDateTime time){
            if (inTransitRequests.contains(request)){
                inTransitRequests.remove(request);
                servicedRequests.add(request);
                request.setScheduledDropOff(time);
            }
            else {
                System.out.println("You cannot schedule a drop off for this request. It is not on the list of scheduled pick ups.");
            }
        }

        public double getCostofRoute(){
            double totalCost = 0;

            double lateDropoffs = 0;
            double durationCost = 0;

            // todo add all costs for the route

            for (Request request : requests){
                lateDropoffs += request.calcDropOffWait();
            }

            if (MAX_ROUTE_DURATION < routeDuration){
                durationCost = routeDuration - MAX_ROUTE_DURATION;
            }

            totalCost = lateDropoffs + durationCost; // todo add other costs, weighted costs

            return totalCost;
        }
    }

}