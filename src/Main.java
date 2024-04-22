import java.util.Random;

public class Main {
    private static final int NUM_POINTS = 100_000_000; // Reduced for practicality
    private static final int MAX_COORDINATE = 10000000; // Maximum coordinate value
    private static final Random random = new Random();

    public static void main(String[] args) {
        // Initialize the QuadTree with bounds large enough to contain all points
        QuadTree quadTree = new QuadTree(0, new Rectangle(0, 0, MAX_COORDINATE, MAX_COORDINATE));

        // Get the Java runtime
        Runtime runtime = Runtime.getRuntime();

        // Perform a garbage collection before starting the test
        runtime.gc();

        // Memory usage before the operations
        long startMemoryUse = runtime.totalMemory() - runtime.freeMemory();

        // Generate and insert places
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < NUM_POINTS; i++) {
            int x = 10 + random.nextInt(MAX_COORDINATE - 10);
            int y = 10 + random.nextInt(MAX_COORDINATE - 10);
            int serviceType = generateRandomService();
            Place place = new Place(x, y);
            place.addService(serviceType);
            try {
                quadTree.insert(place);
            } catch (IllegalArgumentException e) {
                System.err.println("Failed to insert place at (" + x + ", " + y + ")");
            }
        }
        long endTime = System.currentTimeMillis();
        long endMemoryUse = runtime.totalMemory() - runtime.freeMemory();

        System.out.println("Insertion of " + NUM_POINTS + " places completed in " + (endTime - startTime) + " ms");
        System.out.println("Memory used for insertion: " + ((endMemoryUse - startMemoryUse) / 1024 / 1024) + " MB");

        // Insertion of an additional place
        long insertStartTime = System.currentTimeMillis();
        Place additionalPlace = new Place(234133, 517823);
        additionalPlace.addService(ServiceType.ATM);
        quadTree.insert(additionalPlace);
        long insertEndTime = System.currentTimeMillis();
        System.out.println("Insertion of 1 place completed in " + (insertEndTime - insertStartTime) + " ms");

        // Delete a place
        long deleteStartTime = System.currentTimeMillis();
        quadTree.delete(additionalPlace);
        long deleteEndTime = System.currentTimeMillis();
        System.out.println("Deletion of 1 place completed in " + (deleteEndTime - deleteStartTime) + " ms");

        // Querying
        long queryStartTime = System.currentTimeMillis();
        Rectangle queryRectangle = new Rectangle(500000, 500000, 200000, 200000);
        ArrayList<Place> foundPlaces = (ArrayList<Place>) quadTree.query(queryRectangle, new ArrayList<>());
        long queryEndTime = System.currentTimeMillis();
        System.out.println("Querying " + foundPlaces.size() + " places completed in " + (queryEndTime - queryStartTime) + " ms");

        // Add Service Testing
        long addServiceStartTime = System.currentTimeMillis();
        Place myPlace = new Place(100, 200); // Example coordinates
        quadTree.insert(myPlace);
        quadTree.addService(myPlace, ServiceType.ATM); // Using the ATM constant directly
        long addServiceEndTime = System.currentTimeMillis();
        System.out.println("Adding a service to a place completed in " + (addServiceEndTime - addServiceStartTime) + " ms");

        // Remove Service Testing
        long removeServiceStartTime = System.currentTimeMillis();
        quadTree.removeService(myPlace, ServiceType.ATM); // Using the ATM constant directly
        long removeServiceEndTime = System.currentTimeMillis();
        System.out.println("Removing a service from a place completed in " + (removeServiceEndTime - removeServiceStartTime) + " ms");
    }

    private static int generateRandomService() {
        return random.nextInt(ServiceType.NUM_SERVICES);
    }
}
