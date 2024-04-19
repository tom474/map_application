package pnm.quadtree1;

public class SimpleList {
    private int[][] elements;
    private int count = 0;

    public SimpleList(int initialCapacity) {
        elements = new int[initialCapacity][];
    }

    public void add(int[] element) {
        ensureCapacity();
        elements[count++] = element;
    }

    private void ensureCapacity() {
        if (count == elements.length) {
            int newCapacity = elements.length * 2;
            if (newCapacity < 1) {
                newCapacity = 1;
            }
            int[][] newElements = new int[newCapacity][];
            System.arraycopy(elements, 0, newElements, 0, count);
            elements = newElements;
        }
    }

    public int size() {
        return count;
    }

    public int[] get(int index) {
        if (index < 0 || index >= count) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + count);
        }
        return elements[index];
    }

    public void set(int index, int[] element) {
        if (index < 0 || index >= count) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + count);
        }
        elements[index] = element;
    }

    public int[][] toArray() {
        int[][] result = new int[count][];
        System.arraycopy(elements, 0, result, 0, count);
        return result;
    }
}
