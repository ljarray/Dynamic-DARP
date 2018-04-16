import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Created by laura-jane on 2/7/18.
 */

class Request {

    private int REQUEST_ID;

    private LocalDateTime pickUpTime; // earliest pick up time
    private LocalDateTime dropOffTime; // latest drop off time, default is 1 hour after earliest pickup

    private LocationPoint pickUpLoc;
    private LocationPoint dropOffLoc;

    private LocalDateTime scheduledPickUp; // pick up time scheduled by the vehicle route
    private LocalDateTime scheduledDropOff; // drop off time scheduled by the vehicle route


    public Request(int requestID, LocalDateTime pickUpTime, double pLatitude, double pLongitude,
                   LocalDateTime dropOffTime, double dLatitude, double dLongitude){

        this.REQUEST_ID = requestID;
        this.pickUpTime = pickUpTime;
        this.pickUpLoc = new LocationPoint("Request " + REQUEST_ID + " pick up", pLatitude, pLongitude);

        this.dropOffTime = dropOffTime;
        this.dropOffLoc = new LocationPoint("Request " + REQUEST_ID + " drop off", dLatitude, dLongitude);
    }

    public Request(LocalDateTime pickUpTime, double pLatitude, double pLongitude,
                   LocalDateTime dropOffTime, double dLatitude, double dLongitude){

        // todo implement better method for request IDs that cannot cause duplicates
        this.REQUEST_ID = (int)(Math.random() * 999);
        this.pickUpTime = pickUpTime;
        this.pickUpLoc = new LocationPoint("Request " + REQUEST_ID + " pick up", pLatitude, pLongitude);

        this.dropOffTime = dropOffTime;
        this.dropOffLoc = new LocationPoint("Request " + REQUEST_ID + " drop off", dLatitude, dLongitude);

        // System.out.println(this.toString());
        // System.out.println("Request #" + this.getRequestNum() + " created.");

    }

    // Set Functions
    public void setRequestNum(int requestNum){
        this.REQUEST_ID = requestNum;
    }
    public void setPickUpTime(LocalDateTime pickUpTime) {
        this.pickUpTime = pickUpTime;
    }
    public void setDropOffTime(LocalDateTime dropOffTime) {
        this.dropOffTime = dropOffTime;
    }
    public void setPickUpLoc(LocationPoint pickUpLoc) {
        this.pickUpLoc = pickUpLoc;
    }
    public void setDropOffLoc(LocationPoint dropOffLoc) {
        this.dropOffLoc = dropOffLoc;
    }

    public void setScheduledPickUp(LocalDateTime scheduledPickUp) {
        this.scheduledPickUp = scheduledPickUp;
    }
    public void setScheduledDropOff(LocalDateTime scheduledDropOff) {
        this.scheduledDropOff = scheduledDropOff;
    }

    // Get Functions
    public int getRequestNum() {
        return REQUEST_ID;
    }
    public LocalDateTime getPickUpTime() {
        return pickUpTime;
    }
    public LocalDateTime getDropOffTime(){
        return dropOffTime;
    }
    public LocationPoint getPickUpLoc(){
        return pickUpLoc;
    }
    public LocationPoint getDropOffLoc(){ return dropOffLoc; }

    public LocalDateTime getScheduledPickUp(){
        return scheduledPickUp;
    }
    public LocalDateTime getScheduledDropOff(){
        return scheduledDropOff;
    }

    // returns the difference between the requested and scheduled drop off times in minutes
    long calcDropOffWait(){
        if (scheduledDropOff.isAfter(dropOffTime)){
            return ChronoUnit.MINUTES.between(dropOffTime, scheduledDropOff);
        }
        else { return 0; }
    }

    public String toString(){
        return "Request " + getRequestNum() + ":\n" +
                "Created: " + formatTimeStamp(getPickUpTime()) + "\n" +
                "Pick Up Location: " + getPickUpLoc().toString() + "\n" +
                "Drop Off Time: " + formatTimeStamp(getDropOffTime()) + "\n" +
                "Drop Off Location: " + getDropOffLoc();
    }

    private String formatTimeStamp(LocalDateTime time){
        return time.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

}