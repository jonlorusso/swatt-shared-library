package com.swatt.util.json;

import java.util.LinkedList;

import com.swatt.util.general.ConcurrencyUtilities;
import com.swatt.util.general.OperationFailedException;

public class JsonRpcClientPool {			// FIXME: Not Industrial Strength.  Does not deal with un-returned connections
	private String url;
	private String user;
	private String password;
	private int maxSize;
	private LinkedList<JsonRpcClient> freeJsonRpcClients = new LinkedList<JsonRpcClient>();
	private LinkedList<JsonRpcClient> busyJsonRpcClients = new LinkedList<JsonRpcClient>();
	
	private JsonRpcClientListener jsonRpcClientListener;
	
	public JsonRpcClientPool(String url, String user, String password, int maxSize) {
		this.url = url;
		this.user = user;
		this.password = password;
		this.maxSize = maxSize;
	}
	
	public void setJsonRpcClientListener(JsonRpcClientListener jsonRpcClientListener) { this.jsonRpcClientListener = jsonRpcClientListener; }
	
	public JsonRpcClient getJsonRpcClient() throws OperationFailedException  {
		synchronized(freeJsonRpcClients) {
			JsonRpcClient jsonRpcClient = null;
			
			if (freeJsonRpcClients.size() > 0) {
				jsonRpcClient = freeJsonRpcClients.removeFirst();
			} else if ((freeJsonRpcClients.size() + busyJsonRpcClients.size()) < maxSize) {
				jsonRpcClient = new JsonRpcClient(url, user, password, jsonRpcClientListener);
			} else {
				while (freeJsonRpcClients.size() == 0) {
					ConcurrencyUtilities.waitOn(freeJsonRpcClients);
				}
					
				jsonRpcClient = freeJsonRpcClients.removeFirst();
			}
			
			busyJsonRpcClients.add(jsonRpcClient);
			return jsonRpcClient;
		}
	}
	
	public void returnConnection(JsonRpcClient jsonRpcClient) {
		synchronized(freeJsonRpcClients) {
			busyJsonRpcClients.remove(jsonRpcClient);
			freeJsonRpcClients.add(jsonRpcClient);
			ConcurrencyUtilities.notifyAll(freeJsonRpcClients);
		}
	}

}