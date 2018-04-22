package ie.dodwyer.carseatmonitorapp.model;

import java.util.HashMap;

public class Alerts {
    public static HashMap<String, Boolean> alertsRaised = new HashMap();
    public static String CHILD_OUT_OF_SEAT_STATIONARY_ALERT = "ALERT! Child is out of car seat while vehicle is stationary.";
    public static String CHILD_OUT_OF_SEAT_IN_TRANSIT_ALERT = "ALERT! Child is out of car seat while vehicle is in transit.";
    public static String CHILD_STILL_IN_SEAT_ALERT = "ALERT! Child is still in car seat";
    static {
        alertsRaised.put(CHILD_OUT_OF_SEAT_STATIONARY_ALERT, false);
        alertsRaised.put(CHILD_OUT_OF_SEAT_IN_TRANSIT_ALERT, false);
        alertsRaised.put(CHILD_STILL_IN_SEAT_ALERT, false);
    }

}