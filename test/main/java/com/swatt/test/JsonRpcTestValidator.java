package com.swatt.test;

import java.net.URL;

import com.swatt.util.general.NotYetImplementedException;

public class JsonRpcTestValidator extends AbstractTestValidator {
	private String baseUrl;
	private String urlPath = "";
	
	
	@Override
	public void addSpecial(String name, String value) {
		if (name.equalsIgnoreCase("url")) 
			baseUrl = value;
	}

	@Override
	public void addInput(String name, String value) {
		urlPath += "/" + value;
	}


	@Override
	public void validate() {
		try {
			URL url = new URL(baseUrl + urlPath);
			
			// FIXME: Still needs to be implemented
			
			throw new NotYetImplementedException("HI CHRISTIAN... this is where you need to fill in the work for the generic RPC Validator");
		
		} catch (Throwable t) {
			throw new RuntimeException("Error during Validate", t);
		}
	}
	

}
