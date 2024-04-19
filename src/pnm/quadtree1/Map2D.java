package pnm.quadtree1;

enum ServiceType {
    ATM, RESTAURANT, HOSPITAL, GAS_STATION, COFFEE_SHOP, GROCERY_STORE, PHARMACY, HOTEL, BANK, BOOK_STORE;

    public static int size() {
        return ServiceType.values().length;
    }
}

public class Map2D {

    private static final int MAX_CAPACITY = 100000; // Formula: MAP_AREA / MAX_PLACES
    private final int level;
    private final SimpleHashSet places;  // Using SimpleHashSet to manage places
    private final Rectangle bounds;
    private final Map2D[] children;

    public Map2D(int level, Rectangle bounds) {
        this.level = level;
        this.bounds = bounds;
        this.places = new SimpleHashSet(MAX_CAPACITY);  // Initialize with max capacity
        this.children = new Map2D[4];
    }

    private void split() {
        int subWidth = bounds.width / 2;
        int subHeight = bounds.height / 2;
        int x = bounds.x;
        int y = bounds.y;

        children[0] = new Map2D(level + 1, new Rectangle(x + subWidth, y, subWidth, subHeight));
        children[1] = new Map2D(level + 1, new Rectangle(x, y, subWidth, subHeight));
        children[2] = new Map2D(level + 1, new Rectangle(x, y + subHeight, subWidth, subHeight));
        children[3] = new Map2D(level + 1, new Rectangle(x + subWidth, y + subHeight, subWidth, subHeight));

        SimpleHashSet.PlaceAccess placeAccess = places.getPlaceAccess();
        Place place;
        while ((place = placeAccess.next()) != null) {
            int index = getIndex(place);
            children[index].insert(place);
        }
        places.clear();
    }

    private int getIndex(Place place) {
        double verticalMidpoint = bounds.x + bounds.width / 2.0;
        double horizontalMidpoint = bounds.y + bounds.height / 2.0;
        boolean topQuadrant = (place.placeCoor.y < horizontalMidpoint);
        boolean leftQuadrant = (place.placeCoor.x < verticalMidpoint);

        if (leftQuadrant) {
            return topQuadrant ? 1 : 2;
        } else {
            return topQuadrant ? 0 : 3;
        }
    }

    public void insert(Place place) {
        if (!bounds.contains(place.placeCoor.x, place.placeCoor.y)) {
            throw new IllegalArgumentException("Place is out of bounds");
        }

        if (places.size() < MAX_CAPACITY && children[0] == null) {
            places.add(place);
        } else {
            if (children[0] == null) {
                split();
            }
            int index = getIndex(place);
            children[index].insert(place);
        }
    }

    public boolean delete(Place place) {
        if (!bounds.contains(place.placeCoor.x, place.placeCoor.y)) {
            return false;
        }

        if (places.remove(place)) {
            return true;
        }

        if (children[0] != null) {
            int index = getIndex(place);
            return children[index].delete(place);
        }

        return false;
    }

    public List<Place> query(Rectangle range, List<Place> found, ServiceType service) {
        if (!bounds.intersects(range)) {
            return found;
        }

        SimpleHashSet.PlaceAccess placeAccess = places.getPlaceAccess();
        Place place;
        while ((place = placeAccess.next()) != null) {
            if (range.contains(place.placeCoor.x, place.placeCoor.y) && place.offersService(service)) {
                found.add(place);
            }
        }

        if (children[0] != null) {
            for (Map2D child : children) {
                child.query(range, found, service);
            }
        }

        return found;
    }

    /**
     * Queries the quadtree for places within a specified walking distance from a user's position
     * that offer a specified service.
     *
     * @param userPosition The user's current position.
     * @param maxDistance The maximum distance the user is willing to walk.
     * @param service The type of service the user is interested in.
     * @return A list of places within the walking distance offering the specified service.
     */
    public List<Place> queryByDistanceAndService(Point userPosition, int maxDistance, ServiceType service) {
        // Calculate the bounds based on the max distance
        Rectangle searchArea = new Rectangle(
                userPosition.x - maxDistance,
                userPosition.y - maxDistance,
                2 * maxDistance,
                2 * maxDistance
        );

        ArrayList<Place> foundPlaces = new ArrayList<>();
        return query(searchArea, foundPlaces, service);
    }

    public List<Place> queryByService(Point targetPoint, int k, ServiceType service) {
        MaxHeap maxHeap = new MaxHeap(k, targetPoint.x, targetPoint.y);
        queryByServiceRecursive(maxHeap, bounds, service);

        List<Place> result = new ArrayList<>();
        while (!maxHeap.isEmpty()) {
            result.add(maxHeap.poll()); // Add in reverse order since it's a max heap
        }
        return result;
    }

    private void queryByServiceRecursive(MaxHeap maxHeap, Rectangle range, ServiceType service) {
        if (!bounds.intersects(range)) {
            return;
        }

        SimpleHashSet.PlaceAccess placeAccess = places.getPlaceAccess();
        Place place;
        while ((place = placeAccess.next()) != null) {
            if (place.offersService(service)) {
                maxHeap.offer(place);
            }
        }

        if (children[0] != null) {
            for (Map2D child : children) {
                child.queryByServiceRecursive(maxHeap, range, service);
            }
        }
    }

}

class Place {
    protected Point placeCoor;
    protected boolean[] services;  // Boolean array to track which services are offered

    public Place(Point coordinates, boolean[] services) {
        this.placeCoor = coordinates;
        this.services = services;
    }

    public Place(Point placeCoor, ServiceType[] servicesToAdd) {
        this.placeCoor = placeCoor;
        this.services = new boolean[ServiceType.size()];
        for (ServiceType service : servicesToAdd) {
            addService(service);
        }
    }

    public boolean offersService(ServiceType service) {
        return services[service.ordinal()];
    }

    public void addService(ServiceType service) {
        services[service.ordinal()] = true;
    }

    public void removeService(ServiceType service) {
        services[service.ordinal()] = false;
    }

    // New method to update multiple services at once
    public void updateServices(ServiceType[] servicesToAdd, ServiceType[] servicesToRemove) {
        for (ServiceType service : servicesToAdd) {
            addService(service);
        }
        for (ServiceType service : servicesToRemove) {
            removeService(service);
        }
    }

    public int[][] getServices() {
        SimpleList serviceList = new SimpleList(ServiceType.size());
        for (ServiceType service : ServiceType.values()) {
            if (offersService(service)) {
                serviceList.add(new int[]{service.ordinal()});
            }
        }
        return serviceList.toArray();
    }
}


class Point {
    int x, y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean isEqual(int x, int y) {
        return this.x == x && this.y == y;
    }
}

class Rectangle {
    int x, y;
    int width, height;

    public Rectangle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean contains(int x, int y) {
        return x >= this.x && y >= this.y && x < this.x + this.width && y < this.y + this.height;
    }

    public boolean intersects(Rectangle other) {
        return !(other.x > x + width || other.x + other.width < x || other.y > y + height || other.y + other.height < y);
    }
}
