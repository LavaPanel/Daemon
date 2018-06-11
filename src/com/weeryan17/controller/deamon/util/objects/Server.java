package com.weeryan17.controller.deamon.util.objects;

public class Server {
	transient int id;
	
	String ip;
	
	int port;
	
	int querryPort;

	Version version;
	
	String name;
	
	transient String dir;
	
	int ram;
	
	transient boolean running;
	
	transient ServerInfo info;

	public Server(int id, String ip, int port, int querryPort, Version version, String name, int ram) {
		this.id = id;
		this.ip = ip;
		this.port = port;
		this.version = version;
		this.name = name;
		this.ram = ram;
		this.querryPort = querryPort;
	}

	public int getRam() {
		return ram;
	}

	public Version getVersion() {
		return version;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public boolean isNull() {
		return (ip == null || port <= 0 || version == null || name == null || ram <= 0 || querryPort <= 0);
	}
	
	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public ServerInfo getInfo() {
		if (info == null) {
			if (running == true) {
				info = new ServerInfo(this);
			}
		}
		return info;
	}

	public void setInfo(ServerInfo info) {
		this.info = info;
	}

	public String getDir() {
		return dir;
	}

	public void setDir(String dir) {
		this.dir = dir;
	}
}
