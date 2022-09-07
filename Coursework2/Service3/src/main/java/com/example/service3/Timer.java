package com.example.service3;

import com.example.service1.CommonConstants;
import com.example.service1.Ship;

import java.util.ArrayList;

public class Timer implements Runnable {
    ArrayList<Ship> ships;
    public int time;
    private boolean isEnd;
    private int amountShipsInQueue;

    public Timer(ArrayList<Ship> ships)
    {
        this.amountShipsInQueue = 0;
        this.ships = ships;
        this.time = 0;
        this.isEnd = false;
    }

    public int getTime() {
        return time;
    }

    public boolean isEndSimulation()
    {
        return isEnd;
    }

    public int getTotalAverageQueueLength()
    {
        return amountShipsInQueue / time + 1;
    }

    @Override
    public void run() {
        this.time++;

        for (Ship ship : ships)
        {
            if (ship.getArriveTime().getTime() <= time && ship.getShipDelayOnCrane().getTime() == 0)
            {
                amountShipsInQueue++;
            }
        }

        if (this.time > CommonConstants.MAX_AMOUNT_SIMULATION_DAYS * CommonConstants.MINUTES_IN_DAY)
        {
            isEnd = true;
        }
    }

}
