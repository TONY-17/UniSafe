package com.example.escortme.driverApp.ui.driverHome.trips;

public class Trip {
    private Long id;
    private String pickUp;
    private String destination;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    private String pickUpPoint;
    private String destinationPoint;
    private boolean completed;
    private boolean cancelled;
    private boolean accepted;
    private Long driverId;
    private String channel;
    private String student;
    private String dateCreated;

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public boolean isShowStart() {
        return showStart;
    }

    public void setShowStart(boolean showStart) {
        this.showStart = showStart;
    }

    public String getStudent() {
        return student;
    }

    public void setStudent(String student) {
        this.student = student;
    }

    private boolean showStart;

    public String getPickUpPoint() {
        return pickUpPoint;
    }

    public String getDestinationPoint() {
        return destinationPoint;
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public Long getDriverId() {
        return driverId;
    }

    public String getChannel() {
        return channel;
    }

    public String getPickUp() {
        return pickUp;
    }

    public Trip(Long id, String pickUp, String destination, String pickUpPoint, String destinationPoint, String channel,
                boolean showStart,
                String dateCreated, String student) {
        this.id = id;
        this.pickUp = pickUp;
        this.destination = destination;
        this.pickUpPoint = pickUpPoint;
        this.destinationPoint = destinationPoint;
        this.channel = channel;
        this.showStart = showStart;
        this.dateCreated = dateCreated;
        this.student = student;
    }

    public void setPickUp(String pickUp) {
        this.pickUp = pickUp;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
}
