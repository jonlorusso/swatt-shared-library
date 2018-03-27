package com.swatt.test;

public interface GenericTestValidator {
	public void setTopic(String testName);
	public void setDebug(boolean debug);
	
	public void addInput(String name, String value);
	public void addOutput(String name, String value);
	public void addSpecial(String name, String value);
	
	public void validate();
}
