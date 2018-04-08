package com.weeryan17.controller.deamon.util.objects;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.weeryan17.controller.deamon.Deamon;

public class ServerInfo {
	
	transient Server server;
	
	int querryPort;
	
	int port;

	transient ServerQuerry querry;
	
	int ramUsage;
	
	int ramTotal;
	
	double ramPercent;
	
	String remoteAdd;
	
	int playersCurrent;
	
	int playersTotal;

	public ServerInfo(Server server) {
		this.server = server;
		this.port = server.port;
		this.querryPort = server.querryPort;
	}
	
	public ServerQuerry getQuerry() {
		querry.updateData();
		return querry;
	}

	public List<Message> getMessages() {
		List<String> messageJsons = Deamon.getInstance().getRedis().getList(String.valueOf(server.getId()));
		List<Message> messages = new ArrayList<>();
		for (String messageJson: messageJsons) {
			Message message = Deamon.getInstance().getGson().fromJson(messageJson, Message.class);
			messages.add(message);
		}
		return messages;
	}
	
	public void makeQuerry() {
		try {
			this.querry = new ServerQuerry(server.ip, server.querryPort);
			System.out.println(querry);
		} catch (IOException e) {
			//Ignore
		}
	}
	
	public int getRamUsage() {
		return ramUsage;
	}

	public void setRamUsage(int ramUsage) {
		this.ramUsage = ramUsage;
	}

	public int getRamTotal() {
		return ramTotal;
	}

	public void setRamTotal(int ramTotal) {
		this.ramTotal = ramTotal;
	}

	public double getRamPercent() {
		return ramPercent;
	}

	public void setRamPercent(double ramPercent) {
		this.ramPercent = ramPercent;
	}
	
	public String getRemoteAdd() {
		return remoteAdd;
	}

	public void setRemoteAdd(String remoteAdd) {
		this.remoteAdd = remoteAdd;
	}

	public int getPlayersCurrent() {
		querry.updateData();
		this.playersCurrent = querry.getCurrentPlayers();
		return playersCurrent;
	}
	
	public void updatePlayerCounts() {
		if(querry == null) {
			makeQuerry();
			return;
		}
		querry.updateData();
		this.playersCurrent = querry.getCurrentPlayers();
		this.playersTotal = querry.getMaxPlayers();
	}

	public int getPlayersTotal() {
		querry.updateData();
		this.playersTotal = querry.getMaxPlayers();
		return playersTotal;
	}
}
