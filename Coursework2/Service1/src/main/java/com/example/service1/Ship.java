package com.example.service1;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Ship implements Comparable<Ship> {
    private String name;

    private Time arriveTime;
    private Time unloadingTime;

    private Cargo.CargoTypes cargoType;
    private Integer currentWeight;

    @JsonIgnore
    private final Time waitingTime;
    @JsonIgnore
    private int amountUnloadingCranes;
    @JsonIgnore
    private Time shipDelayOnCrane;
    @JsonIgnore
    private Time endUnloadingTime;

    public Ship () {
        this.waitingTime = new Time();
        this.shipDelayOnCrane = new Time();
        this.endUnloadingTime = new Time();
    }

    public Ship(String name, Cargo.CargoTypes typeCargo, int cargoWeight) {
        this.name = name;
        this.cargoType = typeCargo;
        this.currentWeight = cargoWeight;
        this.waitingTime = new Time();
        this.amountUnloadingCranes = 0;
        this.shipDelayOnCrane = new Time();
        this.endUnloadingTime = new Time();
    }

    public Ship(Ship ship)
    {
        this.name = ship.name;
        this.arriveTime = new Time(ship.arriveTime.getTime());
        this.unloadingTime = new Time(ship.unloadingTime.getTime());
        this.cargoType = ship.cargoType;
        this.currentWeight = ship.currentWeight;
        this.waitingTime = new Time(ship.waitingTime.getTime());
        this.amountUnloadingCranes = ship.amountUnloadingCranes;
        this.shipDelayOnCrane = new Time(ship.shipDelayOnCrane.getTime());
        this.endUnloadingTime = new Time(ship.endUnloadingTime.getTime());
    }

    public String getName() {
        return name;
    }

    public Time getArriveTime() {
        return arriveTime;
    }

    public Time getUnloadingTime() {
        return unloadingTime;
    }

    public Cargo.CargoTypes getCargoType() {
        return cargoType;
    }

    public Integer getCurrentWeight() {
        return currentWeight;
    }

    public Time getWaitingTime() {
        return waitingTime;
    }

    public int getAmountUnloadingCranes() {
        return amountUnloadingCranes;
    }

    public Time getShipDelayOnCrane() {
        return shipDelayOnCrane;
    }

    public void setArriveTime(Time arriveTime) {
        this.arriveTime = arriveTime;
    }

    public void setUnloadingTime(Time unloadingTime) {
        this.unloadingTime = unloadingTime;
    }

    public void setShipDelayOnCrane(Time shipDelayOnCrane) {
        this.shipDelayOnCrane = shipDelayOnCrane;
    }

    public void setEndUnloadingTime(Time endUnloadingTime) {
        this.endUnloadingTime = endUnloadingTime;
    }

    public void addWaitingTime(int minutes) {
        this.waitingTime.addTime(minutes);
    }

    public void freeAmountUnloadingCranes() {
        this.amountUnloadingCranes = 0;
    }

    public void incrementAmountUnloadingCranes() {
        if (this.amountUnloadingCranes == 2)
        {
            throw new IllegalArgumentException("Number of cranes should be 1 or 2");
        }
        this.amountUnloadingCranes++;
    }

    public void doPerMinuteUnloading(int perMinuteWeight)
    {
        if (this.currentWeight > perMinuteWeight)
        {
            this.currentWeight -= perMinuteWeight;
        }
        else
        {
            this.currentWeight = 0;
        }
    }

    public int calculateFine()
    {
        return waitingTime.getTime() / CommonConstants.MINUTES_IN_HOUR * CommonConstants.HOURLY_FINE;
    }

    public void show()
    {
        String timeStartUnload = new Time(this.getArriveTime().getTime() + this.getWaitingTime().getTime()).toString();

        System.out.println(CommonConstants.ANSI_PURPLE + "Name: " + CommonConstants.ANSI_YELLOW + this.name
                + CommonConstants.ANSI_PURPLE + " Arrive Time: " + CommonConstants.ANSI_YELLOW + arriveTime
                + CommonConstants.ANSI_PURPLE + " Cargo Type: " + CommonConstants.ANSI_YELLOW + cargoType
                + CommonConstants.ANSI_PURPLE + " Waiting time: " + CommonConstants.ANSI_YELLOW + waitingTime
                + CommonConstants.ANSI_PURPLE + " Start of unloading: " + CommonConstants.ANSI_YELLOW + timeStartUnload
                + CommonConstants.ANSI_PURPLE + " End of unloading: " + CommonConstants.ANSI_YELLOW + endUnloadingTime
                + CommonConstants.ANSI_PURPLE + " Unloading delay: " + CommonConstants.ANSI_YELLOW + shipDelayOnCrane);
    }


    @Override
    public String toString() {
        return CommonConstants.ANSI_PURPLE + "Name: " + CommonConstants.ANSI_YELLOW + name
                + CommonConstants.ANSI_PURPLE + " Arrive Time: " + CommonConstants.ANSI_YELLOW + arriveTime
                + CommonConstants.ANSI_PURPLE + " Cargo Type: " + CommonConstants.ANSI_YELLOW + cargoType
                + CommonConstants.ANSI_PURPLE + " Cargo Weight: " + CommonConstants.ANSI_YELLOW + currentWeight
                + CommonConstants.ANSI_PURPLE + " Expected Unloading Time: " + CommonConstants.ANSI_YELLOW + unloadingTime;
    }

    @Override
    public int compareTo(Ship o) {
        return Integer.compare(this.arriveTime.getTime(), o.arriveTime.getTime());
    }
}
