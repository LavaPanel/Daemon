package com.weeryan17.controller.deamon;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.apache.ftpserver.ftplet.FtpException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.weeryan17.controller.deamon.server.Web;
import com.weeryan17.controller.deamon.util.FtpManager;
import com.weeryan17.controller.deamon.util.GeneralUtil;
import com.weeryan17.controller.deamon.util.Logging;
import com.weeryan17.controller.deamon.util.ServerHandler;
import com.weeryan17.controller.deamon.util.redis.RedisController;

import io.github.binaryoverload.JSONConfig;
import okhttp3.OkHttpClient;

public class Deamon {

	OkHttpClient httpClient;

	Gson gson;

	Logging logger;

	ServerHandler hander;

	String remoteAddress;

	private static Deamon instance;

	private JSONConfig config;
	
	RedisController redis;

	public static void main(String[] args) {
		new Deamon().init();
	}

	public void init() {
		logger = new Logging();
		gson = new GsonBuilder().setPrettyPrinting().create();
		httpClient = new OkHttpClient();

		checkIP();

		hander = new ServerHandler(this);
		hander.loadSerers();

		try {
			File file = new File("config.json");
			if (!file.exists() && !file.createNewFile())
				throw new IllegalStateException("Can't create config file!");
			try {
				config = new JSONConfig("config.json");
			} catch (NullPointerException e) {
				logger.log("Invalid Config Json!", Level.SEVERE, e);
				System.exit(1);
			}
		} catch (IOException e) {
			logger.log("Chould load config file!", Level.SEVERE, e);
			System.exit(1);
		}

		List<String> required = new ArrayList<>();
		required.add("server.address");
		required.add("user");
		required.add("pass");
		required.add("ftp.port");
		required.add("ftp.pass");
		required.add("redis.server.address");

		List<String> missing = new ArrayList<>();

		for (String req : required) {
			if (config.getElement(req) != null) {
				if (!config.getElement(req).isPresent()) {
					missing.add(req);
				}
			} else {
				missing.add(req);
			}
		}

		if (missing.size() > 0) {
			logger.log("Missing required element(s) '" + GeneralUtil.listToString(missing) + "'", Level.SEVERE);
			System.exit(1);
		}
		
		redis = new RedisController(this);
		
		FtpManager ftp = new FtpManager(this);
		ftp.init();
		
		try {
			ftp.start();
		} catch (FtpException e) {
			e.printStackTrace();
		}

		new Web(this).initWeb();
		instance = this;
	}

	public static Deamon getInstance() {
		return instance;
	}

	public JSONConfig getConfig() {
		return config;
	}

	public Logging getLogger() {
		return logger;
	}

	public Gson getGson() {
		return gson;
	}

	public ServerHandler getHander() {
		return hander;
	}

	public OkHttpClient getHttpClient() {
		return httpClient;
	}

	public void checkIP() {
		JsonObject json;
		String ip;
		JsonParser parse = new JsonParser();
		try {
			json = parse.parse(GeneralUtil.readUrl("https://api.ipify.org/?format=json")).getAsJsonObject();
			ip = json.get("ip").getAsString();
			this.remoteAddress = ip;
		} catch (JsonSyntaxException e) {
			logger.log("Error checking ip!", Level.SEVERE, e);
		} catch (Exception e) {
			logger.log("Error checking ip!", Level.SEVERE, e);
		}
	}

	public String getRemoteAddress() {
		return remoteAddress;
	}

	public RedisController getRedis() {
		return redis;
	}

}
