package com.swatt.util.json;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.LinkedList;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import com.swatt.util.general.ConcurrencyUtilities;

public class HttpClientPool { // FIXME: Not Industrial Strength. Does not deal with un-returned connections
    private int maxSize;
    private LinkedList<CloseableHttpClient> freeHttpClients = new LinkedList<CloseableHttpClient>();
    private LinkedList<CloseableHttpClient> busyHttpClients = new LinkedList<CloseableHttpClient>();
    PoolingHttpClientConnectionManager connManager;

    public HttpClientPool(int maxSize) {
        this.maxSize = maxSize;

        PlainConnectionSocketFactory plainSocketFactory = PlainConnectionSocketFactory.getSocketFactory();
        Registry<ConnectionSocketFactory> connSocketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create().register("http", plainSocketFactory).build();

        connManager = new PoolingHttpClientConnectionManager(connSocketFactoryRegistry);
        connManager.setMaxTotal(this.maxSize);
        connManager.setDefaultMaxPerRoute(this.maxSize);
    }
    
    public CloseableHttpResponse execute(String url) {
    	return execute(url, null);
    }

    public CloseableHttpResponse execute(String url, String params) {
        URIBuilder uriBuilder;
        HttpUriRequest httpUriRequest = null;

        try {
            uriBuilder = new URIBuilder(url);
            if (params != null) {
	            HttpPost httpPost = new HttpPost(uriBuilder.build());
	            httpPost.setHeader("Content-Type", "application/json");
	            httpPost.setEntity(new ByteArrayEntity(params.getBytes("UTF-8")));
	            httpUriRequest = httpPost;
            } else
            	httpUriRequest = new HttpGet(uriBuilder.build());
            
        } catch (URISyntaxException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        synchronized (freeHttpClients) {
            CloseableHttpClient httpClient = null;

            if (freeHttpClients.size() > 0) {
                httpClient = freeHttpClients.removeFirst();
            } else if ((freeHttpClients.size() + busyHttpClients.size()) < maxSize) {
                httpClient = createHttpClient();
            } else {
                while (freeHttpClients.size() == 0) {
                    ConcurrencyUtilities.waitOn(freeHttpClients);
                }

                httpClient = freeHttpClients.removeFirst();
            }

            busyHttpClients.add(httpClient);

            CloseableHttpResponse response = null;
            try {
                response = httpClient.execute(httpUriRequest);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                returnConnection(httpClient);
            }
            

            return response;
        }
    }

    public void returnConnection(CloseableHttpClient httpClient) {
        synchronized (freeHttpClients) {
            busyHttpClients.remove(httpClient);
            freeHttpClients.add(httpClient);
            ConcurrencyUtilities.notifyAll(freeHttpClients);
        }
    }

    private CloseableHttpClient createHttpClient() {
        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(connManager).build();

        return httpClient;
    }
}