import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Created by laura-jane on 2/8/18.
 */

public class Handler {
    // Handles incoming requests and manages the optimization algorithm

    private LocalDateTime currentTime;
    private ArrayList<Vehicle> vehicles = new ArrayList<>();
    private HashMap<LocalDateTime, Request> requestSchedule = new HashMap<>();
    private ArrayList<Request> requests = new ArrayList<>();
    private Defaults defaults;

    public Handler(Data data, Defaults defaults){

        this.defaults = defaults;
        this.requestSchedule = data.getRequestSchedule();
        this.requests = data.getRequests();

        System.out.println("\nRequests List\n" + "------------------------------");

        for (Request r : requests){
            System.out.printf("Request #%03d     %s     %26s     %26s\n", r.getRequestNum(),
                    formatTimeStamp(r.getPickUpTime()), r.getPickUpLoc().toString(), r.getDropOffLoc().toString());
        }

        currentTime = setStartTime(requests);

        for (int i = 0; i < 5; i++){
            assignRequestToAVehicle(requests.get(i));
        }

    }

    private void assignRequestToAVehicle(Request request){

        if(vehicles.size() == 0){
            Vehicle v = new Vehicle(defaults);
            vehicles.add(v);
            v.addRequest(request);

            System.out.println("\nRequest #" + request.getRequestNum()
                    + " added to vehicle #" + v.getVehicleID() + ".");

            setRouteForVehicle(v);

        }
        else {
            TreeMap<Double, Vehicle> routeCosts = new TreeMap<>();

            // cost of adding a vehicle is the distance between the depot and pickup

            double newVehicleCost = this.defaults.getDepotLocation().distanceTo(request.getPickUpLoc());

            for ( Vehicle v : vehicles){
                // if seats are available
                if (v.getRequests().size() < v.getMaxSeats()){
                    routeCosts.put(v.route.getInsertionCost(request), v);
                }
            }

            // IF there are vehicles available and it's cheaper to use a new vehicle, use one
            // ELSE add request to vehicle with the cheapest insertion
            if (vehicles.size() < this.defaults.getMaxVehicles() && newVehicleCost < routeCosts.firstKey()){
                Vehicle v = new Vehicle(defaults);
                vehicles.add(v);
                v.addRequest(request);

                System.out.println("Request #" + request.getRequestNum()
                        + " added to Vehicle #" + v.getVehicleID() + ".");

                System.out.println("Setting route for Vehicle #" + v.getVehicleID() + "...");

                setRouteForVehicle(v);

            }
            else{

                Vehicle v = routeCosts.firstEntry().getValue();

                System.out.println("Request #" + request.getRequestNum()
                        + " added to Vehicle #" + v.getVehicleID() + ".");

                v.addRequest(request);

                System.out.println("Setting route for Vehicle #" + v.getVehicleID() + "...");

                setRouteForVehicle(v);
            }
        }
    }

    private void setRouteForVehicle(Vehicle vehicle){
        vehicle.route.setRoute(currentTime);
    }

    private void setRoutes(ArrayList<Vehicle> vehicles){
        for (Vehicle vehicle : vehicles){
            vehicle.route.setRoute(currentTime);
        }
    }

    private void runGeneticAlgorithm(){

        // TODO: 4/12/18 genetic algorithm

        GeneticAlgorithm experiment = new GeneticAlgorithm();

    }

    private LocalDateTime setStartTime(ArrayList<Request> requests){
        // Defaults the current system time to the earliest pick up time for all requests
        return requests.get(0).getPickUpTime();
    }

    private LocalDateTime setStartTime(HashMap<LocalDateTime, Request> schedule){

        LocalDateTime currentTime = LocalDateTime.MAX; // latest date and time supported by LocalDateTime

        Set<LocalDateTime> timeSet = schedule.keySet();
        Request firstRequest = this.requests.get(0);

        // Defaults the current system time to the earliest pick up time for all requests minus time to get there
        for (LocalDateTime time : timeSet){
            if(time.isBefore(currentTime)){
                currentTime = time;
                firstRequest = schedule.get(time);
            }
        }

        double timeToFirstRequest =
                this.defaults.getDepotLocation().timeTo(firstRequest.getPickUpLoc(), defaults.getVehicleDefaultSpeed());

        return currentTime.minusSeconds((long)timeToFirstRequest);

    }

    public String formatTimeStamp(LocalDateTime time){
        return time.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

}


