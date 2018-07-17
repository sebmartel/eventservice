package com.github.sebmartel.eventservice;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.FutureResponseListener;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.sebmartel.eventservice.start.Start;

public class IntegrationTests {
	
	public static Start app;
	public static Thread appThread;
	public static HttpClient client;
	
	@BeforeClass
	public static void init() throws Exception {
		app = new Start();
		appThread = new Thread( () -> app.run() );
		appThread.start();
		client = new HttpClient();
		client.start();
	}
	
	@AfterClass
	public static void shutdown() throws Exception {
		app.stop();
		appThread.join();		
	}

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testPost() throws InterruptedException, ExecutionException, TimeoutException {
		Thread.sleep(2000);
		String ev = "{ \"item\":{ \"id\": 123, \"timestamp\": \"2016-01-01T23:01:01.000Z\" } }";
		ContentResponse req = client.POST("http://localhost:8080/items").content(new StringContentProvider(ev)).send();
		int status = req.getStatus();
		assertThat(status, is(201));
	}
	
	@SuppressWarnings("unused")
	@Test
	public void testPostAndGet() throws InterruptedException, TimeoutException, ExecutionException {
		String ev = "{ \"item\":{ \"id\": 123, \"timestamp\": \"2016-01-01T23:01:01.000Z\" } }";
		ContentResponse req = client.POST("http://localhost:8080/items").content(new StringContentProvider(ev)).send();
		int status = req.getStatus();
		assertThat(status, is(201));
		

		ContentResponse res = client.GET("http://localhost:8080/items");
		assertThat(res.getStatus(), is(200));
		assertThat(res.getContentAsString(), is("[{\"item\":{\"timestamp\":\"2016-01-01T23:01:01.000Z\",\"id\":123}}]"));
	}
}
