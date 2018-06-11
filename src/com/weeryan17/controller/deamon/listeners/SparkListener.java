package com.weeryan17.controller.deamon.listeners;

import com.weeryan17.controller.deamon.Deamon;

import spark.Request;
import spark.Response;

public class SparkListener {
	Deamon instance;
	public SparkListener(Deamon instance) {
		this.instance = instance;
	}
	public void handle(Request request, Response response) throws Exception {
		instance.getLogger().log("Request for " + request.pathInfo() + " from " + request.ip());
	}

}
