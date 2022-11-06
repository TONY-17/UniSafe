package com.example.escortme.driverApp.ui.driverHome.trips;

/*
 * Stores trips that are the same
 * Saves the driver time; So they dont have to pick up students are going to the same location individually
 */
public class DupTrip {
    private String pickUp;
    private String destination;

    public DupTrip(String pickUp, String destination) {
        this.pickUp = pickUp;
        this.destination = destination;
    }

    public String getPickUp() {
        return pickUp;
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

    @Override
    public String toString() {
        return "DupTrip{" +
                "pickUp='" + pickUp + '\'' +
                ", destination='" + destination + '\'' +
                '}';
    }
}
