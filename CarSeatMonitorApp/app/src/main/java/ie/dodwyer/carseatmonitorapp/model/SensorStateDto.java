package ie.dodwyer.carseatmonitorapp.model;

public class SensorStateDto {
    private String deviceId;
    private String carSeatStatusValue;
    private String vehicleSpeedValue;
    private String rssiStatusValue;
    private String geoLat;
    private String geoLong;
    private String connectionState;
    public SensorStateDto() {
    }

    public SensorStateDto(String deviceId, String carSeatStatusValue, String vehicleSpeedValue, String rssiStatusValue, String geoLat, String geoLong, String connectionState) {
        this.deviceId = deviceId;
        this.carSeatStatusValue = carSeatStatusValue;
        this.vehicleSpeedValue = vehicleSpeedValue;
        this.rssiStatusValue = rssiStatusValue;
        this.geoLat = geoLat;
        this.geoLong = geoLong;
        this.connectionState = connectionState;
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

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getCarSeatStatusValue() {
        return carSeatStatusValue;
    }

    public void setCarSeatStatusValue(String carSeatStatusValue) {
        this.carSeatStatusValue = carSeatStatusValue;
    }

    public String getVehicleSpeedValue() {
        return vehicleSpeedValue;
    }

    public void setVehicleSpeedValue(String vehicleSpeedValue) {
        this.vehicleSpeedValue = vehicleSpeedValue;
    }

    public String getRssiStatusValue() {
        return rssiStatusValue;
    }

    public void setRssiStatusValue(String rssiStatusValue) {
        this.rssiStatusValue = rssiStatusValue;
    }
}
