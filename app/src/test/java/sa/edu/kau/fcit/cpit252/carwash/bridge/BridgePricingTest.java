package sa.edu.kau.fcit.cpit252.carwash.bridge;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.junit.Assert.*;

public class BridgePricingTest {

    private static final double DELTA = 0.0001;

    @Test
    public void vehiclePricing_shouldReturnCorrectVehicleNamesAndMultipliers() {
        VehiclePricing sedan = new SedanPricing();
        VehiclePricing crossover = new CrossoverPricing();
        VehiclePricing suv = new SUVPricing();

        assertEquals("Sedan", sedan.getVehicleName());
        assertEquals(1.0, sedan.getMultiplier(), DELTA);

        assertEquals("Crossover", crossover.getVehicleName());
        assertEquals(1.3, crossover.getMultiplier(), DELTA);

        assertEquals("SUV", suv.getVehicleName());
        assertEquals(1.5, suv.getMultiplier(), DELTA);
    }

    @Test
    public void washPackages_shouldReturnCorrectNames() {
        VehiclePricing sedan = new SedanPricing();

        assertEquals("Exterior Only", new ExteriorPackage(sedan).getPackageName());
        assertEquals("Interior Only", new InteriorPackage(sedan).getPackageName());
        assertEquals("Full Service", new FullServicePackage(sedan).getPackageName());
    }

    @Test
    public void exteriorPackage_shouldCalculatePriceForEveryVehicleType() {
        assertEquals(100.0, new ExteriorPackage(new SedanPricing()).getPrice(), DELTA);
        assertEquals(130.0, new ExteriorPackage(new CrossoverPricing()).getPrice(), DELTA);
        assertEquals(150.0, new ExteriorPackage(new SUVPricing()).getPrice(), DELTA);
    }

    @Test
    public void interiorPackage_shouldCalculatePriceForEveryVehicleType() {
        assertEquals(80.0, new InteriorPackage(new SedanPricing()).getPrice(), DELTA);
        assertEquals(104.0, new InteriorPackage(new CrossoverPricing()).getPrice(), DELTA);
        assertEquals(120.0, new InteriorPackage(new SUVPricing()).getPrice(), DELTA);
    }

    @Test
    public void fullServicePackage_shouldCalculatePriceForEveryVehicleType() {
        assertEquals(200.0, new FullServicePackage(new SedanPricing()).getPrice(), DELTA);
        assertEquals(260.0, new FullServicePackage(new CrossoverPricing()).getPrice(), DELTA);
        assertEquals(300.0, new FullServicePackage(new SUVPricing()).getPrice(), DELTA);
    }

    @Test
    public void bridgeClasses_shouldHaveCorrectInheritanceAndImplementationRelations() {
        assertTrue(WashPackage.class.isAssignableFrom(ExteriorPackage.class));
        assertTrue(WashPackage.class.isAssignableFrom(InteriorPackage.class));
        assertTrue(WashPackage.class.isAssignableFrom(FullServicePackage.class));

        assertTrue(VehiclePricing.class.isAssignableFrom(SedanPricing.class));
        assertTrue(VehiclePricing.class.isAssignableFrom(CrossoverPricing.class));
        assertTrue(VehiclePricing.class.isAssignableFrom(SUVPricing.class));
        assertTrue(VehiclePricing.class.isInterface());
        assertTrue(Modifier.isAbstract(WashPackage.class.getModifiers()));
    }

    @Test
    public void washPackage_shouldKeepReferenceToVehiclePricingImplementation() throws Exception {
        VehiclePricing suv = new SUVPricing();
        WashPackage washPackage = new FullServicePackage(suv);

        Field field = WashPackage.class.getDeclaredField("vehiclePricing");
        field.setAccessible(true);

        assertSame("Bridge relation should keep the selected VehiclePricing object", suv, field.get(washPackage));
    }
}
