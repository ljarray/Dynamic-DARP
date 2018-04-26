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

    private String status; // Request Created, Unserviced, In Transit, Delivered


    Request(int requestID, LocalDateTime pickUpTime, double pLatitude, double pLongitude,
                   LocalDateTime dropOffTime, double dLatitude, double dLongitude){

        this.REQUEST_ID = requestID;
        this.pickUpTime = pickUpTime;
        this.pickUpLoc = new LocationPoint(pLatitude, pLongitude);

        this.dropOffTime = dropOffTime;
        this.dropOffLoc = new LocationPoint(dLatitude, dLongitude);

        this.status = "Request Created";

    }

    Request(LocalDateTime pickUpTime, double pLatitude, double pLongitude,
                   LocalDateTime dropOffTime, double dLatitude, double dLongitude){

        // todo implement better method for request IDs that cannot cause duplicates
        this.REQUEST_ID = (int)(Math.random() * 999);
        this.pickUpTime = pickUpTime;
        this.pickUpLoc = new LocationPoint(pLatitude, pLongitude);

        this.dropOffTime = dropOffTime;
        this.dropOffLoc = new LocationPoint(dLatitude, dLongitude);

        this.status = "Request Created";

        // System.out.println(this.toString());
        // System.out.println("Request #" + this.getRequestNum() + " created.");

    }

    // Set Functions
    void setRequestNum(int requestNum){
        this.REQUEST_ID = requestNum;
    }
    void setPickUpTime(LocalDateTime pickUpTime) {
        this.pickUpTime = pickUpTime;
    }
    void setDropOffTime(LocalDateTime dropOffTime) {
        this.dropOffTime = dropOffTime;
    }
    void setPickUpLoc(LocationPoint pickUpLoc) {
        this.pickUpLoc = pickUpLoc;
    }
    void setDropOffLoc(LocationPoint dropOffLoc) {
        this.dropOffLoc = dropOffLoc;
    }

    void setScheduledPickUp(LocalDateTime scheduledPickUp) {
        this.scheduledPickUp = scheduledPickUp;
    }
    void setScheduledDropOff(LocalDateTime scheduledDropOff) {
        this.scheduledDropOff = scheduledDropOff;
    }

    void setStatus(String status) { this.status = status; }

    // Get Functions
    int getID() {
        return REQUEST_ID;
    }
    LocalDateTime getPickUpTime() {
        return pickUpTime;
    }
    LocalDateTime getDropOffTime(){
        return dropOffTime;
    }
    LocationPoint getPickUpLoc(){
        return pickUpLoc;
    }
    LocationPoint getDropOffLoc(){ return dropOffLoc; }

    LocalDateTime getScheduledPickUp(){
        return scheduledPickUp;
    }
    LocalDateTime getScheduledDropOff(){
        return scheduledDropOff;
    }

    String getStatus() { return status; }

    // returns the difference between the requested and scheduled drop off times in minutes
    long calcDropOffWait(){
        if(scheduledDropOff != null){
            if (scheduledDropOff.isAfter(dropOffTime)){
                return ChronoUnit.MINUTES.between(dropOffTime, scheduledDropOff);
            }
            else { return 0; }
        }
        else { return 0; }
    }

    public String toString(){
        return "Request " + getID() + ":\n" +
                "Created: " + formatTimeStamp(getPickUpTime()) + "\n" +
                "Pick Up Location: " + getPickUpLoc().toString() + "\n" +
                "Drop Off Time: " + formatTimeStamp(getDropOffTime()) + "\n" +
                "Drop Off Location: " + getDropOffLoc();
    }

    private String formatTimeStamp(LocalDateTime time){
        return time.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    // Overriding equals() to compare two Request objects
    @Override
    public boolean equals(Object o) {

        // If the object is compared with itself then return true
        if (o == this) {
            return true;
        }

        /* Check if o is an instance of Complex or not
          "null instanceof [type]" also returns false */
        if (!(o instanceof Request)) {
            return false;
        }

        // typecast o to Complex so that we can compare data members
        Request c = (Request) o;

        // Compare the data members and return accordingly
        return Double.compare(getID(), c.getID()) == 0;
    }

}