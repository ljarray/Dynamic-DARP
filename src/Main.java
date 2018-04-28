import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) {
	// write your code here

        String file = "data/data_Mac.txt";
        Defaults defaults = new Defaults();
        Data data = new Data(file, defaults);

        Handler eventHandler = new Handler(data, defaults);

        eventHandler.runHandler();

    }
}

class Defaults {
    private LocationPoint DEPOT_LOCATION;
    private int TIME_TO_DROPOFF; // time between request creation and scheduled drop off in minutes
    private int MAX_ROUTE_LENGTH; // max route length per vehicle in minutes
    private double VEHICLE_DEFAULT_SPEED; // km per minute, default is 50 kmh
    private int MAX_VEHICLES;
    private int MAX_SEATS;

    // weights are based on the customer choice defaults
    private int DRIVE_TIME_WEIGHT = 8;
    private int ROUTE_DURATION_WEIGHT = 1;
    private int PACKAGE_RIDE_TIME_VIOLATION_WEIGHT = 20;
    private int ROUTE_DURATION_VIOLATION_WEIGHT = 20;

    // Other weights described in the DAR problems are not applicable to our use case

    Defaults(LocationPoint DEPOT_LOCATION, int TIME_TO_DROPOFF, int ROUTE_LENGTH, long VEHICLE_DEFAULT_SPEED, int MAX_VEHICLES, int MAX_SEATS,
             int WEIGHT_DRIVE_TIME, int ROUTE_DURATION_WEIGHT, int PACKAGE_RIDE_TIME_VIOLATION_WEIGHT, int ROUTE_DURATION_VIOLATION_WEIGHT){

        this.DEPOT_LOCATION = DEPOT_LOCATION;
        this.TIME_TO_DROPOFF = TIME_TO_DROPOFF;
        this.MAX_ROUTE_LENGTH = ROUTE_LENGTH;
        this.VEHICLE_DEFAULT_SPEED = VEHICLE_DEFAULT_SPEED;
        this.MAX_VEHICLES = MAX_VEHICLES;
        this.MAX_SEATS = MAX_SEATS;

        // weights are based on the customer choice defaults
        this.DRIVE_TIME_WEIGHT = WEIGHT_DRIVE_TIME;
        this.ROUTE_DURATION_WEIGHT = ROUTE_DURATION_WEIGHT;
        this.PACKAGE_RIDE_TIME_VIOLATION_WEIGHT = PACKAGE_RIDE_TIME_VIOLATION_WEIGHT;
        this.ROUTE_DURATION_VIOLATION_WEIGHT = ROUTE_DURATION_VIOLATION_WEIGHT;
    }

    Defaults(){
        // default values when not otherwise defined
        this.DEPOT_LOCATION = new LocationPoint(41.0785371, 29.0108798);
        this.TIME_TO_DROPOFF = 60; // in min
        this.MAX_ROUTE_LENGTH = 480; // in min
        this.VEHICLE_DEFAULT_SPEED = 0.43333333333; // km per minute, default is 50 kmh .8333333
        this.MAX_VEHICLES = 3; // 10
        this.MAX_SEATS = 6;

        // weights are based on the customer choice defaults
        this.DRIVE_TIME_WEIGHT = 8;
        this.ROUTE_DURATION_WEIGHT = 1;
        this.PACKAGE_RIDE_TIME_VIOLATION_WEIGHT = 20;
        this.ROUTE_DURATION_VIOLATION_WEIGHT = 20;
    }

    // get methods. There are no set methods, since defaults should not change except through initialization
    LocationPoint getDepotLocation(){ return DEPOT_LOCATION; }
    int getTimeToDropOff() { return TIME_TO_DROPOFF; }
    int getMaxRouteLength() { return MAX_ROUTE_LENGTH; }
    double getVehicleDefaultSpeed() { return VEHICLE_DEFAULT_SPEED; }
    int getMaxVehicles() { return MAX_VEHICLES; }
    int getMaxSeats() { return MAX_SEATS; }
    int getDriveTimeWeight() { return DRIVE_TIME_WEIGHT; }
    int getRouteDurationWeight() { return ROUTE_DURATION_WEIGHT; }
    int getPackageRideTimeViolationWeight() { return PACKAGE_RIDE_TIME_VIOLATION_WEIGHT; }
    int getRouteDurationViolationWeight() { return ROUTE_DURATION_VIOLATION_WEIGHT; }

}

class Data {

    private TreeMap<LocalDateTime, ArrayList<Request>> requestSchedule = new TreeMap<>();
    private HashMap<Integer, Request> requests = new HashMap<>();
    private ArrayList<Integer> requestIdList = new ArrayList<>();

    Data(String fileName, Defaults defaults){
        readProblem(fileName, defaults);
    }

    private void readProblem(String fileName, Defaults defaults){

        BufferedReader reader = null;

        try {
            File file = new File(fileName);
            reader = new BufferedReader(new FileReader(file));

            String timePattern = "\\d\\d:\\d\\d:\\d\\d";
            String locationPattern = "\\d{2}\\.[\\d]*";

            Pattern t = Pattern.compile(timePattern);
            Pattern l = Pattern.compile(locationPattern);

            String line;

            while ((line = reader.readLine()) != null) {

                // System.out.println(line);

                Matcher mt = t.matcher(line);
                Matcher ml = l.matcher(line);

                if(mt.find()){

                    LocalDateTime pickupTime = stringToDateTime(mt.group());
                    LocalDateTime dropoffTime = pickupTime.plusMinutes(defaults.getTimeToDropOff());

                    // System.out.println("Pick Up Time: " + formatTimeStamp(pickupTime) + "\n" +
                    //                    "Drop Off Time: " + formatTimeStamp(dropoffTime));

                    ArrayList<Double> locations = new ArrayList<>();

                    while (ml.find()){
                       locations.add(Double.valueOf(ml.group()));
                    }

                    if(locations.size() == 4){

                        int requestID = (int)(Math.random() * 899 + 100);

                        while (requests.containsKey(requestID)){
                            requestID = (int)(Math.random() * 899 + 100); // ensures no duplicates
                        }

                        Request request = new Request(requestID, pickupTime, locations.get(0), locations.get(1),
                                dropoffTime, locations.get(2), locations.get(3));

                        ArrayList<Request> requestList = new ArrayList<>();

                        if (this.requestSchedule.containsKey(pickupTime)){
                            requestList = this.requestSchedule.get(pickupTime);
                        }

                        requestList.add(request);

                        this.requestSchedule.put(pickupTime, requestList);
                        this.requests.put(requestID, request);
                        this.requestIdList.add(requestID);

                    } else {
                        System.out.println("Error: there were not the right number of coordinates in data line: \n" + line);
                    }

                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private LocalDateTime stringToDateTime(String string){
        String[] time = string.split("\\D");
        return LocalDate.now().atTime(Integer.valueOf(time[0]), Integer.valueOf(time[1]), Integer.valueOf(time[2]));
    }

    TreeMap<LocalDateTime, ArrayList<Request>> getRequestSchedule() {
        return requestSchedule;
    }
    HashMap<Integer, Request> getRequests(){ return this.requests; }
    ArrayList<Integer> getRequestIdList(){ return requestIdList; }
}