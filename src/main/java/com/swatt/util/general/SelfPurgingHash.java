package com.swatt.util.general;

import java.util.ArrayList;
import java.util.HashMap;

public class SelfPurgingHash {
	
	private HashMap<String, WrappedObject> wrappedObjects = new HashMap<String, WrappedObject>();
	private int purgeThresholdCount = 1000000;
	private long purgeThresholdTime;
	
	private static class WrappedObject {
		private long lastUsedTime;
		private Object value;
		
		WrappedObject(Object value) {
			this.value = value;
			this.lastUsedTime = System.currentTimeMillis();
		}
	}
	
	public SelfPurgingHash(final long purgePollTime,  long purgeThresholdTime) {
		
		this.purgeThresholdTime = purgeThresholdTime;
		
		ConcurrencyUtilities.createThread(() -> {
			for(;;) {
				ConcurrencyUtilities.sleep(purgePollTime);
				
				cleanup();
			}
		}, "Self Purging Hash");
	}
	
	private void cleanup() {
		cleanup(purgeThresholdTime);
	}
	
	private void cleanup(long age) {
		synchronized(wrappedObjects) {
			ArrayList<String> toKill = new ArrayList<String>();
			
			long old = System.currentTimeMillis() - purgeThresholdTime;
			
			for (String key : wrappedObjects.keySet()) {
				WrappedObject wrappedObject = wrappedObjects.get(key);
				
				if (wrappedObject.lastUsedTime < old) 
					toKill.add(key);
			}
			
			for(String key: toKill) {
				wrappedObjects.remove(key);
			}
			
			if (wrappedObjects.size() > purgeThresholdCount) {
				cleanup( age/2);
			}
		}
	}
	
	
	public Object get(String key) {
		synchronized(wrappedObjects) {
			WrappedObject wrappedObject = wrappedObjects.get(key);
			
			if (wrappedObject != null) {
				wrappedObject.lastUsedTime = System.currentTimeMillis();
				
				return wrappedObject.value;
			} else
				return null;
		}
	}
	
	public void remove(String key) {
		synchronized(wrappedObjects) {
			wrappedObjects.remove(key);
		}
	}
	
	public void put(String key, Object value) {
		
		synchronized(wrappedObjects) {
			if (wrappedObjects.size() > purgeThresholdCount) {
				cleanup();
			}
			WrappedObject wrappedObject = new WrappedObject(value);
			wrappedObjects.put(key,  wrappedObject);
		}
	}
	
}
