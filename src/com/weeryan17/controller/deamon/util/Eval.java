package com.weeryan17.controller.deamon.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import com.weeryan17.controller.deamon.Deamon;

public class Eval implements Runnable {
	private static ScriptEngineManager manager = new ScriptEngineManager();
	private final List<String> imports = Arrays.asList("com.weeryan17.controller.deamon",
			"java.util",
			"com.google.gson",
			"java.util.logging");
	
	final Deamon instance;
	final String eval;
	final EvalRunnable runnable;
	
	public Eval(String eval, EvalRunnable runnable) {
		this.eval = eval;
		this.instance = Deamon.getInstance();
		this.runnable = runnable;
	}
	
	public Thread start() {
		Thread thread = new Thread(this);
		thread.start();
		return thread;
	}
	
	@Override
	public void run() {
		ScriptEngine engine = manager.getEngineByName("groovy");
		String importsString = imports.stream().map(s -> "import " + s + ".*;").collect(Collectors.joining("\n"));
		engine.put("instance", instance);
		
		String eResult;
		try {
			eResult = String.valueOf(engine.eval(importsString + '\n' + eval));
		} catch (Exception e) {
			eResult = GeneralUtil.errorString(e);
		}
		runnable.run(eResult);
	}
	
	public interface EvalRunnable {
		public void run(String results);
	}
	
}
