package com.swatt.util.general;

public class NameValue<T> {
	private String name;
	private T value; 
	
	public NameValue(String name, T value) {
		this.name = name;
		this.value = value;
	}
	
	public String getName() { return name; }
	
	public T get() { return this.value; }
	public void setValue(T value) { this.value = value; }

}
