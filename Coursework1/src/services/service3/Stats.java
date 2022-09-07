package services.service3;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import services.service1.Ship;
import services.service1.Time;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

@JsonAutoDetect
public class Stats {
    public ArrayList<Ship> ships;
    public Integer averageLengthUnloading;
    public Time averageWaitingTime;
    public Time averageDelay;
    public Time maxDelay;
    public Integer totalAmountUnloadedShips;
    public Integer amountBulkShips;
    public Integer amountLiquidShips;
    public Integer amountContainerShips;
    public Integer totalFine;
    public Integer bulkFine;
    public Integer liquidFine;
    public Integer containerFine;
    public Integer amountBulkCranes;
    public Integer amountLiquidCranes;
    public Integer amountContainerCranes;

    Stats() {
        ships = new ArrayList<>();
        totalFine = Integer.MAX_VALUE;
    }

    public void saveAverageDelay(Time averageDelay) {
        this.averageDelay = averageDelay;
    }

    public void saveAverageLengthUnloading(int averageLengthUnloading) {
        this.averageLengthUnloading = averageLengthUnloading;
    }

    public void saveAverageWaitingTime(Time averageWaitingTime) {
        this.averageWaitingTime = averageWaitingTime;
    }

    public void saveMaxDelay(Time maxDelay) {
        this.maxDelay = maxDelay;
    }

    public void saveTotalFine(int totalFine) {
        this.totalFine = totalFine;
    }

    public void saveTotalAmountUnloadedShips(int totalAmountUnloadedShips) {
        this.totalAmountUnloadedShips = totalAmountUnloadedShips;
    }

    public void saveAmountBulkCranes(int amountBulkCranes) {
        this.amountBulkCranes = amountBulkCranes;
    }

    public void saveAmountLiquidCranes(int amountLiquidCranes) {
        this.amountLiquidCranes = amountLiquidCranes;
    }

    public void saveAmountContainerCranes(int amountContainerCranes) {
        this.amountContainerCranes = amountContainerCranes;
    }

    public void saveBulkFine(int bulkFine) {
        this.bulkFine = bulkFine;
    }

    public void saveLiquidFine(int liquidFine) {
        this.liquidFine = liquidFine;
    }

    public void saveContainerFine(int containerFine) {
        this.containerFine = containerFine;
    }

    public void saveAmountBulkShips(int amountBulkShips) {
        this.amountBulkShips = amountBulkShips;
    }

    public void saveAmountLiquidShips(int amountLiquidShips) {
        this.amountLiquidShips = amountLiquidShips;
    }

    public void saveAmountContainerShips(int amountContainerShips) {
        this.amountContainerShips = amountContainerShips;
    }

    public static void saveToFile(Stats stats, String fileName) throws IOException {
        ObjectWriter writer = new ObjectMapper().writer().withDefaultPrettyPrinter();
        writer.writeValue(new File(fileName), stats);
    }
}
