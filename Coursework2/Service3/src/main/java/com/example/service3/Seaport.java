package com.example.service3;

import com.example.service1.CommonConstants;
import com.example.service1.Cargo;
import com.example.service1.Ship;
import com.example.service1.Time;
import com.example.service1.Timetable;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Scanner;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class Seaport {

    private int amountUnloadedShips;
    private final ArrayList<Ship> unloadingShips;
    private final ArrayList<Ship> allShips;
    public Timer timer;

    public Seaport(Timetable timetable) {
        this.allShips = new ArrayList<>();
        timetable.getShips().forEach((ship) -> this.allShips.add(new Ship(ship)));
        this.amountUnloadedShips = 0;
        this.unloadingShips = new ArrayList<>(100);
        this.timer = new Timer(this.allShips);
    }

    public int getAmountUnloadedShips() {
        return amountUnloadedShips;
    }

    public void incrementUnloadedShips() {
        this.amountUnloadedShips++;
    }

    public void addUnloadedShip(Ship ship)
    {
        unloadingShips.add(ship);
    }

    public boolean findUnloadingShip(Ship ship)
    {
        return unloadingShips.contains(ship);
    }

    public synchronized Ship searchShipByCargo(Cargo.CargoTypes cargoType) {
        Ship tempShip = new Ship("", cargoType, Integer.MAX_VALUE);
        if (!unloadingShips.isEmpty())
            for (Ship ship : unloadingShips) {
                if (ship.getCargoType() == cargoType && ship.getCurrentWeight() > Cargo.getPerformance(cargoType)) {
                    if (ship.getCurrentWeight() < tempShip.getCurrentWeight() && ship.getAmountUnloadingCranes() < 2) {
                        tempShip = ship;
                    }
                }
            }

        if (tempShip.getName().equals("")) {
            return null;
        }

        return tempShip;
    }

    public ArrayList<Ship> getAllShips() {
        return allShips;
    }

    public void deleteUnloadedShip(Ship ship)
    {
        unloadingShips.remove(ship);
    }

    public static void main(String[] args) {

        RestTemplate rest = new RestTemplate();
        rest.getForObject("http://localhost:8080/service2/createfile", File.class);

        Scanner in = new Scanner(System.in);
        System.out.println("Input the name of file: ");
        String fileName = in.nextLine();
        Timetable timetable = new Timetable();
        try {
            timetable = rest.getForObject("http://localhost:8080/service2/gettimetable?filename="
                    + fileName, Timetable.class);
        }
        catch (Exception e) {
            System.out.println("This file doesn't exist");
        }

        int amountBulkCranes = 1;
        int amountLiquidCranes = 1;
        int amountContainerCranes = 1;

        Object bulkFakeMutex = new Object();
        Object liquidFakeMutex = new Object();
        Object containerFakeMutex = new Object();

        Stats stats = new Stats();
        Timetable tempTimetable = new Timetable(timetable);

        Timetable.generateArrivingDelays(tempTimetable);

        while (true) {
            Seaport port = new Seaport(tempTimetable);
            ArrayList<Ship> tempShips = port.getAllShips();

            ArrayDeque<Ship> bulkShips = makeQueue(tempShips, Cargo.CargoTypes.BULK);
            ArrayDeque<Ship> liquidShips = makeQueue(tempShips, Cargo.CargoTypes.LIQUID);
            ArrayDeque<Ship> containerShips = makeQueue(tempShips, Cargo.CargoTypes.CONTAINER);

            ArrayList<Ship> totalShips = new ArrayList<>();

            int bulkFine = 0;
            int liquidFine = 0;
            int containerFine = 0;
	    int amountBulkShips = 0;
            int amountLiquidShips = 0;
            int amountContainerShips = 0;

            CyclicBarrier barrier
                    = new CyclicBarrier(amountBulkCranes + amountLiquidCranes + amountContainerCranes, port.timer);

            ArrayList<Thread> threads = new ArrayList<>();

            for (int i = 0; i < amountBulkCranes; i++) {
                threads.add(new Thread(new Crane(port, barrier, bulkFakeMutex, bulkShips, totalShips, Cargo.CargoTypes.BULK)));
            }

            for (int i = 0; i < amountLiquidCranes; i++) {
                threads.add(new Thread(new Crane(port, barrier, liquidFakeMutex, liquidShips, totalShips, Cargo.CargoTypes.LIQUID)));
            }

            for (int i = 0; i < amountContainerCranes; i++) {
                threads.add(new Thread(new Crane(port, barrier, containerFakeMutex, containerShips, totalShips, Cargo.CargoTypes.CONTAINER)));
            }

            threads.forEach(Thread::start);

            threads.forEach((thread -> {
                try {
                    thread.join();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));

            System.out.println("\n\n");
            Collections.sort(totalShips);

            totalShips.forEach((ship) -> {
                if (ship.getCurrentWeight() == 0) {
                    ship.show();
                }
            });

            int amountUnloadedShip = port.getAmountUnloadedShips();
            System.out.println(CommonConstants.ANSI_PURPLE + "Current amount of unloaded ships: "
                    + CommonConstants.ANSI_YELLOW + amountUnloadedShip);

            System.out.println(CommonConstants.ANSI_PURPLE + "Current average unloading queue: "
                    + CommonConstants.ANSI_YELLOW + port.timer.getTotalAverageQueueLength());

            int amountShips = port.getAmountUnloadedShips();
            int totalWaitingTime = totalShips.stream().filter(ship -> ship.getCurrentWeight() == 0)
                    .mapToInt(ship -> ship.getWaitingTime().getTime()).sum();
            System.out.println(CommonConstants.ANSI_PURPLE + "Current average waiting time: "
                    + CommonConstants.ANSI_YELLOW + new Time(totalWaitingTime / amountShips));

            int maxDelay = totalShips.stream().filter(ship -> ship.getCurrentWeight() == 0)
                    .mapToInt(ship -> ship.getShipDelayOnCrane().getTime()).max().orElse(Integer.MIN_VALUE);
            int averageDelay = totalShips.stream().filter(ship -> ship.getCurrentWeight() == 0)
                    .mapToInt(ship -> ship.getShipDelayOnCrane().getTime()).sum();
            averageDelay = averageDelay / port.getAmountUnloadedShips();
            System.out.println(CommonConstants.ANSI_PURPLE + "Average Unloading delay: "
                    + CommonConstants.ANSI_YELLOW + new Time(averageDelay));
            System.out.println(CommonConstants.ANSI_PURPLE + "Max Unloading delay: "
                    + CommonConstants.ANSI_YELLOW + new Time(maxDelay));


            int totalFine;

            for (Ship ship: totalShips) {
                if (ship.getCurrentWeight() == 0) {
                    if (ship.getCargoType() == Cargo.CargoTypes.BULK) {
                        amountBulkShips++;
                        bulkFine += ship.calculateFine();
                    }
                    else if (ship.getCargoType() == Cargo.CargoTypes.LIQUID) {
                        amountLiquidShips++;
                        liquidFine += ship.calculateFine();
                    }
                    else if (ship.getCargoType() == Cargo.CargoTypes.CONTAINER) {
                        amountContainerShips++;
                        containerFine += ship.calculateFine();
                    }
                }
            }

            totalFine = bulkFine + liquidFine + containerFine;
            totalFine += CommonConstants.CRANE_FINE * (amountBulkCranes + amountLiquidCranes + amountContainerCranes);

            System.out.println(CommonConstants.ANSI_PURPLE + "Current total fine: "
                    + CommonConstants.ANSI_YELLOW + totalFine);
            System.out.println(CommonConstants.ANSI_PURPLE + "Current amount of Bulk Cranes:  "
                    + CommonConstants.ANSI_YELLOW + amountBulkCranes
                    + CommonConstants.ANSI_PURPLE + "\nCurrent amount of Liquid Cranes: "
                    + CommonConstants.ANSI_YELLOW + amountLiquidCranes
                    + CommonConstants.ANSI_PURPLE + "\nCurrent amount of Container Cranes: "
                    + CommonConstants.ANSI_YELLOW + amountContainerCranes + "\n");

            if (totalFine < stats.totalFine) {
                for (Ship ship: totalShips) {
                    if (ship.getCurrentWeight() == 0) {
                        stats.ships.add(ship);
                    }
                }
                stats.saveAmountBulkShips(amountBulkShips);
                stats.saveAmountLiquidShips(amountLiquidShips);
                stats.saveAmountContainerShips(amountContainerShips);
                stats.saveTotalFine(totalFine);
                stats.saveBulkFine(bulkFine + amountBulkCranes * CommonConstants.CRANE_FINE);
                stats.saveLiquidFine(liquidFine + amountLiquidCranes * CommonConstants.CRANE_FINE);
                stats.saveContainerFine(containerFine + amountContainerCranes * CommonConstants.CRANE_FINE);
                stats.saveAmountBulkCranes(amountBulkCranes);
                stats.saveAmountLiquidCranes(amountLiquidCranes);
                stats.saveAmountContainerCranes(amountContainerCranes);
                stats.saveTotalAmountUnloadedShips(amountUnloadedShip);
                stats.saveAverageLengthUnloading(port.timer.getTotalAverageQueueLength());
                stats.saveAverageWaitingTime(new Time(totalWaitingTime / amountUnloadedShip));
                stats.saveMaxDelay(new Time(maxDelay));
                stats.saveAverageDelay(new Time(averageDelay));
            }

            if (bulkFine > CommonConstants.CRANE_FINE) {
                amountBulkCranes++;
            }
            if (liquidFine > CommonConstants.CRANE_FINE) {
                amountLiquidCranes++;
            }
            if (containerFine > CommonConstants.CRANE_FINE) {
                amountContainerCranes++;
            }

            if (bulkFine < CommonConstants.CRANE_FINE
                    && liquidFine < CommonConstants.CRANE_FINE
                    && containerFine < CommonConstants.CRANE_FINE) {
                System.out.println(CommonConstants.ANSI_PURPLE + "Total Amount of Bulk Cranes: "
                        + CommonConstants.ANSI_YELLOW + amountBulkCranes
                        + CommonConstants.ANSI_PURPLE + "\nTotal Amount of Liquid Cranes: "
                        + CommonConstants.ANSI_YELLOW + amountLiquidCranes
                        + CommonConstants.ANSI_PURPLE + "\nTotal Amount of Container Cranes: "
                        + CommonConstants.ANSI_YELLOW + amountContainerCranes);
                break;
            }
        }

        rest.postForObject("http://localhost:8080/service2/result", stats, Void.class);
    }

    private static ArrayDeque<Ship> makeQueue(ArrayList<Ship> ships, Cargo.CargoTypes cargoType)
    {
        ArrayDeque<Ship> queue = new ArrayDeque<>();

        ships.forEach((ship) -> {
            if (ship.getCargoType() == cargoType)
            {
                queue.add(ship);
            }
        });
        return queue;
    }
}
