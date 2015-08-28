package com.robinkirkman.uncaptivate;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;

import org.jsoup.Connection;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;

import java.util.Map.Entry;

public class Clickthrough {
	public static void uncaptivate() throws IOException { 
		new Clickthrough("http://www.google.com/").process();
	}
	
	public static void main(String[] args) {
		String testURL = "http://www.google.com";
		if(args.length > 0)
			testURL = args[0];
		try {
			new Clickthrough(testURL).process();
			System.exit(0);
		} catch(Throwable t) {
			System.exit(-1);
		}
	}
	
	private Connection test;
	private Queue<Iterator<Entry<String, Connection>>> pending;
	private Set<String> visited;
	
	public Clickthrough(String testURL) throws IOException {
		pending = new ArrayDeque<>();
		visited = new HashSet<String>();
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
			
			String vurl = cxn.request().url().toString() + cxn.request().data();
			if(!visited.add(vurl))
				continue;
			
			cxn.followRedirects(true);
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
