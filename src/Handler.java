import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Created by laura-jane on 2/8/18.
 */

class Handler {
    // Handles incoming requests and manages the optimization algorithm

    private Defaults defaults;
    private Data data;

    private LocalDateTime currentTime;
    private ArrayList<Vehicle> vehicles = new ArrayList<>();
    private TreeMap<LocalDateTime, HashMap<Integer, Request>> requestSchedule = new TreeMap<>();
    private HashMap<Integer, Request> requests = new HashMap<>();

    Handler(Data data, Defaults defaults){

        this.defaults = defaults;
        this.data = data;
        this.requestSchedule = data.getRequestSchedule();
        this.requests = data.getRequests();

    }

    void runHandler(){


        System.out.println("\nRequests List\n" + "------------------------------");

        printRequests();

        currentTime = setStartTime(requestSchedule);
        LocalDateTime endTime = requests.get(data.getRequestIdList().get(9)).getDropOffTime().plusMinutes(30);

        while (currentTime.isBefore(endTime)){

            updateVehicles(currentTime);

            if (newRequestExists(currentTime)){
                getNextRequests(currentTime).values().forEach(this::assignRequestToAVehicle);
            }
            else {

                runGeneticAlgorithm();

            }

            currentTime = currentTime.plusMinutes(1);
        }

        printSummary();

    }

    private boolean newRequestExists(LocalDateTime now){
        if (requestSchedule.higherKey(now) != null){
            for (Request r :requestSchedule.get(requestSchedule.higherKey(now)).values()){
                if (r.getStatus().equals("Request Created")) { return true; }
            }
        }

        return false;
    }

    private HashMap<Integer, Request> getNextRequests(LocalDateTime now){
        if (requestSchedule.higherKey(now) != null){
            HashMap<Integer, Request> requestMap = new HashMap<>();
            requestSchedule.get(requestSchedule.higherKey(now)).values().stream().filter(r -> r.getStatus().equals("Request Created")).forEach(r -> {
                requestMap.put(r.getID(), r);
            });
            if (!requestMap.isEmpty()) { return requestMap; }
        }
        System.out.println("Error: no new requests exist");
        return null;
    }

    private void assignRequestToAVehicle(Request request){

        if(vehicles.size() == 0){
            Vehicle v = new Vehicle(defaults);
            vehicles.add(v);
            v.addRequest(request);

            System.out.println("Request #" + request.getID()
                    + " added to vehicle #" + v.getID() + ".");

            System.out.println("Setting route for Vehicle #" + v.getID() + "...\n");

            setRouteForVehicle(v);

        }
        else {
            TreeMap<Double, Vehicle> routeCosts = new TreeMap<>();

            // cost of adding a vehicle is the distance between the depot and pickup

            double newVehicleCost = this.defaults.getDepotLocation().distanceTo(request.getPickUpLoc());

            for ( Vehicle v : vehicles){
                // if seats are available
                if (v.getFilledSeats() < v.getMaxSeats()){
                    routeCosts.put(v.route.getInsertionCost(request), v);
                }
            }

            // IF there are vehicles available and it's cheaper to use a new vehicle, use one
            // ELSE add request to vehicle with the cheapest insertion
            if (vehicles.size() < this.defaults.getMaxVehicles() && newVehicleCost < routeCosts.firstKey()){
                Vehicle v = new Vehicle(defaults);
                vehicles.add(v);
                v.addRequest(request);

                System.out.println("Request #" + request.getID()
                        + " added to Vehicle #" + v.getID() + ".");

                System.out.println("Setting route for Vehicle #" + v.getID() + "...\n");

                setRouteForVehicle(v);

            }
            else{

                Vehicle v = routeCosts.firstEntry().getValue();

                System.out.println("Request #" + request.getID()
                        + " added to Vehicle #" + v.getID() + ".");

                v.addRequest(request);

                System.out.println("Setting route for Vehicle #" + v.getID() + "...\n");

                setRouteForVehicle(v);
            }
        }
    }

    private void setRouteForVehicle(Vehicle vehicle){
        vehicle.route.setRoute(currentTime);
    }

//    private void setRoutes(ArrayList<Vehicle> vehicles){
//        for (Vehicle vehicle : vehicles){
//            vehicle.route.setRoute(currentTime);
//        }
//    }

    private void updateVehicles(LocalDateTime now){
        for (Vehicle vehicle : vehicles){
            vehicle.updateRequests(now);
            vehicle.updateLocationAtTime(now);
        }
    }

    private void runGeneticAlgorithm(){

        // TODO: 4/12/18 genetic algorithm

        GeneticAlgorithm experiment = new GeneticAlgorithm();

    }

    private LocalDateTime setStartTime(TreeMap<LocalDateTime, HashMap<Integer, Request>> schedule){

        return schedule.firstKey();

    }

    private void printRequests(){
        for (LocalDateTime k : requestSchedule.keySet()){
            for (Integer id : requestSchedule.get(k).keySet()){
                System.out.printf("Request #%03d     %s     %26s     %26s\n", id,
                        formatTimeStamp(requests.get(id).getPickUpTime()),
                        requests.get(id).getPickUpLoc().toString(),
                        requests.get(id).getDropOffLoc().toString());
            }
        }
    }

    private void printSummary(){

        System.out.println("\n==============================\n\n" + "Current Time:\t"
                + formatTimeStamp(currentTime) + "\n\n==============================" );

        for (Vehicle v : vehicles){
            System.out.println("\n==============================\n\n"
                    + "Vehicle #" + v.getID() + "\n\n==============================" );
            v.route.printRoute();
        }

    }

    private String formatTimeStamp(LocalDateTime time){
        return time.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

}


