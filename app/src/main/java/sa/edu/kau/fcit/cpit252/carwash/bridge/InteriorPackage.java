package sa.edu.kau.fcit.cpit252.carwash.bridge;

public class InteriorPackage extends WashPackage {
    private static final double BASE_PRICE = 80;

    public InteriorPackage(VehiclePricing vehiclePricing) {
        super(vehiclePricing);
    }

    @Override
    public double getPrice() {
        return BASE_PRICE * vehiclePricing.getMultiplier();
    }

    @Override
    public String getPackageName() {
        return "Interior Only";

    }
}
