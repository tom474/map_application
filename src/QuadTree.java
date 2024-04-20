class QuadTree {
    private static final int MAX_CAPACITY = 1000000;
    private final int level;
    private final List<Place> places;
    private final int x, y, width, height;
    private final QuadTree[] children;

    public QuadTree(int level, int x, int y, int width, int height) {
        this.level = level;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.places = new ArrayList<>();
        this.children = new QuadTree[4];
    }

    private void split() {
        int subWidth = width / 2;
        int subHeight = height / 2;

        children[0] = new QuadTree(level + 1, x + subWidth, y, subWidth, subHeight);
        children[1] = new QuadTree(level + 1, x, y, subWidth, subHeight);
        children[2] = new QuadTree(level + 1, x, y + subHeight, subWidth, subHeight);
        children[3] = new QuadTree(level + 1, x + subWidth, y + subHeight, subWidth, subHeight);

        for (int i = 0; i < places.size(); i++) {
            Place place = places.get(i);
            int index = getIndex(place.x, place.y);
            children[index].places.add(place);
        }
        places.clear();
    }

    private int getIndex(int px, int py) {
        double verticalMidpoint = x + width / 2.0;
        double horizontalMidpoint = y + height / 2.0;
        boolean topQuadrant = (py < horizontalMidpoint);
        boolean leftQuadrant = (px < verticalMidpoint);

        if (leftQuadrant) {
            return topQuadrant ? 1 : 2;
        } else {
            return topQuadrant ? 0 : 3;
        }
    }

    public void insert(Place place) {
        if (!contains(place.x, place.y)) {
            throw new IllegalArgumentException("Place is out of the bounds of the quad tree");
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
        if (!contains(place.x, place.y)) {
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

    public boolean addService(Place place, ServiceType service) {
        if (findPlace(place)) {
            place.addService(service);
            return true;
        }
        return false;
    }

    public boolean removeService(Place place, ServiceType service) {
        if (findPlace(place)) {
            place.removeService(service);
            return true;
        }
        return false;
    }

    private boolean findPlace(Place target) {
        return findPlaceHelper(target, this);
    }

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
        if (!intersects(range.x, range.y, range.width, range.height)) {
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

    public List<Place> boundedQuery(int userX, int userY, double maxDistance, ServiceType serviceType) {
        int radius = (int) maxDistance;
        Rectangle searchArea = new Rectangle(userX - radius, userY - radius, 2 * radius, 2 * radius);
        List<Place> results = query(searchArea, new ArrayList<>());
        ArrayList<Place> filteredResults = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            Place place = results.get(i);
            if (place.offersService(serviceType) && distance(userX, userY, place.x, place.y) <= maxDistance) {
                filteredResults.add(place);
            }
        }
        // Selection sort by distance for simplicity
        for (int i = 0; i < filteredResults.size() - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < filteredResults.size(); j++) {
                if (distance(userX, userY, filteredResults.get(j).x, filteredResults.get(j).y) < distance(userX, userY, filteredResults.get(minIdx).x, filteredResults.get(minIdx).y)) {
                    minIdx = j;
                }
            }
            Place temp = filteredResults.get(minIdx);
            filteredResults.set(minIdx, filteredResults.get(i));
            filteredResults.set(i, temp);
        }
        return filteredResults;
    }

    public List<Place> serviceQuery(int userX, int userY, ServiceType serviceType, int k) {
        double largeSearchRadius = 10000;  // Arbitrary large radius
        Rectangle searchArea = new Rectangle(userX - (int) largeSearchRadius, userY - (int) largeSearchRadius, (int) (2 * largeSearchRadius), (int) (2 * largeSearchRadius));
        List<Place> results = query(searchArea, new ArrayList<>());
        MaxHeap maxHeap = new MaxHeap(k, userX, userY);

        for (int i = 0; i < results.size(); i++) {
            Place place = results.get(i);
            if (place.offersService(serviceType)) {
                int[] loc = new int[]{place.x, place.y};
                maxHeap.offer(loc);
            }
        }

        ArrayList<Place> topKResults = new ArrayList<>();
        while (!maxHeap.isEmpty() && topKResults.size() < k) {
            int[] location = maxHeap.poll();
            topKResults.add(new Place(location[0], location[1])); // Simplified for context
        }

        return topKResults;
    }

    private boolean contains(int px, int py) {
        return px >= x && py >= y && px < x + width && py < y + height;
    }

    private boolean intersects(int otherX, int otherY, int otherWidth, int otherHeight) {
        return !(otherX > x + width || otherX + otherWidth < x || otherY > y + height || otherY + otherHeight < y);
    }

    private double distance(int x1, int y1, int x2, int y2) {
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    static class Place {
        int x, y;
        long services; // Bitmask for services

        public Place(int x, int y) {
            this.x = x;
            this.y = y;
            this.services = 0; // No services initially
        }

        public void addService(ServiceType service) {
            services |= (1L << service.ordinal());
        }

        public void removeService(ServiceType service) {
            services &= ~(1L << service.ordinal());
        }

        public boolean offersService(ServiceType service) {
            return (services & (1L << service.ordinal())) != 0;
        }
    }

    enum ServiceType {
        ATM, RESTAURANT, HOSPITAL, GAS_STATION, COFFEE_SHOP, GROCERY_STORE, PHARMACY, HOTEL, BANK, BOOK_STORE;
    }

    static class Rectangle {
        int x, y, width, height;

        public Rectangle(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public boolean contains(int px, int py) {
            return px >= x && py >= y && px < x + width && py < y + height;
        }
    }
}



