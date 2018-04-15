package ie.dodwyer.carseatmonitorapp.model;

public class Alert {
    private String deviceId;
    private String alertType;
    private String contactType;
    private String secondaryContactName;
    private String secondaryContactNumber;
    private String timeOfAlert;
    /* actual deviceId is B8:27:EB:F5:E9:C0  */
    public Alert(){
        deviceId = new String("");
        String alertType = new String("");
        String contactType = new String("");
        String secondaryContactName = new String("");
        String secondaryContactNumber = new String("");
        String timeOfAlert = new String("");
    }

    public Alert(String deviceId, String alertType, String contactType, String secondaryContactName, String secondaryContactNumber, String timeOfAlert) {
        this.deviceId = deviceId;
        this.alertType = alertType;
        this.contactType = contactType;
        this.secondaryContactName = secondaryContactName;
        this.secondaryContactNumber = secondaryContactNumber;
        this.timeOfAlert = timeOfAlert;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public String getContactType() {
        return contactType;
    }

    public void setContactType(String contactType) {
        this.contactType = contactType;
    }

    public String getSecondaryContactName() {
        return secondaryContactName;
    }

    public void setSecondaryContactName(String secondaryContactName) {
        this.secondaryContactName = secondaryContactName;
    }

    public String getSecondaryContactNumber() {
        return secondaryContactNumber;
    }

    public void setSecondaryContactNumber(String secondaryContactNumber) {
        this.secondaryContactNumber = secondaryContactNumber;
    }

    public String getTimeOfAlert() {
        return timeOfAlert;
    }

    public void setTimeOfAlert(String timeOfAlert) {
        this.timeOfAlert = timeOfAlert;
    }
}
