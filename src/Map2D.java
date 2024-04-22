/**
 * A class representing a 2D spatial index structure similar to a quadtree,
 * optimized for storing and querying geographical data points with associated services.
 */
class Map2D {
    private static final int MAX_CAPACITY = 10_000_000;
    private final int level;
    private final Boundary bounds;
    private final int[] xs;
    private final int[] ys;
    private final int[] services;
    private final Map2D[] children;
    private int placeCount = 0;

    /**
     * Constructs a new Map2D node.
     * @param level the depth level of the node in the tree.
     * @param bounds the spatial boundaries of this node.
     */
    public Map2D(int level, Boundary bounds) {
        this.level = level;
        this.bounds = bounds;
        this.xs = new int[MAX_CAPACITY];
        this.ys = new int[MAX_CAPACITY];
        this.services = new int[MAX_CAPACITY];
        this.children = new Map2D[4];
    }

    /**
     * Splits this node into four children, distributing its contents among them.
     */
    private void split() {
        Boundary[] childBounds = getRectangles();
        for (int i = 0; i < 4; i++) {
            children[i] = new Map2D(level + 1, childBounds[i]);
        }
        for (int i = 0; i < placeCount; i++) {
            int index = getIndex(xs[i], ys[i]);
            children[index].insert(xs[i], ys[i], services[i]);
        }
        placeCount = 0; // Clear current node data
    }

    /**
     * Computes the boundaries for the four children of this node.
     * @return an array of {@link Boundary} objects for each quadrant.
     */
    private Boundary[] getRectangles() {
        int subWidth = bounds.width / 2;
        int subHeight = bounds.height / 2;
        int x = bounds.x;
        int y = bounds.y;
        return new Boundary[]{
                new Boundary(x, y, subWidth, subHeight), // Top-left
                new Boundary(x + subWidth, y, subWidth, subHeight), // Top-right
                new Boundary(x, y + subHeight, subWidth, subHeight), // Bottom-left
                new Boundary(x + subWidth, y + subHeight, subWidth, subHeight) // Bottom-right
        };
    }

    /**
     * Determines the child index for a given coordinate within the node's bounds.
     * @param x the x-coordinate.
     * @param y the y-coordinate.
     * @return the index of the child quadrant that contains the coordinate.
     */
    private int getIndex(int x, int y) {
        int verticalMidpoint = bounds.x + bounds.width / 2;
        int horizontalMidpoint = bounds.y + bounds.height / 2;
        boolean topQuadrant = (y < horizontalMidpoint);
        boolean rightQuadrant = (x >= verticalMidpoint);
        if (topQuadrant) {
            return rightQuadrant ? 1 : 0;
        } else {
            return rightQuadrant ? 3 : 2;
        }
    }

    /**
     * Inserts a new data point into the tree.
     * @param x the x-coordinate of the data point.
     * @param y the y-coordinate of the data point.
     * @param serviceBits the bitmask representing the services available at this point.
     */
    public void insert(int x, int y, int serviceBits) {
        if (!bounds.contains(x, y)) {
            throw new IllegalArgumentException("Point is out of the bounds of the quad tree");
        }
        if (children[0] != null) {
            int index = getIndex(x, y);
            children[index].insert(x, y, serviceBits);
        } else {
            if (placeCount < MAX_CAPACITY) {
                xs[placeCount] = x;
                ys[placeCount] = y;
                services[placeCount] = serviceBits;
                placeCount++;
            } else {
                split();
                insert(x, y, serviceBits);
            }
        }
    }

    /**
     * Edits the service information for a specific data point.
     * @param x the x-coordinate of the data point.
     * @param y the y-coordinate of the data point.
     * @param newServiceBits the new bitmask representing the services at the data point.
     * @return true if the point was found and modified, false otherwise.
     */
    public boolean editServices(int x, int y, int newServiceBits) {
        for (int i = 0; i < placeCount; i++) {
            if (xs[i] == x && ys[i] == y) {
                services[i] = newServiceBits;
                return true;
            }
        }

        if (children[0] != null) {
            int index = getIndex(x, y);
            return children[index].editServices(x, y, newServiceBits);
        }

        return false;
    }

    /**
     * Deletes a data point from the map.
     * @param x the x-coordinate of the data point to delete.
     * @param y the y-coordinate of the data point to delete.
     * @return true if the data point was found and deleted, false otherwise.
     */
    public boolean delete(int x, int y) {
        for (int i = 0; i < placeCount; i++) {
            if (xs[i] == x && ys[i] == y) {
                // Shift all elements to fill the gap
                System.arraycopy(xs, i + 1, xs, i, placeCount - i - 1);
                System.arraycopy(ys, i + 1, ys, i, placeCount - i - 1);
                System.arraycopy(services, i + 1, services, i, placeCount - i - 1);
                placeCount--;
                return true;
            }
        }

        if (children[0] != null) {
            int index = getIndex(x, y);
            return children[index].delete(x, y);
        }

        return false;
    }

    /**
     * Queries the map for places within a certain distance of a point that offer a specific service.
     * @param userX the x-coordinate of the query center.
     * @param userY the y-coordinate of the query center.
     * @param walkDistance the maximum distance from the query point.
     * @param serviceId the service identifier to query.
     * @param k the maximum number of results to return.
     * @return a list of places that match the query criteria, sorted by distance.
     */
    public ArrayList<Place> queryByService(int userX, int userY, int walkDistance, int serviceId, int k) {
        Boundary searchArea = new Boundary(userX - walkDistance, userY - walkDistance, 2 * walkDistance, 2 * walkDistance);
        ArrayList<PlaceWithDistance> results = new ArrayList<>();
        queryByServiceHelper(searchArea, serviceId, userX, userY, results);

        quickSort(results, 0, results.size() - 1); // QuickSort implemented to sort results

        ArrayList<Place> topKPlaces = new ArrayList<>();
        for (int i = 0; i < Math.min(k, results.size()); i++) {
            topKPlaces.add(results.get(i).place);
        }
        return topKPlaces;
    }

    /**
     * Helper method for recursive querying by service within certain bounds.
     * @param range the spatial boundary to query within.
     * @param serviceId the service identifier to filter by.
     * @param queryX the x-coordinate of the query center.
     * @param queryY the y-coordinate of the query center.
     * @param results list to accumulate the query results.
     */
    private void queryByServiceHelper(Boundary range, int serviceId, int queryX, int queryY, ArrayList<PlaceWithDistance> results) {
        if (!bounds.intersects(range)) {
            return;
        }

        for (int i = 0; i < placeCount; i++) {
            if (range.contains(xs[i], ys[i]) && ServiceType.hasService(services[i], serviceId)) {
                double dist = Math.sqrt(Math.pow(xs[i] - queryX, 2) + Math.pow(ys[i] - queryY, 2));
                results.add(new PlaceWithDistance(new Place(xs[i], ys[i], services[i]), dist));
            }
        }

        if (children[0] != null) {
            for (Map2D child : children) {
                child.queryByServiceHelper(range, serviceId, queryX, queryY, results);
            }
        }
    }

    /**
     * Implements the QuickSort algorithm to sort a list of places by distance.
     * @param array the list of places with distances to sort.
     * @param low the starting index for the sort.
     * @param high the ending index for the sort.
     */
    private void quickSort(ArrayList<PlaceWithDistance> array, int low, int high) {
        if (low < high) {
            int pi = partition(array, low, high);
            quickSort(array, low, pi - 1);
            quickSort(array, pi + 1, high);
        }
    }

    /**
     * Partition function for QuickSort, places pivot element at correct position.
     * @param array the list of places with distances.
     * @param low the starting index.
     * @param high the ending index.
     * @return the index of the pivot element after partitioning.
     */
    private int partition(ArrayList<PlaceWithDistance> array, int low, int high) {
        PlaceWithDistance pivot = array.get(high);
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (array.get(j).compareTo(pivot) <= 0) {
                i++;
                PlaceWithDistance temp = array.get(i);
                array.set(i, array.get(j));
                array.set(j, temp);
            }
        }
        PlaceWithDistance temp = array.get(i + 1);
        array.set(i + 1, array.get(high));
        array.set(high, temp);
        return i + 1;
    }
}

/**
 * This class provides constants and utility methods for managing service types using bitmasks.
 * Each service type is associated with a unique bit in an integer bitmask, allowing for
 * efficient encoding and querying of service types available at a location.
 */
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

    public static final int NUM_SERVICES = 10; // Total number of defined services

    /**
     * Encodes a specific service ID into a bitmask.
     * @param serviceId the ID of the service to encode.
     * @return an integer bitmask with the bit corresponding to the serviceId set to 1.
     */
    public static int encodeService(int serviceId) {
        return 1 << serviceId;
    }

    /**
     * Checks if a specific service is included in a given service bitmask.
     * @param serviceBitmask the bitmask representing services.
     * @param serviceId the ID of the service to check.
     * @return true if the service is included in the bitmask, false otherwise.
     */
    public static boolean hasService(int serviceBitmask, int serviceId) {
        return (serviceBitmask & encodeService(serviceId)) != 0;
    }
}

/**
 * Represents a location on a map with coordinates and services available.
 */
class Place {
    int x, y; // Coordinates
    int services; // Bitmask for services

    /**
     * Constructor for creating a Place object.
     *
     * @param x        The x-coordinate of the place.
     * @param y        The y-coordinate of the place.
     * @param services A bitmask representing the services available at the place.
     */
    public Place(int x, int y, int services) {
        this.x = x;
        this.y = y;
        this.services = services;
    }
}

/**
 * Represents a rectangular boundary in a 2D space.
 */
class Boundary {
    int x, y; // Coordinates of the top-left corner
    int width, height; // Width and height of the boundary

    /**
     * Constructor for creating a Boundary object.
     *
     * @param x      The x-coordinate of the top-left corner of the boundary.
     * @param y      The y-coordinate of the top-left corner of the boundary.
     * @param width  The width of the boundary.
     * @param height The height of the boundary.
     */
    public Boundary(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Checks if the given point (x, y) is contained within this boundary.
     *
     * @param x The x-coordinate of the point.
     * @param y The y-coordinate of the point.
     * @return true if the point is inside the boundary, false otherwise.
     */
    public boolean contains(int x, int y) {
        return x >= this.x && y >= this.y && x < this.x + this.width && y < this.y + this.height;
    }

    /**
     * Checks if this boundary intersects with another boundary.
     *
     * @param other The other boundary to check for intersection.
     * @return true if the boundaries intersect, false otherwise.
     */
    public boolean intersects(Boundary other) {
        // Check if any side of one rectangle is outside the other rectangle
        return !(other.x > x + width || other.x + other.width < x || other.y > y + height || other.y + other.height < y);
    }
}

/**
 * Represents a Place with its corresponding distance.
 */
class PlaceWithDistance implements Comparable<PlaceWithDistance> {
    Place place; // The Place object associated with this PlaceWithDistance
    double distance; // The distance from a reference point to the Place

    /**
     * Constructor for creating a PlaceWithDistance object.
     *
     * @param place    The Place object.
     * @param distance The distance from a reference point to the Place.
     */
    public PlaceWithDistance(Place place, double distance) {
        this.place = place;
        this.distance = distance;
    }

    /**
     * Compares this PlaceWithDistance object with another based on distance.
     *
     * @param other The other PlaceWithDistance object to compare with.
     * @return A negative integer, zero, or a positive integer as this PlaceWithDistance
     *         is less than, equal to, or greater than the specified PlaceWithDistance.
     */
    @Override
    public int compareTo(PlaceWithDistance other) {
        return Double.compare(this.distance, other.distance);
    }
}

/**
 * A simple implementation of a list using a dynamic array to store elements.
 * This custom ArrayList class mimics some functionalities of the Java Collection Framework's ArrayList,
 * but is simplified for educational purposes.
 *
 * @param <E> the type of elements in this list
 */
class ArrayList<E> {
    private Object[] elements; // The array buffer into which the elements of the ArrayList are stored.
    private int size = 0; // The current number of elements contained in the ArrayList.
    private static final int DEFAULT_CAPACITY = 10; // Default initial capacity of the ArrayList.

    /**
     * Constructs an empty list with an initial capacity of ten.
     */
    public ArrayList() {
        elements = new Object[DEFAULT_CAPACITY];
    }

    /**
     * Ensures that the array has the capacity to add new elements. If the current size equals
     * the capacity of the array, the array is resized to double its current size.
     */
    private void ensureCapacity() {
        if (size == elements.length) {
            Object[] newElements = new Object[elements.length * 2];
            System.arraycopy(elements, 0, newElements, 0, size); // Copy existing elements to the new array
            elements = newElements;
        }
    }

    /**
     * Appends the specified element to the end of this list.
     *
     * @param element element to be appended to this list
     */
    public void add(E element) {
        ensureCapacity(); // Ensure there's enough space for the new element
        elements[size++] = element; // Add the element and increment the size
    }

    /**
     * Removes the first occurrence of the specified element from this list, if it is present.
     * If the list does not contain the element, it remains unchanged.
     *
     * @param element element to be removed from this list, if present
     * @return true if this list contained the specified element
     */
    public boolean remove(E element) {
        int index = indexOf(element); // Find the index of the element
        if (index != -1) {
            removeAt(index); // Remove the element by index
            return true;
        }
        return false;
    }

    /**
     * Removes the element at the specified position in this list.
     * Shifts any additional elements to the left (subtracts one from their indices).
     *
     * @param index the index of the element to be removed
     */
    public void removeAt(int index) {
        int numMoved = size - index - 1; // Number of elements to move
        if (numMoved > 0) {
            System.arraycopy(elements, index + 1, elements, index, numMoved); // Shift elements left
        }
        elements[--size] = null; // Clear the slot and reduce size
    }

    /**
     * Returns the element at the specified position in this list.
     *
     * @param index index of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public E get(int index) {
        if (index >= size || index < 0) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        return (E) elements[index];
    }

    /**
     * Replaces the element at the specified position in this list with the specified element.
     *
     * @param index index of the element to replace
     * @param element element to be stored at the specified position
     * @return the element previously at the specified position
     * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index >= size())
     */
    public E set(int index, E element) {
        if (index >= size || index < 0) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        E oldValue = (E) elements[index]; // Cast needed because elements is an Object array
        elements[index] = element;
        return oldValue;
    }

    /**
     * Returns the index of the first occurrence of the specified element in this list,
     * or -1 if this list does not contain the element.
     *
     * @param element element to search for
     * @return the index of the first occurrence of the specified element in this list,
     *         or -1 if this list does not contain the element
     */
    public int indexOf(E element) {
        for (int i = 0; i < size; i++) {
            if (element.equals(elements[i])) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the number of elements in this list.
     *
     * @return the number of elements in this list
     */
    public int size() {
        return size;
    }
}
