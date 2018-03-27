package com.swatt.util.json;

public interface JsonRpcClientListener {				// FIXME: To be completed
	public void jsonRpcClientCreated(JsonRpcClientEvent evt);
	public void invokeInitiated(JsonRpcClientEvent evt);
	public void invokeCompletedSuccessfully(JsonRpcClientEvent evt);
	public void invokeFailed(JsonRpcClientEvent evt);
}
