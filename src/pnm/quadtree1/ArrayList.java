package pnm.quadtree1;

/**
 * A simple implementation of a list using a dynamic array to store elements.
 * This custom ArrayList class mimics some of the functionalities of the Java Collection Framework's ArrayList,
 * but is simplified for educational purposes.
 *
 * @param <E> the type of elements in this list
 */
public class ArrayList<E> implements List<E> {
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
    @Override
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
    @Override
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
     * Shifts any subsequent elements to the left (subtracts one from their indices).
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
    @Override
    public E get(int index) {
        if (index >= size || index < 0) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        return (E) elements[index];
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
     * Removes all the elements from this list.
     * The list will be empty after this call returns.
     */
    @Override
    public void clear() {
        for (int i = 0; i < size; i++) {
            elements[i] = null; // Clear each element to help with garbage collection
        }
        size = 0; // Reset size to zero
    }

    /**
     * Returns the number of elements in this list.
     *
     * @return the number of elements in this list
     */
    @Override
    public int size() {
        return size;
    }
}
