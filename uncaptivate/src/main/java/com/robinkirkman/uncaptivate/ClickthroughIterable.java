package com.robinkirkman.uncaptivate;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.jsoup.Connection;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ClickthroughIterable implements Iterable<Entry<String, Connection>> {
	private List<Entry<String, Connection>> clicks;
	
	public ClickthroughIterable(Document doc) {
		clicks = new ArrayList<>();
		
		Elements forms = doc.select("form");
		for(Element form : forms) {
			String action = form.attr("abs:action");
			String method = form.attr("method");
			
			if(action == null)
				continue;
			
			Elements submits = form.select("input");
			for(Element submit : submits) {
				String name = submit.attr("name");
				String value = submit.attr("value");
				
				value = (value == null) ? "" : value;
				
				Connection click = HttpConnection.connect(action);
				if(name != null)
					click.data(name, value);
				
				if("post".equalsIgnoreCase(method))
					clicks.add(new AbstractMap.SimpleImmutableEntry<>("post", click));
				else
					clicks.add(new AbstractMap.SimpleImmutableEntry<>("get", click));
			}
		}
		
		
		Elements links = doc.select("a[href]");
		for(Element a : links) {
			clicks.add(new AbstractMap.SimpleImmutableEntry<>("get", HttpConnection.connect(a.attr("abs:href"))));
		}
	}

	@Override
	public Iterator<Entry<String, Connection>> iterator() {
		return clicks.iterator();
	}
}
