package pnm.quadtree1;

class SimpleHashSet {
    private PlaceList[] buckets;
    private int capacity;
    private int size = 0;  // Field to track the total number of elements in the HashSet

    public SimpleHashSet(int capacity) {
        this.capacity = capacity;
        this.buckets = new PlaceList[capacity];
        for (int i = 0; i < capacity; i++) {
            buckets[i] = new PlaceList();
        }
    }

    private int getBucketIndex(Place place) {
        return Math.abs(place.hashCode()) % capacity;
    }

    public void add(Place place) {
        int index = getBucketIndex(place);
        if (buckets[index].add(place)) {
            size++;
        }
    }

    public boolean contains(Place place) {
        int index = getBucketIndex(place);
        return buckets[index].contains(place);
    }

    public boolean remove(Place place) {
        int index = getBucketIndex(place);
        if (buckets[index].remove(place)) {
            size--;
            return true;
        }
        return false;
    }

    public void clear() {
        for (PlaceList bucket : buckets) {
            bucket.clear();
        }
        size = 0;  // Reset the size to zero
    }

    public int size() {
        return size;  // Method to get the current size of the HashSet
    }

    public PlaceAccess getPlaceAccess() {
        return new PlaceAccess();
    }

    public class PlaceAccess {
        private int currentBucket = 0;
        private PlaceList.Node currentNode = null;

        public Place next() {
            while (currentBucket < capacity) {
                if (currentNode == null) {
                    currentNode = buckets[currentBucket].head;
                } else {
                    currentNode = currentNode.next;
                }

                if (currentNode == null) {
                    currentBucket++;
                } else {
                    return currentNode.place;
                }
            }
            return null;  // Return null when no more elements are available
        }
    }
}

class PlaceList {
    Node head;

    static class Node {
        Place place;
        Node next;

        Node(Place place) {
            this.place = place;
            this.next = null;
        }
    }

    public boolean add(Place place) {
        if (!contains(place)) {
            Node newNode = new Node(place);
            newNode.next = head;
            head = newNode;
            return true;
        }
        return false;
    }

    public boolean contains(Place place) {
        Node current = head;
        while (current != null) {
            if (current.place.equals(place)) {
                return true;
            }
            current = current.next;
        }
        return false;
    }

    public boolean remove(Place place) {
        Node current = head;
        Node prev = null;
        while (current != null) {
            if (current.place.equals(place)) {
                if (prev == null) {
                    head = current.next;
                } else {
                    prev.next = current.next;
                }
                return true;
            }
            prev = current;
            current = current.next;
        }
        return false;
    }

    public void clear() {
        head = null;
    }
}
