import java.util.Random;

public class Main {
    private static final int NUM_POINTS = 10000000;  // Reduced for practicality
    private static final int MAX_COORDINATE = 10000000; // Maximum coordinate value
    public static Random random = new Random();

    public static void main(String[] args) {
        // Initialize the QuadTree with bounds large enough to contain all points
        QuadTree quadTree = new QuadTree(0, 0, 0, MAX_COORDINATE, MAX_COORDINATE);

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
            QuadTree.Place place = new QuadTree.Place(x, y);
            place.addService(generateRandomService());  // Assuming you add one random service to each place
            try {
                quadTree.insert(place);
            } catch (IllegalArgumentException e) {
                System.err.println("Failed to insert place: " + place);
            }
        }
        long endTime = System.currentTimeMillis();
        long endMemoryUse = runtime.totalMemory() - runtime.freeMemory();

        System.out.println("Insertion of " + NUM_POINTS + " places completed in " + (endTime - startTime) + " ms");
        System.out.println("Memory used for insertion: " + ((endMemoryUse - startMemoryUse) / 1024 / 1024) + " MB");

        // Insertion of an additional place
        long insertStartTime = System.currentTimeMillis();
        QuadTree.Place additionalPlace = new QuadTree.Place(234133, 517823);
        additionalPlace.addService(QuadTree.ServiceType.ATM);
        quadTree.insert(additionalPlace);
        long insertEndTime = System.currentTimeMillis();
        System.out.println("Insertion of 1 place completed in " + (insertEndTime - insertStartTime) + " ms");

        // Querying
        long queryStartTime = System.currentTimeMillis();
        QuadTree.Rectangle queryRectangle = new QuadTree.Rectangle(500000, 500000, 200000, 200000);
        List<QuadTree.Place> foundPlaces = quadTree.query(queryRectangle, new ArrayList<>());
        long queryEndTime = System.currentTimeMillis();
        System.out.println("Querying " + foundPlaces.size() + " places completed in " + (queryEndTime - queryStartTime) + " ms");
    }

    private static QuadTree.ServiceType generateRandomService() {
        return QuadTree.ServiceType.values()[random.nextInt(QuadTree.ServiceType.values().length)];
    }

    // Inner classes like QuadTree, Place, ServiceType, and Rectangle should be appropriately nested here or as separate classes within the file, as per your current structure.
}
