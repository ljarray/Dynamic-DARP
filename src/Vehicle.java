import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

/**
 * Created by laura-jane on 2/7/18.
 */

class Vehicle {

    private int maxSeats; // defines the max number of requests the vehicle can hold
    private int maxRouteDuration; // the maximum route duration

    private ArrayList<Request> requests = new ArrayList<Request>(); // a list to hold the requests
    private ArrayList<Request> servicedRequests = new ArrayList<Request>();
    private ArrayList<Request> inTransitRequests = new ArrayList<Request>();
    private ArrayList<Request> unservicedRequests = new ArrayList<Request>();

    Route route = new Route();
    private LocationPoint DEPOT_LOCATION;
    private LocationPoint location;
    private double speed; // speed should be in distance per min

    Vehicle (int seats, int duration, double speed, LocationPoint depot_location){
        this.maxSeats = seats;
        this.maxRouteDuration = duration;
        this.speed = speed;
        this.DEPOT_LOCATION = depot_location;
    }

    // get methods
    public int getMaxSeats(){ return maxSeats; }
    public int getMaxRouteDuration(){ return maxRouteDuration; }

    // set methods
    public void setMaxSeats(int maxSeats){
        this.maxSeats = maxSeats;
    }
    public void setMaxDuration(int maxRouteDuration){
        this.maxRouteDuration = maxRouteDuration;
    }

    public void addRequest(Request request){
        requests.add(request);
        unservicedRequests.add(request);
    }

    public void removeRequest(Request request){
        if (unservicedRequests.contains(request)){
            unservicedRequests.remove(request);
            requests.remove(request);
        }
        else {
            System.out.println("You cannot remove this request from this vehicle. It is either in transit or has been serviced.");
        }
    }

    public void pickUpRequest(Request request){
        if (unservicedRequests.contains(request)){
            unservicedRequests.remove(request);
            inTransitRequests.add(request);
        }
        else {
            System.out.println("You cannot pickup this request. It is in transit, has been serviced, or is not on the request list for this vehicle.");
        }
    }

    public void dropOffRequest(Request request){
        if (inTransitRequests.contains(request)){
            inTransitRequests.remove(request);
            servicedRequests.add(request);
        }
        else {
            System.out.println("You cannot drop off this request. It has either not been picked up, has already been serviced, or is not on the request list for this vehicle.");
        }
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

                // todo add time more precisely
                // time is currently being added only by integer minutes. Write a function to handle smaller time slices.
                currentTime.plusMinutes((long)DEPOT_LOCATION.timeTo(nextLocation, speed));
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
                    currentTime.plusMinutes((long)currentLocation.timeTo(nextLocation, speed));
                    schedulePickUp(nextRequest, currentTime);
                }
                else if (inTransitRequests.contains(nextRequest)){
                    nextLocation = nextRequest.getDropOffLoc();
                    currentTime.plusMinutes((long)currentLocation.timeTo(nextLocation, speed));
                    scheduleDropOff(nextRequest, currentTime);
                } else {
                    System.out.println("Error: Next Location was not set at time " + currentTime + " for Request "
                            + nextRequest.getRequestNum() + "." );
                }

                currentLocation = nextLocation;
            }

        }


        public double getCostofRoute(){
            double totalCost = 0;

            double lateDropoffs = 0;
            double durationCost = 0;

            for (Request request : requests){
                lateDropoffs += request.calcDropOffWait();
            }

            if (maxRouteDuration < routeDuration){
                durationCost = routeDuration - maxRouteDuration;
            }

            totalCost = lateDropoffs + durationCost; // todo add other costs, weighted costs

            return totalCost;
        }

        TreeMap<Double, Request> getNearestFour(LocationPoint currentLocation, LocalDateTime currentTime){

            TreeMap<Double, Request> nearestFour = new TreeMap<Double, Request>();
            TreeMap<Double, Request> separationTree = new TreeMap<Double, Request>();

            for (Request dropoff : scheduledPickUps){
                // check possible drop off locations first
                separationTree.put(getSeparationWithTimeWindow(currentLocation, currentTime, dropoff.getDropOffLoc(), dropoff.getDropOffTime()), dropoff);
            }

            // the number of requests en route cannot exceed the vehicle's available space
            if (inTransitRequests.size() < maxSeats){
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

        // location only separation - time and distance separation only
        double getSeparation(LocationPoint startingPoint, LocationPoint endingPoint){
            double separation = 0;

            separation = startingPoint.distanceTo(endingPoint); // calculate distance separation
            separation += timeToTravel(separation); // add time

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
            arrivalTime = currentTime.plusMinutes((long)travelTime);

            if(arrivalTime.isAfter(dropOffTime)){
                separation = distance + travelTime - WEIGHT_PACKAGE_RIDE_TIME_VIOLATION * (ChronoUnit.MINUTES.between(arrivalTime, dropOffTime));
            }
            else {
                separation = distance + travelTime;
            }

            return separation;
        }

        double getCostToMove(LocationPoint startingPoint, LocationPoint endingPoint){
            double cost = 0;
            // TODO: 2/12/18 write cost of move calculation
            cost = startingPoint.distanceTo(endingPoint) + startingPoint.timeTo(endingPoint, speed);
            return cost;
        }

        double timeToTravel(double distance){
            return distance / speed; // speed should be distance per min
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
    }

}