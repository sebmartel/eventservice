package com.github.sebmartel.eventservice.start;

import java.io.IOException;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

/**
 * EventService application start
 * 
 * This is just scaffolding, not production ready.
 * 
 */
public class Start implements Runnable {

	Server server;

	public Start() {
	}
	
	public void run() {
		server = new Server(8080);	
		server.setHandler(itemsHandler());
		try {
			server.start();
			server.join();
		} catch (InterruptedException e) {
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void stop() throws Exception {
		if (server != null) {
			server.stop();
		}
	}
	
	public static void main(String[] args) throws Exception {
		Start app = new Start();
		app.run();
	}

	private static Handler itemsHandler() {

		return new AbstractHandler() {

			@Override
			public void handle(String target, Request baseRequest,
					HttpServletRequest request, HttpServletResponse response)
					throws IOException, ServletException {
				
				if (target.equals("/items")) {
					String method = baseRequest.getMethod();
					switch (method) {
					case "POST": handlePostItem(baseRequest, request, response); break;
					case "GET": handleGetItems(baseRequest, request, response); break;
					default:
					}
				}		
			}

			private void handleGetItems(Request base, HttpServletRequest req, HttpServletResponse res) throws IOException {
				res.setContentType("application/json");
				res.setStatus(200);
				base.setHandled(true);
				res.getWriter().println("hello world");
			}

			private void handlePostItem(Request base, HttpServletRequest req, HttpServletResponse res) throws IOException {
				String body = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
				System.out.println( body );
				
				res.setContentType("application/json");
				res.setStatus(201);
				base.setHandled(true);
				res.getWriter().println("hello world");
			}
		};
	}
}