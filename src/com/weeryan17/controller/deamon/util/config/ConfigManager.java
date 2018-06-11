package com.weeryan17.controller.deamon.util.config;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import io.github.binaryoverload.JSONConfig;

public class ConfigManager {
	List<String> required = new ArrayList<>();
	
	JSONConfig config;
	
	public void addRequirement(String... paths) {
		for(String path: paths) {
			required.add(path);
		}
	}
	
	public void addRequirement(Collection<?extends String> paths) {
		required.addAll(paths);
	}
	
	public void load(String file) throws ConfigMissingElementsException, FileNotFoundException, NullPointerException {
		config = new JSONConfig(file);
		List<String> missing = new ArrayList<>();
		for(String requirement: required) {
			if(config.getElement(requirement) == null) {
				missing.add(requirement);
			} else {
				if(!config.getElement(requirement).isPresent()) {
					missing.add(requirement);
				}
			}
		}
		
		if(missing.size() > 0) {
			throw new ConfigMissingElementsException("Missing elements", missing);
		}
		
	}
	
	public JSONConfig getConfig() throws ConfigNotLoadedException {
		if(config == null) throw new ConfigNotLoadedException("Config isn't loaded");
		return config;
	}
	
	
	public BooleanGroup getBooleanGroup(String path) throws ConfigNotLoadedException, ConfigMissingElementsException {
		if(config == null) throw new ConfigNotLoadedException("Config isn't loaded");
		boolean exists = true;
		if(config.getElement(path) == null) {
			exists = false;
		} else {
			if(!config.getElement(path).isPresent()) {
				exists = false;
			} else {
				if(config.getArray(path).get() == null) {
					exists = false;
				}
			}
		}
		if(!exists) {
			List<String> missing = new ArrayList<>();
			missing.add(path);
			throw new ConfigMissingElementsException("Missing elements", missing);
		}
		return null;
	}
	
	public class BooleanGroup {
		List<String> present = new ArrayList<>();
		
		public BooleanGroup(String path, JSONConfig config) {
			if(config.getArray(path).isPresent()) {
				JsonArray options = config.getArray(path).get();
				for(JsonElement elm : options) {
					if(elm.isJsonPrimitive()) {
						JsonPrimitive prim = elm.getAsJsonPrimitive();
						if(prim.isString()) {
							present.add(prim.getAsString());
						}
					}
				}
			}
		}
		
		public boolean getOption(String option) {
			return present.contains(option);
		}
	}
	
	//Exceptions
	
	public class ConfigMissingElementsException extends Exception {
		private static final long serialVersionUID = 1L;
		
		List<String> missing;
		
		public ConfigMissingElementsException(String message, List<String> missing) {
			super(message);
			this.missing = missing;
		}
		
		public List<String> getMissing() {
			return missing;
		}

	}
	
	public class ConfigNotLoadedException extends NullPointerException {
		private static final long serialVersionUID = 1L;
		
		public ConfigNotLoadedException(String message) {
			super(message);
		}
	}

}
