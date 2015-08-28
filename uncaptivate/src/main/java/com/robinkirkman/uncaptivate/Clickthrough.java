package com.robinkirkman.uncaptivate;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;

import org.jsoup.Connection;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;

import java.util.Map.Entry;

public class Clickthrough {
	public static void uncaptivate() throws IOException { 
		new Clickthrough("http://www.google.com/").process();
	}
	
	private Connection test;
	private Queue<Iterator<Entry<String, Connection>>> pending;
	
	public Clickthrough(String testURL) throws IOException {
		pending = new ArrayDeque<>();
		test = HttpConnection.connect(testURL);
		test.followRedirects(false);
		
		Connection head = HttpConnection.connect(testURL);
		head.followRedirects(true);
		pending.offer(new ClickthroughIterable(head.get()).iterator());
	}
	
	public boolean isUncaptivated() {
		try {
			test.get();
			return true;
		} catch(Exception e) {
			return false;
		}
	}
	
	public void process() throws IOException {
		while(!isUncaptivated()) {
			while(pending.size() > 0 && !pending.peek().hasNext())
				pending.poll();
			if(pending.size() == 0)
				throw new IOException("Unable to uncaptivate");
			Entry<String, Connection> req = pending.peek().next();
			String method = req.getKey();
			Connection cxn = req.getValue();
			Document doc = null;
			if("post".equalsIgnoreCase(method)) {
				try {
					doc = cxn.post();
				} catch(Exception e) {
					e.printStackTrace();
				}
			} else {
				try {
					doc = cxn.get();
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			if(doc != null)
				pending.offer(new ClickthroughIterable(doc).iterator());
		}
	}
}
