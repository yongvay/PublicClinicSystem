package ADT;

import java.util.Iterator;
import java.util.Comparator;

/**
 * @author Ng Yong Vay Custom List Implementation using an array. Note:
 * Positional methods use 1-based indexing (1 to numberOfEntries) as per the
 * assignment's recommended good practices.
 */
public class List<T> implements ListInterface<T>, Iterable<T> {

    private T[] elements;
    private int numberOfEntries;
    private static final int DEFAULT_CAPACITY = 10;

    @SuppressWarnings("unchecked")
    public List() {
        // Safe cast: creating an array of Objects and casting to T[]
        elements = (T[]) new Object[DEFAULT_CAPACITY];
        numberOfEntries = 0;
    }

    // Constructor to support copying and initializing from an Iterable
    public List(Iterable<? extends T> src) {
        this();
        if (src != null) {
            for (T item : src) {
                add(item);
            }
        }
    }

    @Override
    public int getNumberOfEntries() {
        return numberOfEntries;
    }

    @Override
    public boolean isEmpty() {
        return numberOfEntries == 0;
    }

    @Override
    public boolean isFull() {
        return false; // Dynamic arrays are never "full" in this context
    }

    @Override
    public void clear() {
        for (int i = 0; i < numberOfEntries; i++) {
            elements[i] = null; // Help garbage collection
        }
        numberOfEntries = 0;
    }

    @Override
    public boolean add(T newEntry) {
        ensureCapacity();
        elements[numberOfEntries] = newEntry;
        numberOfEntries++;
        return true;
    }

    @Override
    public boolean add(int newPosition, T newEntry) {
        boolean isSuccessful = true;

        if ((newPosition >= 1) && (newPosition <= numberOfEntries + 1)) {
            ensureCapacity();
            makeRoom(newPosition);
            elements[newPosition - 1] = newEntry;
            numberOfEntries++;
        } else {
            isSuccessful = false;
            throw new IndexOutOfBoundsException("Given position of add's new entry is out of bounds.");
        }

        return isSuccessful;
    }

    @Override
    public T remove(int givenPosition) {
        T result = null;

        if ((givenPosition >= 1) && (givenPosition <= numberOfEntries)) {
            result = elements[givenPosition - 1];
            if (givenPosition < numberOfEntries) {
                removeGap(givenPosition);
            }
            numberOfEntries--;
            elements[numberOfEntries] = null; // Help garbage collection
        } else {
            throw new IndexOutOfBoundsException("Given position of remove is out of bounds.");
        }

        return result;
    }

//    @Override
//    public boolean remove(T anEntry) {
//        for (int index = 0; index < numberOfEntries; index++) {
//            if (anEntry == null ? elements[index] == null : anEntry.equals(elements[index])) {
//                remove(index + 1); // Pass 1-based index to the other remove method
//                return true;
//            }
//        }
//        return false;
//    }
    @Override
    public boolean remove(T anEntry) {
        // Reuse getPosition to find the 1-based index
        int position = getPosition(anEntry);
        if (position != -1) {
            remove(position); // Call the index-based remove
            return true;
        }
        return false;
    }

    @Override
    public boolean replace(int givenPosition, T newEntry) {
        boolean isSuccessful = true;

        if ((givenPosition >= 1) && (givenPosition <= numberOfEntries)) {
            elements[givenPosition - 1] = newEntry;
        } else {
            isSuccessful = false;
            throw new IndexOutOfBoundsException("Given position of replace is out of bounds.");
        }

        return isSuccessful;
    }

    @Override
    public T getEntry(int givenPosition) {
        T result = null;

        if ((givenPosition >= 1) && (givenPosition <= numberOfEntries)) {
            result = elements[givenPosition - 1];
        } else {
            throw new IndexOutOfBoundsException("Given position of getEntry is out of bounds.");
        }

        return result;
    }

//    @Override
//    public boolean contains(T anEntry) {
//        boolean found = false;
//        for (int index = 0; !found && (index < numberOfEntries); index++) {
//            if (anEntry == null ? elements[index] == null : anEntry.equals(elements[index])) {
//                found = true;
//            }
//        }
//        return found;
//    }
    @Override
    public boolean contains(T anEntry) {
        // Reuse getPosition to avoid duplicate loop logic
        return getPosition(anEntry) != -1;
    }

    // Search Method
    @Override
    public int getPosition(T anEntry) {
        for (int index = 0; index < numberOfEntries; index++) {
            if (anEntry == null ? elements[index] == null : anEntry.equals(elements[index])) {
                return index + 1; // Return 1-based position
            }
        }
        return -1; // Return -1 to indicate the entry was not found
    }

    @Override
    public ListInterface<T> sort(Comparator<T> comparator) {
        // Return a new list to maintain immutability of the original list
        List<T> sortedList = new List<>(this);
        if (sortedList.numberOfEntries > 1) {
            mergeSort(sortedList.elements, 0, sortedList.numberOfEntries - 1, comparator);
        }
        return sortedList;
    }

    // --- Iterator Implementation ---
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int currentIndex = 0;

            @Override
            public boolean hasNext() {
                return currentIndex < numberOfEntries;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new IllegalStateException("No more elements");
                }
                return elements[currentIndex++];
            }
        };
    }

    // --- Private Helper Methods ---
    @SuppressWarnings("unchecked")
    private void ensureCapacity() {
        if (numberOfEntries >= elements.length) {
            int newCapacity = elements.length * 2;
            T[] oldElements = elements;
            elements = (T[]) new Object[newCapacity];
            for (int i = 0; i < oldElements.length; i++) {
                elements[i] = oldElements[i];
            }
        }
    }

    // Shifts elements right to make room for a new item at 1-based index
    private void makeRoom(int newPosition) {
        int newIndex = newPosition - 1;
        int lastIndex = numberOfEntries - 1;
        for (int index = lastIndex; index >= newIndex; index--) {
            elements[index + 1] = elements[index];
        }
    }

    // Shifts elements left to fill the gap left by a removed item at 1-based index
    private void removeGap(int givenPosition) {
        int removedIndex = givenPosition - 1;
        int lastIndex = numberOfEntries - 1;
        for (int index = removedIndex; index < lastIndex; index++) {
            elements[index] = elements[index + 1];
        }
    }

    // --- Merge Sort Logic ---
    private void mergeSort(T[] arr, int left, int right, Comparator<T> comparator) {
        if (left < right) {
            int mid = left + (right - left) / 2;
            mergeSort(arr, left, mid, comparator);
            mergeSort(arr, mid + 1, right, comparator);
            merge(arr, left, mid, right, comparator);
        }
    }

    @SuppressWarnings("unchecked")
    private void merge(T[] arr, int left, int mid, int right, Comparator<T> comparator) {
        int n1 = mid - left + 1;
        int n2 = right - mid;

        Object[] L = new Object[n1];
        Object[] R = new Object[n2];

        for (int i = 0; i < n1; ++i) {
            L[i] = arr[left + i];
        }
        for (int j = 0; j < n2; ++j) {
            R[j] = arr[mid + 1 + j];
        }

        int i = 0, j = 0, k = left;
        while (i < n1 && j < n2) {
            if (comparator.compare((T) L[i], (T) R[j]) <= 0) {
                arr[k++] = (T) L[i++];
            } else {
                arr[k++] = (T) R[j++];
            }
        }
        while (i < n1) {
            arr[k++] = (T) L[i++];
        }
        while (j < n2) {
            arr[k++] = (T) R[j++];
        }
    }
}