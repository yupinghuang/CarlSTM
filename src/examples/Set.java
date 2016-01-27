package examples;

/**
 * A set is a collection of objects.
 * 
 * @param <T> type of the objects in the set.
 */
public interface Set<T> {
	/**
	 * Add an object to the set.
	 * 
	 * @param x object to be added
	 * @return true if the object was not already in the set and false otherwise
	 */
	public boolean add(T x);

	/**
	 * Check if an object is in the set.
	 * 
	 * @param x object to be searched for
	 * @return true if the object is in the set and false otherwise
	 */
	public boolean contains(T x);
}
