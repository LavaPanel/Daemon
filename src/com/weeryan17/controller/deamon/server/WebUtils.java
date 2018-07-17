package com.weeryan17.controller.deamon.server;

import java.util.logging.Level;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.weeryan17.controller.deamon.Deamon;
import com.weeryan17.controller.deamon.util.objects.Server;

public class WebUtils {
	Deamon instance;
	public WebUtils(Deamon instance) {
		this.instance = instance;
	}
	
	public String badRequestMessage(WebUtils.WebBadResponceType type, String ip) {
		JsonObject json = new JsonObject();
		JsonObject request = new JsonObject();
		json.addProperty("good", false);
		request.addProperty("message", type.message);
		request.addProperty("raw_type", type.name());
		json.add("reply", request);
		
		instance.getLogger().log("Bot recived a bad request", Level.WARNING);
		
		return instance.getGson().toJson(json);
	}
	
	public boolean isString(JsonElement element) {
		if (!element.isJsonPrimitive()) {
			return false;
		}
		JsonPrimitive primitive = element.getAsJsonPrimitive();
		if (!primitive.isString()) {
			return false;
		}
		return true;
	}
	
	public Server getServer(String stringId) {
		int id;
		try {
			id = Integer.valueOf(stringId);
		} catch (NumberFormatException e) {
			return null;
		}
		return instance.getHander().getServers().get(id);
	}
	
	public enum WebBadResponceType {
		INVALID_TYPE("Inavlid content type!"),
		BAD_JSON("Json syntax is bad!"),
		INVALID_JSON("Json is invalid for this request!"),
		INVALID_SERVER("The server id is invalid!");
		
		String message;
		
		WebBadResponceType(String message) {
			this.message = message;
		}
		
		public String getMessage() {
			return message;
		}
	}
}
