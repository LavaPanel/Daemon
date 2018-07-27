package com.weeryan17.controller.deamon.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;

import com.weeryan17.controller.deamon.Deamon;
import com.weeryan17.controller.deamon.util.objects.Server;
import com.weeryan17.controller.deamon.util.redis.Message;

public class ServerHandler {
	List<Server> servers = new ArrayList<>();
	
	Map<Integer, Process> processes = new HashMap<>();

	List<String> potentail = new ArrayList<>();

	Deamon instance;
	public ServerHandler(Deamon instance) {
		this.instance = instance;
	}
	
	public void startServer(Server server) throws IOException {
		instance.getLogger().log("Starting Server " + server.getName());
		String loc = "jars/" + server.getVersion().getLocation();
		File jar = new File(loc);
		String ram = "-Xmx" + String.valueOf(server.getRam()) + "M";
		ArrayList<String> extrasList = new ArrayList<>();
		if (server.getVersion().isVannila()) {
			extrasList.add("nogui");
		}
		String[] extras = extrasList.toArray(new String[extrasList.size()]);
		String[] command = new String[] {"java", "-jar", ram, jar.getAbsolutePath()};
		command = ArrayUtils.addAll(command, extras);
		ProcessBuilder builder = new ProcessBuilder(command);
		File serverLoc = new File("servers/" + server.getDir());
		builder.directory(serverLoc);
		builder.redirectErrorStream(true);
		final Process proc = builder.start();
		server.setRunning(true);
		server.getInfo().makeQuerry();
		server.getInfo().setRemoteAdd(instance.getRemoteAddress());
		processes.put(server.getId(), proc);
		new Thread(new Runnable() {

			@Override
			public void run() {
				InputStream in = proc.getInputStream();
				Timer timer = new Timer();
				timer.schedule(new TimerTask() {

					@Override
					public void run() {
						long used = 0;
						long total = 0;
						
						int usedMB = (int) (used / 1024 / 1024);
						int totalMB = (int) (total / 1024 /1024);
						double percent = (double) usedMB / (double) totalMB;
						
						server.getInfo().setRamUsage(usedMB);
						server.getInfo().setRamTotal(totalMB);
						server.getInfo().setRamPercent(percent);
						
						server.getInfo().setCpuUsage(ProcessUtil.getCpuUsageSenceLastCheck(proc.pid(), proc.toHandle().info()));
						
						server.getInfo().updatePlayerCounts();
					}
					
				}, 0, 1000);
				while (proc.isAlive()) {
					BufferedReader reader = new BufferedReader(new InputStreamReader(in));
					String line = null;
					try {
						while ((line = reader.readLine()) != null) {
							Message message = getMessageFromString(line);
							instance.getRedis().listAdd(String.valueOf(server.getId()), instance.getGson().toJson(message));
						}
					} catch (IOException e) {
						instance.getLogger().log("Error reading server messages", Level.WARNING, e);
					}
				}
				timer.cancel();
				server.setRunning(false);
				server.setInfo(null);
				ProcessUtil.stoped(proc.pid());
				instance.getRedis().delete(String.valueOf(server.getId()));
				instance.getLogger().log("Server " + server.getName() + " stoped");
			}
			
		}).start();
		instance.getLogger().log("Server " + server.getName() + " started");
	}
	
	public void stopServer(Server server, String message) {
		this.sendCommand(server, "stop", new ArrayList<>());
		//TODO message
	}
	
	public void loadSerers() {
		File serversDir = new File("servers");
		if(!serversDir.exists()) {
			serversDir.mkdirs();
		}
		List<String> dirNames = Arrays.asList(serversDir.list(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return new File(dir, name).isDirectory();
			}
			
		}));
		dirNames.remove(".");
		dirNames.remove("..");
		for (String dirName: dirNames) {
			File dir = new File(serversDir, dirName);
			List<String> files = Arrays.asList(dir.list());
			files.remove(".");
			files.remove("..");
			if (files.contains("server.json")) {
				BufferedReader reader = null;
				try {
					reader = new BufferedReader(new FileReader(new File(serversDir, dirName + "/server.json")));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				Server server = instance.getGson().fromJson(reader, Server.class);
				if (server.isNull()) {
					instance.getLogger().log("Inavlid server json: " + dirName, Level.WARNING);
					return;
				}
				server.setId(servers.size());
				server.setDir(dirName);
				servers.add(server);
			} else if (files.contains("server.properties")) {
				potentail.add(dirName);
			}
		}
		instance.getLogger().log("Servers loaded!");
	}
	
	public Message getMessageFromString(String line) {
		if(line.startsWith("[")){
			String[] split = Pattern.compile("(]: )").split(line);
			String info = split[0];
			String message = split[1];
			String[] infoSplit = info.replaceAll(" ", "").replaceAll("\\[", "").split("]");
			String threadPart = infoSplit[1];
			String time = infoSplit[0];
			String[] threadSplit = threadPart.split("/");
			String thread = threadSplit[0];
			String level = threadSplit[1];
			return new Message(Message.MessageLevel.valueOf(level), time, thread, message);
		} else {
			return new Message(Message.MessageLevel.OTHER, "", "", line);
		}
	}
	
	public List<Server> getServers() {
		return servers;
	}
	
	public List<String> getPotentail() {
		return potentail;
	}
	
	public void sendCommand(Server server, String command, List<String> args) {
		if(!processes.containsKey(server.getId())) {
			return;
		}
		StringBuilder commandBuilder = new StringBuilder();
		commandBuilder.append(command);
		for(String arg: args) {
			commandBuilder.append(" ").append(arg);
		}
		instance.getLogger().log("Command '" + commandBuilder.toString() + "' for the server " + server.getName() + "(" + server.getId() + ") from the console");
		OutputStream outStream = processes.get(server.getId()).getOutputStream();
		PrintWriter out = new PrintWriter(outStream);
		out.println(commandBuilder.toString());
		out.flush();
	}
}
