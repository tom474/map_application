import java.util.Random;

public class Main {
    private static final int NUM_POINTS = 100_000_000; // For practical demonstration
    private static final int MAX_COORDINATE = 10_000_000; // Maximum coordinate value
    private static final Random random = new Random();

    public static void main(String[] args) {
        // Initialize the Map2D with bounds large enough to contain all points
        Map2D map2D = new Map2D(0, new Boundary(0, 0, MAX_COORDINATE, MAX_COORDINATE));

        // Get the Java runtime
        Runtime runtime = Runtime.getRuntime();

        // Perform a garbage collection before starting the test
        runtime.gc();

        // Memory usage before the operations
        long startMemoryUse = runtime.totalMemory() - runtime.freeMemory();

        // Generate and insert places
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < NUM_POINTS; i++) {
            int x = random.nextInt(MAX_COORDINATE);
            int y = random.nextInt(MAX_COORDINATE);
            int serviceType = generateRandomService();
            int serviceBits = ServiceType.encodeService(serviceType);
            map2D.insert(x, y, serviceBits);
        }
        long endTime = System.currentTimeMillis();
        long endMemoryUse = runtime.totalMemory() - runtime.freeMemory();

        System.out.println("Insertion of " + NUM_POINTS + " points completed in " + (endTime - startTime) + " ms");
        System.out.println("Memory used for insertion: " + ((endMemoryUse - startMemoryUse) / 1024 / 1024) + " MB");

        // Insert an additional point
        int additionalX = 5_000_000;
        int additionalY = 5_000_000;
        int additionalService = ServiceType.GAS_STATION;
        int additionalServiceBits = ServiceType.encodeService(additionalService);
        long additionalInsertStart = System.currentTimeMillis();
        map2D.insert(additionalX, additionalY, additionalServiceBits);
        long additionalInsertEnd = System.currentTimeMillis();

        System.out.println("Insertion of additional point completed in " + (additionalInsertEnd - additionalInsertStart) + " ms");

        // Modify the services of the additional point
        int newServices = ServiceType.encodeService(ServiceType.ATM);
        long modifyStart = System.currentTimeMillis();
        boolean modified = map2D.editServices(additionalX, additionalY, newServices);
        long modifyEnd = System.currentTimeMillis();

        System.out.println("Modification of services completed in " + (modifyEnd - modifyStart) + " ms, Success: " + modified);

        // Remove the additional point
        long removeStart = System.currentTimeMillis();
        boolean removed = map2D.delete(additionalX, additionalY);
        long removeEnd = System.currentTimeMillis();

        System.out.println("Removal of additional point completed in " + (removeEnd - removeStart) + " ms, Success: " + removed);

        // Querying by space and service with custom location and radius
        int userX = 5_000_000; // User's current location (x coordinate)
        int userY = 5_000_000; // User's current location (y coordinate)
        int walkDistance = 100; // Distance user is willing to walk
        int queryService = ServiceType.RESTAURANT;
        int k = 50; // Number of results to return
        long serviceQueryStartTime = System.currentTimeMillis();
        ArrayList<Place> foundServicePlaces = map2D.queryByService(userX, userY, walkDistance, queryService, k);
        long serviceQueryEndTime = System.currentTimeMillis();

        System.out.println("Querying top " + k + " closest places with service " +
                ServiceType.class.getFields()[queryService].getName() + " within " + walkDistance + " distance completed in " +
                (serviceQueryEndTime - serviceQueryStartTime) + " ms");
    }

    private static int generateRandomService() {
        return random.nextInt(ServiceType.NUM_SERVICES);
    }
}
