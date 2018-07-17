package com.weeryan17.controller.deamon.server;

import static spark.Spark.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.qmetric.spark.authentication.AuthenticationDetails;
import com.qmetric.spark.authentication.BasicAuthenticationFilter;
import com.weeryan17.controller.deamon.Deamon;
import com.weeryan17.controller.deamon.listeners.SparkListener;
import com.weeryan17.controller.deamon.util.objects.Server;

public class Web {
	WebUtils utils;
	Deamon instance;
	JsonParser parser;

	public Web(Deamon instance) {
		this.instance = instance;
		utils = new WebUtils(instance);
		parser = new JsonParser();
	}

	public void initWeb() {
		SparkListener listener = new SparkListener(instance);
		port(instance.getConfig().getInteger("port").getAsInt());
		before("/*", (q, a) -> {
			listener.handle(q, a);
		});
		before(new BasicAuthenticationFilter(new AuthenticationDetails(instance.getConfig().getString("user").get(),
				instance.getConfig().getString("pass").get())));
		path("/server", () -> {
			path("/:id", () -> {
				get("/stats", (request, responce) -> {
					responce.type("application/json");
					Server server = utils.getServer(request.params(":id"));
					if (server == null) {
						return utils.badRequestMessage(WebUtils.WebBadResponceType.INVALID_SERVER, request.ip());
					}
					return instance.getGson().toJson(server.getInfo());
				});
				path("/stats", () -> {
					get("/messages", (request, responce) -> {
						responce.type("application/json");
						Server server = utils.getServer(request.params(":id"));
						if (server == null) {
							return utils.badRequestMessage(WebUtils.WebBadResponceType.INVALID_SERVER, request.ip());
						}
						return instance.getGson().toJson(server.getInfo().getMessages());
					});
				});
				post("/command", (request, responce) -> {
					responce.type("application/json");
					Server server = utils.getServer(request.params(":id"));
					if (server == null) {
						return utils.badRequestMessage(WebUtils.WebBadResponceType.INVALID_SERVER, request.ip());
					}
					if (request.contentType() != null && request.contentType().equals("application/json")) {
						JsonElement rawJson = parser.parse(request.body());
						if (!rawJson.isJsonObject()) {
							return utils.badRequestMessage(WebUtils.WebBadResponceType.BAD_JSON, request.ip());
						} else {
							JsonObject json = rawJson.getAsJsonObject();
							if (!(json.has("command") || json.has("args"))) {
								return utils.badRequestMessage(WebUtils.WebBadResponceType.INVALID_JSON, request.ip());
							}
							JsonElement rawArgs = json.get("args");
							JsonElement rawCommand = json.get("command");
							if (!utils.isString(rawCommand) || !rawArgs.isJsonArray()) {
								return utils.badRequestMessage(WebUtils.WebBadResponceType.INVALID_JSON, request.ip());
							}
							String command = rawCommand.getAsJsonPrimitive().getAsString();
							JsonArray jsonArgs = rawArgs.getAsJsonArray();
							List<String> args = new ArrayList<>();
							for (JsonElement jsonArg : jsonArgs) {
								if (!utils.isString(jsonArg)) {
									return utils.badRequestMessage(WebUtils.WebBadResponceType.INVALID_JSON, request.ip());
								}
								args.add(jsonArg.getAsString());
							}
							instance.getHander().sendCommand(server, command, args);
							JsonObject responceJson = new JsonObject();
							responceJson.addProperty("good", true);
							return instance.getGson().toJson(responceJson);
						}
					} else {
						return utils.badRequestMessage(WebUtils.WebBadResponceType.INVALID_TYPE, request.ip());
					}
				});
				post("/stop", (request, responce) -> {
					responce.type("application/json");
					Server server = utils.getServer(request.params(":id"));
					if (server == null) {
						return utils.badRequestMessage(WebUtils.WebBadResponceType.INVALID_SERVER, request.ip());
					}
					if (request.contentType() != null && request.contentType().equals("application/json")) {
						JsonElement rawJson = parser.parse(request.body());
						if (!rawJson.isJsonObject()) {
							return utils.badRequestMessage(WebUtils.WebBadResponceType.BAD_JSON, request.ip());
						} else {
							JsonObject json = rawJson.getAsJsonObject();
							if (!json.has("message")) {
								return utils.badRequestMessage(WebUtils.WebBadResponceType.INVALID_JSON, request.ip());
							}
							String message = json.get("message").getAsString();
							instance.getHander().stopServer(server, message);
							JsonObject responceJson = new JsonObject();
							responceJson.addProperty("good", true);
							return instance.getGson().toJson(responceJson);
						}
					} else {
						return utils.badRequestMessage(WebUtils.WebBadResponceType.INVALID_TYPE, request.ip());
					}
				});
				post("/edit", (request, responce) -> {
					responce.type("application/json");
					Server server = utils.getServer(request.params(":id"));
					if (server == null) {
						return utils.badRequestMessage(WebUtils.WebBadResponceType.INVALID_SERVER, request.ip());
					}
					if (request.contentType() != null && request.contentType().equals("application/json")) {
						return "";
					} else {
						return utils.badRequestMessage(WebUtils.WebBadResponceType.INVALID_TYPE, request.ip());
					}
				});
				get("/start", (request, responce) -> {
					responce.type("application/json");
					Server server = utils.getServer(request.params(":id"));
					if (server == null) {
						return utils.badRequestMessage(WebUtils.WebBadResponceType.INVALID_SERVER, request.ip());
					}
					instance.getHander().startServer(server);
					JsonObject responceJson = new JsonObject();
					responceJson.addProperty("good", true);
					return instance.getGson().toJson(responceJson);
				});
			});
			post("/create", (request, responce) -> {
				responce.type("application/json");
				return "";
			});
		});

		get("/servers", (request, responce) -> {
			responce.type("application/json");
			List<JsonObject> servers = new ArrayList<>();
			for (Server server : instance.getHander().getServers()) {
				JsonObject jsonServer = new JsonObject();
				jsonServer.addProperty("id", server.getId());
				jsonServer.addProperty("name", server.getName());
				servers.add(jsonServer);
			}
			JsonArray serversJson = parser.parse(instance.getGson().toJson(servers)).getAsJsonArray();
			List<JsonObject> potentials = new ArrayList<>();
			for (String server : instance.getHander().getPotentail()) {
				JsonObject jsonServer = new JsonObject();
				jsonServer.addProperty("directory", server);
				potentials.add(jsonServer);
			}
			JsonArray potentialsJson = parser.parse(instance.getGson().toJson(potentials)).getAsJsonArray();
			JsonObject json = new JsonObject();
			json.add("servers", serversJson);
			json.add("potentials", potentialsJson);
			return instance.getGson().toJson(json);
		});

		get("/logs", (request, responce) -> {
			responce.type("text");
			FileReader fileReader = new FileReader("latest.log");

			BufferedReader bufferedReader = new BufferedReader(fileReader);

			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				sb.append(line).append("\n");
			}
			bufferedReader.close();

			return sb.toString();
		});
		
		path("/versions", () -> {
			get("/", (req, res) -> {
				return "";
			});
		});

		instance.getLogger().log("Spark started!");
	}
}
