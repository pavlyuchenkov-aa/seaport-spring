package services.service1;

import services.common.CommonConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Timetable {

    private ArrayList<Ship> ships = new ArrayList<>();
    private final int amountShips;

    public Timetable()
    {
        this.amountShips = 100;
    }

    public Timetable(Timetable timetable)
    {
        this.amountShips = timetable.getAmountShips();
        this.ships = timetable.getShips();
    }

    public Timetable generate()
    {
        for (int i = 0; i < amountShips; i++)
        {
            ships.add(generateRandomShip(i));
        }

        Collections.sort(ships);
        return this;
    }

    public void show()
    {
        ships.forEach(System.out::println);
    }

    public ArrayList<Ship> getShips()
    {
        return ships;
    }

    public void addShip(Ship ship)
    {
        ships.add(ship);
        Collections.sort(ships);
    }

    public int getAmountShips()
    {
        return amountShips;
    }

    public static void generateArrivingDelays(Timetable timetable) {
        Random random = new Random();
        int maxDelay = CommonConstants.MAX_ARRIVING_DELAY * CommonConstants.MINUTES_IN_DAY;

        for (int i = 0; i < timetable.getAmountShips(); i++)
        {
            timetable.getShips().get(i)
                    .setArriveTime(new Time(timetable.getShips().get(i).getArriveTime().getTime()
                            + (random.nextInt(maxDelay * 2) * (-1) + maxDelay)));

            if (timetable.getShips().get(i).getArriveTime().getTime() < 0)
            {
                timetable.getShips().get(i).setArriveTime(new Time(0));
            }
        }
        Collections.sort(timetable.getShips());
    }

    public static Ship generateRandomShip(int number)
    {
        Cargo.CargoTypes cargoType;
        Random random = new Random();
        int randomValue = random.nextInt(3);

        if (randomValue == 0)
        {
            cargoType = Cargo.CargoTypes.BULK;
        }
        else if (randomValue == 1)
        {
            cargoType = Cargo.CargoTypes.LIQUID;
        }
        else
        {
            cargoType = Cargo.CargoTypes.CONTAINER;
        }

        int weight;
        int unloadingTime;

        if (Cargo.CargoTypes.BULK == cargoType)
        {
            weight = CommonConstants.MIN_CARGO_WEIGHT
                    + random.nextInt(CommonConstants.MAX_CARGO_WEIGHT - CommonConstants.MIN_CARGO_WEIGHT + 1);
            unloadingTime = weight / CommonConstants.BULK_CRANE_PERFORMANCE;
        }
        else if (Cargo.CargoTypes.LIQUID == cargoType)
        {
            weight = CommonConstants.MIN_CARGO_WEIGHT
                    + random.nextInt(CommonConstants.MAX_CARGO_WEIGHT - CommonConstants.MIN_CARGO_WEIGHT + 1);
            unloadingTime = weight / CommonConstants.LIQUID_CRANE_PERFORMANCE;
        }
        else
        {
            weight = CommonConstants.MIN_CARGO_WEIGHT
                    + random.nextInt(CommonConstants.MAX_CARGO_WEIGHT - CommonConstants.MIN_CARGO_WEIGHT + 1);
            unloadingTime = weight / CommonConstants.CONTAINER_CRANE_PERFORMANCE;
        }

        Ship ship = new Ship("ship" + number, cargoType, weight);
        ship.setArriveTime(Time.getRandomTime(CommonConstants.MAX_AMOUNT_SIMULATION_DAYS,
                CommonConstants.HOURS_IN_DAY, CommonConstants.MINUTES_IN_HOUR));

        ship.setUnloadingTime(new Time(unloadingTime));
        return ship;
    }

}

