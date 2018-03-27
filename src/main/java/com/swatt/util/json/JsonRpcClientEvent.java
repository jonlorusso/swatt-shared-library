package com.swatt.util.json;

import java.lang.reflect.Type;
import java.util.Map;

public class JsonRpcClientEvent {
	private JsonRpcClient jsonRpcClient;
	
	private String methodName;
	private Object argument;
	private Type returnType;
	private Map<String, String> extraHeaders;
	
	
	JsonRpcClientEvent(JsonRpcClient jsonRpcClient, String methodName, Object argument, Type returnType, Map<String, String> extraHeaders) {
		this.jsonRpcClient = jsonRpcClient;
		this.methodName = methodName;
		this.argument = argument;
		this.returnType = returnType;
		this.extraHeaders = extraHeaders;
	}
	
	JsonRpcClientEvent(JsonRpcClient jsonRpcClient) {
		this.jsonRpcClient = jsonRpcClient;
	}

	
	public final JsonRpcClient getJsonRpcClient() { return jsonRpcClient; }
	public final void setJsonRpcClient(JsonRpcClient jsonRpcClient) { this.jsonRpcClient = jsonRpcClient; }
	public final String getMethodName() { return methodName; }
	public final void setMethodName(String methodName) { this.methodName = methodName; }
	public final Object getArgument() { return argument; }
	public final void setArgument(Object argument) { this.argument = argument; }
	public final Type getReturnType() { return returnType; }
	public final void setReturnType(Type returnType) { this.returnType = returnType; }

	public final Map<String, String> getExtraHeaders() { return extraHeaders; }
	public final void setExtraHeaders(Map<String, String> extraHeaders) { this.extraHeaders = extraHeaders; }


}
