package com.citytransportanalysis.modeling.entity;

/**
 * Passenger entity representing passenger.
 */
public class Passenger {
    private String name;
    private Stop from;
    private Stop to;

    /**
     * Пассажир
     *
     * @param from остановка посадки пассажира {@link Stop}
     */
    public Passenger(Stop from) {
        this.from = from;
    }
}
