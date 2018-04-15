package ie.dodwyer.carseatmonitorapp.model;

import ie.dodwyer.carseatmonitorapp.activities.MainActivity;

public class SensorState {
    private String deviceId;
    private String carSeatStatusValue;
    private String vehicleSpeedValue;
    private String rssiStatusValue;
    private String geoLat;
    private String geoLong;
    private String connectionState;
    private String connectionStateTimeStamp;

    public SensorState(String carSeatStatusValue, String vehicleSpeedValue, String rssiStatusValue) {
        this.carSeatStatusValue = carSeatStatusValue;
        this.vehicleSpeedValue = vehicleSpeedValue;
        this.rssiStatusValue = rssiStatusValue;
    }

    public SensorState() {
        this.carSeatStatusValue = new String();
        this.vehicleSpeedValue = new String();
        this.rssiStatusValue = new String();
    }

    public String getConnectionStateTimeStamp() {
        return connectionStateTimeStamp;
    }

    public void setConnectionStateTimeStamp(String connectionStateTimeStamp) {
        this.connectionStateTimeStamp = connectionStateTimeStamp;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getCarSeatStatusValue() {
        return carSeatStatusValue;
    }

    public String setCarSeatStatusValue(String carSeatStatusValue) {
        this.carSeatStatusValue = carSeatStatusValue;
        return checkSensorStateForAlert();
    }

    public String getVehicleSpeedValue() {
        return vehicleSpeedValue;
    }

    public String setVehicleSpeedValue(String vehicleSpeedValue) {
        this.vehicleSpeedValue = vehicleSpeedValue;
        return checkSensorStateForAlert();
    }

    public String getRssiStatusValue() {
        return rssiStatusValue;
    }

    public String setRssiStatusValue(String rssiStatusValue) {
        this.rssiStatusValue = rssiStatusValue;
        return checkSensorStateForAlert();
    }

    public String getGeoLat() {
        return geoLat;
    }

    public void setGeoLat(String geoLat) {
        this.geoLat = geoLat;
    }

    public String getGeoLong() {
        return geoLong;
    }

    public void setGeoLong(String geoLong) {
        this.geoLong = geoLong;
    }

    public String getConnectionState() {
        return connectionState;
    }

    public void setConnectionState(String connectionState) {
        this.connectionState = connectionState;
    }

    public String checkSensorStateForAlert(){
        boolean inProximity = false;
        boolean occupied = false;
        boolean vehicleMoving = false;
        if(!vehicleSpeedValue.isEmpty()) {
            if (Float.parseFloat(vehicleSpeedValue) > 2) {
                vehicleMoving = true;
            }
        }
        if(!carSeatStatusValue.isEmpty()) {
            if (carSeatStatusValue.equals(MainActivity.OCCUPIED)) {
                occupied = true;
            }
        }
        if(!rssiStatusValue.isEmpty()) {
            if (rssiStatusValue.equals(MainActivity.IN_PROXIMITY)) {
                inProximity = true;
            }
        }
        if(inProximity==true&&vehicleMoving==true&&occupied==false){
            return Alerts.CHILD_OUT_OF_SEAT_IN_TRANSIT_ALERT;
        }
        if(inProximity==true&&vehicleMoving==false&&occupied==false){
            return Alerts.CHILD_OUT_OF_SEAT_STATIONARY_ALERT;
        }
        if(inProximity==false&&vehicleMoving==false&&occupied==true){
            return Alerts.CHILD_STILL_IN_SEAT_ALERT;
        }
        return null;
    }
}
