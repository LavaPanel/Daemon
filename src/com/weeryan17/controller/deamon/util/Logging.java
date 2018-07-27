package com.weeryan17.controller.deamon.util;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.google.gson.JsonObject;
import com.weeryan17.controller.deamon.Deamon;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Logging {
	
	Deamon instance;
	Logger logger;
	public Logging(Deamon instance) {
		this.instance = instance;
		logger = Logger.getLogger("com.weeryan17");
		try {
			FileHandler fh = new FileHandler("latest.log");
			logger.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);
		} catch (SecurityException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void log(String message) {
		log(message, Level.INFO);
	}
	
	public void log(String message, Level level) {
		if (level.intValue() > Level.INFO.intValue() && level != Level.OFF) {
			String levelString = "warn";
			if(level.intValue() >= Level.SEVERE.intValue()) {
				levelString = "error";
			}
			sendToServer(levelString, message);
		}
		logger.log(level, message);
	}
	
	public void log(String message, Level level, Exception error) {
		if (level.intValue() > Level.INFO.intValue() && level != Level.OFF) {
			String levelString = "warn";
			if(level.intValue() >= Level.SEVERE.intValue()) {
				levelString = "error";
			}
			sendToServer(levelString, message + "\n<code>" + GeneralUtil.errorString(error) + "</code>");
		}
		logger.log(level, message, error);
	}
	
	public void sendToServer(String level, String error) {
		JsonObject json = new JsonObject();
		json.addProperty("level", level);
		json.addProperty("message", error);
		String address = instance.getConfig().getString("server.address").get();
		address += "/util/java/alert.php";
		MediaType mediaType = MediaType.parse("application/json");
		RequestBody body = RequestBody.create(mediaType, instance.getGson().toJson(json));
		Request req = new Request.Builder().url(address).post(body)
				  .addHeader("Content-Type", "application/json").build();
		try {
			Response res = instance.getHttpClient().newCall(req).execute();
			if(!res.isSuccessful()) {
				System.out.println("error sending error to site");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
