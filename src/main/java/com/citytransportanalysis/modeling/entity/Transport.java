package com.citytransportanalysis.modeling.entity;

import java.time.LocalTime;
import java.util.List;

/**
 * Transport entity representing transport.
 */
public class Transport {
    private String routeNumber;
    private int id;
    private Status status;
    private Type transportType;
    private double speed;
    private int seatPlaces;
    private int standPlaces;
    private List<Passenger> passengers;
    private LocalTime startTime, endTime;

    private double moveTime = 0;
    private int tripCount = 0;
    private int allPassengersCount = 0;

    public int getAllPassengersCount() {
        return allPassengersCount;
    }

    public void addToAllPassengersCount(int passengersCount) {
        this.allPassengersCount += passengersCount;
    }

    public enum Type {
        Trolleybus, Bus, Microbus
    }
    public enum Status {
        OnStop, OnWay
    }

    public String getRouteNumber() {
        return routeNumber;
    }

    public void setRouteNumber(String routeNumber) {
        this.routeNumber = routeNumber;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Type getTransportType() {
        return transportType;
    }

    public void setTransportType(Type transportType) {
        this.transportType = transportType;
    }

    public void setSeatPlaces(int seatPlaces) {
        this.seatPlaces = seatPlaces;
    }

    public void setStandPlaces(int standPlaces) {
        this.standPlaces = standPlaces;
    }

    public int getTotalPlaces() {
        return seatPlaces+standPlaces;
    }

    public int getOccupiedPlaces() {
        return passengers.size();
    }

    public List<Passenger> getPassengers() {
        return passengers;
    }

    public void setPassengers(List<Passenger> passengers) {
        this.passengers = passengers;
    }

    public double getMoveTime() {
        return moveTime;
    }

    public void addMoveTime(double moveTime) {
        this.moveTime += moveTime;
    }

    public void cleanMoveTime() {
        this.moveTime = 0;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getCurrentTime() {
        return startTime.plusSeconds((long) moveTime);
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public int getTripCount() {
        return tripCount;
    }

    public void addTripCount() {
        this.tripCount += 1;
    }

    public int getFreePlaces() {
        return this.getTotalPlaces() - this.getOccupiedPlaces();
    }

    @Override
    public String toString() {
        return transportType.toString() + " " + id;
    }
}
