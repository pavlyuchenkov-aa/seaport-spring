package services.service3;

import services.common.CommonConstants;
import services.service1.Ship;
import services.service1.Time;
import services.service1.Cargo;

import java.util.*;
import java.util.concurrent.CyclicBarrier;

public class Crane implements Runnable {

    private final Seaport seaPort;
    private final CyclicBarrier cyclicBarrier;
    private final Object fakeMutex;
    private final ArrayDeque<Ship> ships;
    private final ArrayList<Ship> unloadedShips;
    private static final Random random = new Random();
    private Ship unloadingShip;
    private final Cargo.CargoTypes cargoType;

    public Crane(Seaport seaPort, CyclicBarrier barrier, Object mutex, ArrayDeque<Ship> ships, ArrayList<Ship> unloadedShips, Cargo.CargoTypes cargoType) {
        this.unloadingShip = null;
        this.seaPort = seaPort;
        this.cyclicBarrier = barrier;
        this.fakeMutex = mutex;
        this.ships = ships;
        this.unloadedShips = unloadedShips;
        this.cargoType = cargoType;
    }

    @Override
    public void run()
    {
        try {
            int craneDelayCounter = 0;
            int randomDelay;
            do {
                if (unloadingShip != null) {
                    synchronized (unloadingShip) {
                        unloadingShip.doPerMinuteUnloading(Cargo.getPerformance(cargoType));

                        if (unloadingShip.getCurrentWeight() == 0) {
                            craneDelayCounter--;
                        }

                        if (unloadingShip.getCurrentWeight() == 0 && craneDelayCounter <= 0) {
                            if (seaPort.findUnloadingShip(unloadingShip)) {
                                synchronized (seaPort) {
                                    seaPort.incrementUnloadedShips();
                                }
                            }

                            if (unloadingShip != null) {
                                unloadingShip.freeAmountUnloadingCranes();
                                unloadingShip.setEndUnloadingTime(new Time(seaPort.timer.getTime()));
                            }

                            synchronized (seaPort) {
                                seaPort.deleteUnloadingShip(unloadingShip);
                            }

                            unloadingShip = null;
                        }
                    }
                } else {
                    randomDelay = random.nextInt(CommonConstants.MAX_UNLOADING_DELAY
                            * CommonConstants.MINUTES_IN_DAY + 1);
                    craneDelayCounter = randomDelay;

                    synchronized (fakeMutex) {
                        Ship currentShip = seaPort.searchShipByCargo(cargoType);

                        if (currentShip != null) {
                            int waitingTime = 0;
                            int unloadTime = 0;

                            if (!ships.isEmpty()) {
                                waitingTime = seaPort.timer.getTime()
                                        - Objects.requireNonNull(ships.peek()).getArriveTime().getTime();

                                unloadTime = currentShip.getCurrentWeight()
                                        / Cargo.getPerformance(currentShip.getCargoType());
                            }

                            if (isConnectAs2(ships, currentShip, waitingTime, unloadTime)) {
                                unloadingShip = currentShip;
                                craneDelayCounter = unloadingShip.getShipDelayOnCrane().getTime();
                                unloadingShip.incrementAmountUnloadingCranes();
                            } else {
                                if (waitingTime >= 0 && !ships.isEmpty()) {
                                    popShip();
                                    unloadingShip.setShipDelayOnCrane(new Time(randomDelay));
                                }
                            }
                        } else {
                            if (!ships.isEmpty()) {
                                if (seaPort.timer.getTime() - Objects.requireNonNull(ships.peek()).getArriveTime().getTime() >= 0) {
                                    popShip();
                                    unloadingShip.setShipDelayOnCrane(new Time(randomDelay));
                                }
                            }
                        }
                    }
                }

                cyclicBarrier.await();
                //timer increments

            } while (!seaPort.timer.isEndSimulation());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private boolean isConnectAs2(ArrayDeque<Ship> ships, Ship currentShip,
                                                 int waitingTime, int unloadTime)
    {
        if (currentShip.getAmountUnloadingCranes() < CommonConstants.MAX_AMOUNT_UNLOADING_CRANES && ships.isEmpty()) {
            return true;
        }

        if (waitingTime < 0 && currentShip.getAmountUnloadingCranes() < CommonConstants.MAX_AMOUNT_UNLOADING_CRANES) {
            return true;
        }

        if (ships.peek() != null) {
            return (waitingTime > 0
                    && (double) (ships.peek().getCurrentWeight() / currentShip.getCurrentWeight()) < CommonConstants.MAX_SHIPS_RATIO
                    && ships.peek() != null
                    && (double) (ships.peek().getCurrentWeight() / currentShip.getCurrentWeight()) > CommonConstants.MIN_SHIPS_RATIO
                    && unloadTime + waitingTime < Objects.requireNonNull(ships.peek()).getUnloadingTime().getTime()
                    && currentShip.getAmountUnloadingCranes() < CommonConstants.MAX_AMOUNT_UNLOADING_CRANES);
        }
        return false;
    }

    private synchronized void popShip() {
        synchronized (ships) {
            unloadingShip = ships.poll();
            synchronized (unloadedShips) {
                unloadedShips.add(unloadingShip);
            }

            unloadingShip.incrementAmountUnloadingCranes();
            unloadingShip.addWaitingTime(seaPort.timer.getTime() - unloadingShip.getArriveTime().getTime());
            seaPort.addUnloadingShip(unloadingShip);
        }
    }
}