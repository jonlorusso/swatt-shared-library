package com.swatt.util.general;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

@SuppressWarnings("all")							// This is too gnarly to try and figure out all the generic castings.
public class IntHashMap<T>  {
	private static final int DEFAULT_INITIAL_CAPACITY = 16;
	private static final int MAXIMUM_CAPACITY = 1 << 30;
	private static final float DEFAULT_LOAD_FACTOR = 0.75f;
	private static final Object NULL_KEY = new Object();

	
	private Entry[] table;
	private int size;
	private int threshold;
	private final float loadFactor;
	private volatile int modCount;

	public IntHashMap(int initialCapacity, float loadFactor) {
		if (initialCapacity < 0)
			throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
		
		if (initialCapacity > MAXIMUM_CAPACITY)
			initialCapacity = MAXIMUM_CAPACITY;
		
		if (loadFactor <= 0 || Float.isNaN(loadFactor))
			throw new IllegalArgumentException("Illegal load factor: " + loadFactor);

		// Find a power of 2 >= initialCapacity
		int capacity = 1;
		while (capacity < initialCapacity) 
			capacity <<= 1;
	
		this.loadFactor = loadFactor;
		threshold = (int)(capacity * loadFactor);
		table = new Entry[capacity];
	}
 
	
	public IntHashMap(int initialCapacity) {
		this(initialCapacity, DEFAULT_LOAD_FACTOR);
	}

	
	public IntHashMap() {
		this.loadFactor = DEFAULT_LOAD_FACTOR;
		threshold = (int)(DEFAULT_INITIAL_CAPACITY * DEFAULT_LOAD_FACTOR);
		table = new Entry[DEFAULT_INITIAL_CAPACITY];
	}


	// internal utilities

	
	static Object maskNull(Object key) {
		return (key == null ? NULL_KEY : key);
	}

	
	static Object unmaskNull(Object key) {
		return (key == NULL_KEY ? null : key);
	}

	
	static int hash(long x) {
		int h = (int) ((x & 0xFFFFFFFF) + (x >> 32));

		h += ~(h << 9);
		h ^= (h >>> 14);
		h += (h << 4);
		h ^= (h >>> 10);
		return h;
	}

	
	static int indexFor(int h, int length) {
		return h & (length-1);
	}
 
	
	public int size() {
		return size;
	}
 
	
	public boolean isEmpty() {
		return size == 0;
	}

	
	public Object get(long key) {
		int hash = hash(key);
		
		int i = indexFor(hash, table.length);
		Entry e = table[i]; 
		while (true) {
			if (e == null)
				return e;
			if (e.hash == hash && (key == e.key)) 
				return e.value;
			e = e.next;
		}
	}

	
	public boolean containsKey(long key) {
		int hash = hash(key);
		int i = indexFor(hash, table.length);
		Entry e = table[i]; 
		while (e != null) {
			if (e.hash == hash && (key == e.key)) 
				return true;
			e = e.next;
		}
		return false;
	}

	
	Entry getEntry(long key) {
		int hash = hash(key);
		int i = indexFor(hash, table.length);
		Entry e = table[i]; 
		while (e != null && !(e.hash == hash && (key == e.key)))
			e = e.next;
		return e;
	}
 
	
	public Object put(long key, Object value) {
		int hash = hash(key);
		int i = indexFor(hash, table.length);

		for (Entry e = table[i]; e != null; e = e.next) {
			if (e.hash == hash && (key == e.key)) {
				Object oldValue = e.value;
				e.value = value;
				return oldValue;
			}
		}

		modCount++;
		addEntry(hash, key, value, i);
		return null;
	}

	
	void resize(int newCapacity) {
		Entry[] oldTable = table;
		int oldCapacity = oldTable.length;
		if (oldCapacity == MAXIMUM_CAPACITY) {
			threshold = Integer.MAX_VALUE;
			return;
		}

		Entry[] newTable = new Entry[newCapacity];
		transfer(newTable);
		table = newTable;
		threshold = (int)(newCapacity * loadFactor);
	}

	
	void transfer(Entry[] newTable) {
		Entry[] src = table;
		int newCapacity = newTable.length;
		for (int j = 0; j < src.length; j++) {
			Entry e = src[j];
			if (e != null) {
				src[j] = null;
				do {
					Entry next = e.next;
					int i = indexFor(e.hash, newCapacity); 
					e.next = newTable[i];
					newTable[i] = e;
					e = next;
				} while (e != null);
			}
		}
	}

	
	public Object remove(long key) {
		Entry e = removeEntryForKey(key);
		return (e == null ? e : e.value);
	}

	
	Entry removeEntryForKey(long key) {
		int hash = hash(key);
		int i = indexFor(hash, table.length);
		Entry prev = table[i];
		Entry e = prev;

		while (e != null) {
			Entry next = e.next;
			if (e.hash == hash && (key == e.key)) {
				modCount++;
				size--;
				if (prev == e) 
					table[i] = next;
				else
					prev.next = next;
				return e;
			}
			prev = e;
			e = next;
		}
 
		return e;
	}

	
	public void clear() {
		modCount++;
		Entry tab[] = table;
		for (int i = 0; i < tab.length; i++) 
			tab[i] = null;
		size = 0;
	}

	
	public boolean containsValue(Object value) {
	if (value == null) 
			return containsNullValue();

	Entry tab[] = table;
		for (int i = 0; i < tab.length ; i++)
			for (Entry e = tab[i] ; e != null ; e = e.next)
				if (value.equals(e.value))
					return true;
	return false;
	}

	
	private boolean containsNullValue() {
		Entry tab[] = table;
		for (int i = 0; i < tab.length ; i++)
			for (Entry e = tab[i] ; e != null ; e = e.next)
				if (e.value == null)
					return true;
		return false;
	}

	static class Entry {
		final long key;
		Object value;
		final int hash;
		Entry next;

		
		Entry(int hash, long key, Object value, Entry next) { 
			this.hash = hash; 
			this.value = value; 
			this.next = next;
			this.key = key;
		}

		public Object getValue() {
			return value;
		}
	
		public Object setValue(Object newValue) {
			Object oldValue = value;
			value = newValue;
			return oldValue;
		}
	
		public boolean equals(Object obj) {
			if (!(obj instanceof Entry))
				return false;
			
			Entry other = (Entry) obj;
			
			if (key == other.key) 
				return equals(value, other.value);

			return false;
		}
	
		public String toString() {
			return key + "=" + getValue();
		}
		
		public static final boolean equals(Object obj1, Object obj2) {
			if (obj1 == null) {
				return (obj2 == null);
			} else {
				if (obj2 != null)
					return obj1.equals(obj2);
				else 
					return false;
			}
		}

	}
	
	

	
	void addEntry(int hash, long key, Object value, int bucketIndex) {
		table[bucketIndex] = new Entry(hash, key, value, table[bucketIndex]);
		if (size++ >= threshold) 
			resize(2 * table.length);
	}

	
	void createEntry(int hash, long key, Object value, int bucketIndex) {
		table[bucketIndex] = new Entry(hash, key, value, table[bucketIndex]);
		size++;
	}

	private abstract class HashIterator implements Iterator {
		Entry next;				 // next entry to return
		int expectedModCount;		// For fast-fail 
		int index;				 // current slot 
		Entry current;			 // current entry

		HashIterator() {
			expectedModCount = modCount;
			Entry[] t = table;
			int i = t.length;
			Entry n = null;
			if (size != 0) { // advance to first entry
				while (i > 0 && (n = t[--i]) == null)
					;
			}
			next = n;
			index = i;
		}

		public boolean hasNext() {
			return next != null;
		}

		Entry nextEntry() { 
			if (modCount != expectedModCount)
				throw new ConcurrentModificationException();
			
			Entry e = next;
			
			if (e == null) 
				throw new NoSuchElementException();
				
			Entry n = e.next;
			Entry[] t = table;
			int i = index;
			while (n == null && i > 0)
				n = t[--i];
			index = i;
			next = n;
			return current = e;
		}

		public void remove() {
			if (current == null)
				throw new IllegalStateException();
			
			if (modCount != expectedModCount)
				throw new ConcurrentModificationException();
			
			long key = current.key;
			current = null;
			IntHashMap.this.removeEntryForKey(key);
			expectedModCount = modCount;
		}

	}

	private class ValueIterator extends HashIterator {
		public Object next() {
			return nextEntry().value;
		}
	}

	private class KeyIterator implements IntIterator {
		long nextKey;
		
		HashIterator hashIterator = new HashIterator() {
			public Object next() {
				return nextEntry();
			}
		};
		
		public boolean hasNext() {
			return hashIterator.hasNext();
		}

		public int nextInt() {
			return (int) hashIterator.nextEntry().key;
		}

		public long nextLong() {
			return hashIterator.nextEntry().key;
		}
		
		public void remove() {
			hashIterator.remove();
		}
	}

	private class EntryIterator extends HashIterator {
		public Object next() {
			return nextEntry().value;
		}
	}
	
	Iterator newValueIterator() {
		return new ValueIterator();
	}
	
	public IntIterator keyIterator() {
		return new KeyIterator();
	}
	
	public Iterator iterator() { return new EntryIterator(); 	}
	
	public Collection values() {
		return new Values();
	}

	private class Values extends AbstractCollection {
		public Iterator iterator() {
			return newValueIterator();
		}
		public int size() {
			return size;
		}
		public boolean contains(Object o) {
			return containsValue(o);
		}
		public void clear() {
			IntHashMap.this.clear();
		}
	}


	

}