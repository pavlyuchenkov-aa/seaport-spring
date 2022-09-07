package services.service3;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import services.common.CommonConstants;
import services.service1.Cargo;
import services.service1.Ship;
import services.service1.Time;
import services.service1.Timetable;

import java.io.File;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.concurrent.CyclicBarrier;
import java.io.IOException;

public class Seaport {

    private int amountUnloadedShips;
    private final ArrayList<Ship> unloadingShips;
    private final ArrayList<Ship> allShips;
    public Timer timer;
    private Stats stats;

    public Seaport() {
        ObjectMapper reader = new ObjectMapper();
        this.allShips = new ArrayList<>();
        this.amountUnloadedShips = 0;
        this.unloadingShips = new ArrayList<>(10);
        this.timer = new Timer(this.allShips);
        try {
            Timetable timetable = reader.readValue(new File("timetable.json"), new TypeReference<>() {});
            timetable.getShips().forEach((ship) -> this.allShips.add(new Ship(ship)));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Seaport(Timetable timetable) {
        this.allShips = new ArrayList<>();
        timetable.getShips().forEach((ship) -> this.allShips.add(new Ship(ship)));
        this.amountUnloadedShips = 0;
        this.unloadingShips = new ArrayList<>(100);
        this.timer = new Timer(this.allShips);
    }

    public void saveSimulation(Stats stats, String fileName) throws IOException {
        Stats.saveToFile(stats, fileName);
    }

    public Stats getStats() {
        return this.stats;
    }

    public int getAmountUnloadedShips() {
        return amountUnloadedShips;
    }

    public void incrementUnloadedShips() {
        this.amountUnloadedShips++;
    }

    public void addUnloadingShip(Ship ship)
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

    public void deleteUnloadingShip(Ship ship)
    {
        unloadingShips.remove(ship);
    }

    public void simulate(Timetable timetable) {
        int amountBulkCranes = 1;
        int amountLiquidCranes = 1;
        int amountContainerCranes = 1;

        Object bulkFakeMutex = new Object();
        Object liquidFakeMutex = new Object();
        Object containerFakeMutex = new Object();

        stats = new Stats();
        Timetable tempTimetable = new Timetable(timetable);

        Timetable.generateArrivingDelays(tempTimetable);

        while (true) {
            Seaport seaport = new Seaport(tempTimetable);
            ArrayList<Ship> tempShips = seaport.getAllShips();

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
                    = new CyclicBarrier(amountBulkCranes + amountLiquidCranes + amountContainerCranes, seaport.timer);

            ArrayList<Thread> threads = new ArrayList<>();

            for (int i = 0; i < amountBulkCranes; i++) {
                threads.add(new Thread(new Crane(seaport, barrier, bulkFakeMutex, bulkShips, totalShips, Cargo.CargoTypes.BULK)));
            }

            for (int i = 0; i < amountLiquidCranes; i++) {
                threads.add(new Thread(new Crane(seaport, barrier, liquidFakeMutex, liquidShips, totalShips, Cargo.CargoTypes.LIQUID)));
            }

            for (int i = 0; i < amountContainerCranes; i++) {
                threads.add(new Thread(new Crane(seaport, barrier, containerFakeMutex, containerShips, totalShips, Cargo.CargoTypes.CONTAINER)));
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

            int amountUnloadedShips = 0;

            for (Ship ship: totalShips) {
                if (ship.getCurrentWeight() == 0) {
                    amountUnloadedShips++;
                    ship.show();
                }
            }

            System.out.println(CommonConstants.ANSI_PURPLE + "Current amount of unloaded ships: "
                    + CommonConstants.ANSI_YELLOW + amountUnloadedShips);

            System.out.println(CommonConstants.ANSI_PURPLE + "Current average unloading queue: "
                    + CommonConstants.ANSI_YELLOW + seaport.timer.getAverageQueueLength());

            int totalWaitingTime = totalShips.stream().filter(ship -> ship.getCurrentWeight() == 0)
                    .mapToInt(ship -> ship.getWaitingTime().getTime()).sum();
            System.out.println(CommonConstants.ANSI_PURPLE + "Current average waiting time: "
                    + CommonConstants.ANSI_YELLOW + new Time(totalWaitingTime / amountUnloadedShips));

            int maxDelay = totalShips.stream().filter(ship -> ship.getCurrentWeight() == 0)
                    .mapToInt(ship -> ship.getShipDelayOnCrane().getTime()).max().orElse(0);
            int averageDelay = (totalShips.stream().filter(ship -> ship.getCurrentWeight() == 0)
                    .mapToInt(ship -> ship.getShipDelayOnCrane().getTime()).sum())
                    / seaport.getAmountUnloadedShips();
            System.out.println(CommonConstants.ANSI_PURPLE + "Average Unloading delay: "
                    + CommonConstants.ANSI_YELLOW + new Time(averageDelay));
            System.out.println(CommonConstants.ANSI_PURPLE + "Max Unloading delay: "
                    + CommonConstants.ANSI_YELLOW + new Time(maxDelay));

            int totalFine;

            for (Ship ship : totalShips) {
                if (ship.getCurrentWeight() == 0) {
                    if (ship.getCargoType() == Cargo.CargoTypes.BULK) {
                        amountBulkShips++;
                        bulkFine += ship.calculateFine();
                    } else if (ship.getCargoType() == Cargo.CargoTypes.LIQUID) {
                        amountLiquidShips++;
                        liquidFine += ship.calculateFine();
                    } else if (ship.getCargoType() == Cargo.CargoTypes.CONTAINER) {
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
                stats.saveTotalAmountUnloadedShips(amountUnloadedShips);
                stats.saveAverageLengthUnloading(seaport.timer.getAverageQueueLength());
                stats.saveAverageWaitingTime(new Time(totalWaitingTime / amountUnloadedShips));
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
