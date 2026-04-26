package sa.edu.kau.fcit.cpit252.carwash.bridge;

public class SUVPricing implements VehiclePricing {
    @Override
    public double getMultiplier() {
        return 1.5;
    }

    @Override
    public String getVehicleName() {
        return "SUV";
    }
}

