package com.weeryan17.controller.deamon.listeners;

import java.util.logging.Level;

import com.weeryan17.controller.deamon.Deamon;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

public class LoggingEvent extends AppenderBase<ILoggingEvent> {
	
	Deamon insatnce;
	
	public Deamon getInstance() {
		if(this.insatnce == null) {
			this.insatnce = Deamon.getInstance();
		}
		return this.insatnce;
	}
	
	@Override
	protected void append(ILoggingEvent e) {
		getInstance().getLogger().log(e.getLoggerName() + ": " + e.getFormattedMessage(), Level.parse(e.getLevel().levelStr));
	}
	
}
