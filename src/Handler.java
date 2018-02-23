import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Created by laura-jane on 2/8/18.
 */

public class Handler {
    // Handles incoming requests and manages the optimization algorithm

    private LocalDateTime currentTime;
    
    public Handler(Data data, Defaults defaults){
        
        currentTime = LocalDateTime.MAX; // latest date and time supported by LocalDateTime

        // TODO: 2/12/18 Set the system time 
        
    }

    public void runGeneticAlgorithm(){


    }


    public void setRouteForVehicle(Vehicle vehicle){
        vehicle.route.setRoute(currentTime);
    }

    public void setRoutes(ArrayList<Vehicle> vehicles){
        for (Vehicle vehicle : vehicles){
            vehicle.route.setRoute(currentTime);
        }
    }

    public String formatTimeStamp(LocalDateTime time){
        return time.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

}

