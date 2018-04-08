package com.weeryan17.controller.deamon.listeners;

import java.util.logging.Level;

import com.weeryan17.controller.deamon.Deamon;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

public class LogListener extends Filter<ILoggingEvent> {
	Deamon instance;
	public LogListener() {
		instance = Deamon.getInstance();
	}

	@Override
	public FilterReply decide(ILoggingEvent e) {
		instance.getLogger().log(e.getFormattedMessage(), Level.parse(e.getLevel().levelStr));
		return FilterReply.NEUTRAL;
	}

}
