package com.example.service1;

public class Cargo {
    public enum CargoTypes
    {
        BULK,
        LIQUID,
        CONTAINER,
    }

    public static int getPerformance(CargoTypes type)
    {
        if (type == CargoTypes.BULK)
        {
            return CommonConstants.BULK_CRANE_PERFORMANCE;
        }
        else if (type == CargoTypes.LIQUID)
        {
            return CommonConstants.LIQUID_CRANE_PERFORMANCE;
        }
        else
        {
            return CommonConstants.CONTAINER_CRANE_PERFORMANCE;
        }
    }
}
