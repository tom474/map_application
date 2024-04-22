class QuadTree {
    private static final int MAX_CAPACITY = 10_000_000;
    private final int level;
    private final List<Place> places;
    private final Rectangle bounds;
    private QuadTree[] children;

    public QuadTree(int level, Rectangle bounds) {
        this.level = level;
        this.bounds = bounds;
        this.places = new ArrayList<>();
        this.children = new QuadTree[4];
    }

    private void split() {
        int subWidth = bounds.width / 2;
        int subHeight = bounds.height / 2;
        int x = bounds.x;
        int y = bounds.y;

        // Initially, do not allocate the QuadTree objects.
        children = new QuadTree[4]; // Initialize a children array but do not create new QuadTrees yet.

        // Allocate children only when needed during the splitting process.
        for (int i = 0; i < places.size(); i++) {
            Place place = places.get(i);
            int index = getIndex(place.x, place.y);

            // Create the child node when the first relevant place is found
            if (children[index] == null) {
                Rectangle newBounds = switch (index) {
                    case 0 -> new Rectangle(x + subWidth, y, subWidth, subHeight);
                    case 1 -> new Rectangle(x, y, subWidth, subHeight);
                    case 2 -> new Rectangle(x, y + subHeight, subWidth, subHeight);
                    case 3 -> new Rectangle(x + subWidth, y + subHeight, subWidth, subHeight);
                    default -> null;
                };
                children[index] = new QuadTree(level + 1, newBounds);
            }
            children[index].insert(place);
        }
        places.clear(); // Clear the places as they are now distributed among the children
    }


    private int getIndex(int x, int y) {
        int verticalMidpoint = bounds.x + bounds.width / 2;
        int horizontalMidpoint = bounds.y + bounds.height / 2;
        boolean topQuadrant = (y < horizontalMidpoint);
        boolean leftQuadrant = (x < verticalMidpoint);

        if (leftQuadrant) {
            return topQuadrant ? 1 : 2;
        } else {
            return topQuadrant ? 0 : 3;
        }
    }

    public void insert(Place place) {
        if (!bounds.contains(place.x, place.y)) {
            throw new IllegalArgumentException("Place " + place + " is out of the bounds of the quad tree");
        }

        if (places.size() < MAX_CAPACITY && children[0] == null) {
            places.add(place);
        } else {
            if (children[0] == null) {
                split();
            }
            int index = getIndex(place.x, place.y);
            children[index].insert(place);
        }
    }

    public boolean delete(Place place) {
        if (!bounds.contains(place.x, place.y)) {
            return false;
        }

        for (int i = 0; i < places.size(); i++) {
            if (places.get(i).equals(place)) {
                places.removeAt(i);
                return true;
            }
        }

        if (children[0] != null) {
            int index = getIndex(place.x, place.y);
            return children[index].delete(place);
        }

        return false;
    }

    /**
     * Adds a service to the specified place within the quad tree.
     *
     * @param place     The place to which the service should be added.
     * @param serviceId The service to add.
     * @return true if the service was added, false if the place does not exist or the service is already added.
     */
    public boolean addService(Place place, int serviceId) {
        if (findPlace(place)) {
            place.addService(serviceId); // Directly use the service ID which is an int
            return true;
        }
        return false;
    }

    /**
     * Removes a service from the specified place within the quad tree.
     *
     * @param place     The place from which the service should be removed.
     * @param serviceId The service to remove.
     * @return true if the service was removed, false if the place does not exist or the service was not offered.
     */
    public boolean removeService(Place place, int serviceId) {
        if (findPlace(place)) {
            place.removeService(serviceId); // Directly use the service ID which is an int
            return true;
        }
        return false;
    }

    /**
     * Finds a place within the quad tree.
     *
     * @param target The place to find.
     * @return true if the place exists in the quad tree, false otherwise.
     */
    private boolean findPlace(Place target) {
        return findPlaceHelper(target, this);
    }

    /**
     * Recursive helper method to find a place in the quad tree.
     *
     * @param target The place to find.
     * @param node   The current node of the quad tree being searched.
     * @return true if the place is found, false otherwise.
     */
    private boolean findPlaceHelper(Place target, QuadTree node) {
        if (node == null) {
            return false;
        }

        for (int i = 0; i < node.places.size(); i++) {
            if (node.places.get(i).equals(target)) {
                return true;
            }
        }

        if (node.children[0] != null) {
            int index = node.getIndex(target.x, target.y);
            return findPlaceHelper(target, node.children[index]);
        }

        return false;
    }

    public List<Place> query(Rectangle range, List<Place> found) {
        if (!bounds.intersects(range)) {
            return found;
        }

        for (int i = 0; i < places.size(); i++) {
            Place place = places.get(i);
            if (range.contains(place.x, place.y)) {
                found.add(place);
            }
        }

        if (children[0] != null) {
            for (QuadTree child : children) {
                child.query(range, found);
            }
        }

        return found;
    }
}

class ServiceType {
    public static final int ATM = 0;
    public static final int RESTAURANT = 1;
    public static final int HOSPITAL = 2;
    public static final int POLICE_STATION = 3;
    public static final int FIRE_STATION = 4;
    public static final int GAS_STATION = 5;
    public static final int GROCERY_STORE = 6;
    public static final int PHARMACY = 7;
    public static final int POST_OFFICE = 8;
    public static final int SCHOOL = 9;

    public static final int NUM_SERVICES = 9; // Update this count based on actual services
}


class Place {
    int x, y; // Coordinates
    int services; // Bitmask for services

    public Place(int x, int y) {
        this.x = x;
        this.y = y;
        this.services = 0;
    }

    public void addService(int serviceId) {
        if (serviceId >= 0 && serviceId < ServiceType.NUM_SERVICES) {
            services |= (1 << serviceId);
        } else {
            throw new IllegalArgumentException("Invalid service ID: " + serviceId);
        }
    }

    public void removeService(int serviceId) {
        if (serviceId >= 0 && serviceId < ServiceType.NUM_SERVICES) {
            services &= ~(1 << serviceId);
        } else {
            throw new IllegalArgumentException("Invalid service ID: " + serviceId);
        }
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
