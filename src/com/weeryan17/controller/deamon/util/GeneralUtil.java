package com.weeryan17.controller.deamon.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

import com.google.gson.JsonObject;

public class GeneralUtil {
	public static JsonObject getJsonText(String text) {
		JsonObject json = new JsonObject();
		json.addProperty("text", text);
		return json;
	}
	
	public static <T> String listToString(List<T> list) {
		StringBuilder sb = new StringBuilder();
		sb.append(list.get(0).toString());
		list = list.subList(1, list.size());
		for(T t: list) {
			sb.append(", ").append(t.toString());
		}
		return sb.toString();
	}
	
	public static String readUrl(String urlString) throws Exception {
	    BufferedReader reader = null;
	    try {
	        URL url = new URL(urlString);
	        reader = new BufferedReader(new InputStreamReader(url.openStream()));
	        StringBuffer buffer = new StringBuffer();
	        int read;
	        char[] chars = new char[1024];
	        while ((read = reader.read(chars)) != -1)
	            buffer.append(chars, 0, read); 

	        return buffer.toString();
	    } finally {
	        if (reader != null)
	            reader.close();
	    }
	}
}
