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


    }
}

class Defaults {
    LocationPoint DEPOT_LOCATION = new LocationPoint("depot", 41.0785371, 29.0108798);
    long TIME_TO_DROPOFF = 60; // time between request creation and scheduled drop off in minutes
    long ROUTE_LENGTH = 480; // max route length per vehicle in minutes
    double VEHICLE_DEFAULT_SPEED = 0.83333333333; // km per minute, default is 50 kmh
    int MAX_SEATS = 6;

    // weights are based on the customer choice defaults
    int WEIGHT_PACKAGE_RIDE_TIME_VIOLATION = 20;
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