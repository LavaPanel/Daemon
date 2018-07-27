package com.weeryan17.controller.deamon.server;

import static spark.Spark.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.qmetric.spark.authentication.AuthenticationDetails;
import com.qmetric.spark.authentication.BasicAuthenticationFilter;
import com.weeryan17.controller.deamon.Deamon;
import com.weeryan17.controller.deamon.listeners.SparkListener;
import com.weeryan17.controller.deamon.util.Download;
import com.weeryan17.controller.deamon.util.Eval;
import com.weeryan17.controller.deamon.util.objects.Server;

public class Web {
	WebUtils utils;
	Deamon instance;
	JsonParser parser;

	JsonObject versionsCache;
	Date updateTime;

	Map<UUID, Download> downloads;

	public Web(Deamon instance) {
		this.instance = instance;
		utils = new WebUtils(instance);
		parser = new JsonParser();
		downloads = new HashMap<>();
	}
	
	String res;

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
									return utils.badRequestMessage(WebUtils.WebBadResponceType.INVALID_JSON,
											request.ip());
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

		get("/versions", (request, responce) -> {
			responce.type("application/json");
			boolean snapshots = false;
			String parm = request.queryParams("snapshots");
			if (parm != null) {
				snapshots = true;
			}

			if (versionsCache == null || new Date().getTime() - updateTime.getTime() > TimeUnit.HOURS.toMillis(12)) {
				JsonObject allVersions = new JsonObject();
				URL versionsUrl = new URL("http://launchermeta.mojang.com/mc/game/version_manifest.json");
				BufferedReader reader = new BufferedReader(new InputStreamReader(versionsUrl.openStream()));

				JsonObject json = parser.parse(reader).getAsJsonObject();

				allVersions.add("vannila", json);

				versionsCache = allVersions;

				updateTime = new Date();
			}

			JsonObject versions = new JsonObject();

			JsonObject vannilaVersions = new JsonObject();

			JsonObject spigotVersions = new JsonObject();

			JsonArray vannilaVersionsRaw = versionsCache.getAsJsonObject("vannila").getAsJsonArray("versions");

			JsonArray snapshotsObj = null;
			if (snapshots) {
				snapshotsObj = new JsonArray();
			}

			for (JsonElement elm : vannilaVersionsRaw) {
				JsonObject version = elm.getAsJsonObject();
				String type = version.get("type").getAsString();
				if (type.equals("release") || (snapshots && type.equals("snapshot"))) {
					String id = version.get("id").getAsString();
					if (!type.endsWith("snapshot")) {
						String[] info = id.split("\\.");
						String majorVersion = info[0] + "." + info[1];
						String minorVersion = "0";
						if (info.length > 2) {
							minorVersion = info[2];
						}
						// Check for >= mc version 1.8 for spigot
						if (Integer.parseInt(info[1]) >= 8) {
							if (spigotVersions.has(majorVersion)) {
								JsonArray versionArray = spigotVersions.getAsJsonArray(majorVersion);
								versionArray.add(minorVersion);
								spigotVersions.add(majorVersion, versionArray);
							} else {
								JsonArray versionArray = new JsonArray();
								versionArray.add(minorVersion);
								spigotVersions.add(majorVersion, versionArray);
							}
						}
						if (vannilaVersions.has(majorVersion)) {
							JsonArray versionArray = vannilaVersions.getAsJsonArray(majorVersion);
							versionArray.add(minorVersion);
							vannilaVersions.add(majorVersion, versionArray);
						} else {
							JsonArray versionArray = new JsonArray();
							versionArray.add(minorVersion);
							vannilaVersions.add(majorVersion, versionArray);
						}
					} else {
						snapshotsObj.add(id);
					}

				}
			}

			if (snapshots) {
				vannilaVersions.add("snapshots", snapshotsObj);
			}

			versions.add("vannila", vannilaVersions);

			versions.add("spigot", spigotVersions);

			return instance.getGson().toJson(versions);
		});

		path("/versions", () -> {
			post("/download", (request, responce) -> {
				responce.type("application/json");
				if (versionsCache != null) {
					if (request.contentType() != null && request.contentType().equals("application/json")) {
						JsonObject json = parser.parse(request.body()).getAsJsonObject();
						String type = json.get("type").getAsString();
						String versionString = json.get("version").getAsString();
						JsonArray versions = versionsCache.get("vannila").getAsJsonObject().get("versions")
								.getAsJsonArray();
						JsonObject version = null;
						for (JsonElement versionRaw : versions) {
							if (versionRaw.getAsJsonObject().get("id").getAsString().equals(versionString)) {
								version = versionRaw.getAsJsonObject();
							}
						}
						String link = "";
						if (type.equals("vannila")) {
							URL versioninfoUrl = new URL(version.get("url").getAsString());
							BufferedReader reader = new BufferedReader(
									new InputStreamReader(versioninfoUrl.openStream()));

							JsonObject versioninfo = parser.parse(reader).getAsJsonObject();

							link = versioninfo.get("downloads").getAsJsonObject().get("server").getAsJsonObject()
									.get("url").getAsString();
						} else if (type.equals("spigot")) {
							link = "https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar";
						}
						String lamdaLink = link;
						UUID downloadID;
						URL url = new URL(lamdaLink);
						File file = new File("jars/spigot/BuildTools/BuildTools.jar");
						if (!type.equals("spigot")) {
							file = new File("jars/" + type + "/" + versionString + ".jar");
						}
						file.getParentFile().mkdirs();
						Download download = new Download(url, file);
						downloadID = UUID.randomUUID();
						downloads.put(downloadID, download);

						File acctualFile = file;

						Thread thread = new Thread(new Runnable() {

							@Override
							public void run() {
								try {
									while (download.getStatus() != Download.COMPLETE) {
									}

									if (type.equals("spigot")) {
										instance.getLogger().log("starting spigot build process");
										ProcessBuilder pb = new ProcessBuilder()
												.command("java", "-jar", "BuildTools.jar", "--rev", versionString)
												.directory(acctualFile.getParentFile());
										Process proc = pb.start();
										InputStream in = proc.getInputStream();
										while (proc.isAlive()) {
											BufferedReader reader = new BufferedReader(new InputStreamReader(in));
											String line;
											while ((line = reader.readLine()) != null) {
												instance.getRedis().listAdd("Spigot-build-" + downloadID.toString(),
														line);
											}
										}
									}

								} catch (IOException e) {
									instance.getLogger().log("Error downloading jar file " + url.toString(),
											Level.WARNING, e);
								}
							}

						});
						thread.start();
						JsonObject jsonRes = new JsonObject();
						jsonRes.addProperty("id", downloadID.toString());
						return instance.getGson().toJson(jsonRes);
					} else {
						return utils.badRequestMessage(WebUtils.WebBadResponceType.INVALID_TYPE, request.ip());
					}
				} else {
					return utils.badRequestMessage(WebUtils.WebBadResponceType.BAD_ACCESS, request.ip());
				}
			});
			path("/download", () -> {
				get("/status/:id", (request, responce) -> {
					responce.type("application/json");
					JsonObject jsonRes = new JsonObject();
					UUID downloadId = UUID.fromString(request.params(":id"));

					Download download = downloads.get(downloadId);

					jsonRes.addProperty("status", Download.STATUSES[download.getStatus()]);
					jsonRes.addProperty("progress", download.getProgress());

					return instance.getGson().toJson(jsonRes);
				});
			});
			post("/refresh", (request, responce) -> {
				URL versionsUrl = new URL("http://launchermeta.mojang.com/mc/game/version_manifest.json");
				BufferedReader reader = new BufferedReader(new InputStreamReader(versionsUrl.openStream()));

				JsonObject json = parser.parse(reader).getAsJsonObject();

				versionsCache = json;

				updateTime = new Date();
				return "good";
			});
		});
		post("/eval", (request, responce) -> {
			Thread thread = new Eval(request.body(), new Eval.EvalRunnable() {

				@Override
				public void run(String results) {
					res = results;
				}

			}).start();
			thread.join();
			return res;
		});

		instance.getLogger().log("Spark started!");
	}
}
