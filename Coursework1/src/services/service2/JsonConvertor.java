package services.service2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import services.common.CommonConstants;
import services.service1.Cargo;
import services.service1.Timetable;
import services.service1.Ship;
import services.service1.Time;
import services.service3.Seaport;
import java.io.File;
import java.util.Scanner;

public class JsonConvertor {

    public static void addConsoleShip(Timetable timetable)
    {
        while(true) {
            System.out.print("Do you want to add ship? Yes/No ");
            Scanner in = new Scanner(System.in);
            String answer = in.nextLine();
            if (!(answer.equalsIgnoreCase("Yes"))) {
                break;
            }
            Cargo.CargoTypes cargoType;
            Time unloadingTime;
            System.out.print("Enter ship name: ");
            String name = in.nextLine();
            System.out.print("Enter the type of cargo: ");
            String cargoTypeString = in.nextLine();
            if (cargoTypeString.equalsIgnoreCase("BULK")) {
                cargoType = Cargo.CargoTypes.BULK;
            }
            else if (cargoTypeString.equalsIgnoreCase("LIQUID")) {
                cargoType = Cargo.CargoTypes.LIQUID;
            }
            else if (cargoTypeString.equalsIgnoreCase("CONTAINER")) {
                cargoType = Cargo.CargoTypes.CONTAINER;
            }
            else {
                throw new IllegalArgumentException("This type of cargo doesn't exist");
            }

            System.out.print("Enter the day of arrive: ");
            int arriveDay = in.nextInt();

            System.out.print("Enter the hours of arrive: ");
            int arriveHours = in.nextInt();

            System.out.print("Enter the minutes of arrive: ");
            int arriveMinutes = in.nextInt();

            Time arriveTime = new Time(arriveDay, arriveHours, arriveMinutes);

            System.out.print("Enter the weight of cargo: ");
            int cargoWeight = in.nextInt();

            if (Cargo.CargoTypes.BULK == cargoType)
            {
                unloadingTime = new Time(cargoWeight / CommonConstants.BULK_CRANE_PERFORMANCE);
            }
            else if (Cargo.CargoTypes.LIQUID == cargoType)
            {
                unloadingTime = new Time (cargoWeight / CommonConstants.LIQUID_CRANE_PERFORMANCE);
            }
            else
            {
                unloadingTime = new Time(cargoWeight / CommonConstants.CONTAINER_CRANE_PERFORMANCE);
            }

            Ship newShip = new Ship(name, cargoType, cargoWeight);
            newShip.setArriveTime(arriveTime);

            newShip.setUnloadingTime(unloadingTime);
            timetable.addShip(newShip);
        }
    }

    public static void main(String[] args) {
        try {
            Timetable timetable = new Timetable().generate();
            timetable.show();

            addConsoleShip(timetable);

            ObjectWriter writer = new ObjectMapper().writer().withDefaultPrettyPrinter();
            writer.writeValue(new File("timetable.json"), timetable);

            Seaport seaPort = new Seaport();
            seaPort.simulate(timetable);
            seaPort.saveSimulation(seaPort.getStats(), "stats.json");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}

