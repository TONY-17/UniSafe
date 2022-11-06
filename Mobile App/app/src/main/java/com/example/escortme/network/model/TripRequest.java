package com.example.escortme.network.model;

public class TripRequest {
    private String pickUp;
    private String destination;
    private String pickUpPoint;
    private String destinationPoint;
    private boolean completed;
    private boolean cancelled;
    private boolean accepted;
    private String dateCreated;
    private String channel;

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public void setPickUp(String pickUp) {
        this.pickUp = pickUp;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setPickUpPoint(String pickUpPoint) {
        this.pickUpPoint = pickUpPoint;
    }

    public void setDestinationPoint(String destinationPoint) {
        this.destinationPoint = destinationPoint;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public String getPickUp() {
        return pickUp;
    }

    public String getDestination() {
        return destination;
    }

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

    public String getChannel() {
        return channel;
    }

    @Override
    public String toString() {
        return "TripRequest{" +
                "pickUp='" + pickUp + '\'' +
                ", destination='" + destination + '\'' +
                ", pickUpPoint='" + pickUpPoint + '\'' +
                ", destinationPoint='" + destinationPoint + '\'' +
                ", completed=" + completed +
                ", cancelled=" + cancelled +
                ", accepted=" + accepted +
                ", channel='" + channel + '\'' +
                '}';
    }
}
