package ADT;

import java.util.Comparator;

/**
 * @author Ng Yong Vay
 */
public interface ListInterface<T> extends Iterable<T> { 
  
  // Return the total number of elements currently stored in the list
  int getNumberOfEntries();                               

  // Get element at given index (1-based)
  T getEntry(int givenPosition);                         

  // Replaces element currently at the specified index with the new element
  boolean replace(int givenPosition, T newEntry);        

  // Append to the end of the list
  boolean add(T newEntry);                               

  // Insert element at given index (1-based)
  boolean add(int newPosition, T newEntry);              
    
  // Remove element at the specified index (1-based)
  T remove(int givenPosition);                           

  // Remove element by matching its value 
  boolean remove(T anEntry);                             
  
  // Check is empty or not
  boolean isEmpty();                                     

  // Check if list is full
  boolean isFull();                                      

  // Completely empty the list
  void clear();                                          
  
  // Checks if the specific entry exists in the list 
  boolean contains(T anEntry);                           
  
  // Sorting method
  ListInterface<T> sort(Comparator<T> comparator);
  

  // Search method
  // Gets the 1-based position of an entry in the list. Returns -1 if not found.
  int getPosition(T anEntry);

  ListInterface<T> findAll(SearchCriteria<T> criteria);
  T findFirst(SearchCriteria<T> criteria);
}