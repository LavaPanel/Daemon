package com.weeryan17.controller.deamon.util;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Logging {
	Logger logger;
	public Logging() {
		logger = Logger.getLogger("tk.weeryan17");
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
			//TODO send to site
		}
		logger.log(level, message);
	}
	
	public void log(String message, Level level, Exception error) {
		if (level.intValue() > Level.INFO.intValue() && level != Level.OFF) {
			//TODO send to site
		}
		logger.log(level, message, error);
	}
}
