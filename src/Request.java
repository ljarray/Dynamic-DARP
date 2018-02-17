import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Created by laura-jane on 2/7/18.
 */

class Request {

    private int requestNum;

    private LocalDateTime pickUpTime; // earliest pick up time
    private LocalDateTime dropOffTime; // latest drop off time, default is 1 hour after earliest pickup

    private LocationPoint pickUpLoc;
    private LocationPoint dropOffLoc;

    private LocalDateTime scheduledPickUp; // pick up time scheduled by the vehicle route
    private LocalDateTime scheduledDropOff; // drop off time scheduled by the vehicle route


    public Request(int requestNum, LocalDateTime pTime, double pLatitude, double pLongitude,
                   LocalDateTime dTime, double dLatitude, double dLongitude, int seats){

        this.requestNum = requestNum;
        pickUpTime = pTime;
        pickUpLoc = new LocationPoint("Request " + requestNum + " pick up", pLatitude, pLongitude);

        dropOffTime = dTime;
        dropOffLoc = new LocationPoint("Request " + requestNum + " drop off", dLatitude, dLongitude);
    }

    // Set Functions
    public void setRequestNum(int requestNum){
        this.requestNum = requestNum;
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
        return requestNum;
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
    public LocationPoint getDropOffLoc(){
        return dropOffLoc;
    }

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

}