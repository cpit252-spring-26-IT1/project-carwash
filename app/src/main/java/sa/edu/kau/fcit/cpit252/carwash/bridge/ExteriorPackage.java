package sa.edu.kau.fcit.cpit252.carwash.bridge;

public class ExteriorPackage extends WashPackage {
    private static final double BASE_PRICE = 100;

    public ExteriorPackage(VehiclePricing vehiclePricing) {
        super(vehiclePricing);
    }

    @Override
    public double getPrice() {
        return BASE_PRICE * vehiclePricing.getMultiplier();
    }

    @Override
    public String getPackageName() {
        return "Exterior Only";
    }
}
