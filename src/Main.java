import javax.xml.stream.Location;
import java.io.EOFException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.StringTokenizer;

public class Main {

    public static void main(String[] args) {
	// write your code here

        String file = "data/d15_3.txt";
        Data problem = new Data(file);
        Defaults defaults = new Defaults();


    }
}

class Defaults {
    LocationPoint DEPOT_LOCATION;
    int TIME_TO_DROPOFF; // time between request creation and scheduled drop off in minutes
    int ROUTE_LENGTH; // max route length per vehicle in minutes
    double VEHICLE_DEFAULT_SPEED; // km per minute, default is 50 kmh
    int MAX_SEATS;

    // weights are based on the customer choice defaults
    int WEIGHT_DRIVE_TIME = 8;
    int WEIGHT_ROUTE_DURATION = 1;
    int WEIGHT_PACKAGE_RIDE_TIME_VIOLATION = 20;
    int WEIGHT_ROUTE_DURATION_VIOLATION = 20;

    // Other weights described in the DAR problems are not applicable to our use case

    Defaults(LocationPoint DEPOT_LOCATION, int TIME_TO_DROPOFF, int ROUTE_LENGTH, long VEHICLE_DEFAULT_SPEED, int MAX_SEATS,
             int WEIGHT_DRIVE_TIME, int WEIGHT_ROUTE_DURATION, int WEIGHT_PACKAGE_RIDE_TIME_VIOLATION, int WEIGHT_ROUTE_DURATION_VIOLATION){

        this.DEPOT_LOCATION = DEPOT_LOCATION;
        this.TIME_TO_DROPOFF = TIME_TO_DROPOFF;
        this.ROUTE_LENGTH = ROUTE_LENGTH;
        this.VEHICLE_DEFAULT_SPEED = VEHICLE_DEFAULT_SPEED;
        this.MAX_SEATS = MAX_SEATS;

        // weights are based on the customer choice defaults
        this.WEIGHT_DRIVE_TIME = WEIGHT_DRIVE_TIME;
        this.WEIGHT_ROUTE_DURATION = WEIGHT_ROUTE_DURATION;
        this.WEIGHT_PACKAGE_RIDE_TIME_VIOLATION = WEIGHT_PACKAGE_RIDE_TIME_VIOLATION;
        this.WEIGHT_ROUTE_DURATION_VIOLATION = WEIGHT_ROUTE_DURATION_VIOLATION;
    }

    Defaults(){
        // default values when not otherwise defined
        this.DEPOT_LOCATION = new LocationPoint("depot", 41.0785371, 29.0108798);
        this.TIME_TO_DROPOFF = 60; // in min
        this.ROUTE_LENGTH = 480; // in min
        this.VEHICLE_DEFAULT_SPEED = 0.83333333333; // km per minute, default is 50 kmh
        this.MAX_SEATS = 6;

        // weights are based on the customer choice defaults
        this.WEIGHT_DRIVE_TIME = 8;
        this.WEIGHT_ROUTE_DURATION = 1;
        this.WEIGHT_PACKAGE_RIDE_TIME_VIOLATION = 20;
        this.WEIGHT_ROUTE_DURATION_VIOLATION = 20;
    }

    // get methods. There are no set methods, since defaults should not change except through initialization
    public LocationPoint getDEPOT_LOCATION(){ return DEPOT_LOCATION; }
    public int getTIME_TO_DROPOFF() { return TIME_TO_DROPOFF; }
    public int getROUTE_LENGTH() { return ROUTE_LENGTH; }
    public double getVEHICLE_DEFAULT_SPEED() { return VEHICLE_DEFAULT_SPEED; }
    public int getMAX_SEATS() { return MAX_SEATS; }
    public int getWEIGHT_DRIVE_TIME() { return WEIGHT_DRIVE_TIME; }
    public int getWEIGHT_ROUTE_DURATION() { return WEIGHT_ROUTE_DURATION; }
    public int getWEIGHT_PACKAGE_RIDE_TIME_VIOLATION() { return WEIGHT_PACKAGE_RIDE_TIME_VIOLATION; }
    public int getWEIGHT_ROUTE_DURATION_VIOLATION() { return WEIGHT_ROUTE_DURATION_VIOLATION; }

}

class Data {

    private LineNumberReader in;
    //private int[] prob = new int[5];
    //private float[][] coo;
    //private int[][] req;
    // private int count;
    private String filename;

    Data(String file){
        filename = file;
        readProblem();
    }

    private void readProblem(){

    }
}

//class Data {
//
//    private LineNumberReader in;
//    private int[] prob = new int[5];
//    private float[][] coo;
//    private int[][] req;
//    // private int count;
//    private String filename;
//
////method that reads number of depots, number of stops, max duration
////time of a tour, max capacity of cars, max riding time of customers
//
//    public Data(String file) {
//        filename = file;
//        readProblem();
//        read();
//    }
//
//    public void readProblem() {
//        try {
//            in = new LineNumberReader(new FileReader(filename));
//            StringTokenizer dimen;
////reads the information into a vector prob
//            for (int i = 1; i < 2; i++) {
//                String dimension = in.readLine();
//                dimen = new StringTokenizer(dimension);
//                while (dimen.hasMoreTokens()) {
//                    prob[0] = Integer.parseInt(dimen.nextToken());
//                    prob[1] = Integer.parseInt(dimen.nextToken());
//                    prob[2] = Integer.parseInt(dimen.nextToken());
//                    prob[3] = Integer.parseInt(dimen.nextToken());
//                    prob[4] = Integer.parseInt(dimen.nextToken());
//                }//while ends
//            }//for ends
//        }//try ends
//        catch (EOFException eof) {
//            closeFile();
//        } catch (IOException e) {
//            System.out.println("1 The file " + filename + " could not be opened "
//                    + e.toString());
//            System.exit(1);
//        }//CATCH ENDS
//    }//readProblem ends
//
////method that returns number of vehicles available
//
//    int getNOCar() {
//        return (prob[0]);
//    }
////method that returns number of stops
//
//    int getStops() {
//        return (prob[1]);
//    }
////method that returns allowable route duration
//
//    int getRouteDuration() {
//        return (prob[2]);
//    }
////method that returns capacity of cars
//
//    int getCapacity() {
//        return (prob[3]);
//    }
////method that returns maximum riding time for customers
//
//    int getMaxRideTime() {
//        return (prob[4]);
//    }
//
////method that reads cooridnates of customers into a matrix coo and
////service time, load change, lower time window and upper time window
////into matrix req
//
//    public void read() {
//        try {
//            in = new LineNumberReader(new FileReader(filename));
//            StringTokenizer tokens;
////Reads cooridinates of the customers into a matrix coo
////and rest into a matrix req
//            int dim = getStops() + 1;
//            int car = getNOCar();
//            req = new int[dim][4];
//            coo = new float[dim][2];
//            int d = 0;
//            if (dim - 1 > 0 && dim - 1 < 10) {
//                d = 0;
//            }
//            if (dim - 1 > 9 && dim - 1 < 100) {
//                d = 1;
//            }
//            if (dim - 1 > 99 && dim - 1 < 1000) {
//                d = 2;
//            }
//            if (car > 9 && car < 100) {
//                d = d + 1;
//            }
//            in.skip(12 + d);
//            for (int i = 0; i < dim + 1; i++) {
//                String tokenstring = in.readLine();
//                int index;
//                tokens = new StringTokenizer(tokenstring);
//                while (tokens.hasMoreTokens()) {
//                    index = Integer.parseInt(tokens.nextToken());
//                    coo[index][0] = Float.parseFloat(tokens.nextToken());
//                    coo[index][1]
//                            = Float.parseFloat(tokens.nextToken());
//                    req[index][0]
//                            = Integer.parseInt(tokens.nextToken());
//                    req[index][1]
//                            = Integer.parseInt(tokens.nextToken());
//                    req[index][2]
//                            = Integer.parseInt(tokens.nextToken());
//                    req[index][3]
//                            = Integer.parseInt(tokens.nextToken());
//                }//WHILE ENDS
//
//
//            } //FOR ENDS
//        } //TRY ENDS
//        catch (EOFException eof) {
//            closeFile();
//        } catch (IOException e) {
//            System.out.println("2 The file " + filename + " could not be opened "
//                    + e.toString());
//        }//CATCH ENDS
//    }//readRequest ends
//
//
//
//    private void closeFile() {
//        try {
//            in.close();
//            System.exit(0);
//        } catch (IOException e) {
//            System.err.println("Error closing file" + e.toString());
//            System.exit(1);
//        }
//    }//closeFile ends
//
//    public float[][] getCoo() {
//        return (coo);
//    }
//
//    public int[][] getReq() {
//        return (req);
//    }
//} //CLASS Data ENDS