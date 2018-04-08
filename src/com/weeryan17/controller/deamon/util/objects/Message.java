package com.weeryan17.controller.deamon.util.objects;

public class Message {
	MessageLevel level;
	
	String thread;
	
	String time;
	
	String message;
	
	public Message(MessageLevel level, String time, String thread, String message) {
		this.level = level;
		this.thread = thread;
		this.time = time;
		this.message = message;
	}
	
	public MessageLevel getLevel() {
		return level;
	}

	public String getThread() {
		return thread;
	}

	public String getTime() {
		return time;
	}

	public String getMessage() {
		return message;
	}

	public enum MessageLevel {
		DEBUG,
		INFO,
		WARN,
		ERROR,
		OTHER
	}
}
