package fi.methics.webapp.musaplink.util;

@SuppressWarnings("unchecked")
public class ExpirableSet<K> {

    private ExpirableMap<K, Object> internalMap;

    /**
     * Constructs a expiring wrapper for a set. 
     * Uses a standard ExpirableMap as the underlying map implementation.
     * @param lifetime the maximum life time the objects should have
     * in the map. (milliseconds)
     */
    public ExpirableSet(final long lifetime) {
        this.internalMap = new ExpirableMap<K, Object>(lifetime);
    }

    /**
     * Constructs a expiring wrapper for a set. 
     * Uses a standard ExpirableMap as the underlying map implementation.
     * @param lifetime the maximum life time the objects should have
     * in the map. (milliseconds)
     */
    public ExpirableSet(final Interval lifetime) {
        this.internalMap = new ExpirableMap<K, Object>(lifetime);
    }

    /**
     * Check if this set contains given key
     * @param key Key to check
     * @return true if contains
     */
    public boolean containsKey(K key) {
        return this.internalMap.containsKey(key);
    }

    /**
     * Add an entry to this set
     * @param key Key to add
     */
    public void add(K key) {
        this.internalMap.put(key, null);
    }
    
}
