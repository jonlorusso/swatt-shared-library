package com.swatt.util.json;

import java.lang.reflect.Type;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.swatt.util.general.OperationFailedException;

public class JsonRpcClient {
	
	private JsonRpcHttpClient externalHttpClient;
	private JsonRpcClientListener jsonRpcClientListener;

    public JsonRpcClient(String url, String rpcUser, String rpcPassword) throws OperationFailedException {
    	this(url, rpcUser, rpcPassword, null);
    }
	
    public JsonRpcClient(String url, String rpcUser, String rpcPassword, JsonRpcClientListener jsonRpcClientListener) throws OperationFailedException {
        try {
        	this.jsonRpcClientListener = jsonRpcClientListener;
        	
            URL uri = new URL(url);

            if (rpcUser != null) {
                Authenticator.setDefault(new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(rpcUser, rpcPassword.toCharArray());
                    }
                });
            }

            this.externalHttpClient = new JsonRpcHttpClient(uri);
            
            if (jsonRpcClientListener != null) {
            	JsonRpcClientEvent evt = new JsonRpcClientEvent(this);
            	jsonRpcClientListener.jsonRpcClientCreated(evt);
            }
            
        } catch (MalformedURLException e) {
        	throw new OperationFailedException("Unable to build JsonRpcClient", e);
        }
    }
	
	// IN PROGRESS... WRAPPING OF THE JSON-RPC for Java - https://github.com/briandilley/jsonrpc4j/blob/master/README.md

	@SuppressWarnings("unchecked")
	public <T> T invoke(String methodName, Object argument, Class<T> clazz) throws OperationFailedException {
		return (T) invoke(methodName, argument, Type.class.cast(clazz));
	}
	
	public Object invoke(String methodName, Object argument, Type returnType) throws OperationFailedException {
		return invoke(methodName, argument, returnType, new HashMap<String, String>());
	}
	
	public Object invoke(String methodName, Object argument, Type returnType, Map<String, String> extraHeaders) throws OperationFailedException {
        if (jsonRpcClientListener != null) {
        	JsonRpcClientEvent evt = new JsonRpcClientEvent(this, methodName, argument, returnType, extraHeaders);
        	jsonRpcClientListener.invokeInitiated(evt);
        }
        
        try {
        	Object result = externalHttpClient.invoke(methodName, argument, returnType, extraHeaders);
        	
            if (jsonRpcClientListener != null) {
            	JsonRpcClientEvent evt = new JsonRpcClientEvent(this, methodName, argument, returnType, extraHeaders);
            	jsonRpcClientListener.invokeCompletedSuccessfully(evt);
            }
        	
        	return result;
        } catch (Throwable t) {
            if (jsonRpcClientListener != null) {
            	JsonRpcClientEvent evt = new JsonRpcClientEvent(this, methodName, argument, returnType, extraHeaders);
            	jsonRpcClientListener.invokeFailed(evt);
            }
        	throw new OperationFailedException("JsonRpcClient.invoke failure", t);
        }
	}
	

}
