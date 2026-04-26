package sa.edu.kau.fcit.cpit252.carwash.bridge;

public class SedanPricing implements VehiclePricing  {
    @Override
    public double getMultiplier() {

        return 1.0;

    }

    @Override
    public String getVehicleName() {
        return "Sedan";

    }

}
