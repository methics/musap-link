//
//  (c) Copyright 2003-2014 Methics Oy. All rights reserved. 
//
package fi.methics.webapp.musaplink.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * The class wraps an java.util.Map so that the wrapped map includes
 * the time dimension; the objects stored in the new map have a
 * maximum life time.  If the object is not removed from the map
 * before its life time exceeds, the wrapper destroys it.
 *
 * <P>
 * The implementation is not thread-safe. One should wrap this class
 * with Collection.synchronizedMap:
 * <P>
 * <code>Map m = Collections.synchronizedMap(new ExpirableMap(...));</code>
 * <P>
 * The developer should note that the remove operation is limited to
 * the speed of TreeMap.remove().
 *
 * @author Teemu Koponen (teemu.koponen@methics.fi)
 * @author Matti Aarnio (matti.aarnio@methics.fi) rewrote for Java5 type generators
 */

@SuppressWarnings("unchecked")
public class ExpirableMap<K,V> implements Map<K,V> {

    /** The timestamps */
    protected TreeMap<Long, K[]> timestamps;

    /** The objects */
    protected Map<K,V> objects;

    /** The key to timestamps mappings */
    protected HashMap<K,Long> keyTimestamps;

    /** The maximum lifetime of objects (milliseconds) */
    protected long defaultLifetime;

    /**
     * Constructs a expiring wrapper for a map. 
     * Uses a standard HashMap as the underlying map implementation.
     * @param lifetime the maximum life time the objects should have
     * in the map. (milliseconds)
     */
    public ExpirableMap(final long lifetime) {
        this(new HashMap<K,V>(), lifetime);
    }

    /**
     * Constructs a expiring wrapper for a map. 
     * Uses a standard HashMap as the underlying map implementation.
     * @param lifetime the maximum life time the objects should have
     * in the map. (milliseconds)
     */
    public ExpirableMap(final Interval lifetime) {
        this(new HashMap<K,V>(), lifetime);
    }

    /**
     * Constructs a expiring wrapper for a map. 
     *
     * @param m the map to wrap.
     * @param lifetime the maximum life time the objects should have
     * in the map. (milliseconds)
     */
    public ExpirableMap(Map<K,V> m, long lifetime) {
        this.defaultLifetime = lifetime;
        this.objects         = m;
        this.keyTimestamps = new HashMap<K,Long>();
        this.timestamps    = new TreeMap<Long,K[]>();

        Iterator<Entry<K,V>> i = m.entrySet().iterator();
        List<Entry<K,V>>     l = new LinkedList<Entry<K,V>>();
        while (i.hasNext()) {
            l.add(i.next());
        }       

        i = l.iterator();
        while (i.hasNext()) {
            Entry<K,V> e = i.next();
            put(e.getKey(), e.getValue());
        }
    }

    /**
     * Constructs a expiring wrapper for a map. 
     *
     * @param m the map to wrap.
     * @param lifetime the maximum life time the objects should have
     * in the map. (milliseconds)
     */
    public ExpirableMap(Map<K,V> m, Interval lifetime) {
        this(m, lifetime.toIntMillis());
    }

    @Override
    public void clear() {
        this.timestamps.clear();
        this.objects.clear();
        this.keyTimestamps.clear();
    }

    @Override
    public boolean containsKey(Object key) {
        this.expire();
        return this.objects.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        this.expire();
        return this.objects.containsValue(value);
    }

    @Override
    public Set<Entry<K,V>> entrySet() {
        this.expire();
        return this.objects.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        ExpirableMap<?,?> t;
        if (o instanceof ExpirableMap) {
            t = (ExpirableMap<?,?>)o;
        } else {
            return false;
        }
        if (t == this) {
            return true;
        }

        if (t.size() != this.size()) {
            return false;
        }

        Iterator<Entry<K,V>> i = this.entrySet().iterator();
        while (i.hasNext()) {
            Entry<K,V> e = i.next();
            K key   = e.getKey();
            V value = e.getValue();
            if (value == null) {
                if (!(t.get(key) == null && t.containsKey(key)))
                    return false;
            } else {
                if (!value.equals(t.get(key)))
                    return false;
            }
        }

        return true;
    }

    // The interface has the comment
    @Override
    public V get(Object key) {
        this.expire();
        return this.objects.get(key);
    }

    @Override
    public int hashCode() {
        this.expire();
        return this.objects.hashCode() + this.timestamps.hashCode();
    }

    @Override
    public boolean isEmpty() {
        this.expire();
        return this.objects.isEmpty();
    }

    @Override
    public Set<K> keySet() {
        this.expire();
        return this.objects.keySet();
    }

    public V put(K key, V value, long lifetime) {
        V ret = this.remove(key);
        Long lifeEnds = Long.valueOf(System.currentTimeMillis() + lifetime);
        this.objects.put(key, value);

        K [] t = this.timestamps.remove(lifeEnds);
        if (t == null) {
            t = (K[])(new Object [1]);
            t[0] = key;
        } else {
            K [] t2 = (K[])(new Object[t.length + 1]);
            for (int i = 0; i < t.length; i++) {
                t2[i] = t[i];
            }

            t2[t2.length - 1] = key;
            t = t2;
        }

        this.timestamps.put(lifeEnds, t);
        this.keyTimestamps.put(key, lifeEnds);

        return ret;
    }

    @Override
    public V put(Object key, Object value) {
        return this.put((K)key, (V)value, this.defaultLifetime);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m)
    {
        this.expire();

        Iterator<?> i = m.entrySet().iterator();
        while (i.hasNext()) {
            Entry<?,?> e = (Entry<?,?>) i.next();
            this.put(e.getKey(), e.getValue());
        }
    }

    @Override
    public V remove(Object key) {
        return this.remove((K)key, true);
    }
    
    /**
     * Set default cache time. This overrides the value given in constructor.
     * @param cacheTime Cache time in milliseconds
     */
    public void setDefaultTime(final long cacheTime) {
        this.defaultLifetime = cacheTime;
    }

    @Override
    public int size() {
        this.expire();
        return this.objects.size();
    }

    @Override
    public Collection<V> values() {
        this.expire();
        return this.objects.values();
    }

    /**
     * Removes expired objects.
     */
    protected void expire() {
        long now = System.currentTimeMillis();
        while (this.timestamps.size() > 0) {
            Long l = this.timestamps.firstKey();
            K [] v = this.timestamps.get(l);
            if (l.longValue() < now) {
                for (int i = 0; i < v.length; i++) {
                    remove(v[i], false);
                    
                }
                this.timestamps.remove(l);
                continue;
            } 
            
            // Nothing to remove
            return;
        }
    }
    
    protected V remove(K key, boolean expire) {
        if (expire) {
            this.expire();
        }

        V ret = this.objects.remove(key);
        Long lifeEnds = this.keyTimestamps.remove(key);
        if (lifeEnds != null) {
            K [] t = this.timestamps.get(lifeEnds);
            if (t.length > 1) {
                List<K> l = new LinkedList<K>();
                for (int i = 0; i < t.length; i++) {
                    if (!t[i].equals(key)) {
                        l.add(t[i]);
                    }
                }
                this.timestamps.put(lifeEnds, (K[])l.toArray(new Object[l.size()]));
                
            } else {
                this.timestamps.remove(lifeEnds);
            }
        } 

        return ret;
    }
}
