package com.weeryan17.controller.deamon.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.impl.DefaultFtpServerContext;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;

import com.weeryan17.controller.deamon.Deamon;

public class FtpManager {
	DefaultFtpServerContext context;
	UserManager userManager;
	FtpServerFactory factory;
	FtpServer server;
	
	Deamon instance;
	
	public FtpManager(Deamon instance) {
		this.instance = instance;
	}
	
	public void init() {
		context = new DefaultFtpServerContext();
		userManager = context.getUserManager();
		BaseUser adminUser = new BaseUser();
		adminUser.setName("admin");
		adminUser.setPassword(instance.getConfig().getString("ftp.pass").get());
		adminUser.setEnabled(true);
		List<Authority> athorities = new ArrayList<>();
		athorities.add(new WritePermission());
		adminUser.setAuthorities(athorities);
		
		File adminUserHome = new File("servers");
		adminUser.setHomeDirectory(adminUserHome.getPath());
		adminUser.setMaxIdleTime(0);
		try {
			userManager.save(adminUser);
		} catch (FtpException e) {
			instance.getLogger().log("Error adding admin ftp user", Level.WARNING, e);
		}

		FtpServerFactory factory = new FtpServerFactory();
		factory.setUserManager(userManager);

		ListenerFactory listenFactory = new ListenerFactory();
		listenFactory.setPort(instance.getConfig().getInteger("ftp.port").getAsInt());
		factory.addListener("default", listenFactory.createListener());

		server = factory.createServer();
	}
	
	public void start() throws FtpException {
		server.start();
	}
	
	public void stop() {
		server.stop();
	}
	
	public void addUser(String user, String pass, String serverDir) {
		BaseUser adminUser = new BaseUser();
		adminUser.setName(user);
		adminUser.setPassword(pass);
		adminUser.setEnabled(true);
		List<Authority> athorities = new ArrayList<>();
		athorities.add(new WritePermission());
		adminUser.setAuthorities(athorities);
		
		File adminUserHome = new File("servers/" + serverDir);
		adminUser.setHomeDirectory(adminUserHome.getPath());
		adminUser.setMaxIdleTime(0);
		try {
			userManager.save(adminUser);
		} catch (FtpException e) {
			instance.getLogger().log("Error adding " + user + " ftp user", Level.WARNING, e);
		}
	}
}
