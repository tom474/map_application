package pnm.quadtree1;

public class MaxHeap {
    private Place[] heap;
    private int size;
    private int maxSize;
    private int targetX;
    private int targetY;

    public MaxHeap(int k, int targetX, int targetY) {
        this.maxSize = k;
        this.heap = new Place[k];
        this.size = 0;
        this.targetX = targetX;
        this.targetY = targetY;
    }

    private int parent(int pos) { return (pos - 1) / 2; }
    private int leftChild(int pos) { return (2 * pos) + 1; }
    private int rightChild(int pos) { return (2 * pos) + 2; }

    private void swap(int fpos, int spos) {
        Place tmp = heap[fpos];
        heap[fpos] = heap[spos];
        heap[spos] = tmp;
    }

    private boolean isLeaf(int pos) { return pos >= (size / 2) && pos < size; }

    long distSquared(int x, int y) {
        long dx = x - targetX;
        long dy = y - targetY;
        return dx * dx + dy * dy;
    }

    private void maxHeapify(int pos) {
        if (!isLeaf(pos)) {
            int left = leftChild(pos);
            int right = rightChild(pos);
            int largest = pos;
            if (left < size && distSquared(heap[left].placeCoor.x, heap[left].placeCoor.y) > distSquared(heap[pos].placeCoor.x, heap[pos].placeCoor.y)) {
                largest = left;
            }
            if (right < size && distSquared(heap[right].placeCoor.x, heap[right].placeCoor.y) > distSquared(heap[largest].placeCoor.x, heap[largest].placeCoor.y)) {
                largest = right;
            }
            if (largest != pos) {
                swap(pos, largest);
                maxHeapify(largest);
            }
        }
    }

    public void offer(Place element) {
        if (size < maxSize) {
            heap[size] = element;
            int current = size;
            while (current > 0 && distSquared(heap[current].placeCoor.x, heap[current].placeCoor.y) > distSquared(heap[parent(current)].placeCoor.x, heap[parent(current)].placeCoor.y)) {
                swap(current, parent(current));
                current = parent(current);
            }
            size++;
        } else {
            if (distSquared(element.placeCoor.x, element.placeCoor.y) < distSquared(heap[0].placeCoor.x, heap[0].placeCoor.y)) {
                heap[0] = element;
                maxHeapify(0);
            }
        }
    }

    public Place poll() {
        Place popped = heap[0];
        heap[0] = heap[--size];
        maxHeapify(0);
        return popped;
    }

    public Place peek() {
        return heap[0];
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }
}
