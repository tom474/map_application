package pnm.quadtree1;

import java.util.Random;

public class Map2DTest {
    private static final int NUM_PLACES = 100000000; // Number of places to insert
    private static final int AREA_WIDTH = 10000000; // Simulated area width
    private static final int AREA_HEIGHT = 10000000; // Simulated area height

    public static void main(String[] args) {
        // Initialize the Map2D with a root level and a large bounding rectangle
        Map2D map = new Map2D(0, new Rectangle(0, 0, AREA_WIDTH, AREA_HEIGHT));

        // Random generator for coordinates and service types
        Random random = new Random();

        // Start timing
        long startTime = System.nanoTime();

        for (int i = 0; i < NUM_PLACES; i++) {
            int x = random.nextInt(AREA_WIDTH);
            int y = random.nextInt(AREA_HEIGHT);

            // Generate random services for each place
            boolean[] services = new boolean[ServiceType.size()];
            for (int j = 0; j < services.length; j++) {
                services[j] = random.nextBoolean();
            }

            Place place = new Place(new Point(x, y), services);
            map.insert(place);
        }

        // End timing
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000; // Convert to milliseconds

        // Output the run time
        System.out.println("Insertion of " + NUM_PLACES + " places took " + duration + " ms");
    }
}

