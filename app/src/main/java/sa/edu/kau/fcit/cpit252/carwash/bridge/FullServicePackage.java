package sa.edu.kau.fcit.cpit252.carwash.bridge;

public class FullServicePackage extends WashPackage {

    private static final double BASE_PRICE = 200;

    public FullServicePackage(VehiclePricing vehiclePricing) {
        super(vehiclePricing);
    }

    @Override
    public double getPrice() {
        return BASE_PRICE * vehiclePricing.getMultiplier();
}

    @Override
    public String getPackageName() {
        return "Full Service";

    }
}
