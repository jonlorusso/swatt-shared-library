package com.swatt.test;

import java.util.ArrayList;

import com.swatt.util.general.NameValue;

public abstract class AbstractTestValidator implements GenericTestValidator {
	protected String topic;
	protected boolean debug = false;
	protected ArrayList<NameValue<String>> inputs = new ArrayList<NameValue<String>>();
	protected ArrayList<NameValue<String>> outputs = new ArrayList<NameValue<String>>();
	protected ArrayList<NameValue<String>> specials = new ArrayList<NameValue<String>>();
	

	@Override
	public void setTopic(String topic) {
		this.topic = topic;
	}

	@Override
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	@Override
	public void addInput(String name, String value) {
		NameValue<String> nameValue = new NameValue<String>(name, value);
		inputs.add(nameValue);
	}

	@Override
	public void addOutput(String name, String value) {
		NameValue<String> nameValue = new NameValue<String>(name, value);
		outputs.add(nameValue);		
	}

	@Override
	public void addSpecial(String name, String value) {
		NameValue<String> nameValue = new NameValue<String>(name, value);
		specials.add(nameValue);		
	}


	public boolean isDebug() { return debug; }
	public String getTopic() { return topic; }

}
