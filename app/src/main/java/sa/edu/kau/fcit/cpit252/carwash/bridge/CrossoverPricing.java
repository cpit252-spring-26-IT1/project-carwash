package sa.edu.kau.fcit.cpit252.carwash.bridge;

public class CrossoverPricing implements VehiclePricing {

    @Override
    public double getMultiplier() {
        return 1.3;
    }

    @Override
    public String getVehicleName() {
        return "Crossover";
    }
}
