package com.citytransportanalysis.modeling.entity;


import java.util.Arrays;
import java.util.List;

/**
 * RouteSegment entity representing route.
 */
public class RouteSegment {
    private List<Stop> twoStops;   //exactly two stops
    private double passingTime;
    private double length;

    /**
     * Сегмент пути
     *
     * @param stop1       остановка 1 {@link Stop}
     * @param stop2       остановка 2 {@link Stop}
     * @param passingTime время движения по сегменту пути в секундах
     * @param length      длина пути
     */
    public RouteSegment(Stop stop1, Stop stop2, double passingTime, double length) {
        this.twoStops = Arrays.asList(stop1, stop2);
        this.passingTime = passingTime;
        this.length = length;
    }

    public List<Stop> getTwoStops() {
        return twoStops;
    }

    public double getPassingTime() {
        return passingTime;
    }
}
