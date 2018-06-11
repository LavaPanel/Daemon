package com.weeryan17.controller.deamon.util.objects;

import java.io.IOException;
import java.util.List;

import ch.jamiete.mcping.MinecraftPing;
import ch.jamiete.mcping.MinecraftPingOptions;
import ch.jamiete.mcping.MinecraftPingReply;
import ch.jamiete.mcping.MinecraftPingReply.Player;

public class ServerQuerry {
	String address;
	int port;
	MinecraftPingReply data;
	public ServerQuerry(String address, int port) throws IOException {
		data = new MinecraftPing().getPing(new MinecraftPingOptions().setHostname(address).setPort(port));
		this.address = address;
		this.port = port;
	}
	
	public void updateData() {
		MinecraftPingReply oldData = data;
		try {
			data = new MinecraftPing().getPing(new MinecraftPingOptions().setHostname(address).setPort(port));
		} catch (IOException e) {
			data = oldData;
		}
	}
	
	public List<Player> getPlayers() {
		return data.getPlayers().getSample();
	}
	
	public int getCurrentPlayers() {
		return data.getPlayers().getOnline();
	}
	
	public int getMaxPlayers() {
		return data.getPlayers().getMax();
	}
	
	public String getVersion() {
		return data.getVersion().getName();
	}
	
	public String getMOTD() {
		return data.getDescription().getText();
	}
}
