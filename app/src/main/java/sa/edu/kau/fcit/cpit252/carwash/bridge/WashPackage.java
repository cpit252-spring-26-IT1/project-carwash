package sa.edu.kau.fcit.cpit252.carwash.bridge;

public abstract class WashPackage {
    protected VehiclePricing vehiclePricing;

    public WashPackage(VehiclePricing vehiclePricing) {
        this.vehiclePricing = vehiclePricing;
    }

    public abstract double getPrice();
    public abstract String getPackageName();
}
