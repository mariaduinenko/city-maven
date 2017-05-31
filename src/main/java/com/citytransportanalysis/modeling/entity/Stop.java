package com.citytransportanalysis.modeling.entity;

import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * Stop entity representing stop.
 */
public class Stop {
    private String name;
    private LinkedList<Passenger> passengers;
    private Map<LocalTime, Double> passengerComingTime;
    private Map<LocalTime, Double> passengerExitProbability;
    private double waitTime;
    private LocalTime lastPasengersGeneration;

    private int sittedPassengers;
    private int gettedOutPassengers;
    private int passengersOnStop;
    private int passengersLeft;


    /**
     * Остановка
     *
     * @param name                     название
     * @param passengerComingTime      список времени прихода пассажиров для каждого часа {@link Map}<{@link LocalTime}, {@link Double}>
     * @param passengerExitProbability список вероятности прихода пассажиров для каждого часа {@link Map}<{@link LocalTime}, {@link Double}>
     * @param waitTime                 время ожидания на остановке
     */
    public Stop(String name, Map<LocalTime, Double> passengerComingTime, Map<LocalTime, Double> passengerExitProbability, double waitTime) {
        this.name = name;
        this.passengerComingTime = passengerComingTime;
        this.passengerExitProbability = passengerExitProbability;
        this.waitTime = waitTime;
        passengers = new LinkedList<>();
    }

    public double getWaitTime() {
        return waitTime;
    }

    public String getName() {
        return name;
    }

    public LinkedList<Passenger> getPassengers() {
        return passengers;
    }

    /** Генерация пассажиров
     * @param time текущее время
     */
    private void GeneratePassengers(LocalTime time){
        Double difference;
        Double comingPeriod;
        LocalTime timeFrom = time.truncatedTo(ChronoUnit.HOURS);
        if (lastPasengersGeneration!=null) {
            difference = (double)Duration.between(lastPasengersGeneration, time).getSeconds();
        } else{
            difference = (double)time.getMinute() * 60; //difference in seconds from previous hour to current minutes
        }
        comingPeriod = passengerComingTime.get(timeFrom);
        //System.out.printf("%s\n", comingPeriod);
        for(double i = 0; i<=difference; i=i+comingPeriod){
            passengers.add(new Passenger(this));
        }
        //System.out.printf("Added %s passengers\n", passengers.size());
        lastPasengersGeneration = time;
    }

    /** Посадка пассажиров в транспорт
     * @param transport транспорт {@link Transport}
     * @return список пассажиров в транспорте
     */
    public List<Passenger> SettingInTransport(Transport transport){
        GeneratePassengers(transport.getCurrentTime());
        passengersOnStop = passengers.size();
        int toSit = transport.getFreePlaces();
        int setted = 0;
        while(toSit>0 && passengers.size()>0){
            transport.getPassengers().add(passengers.pollFirst());
            setted++;
            toSit--;
        }
        passengersLeft = passengersOnStop - setted;
        this.setSittedPassengers(setted);
        return transport.getPassengers();
    }

    /** Высадка пассажиров из транспорта
     * @param transport транспорт {@link Transport}
     * @return список пассажиров в транспорте
     */
    public List<Passenger> GettingOutFromTransport(Transport transport){
        Double exitProbability = passengerExitProbability.get(transport.getCurrentTime().truncatedTo(ChronoUnit.HOURS));
        int exitCount = (int) Math.round(transport.getPassengers().size() * exitProbability);
        this.setGettedOutPassengers(exitCount);
        transport.getPassengers().subList(0, exitCount).clear();
        return transport.getPassengers();
    }

    public int getGettedOutPassengers() {
        return gettedOutPassengers;
    }

    private void setGettedOutPassengers(int gettedOutPassengers) {
        this.gettedOutPassengers = gettedOutPassengers;
    }

    public int getSittedPassengers() {
        return sittedPassengers;
    }
    public int getPassengersOnStop() {
        return passengersOnStop;
    }
     public int getPassengersLeft() {
              return passengersLeft;
     }
    private void setSittedPassengers(int sittedPassengers) {
        this.sittedPassengers = sittedPassengers;
    }

    public String toString() {
        return this.getName();
    }
}
