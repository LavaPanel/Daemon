package com.weeryan17.controller.deamon.util.redis;

import java.util.List;
import java.util.logging.Level;

import com.weeryan17.controller.deamon.Deamon;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisController {
	Deamon instance;

	public RedisController(Deamon instance) {
		this.instance = instance;
		this.buildRedis();
	}
	
	Jedis jedis;
	private JedisPool jedisPool;

	public void buildRedis() {
		int port;
		if (instance.getConfig().getElement("redis.server.port").isPresent()) {
			port = instance.getConfig().getInteger("redis.server.port").getAsInt();
		} else {
			port = 6379;
		}
		if (instance.getConfig().getString("redis.pass").get().isEmpty()) {
			jedisPool = new JedisPool(new JedisPoolConfig(),
					instance.getConfig().getString("redis.server.address").get(), port, 3000);
		} else {
			jedisPool = new JedisPool(new JedisPoolConfig(),
					instance.getConfig().getString("redis.server.address").get(), port, 3000,
					instance.getConfig().getString("redis.pass").get());
		}
		try {
			jedis = jedisPool.getResource();
			String responce = jedis.ping();
			if (!responce.equals("PONG")) {
				instance.getLogger().log("Didn't get pong from redis: " + responce, Level.SEVERE);
				System.exit(1);
				return;
			}
		} catch (Exception e) {
			instance.getLogger().log("Error building redis", Level.SEVERE, e);
			System.exit(1);
			return;
		}
	}

	public JedisPool getJedisPool() {
		return jedisPool;
	}

	public void set(String key, String value) {
		jedis.set(key, value);
	}

	public String get(String key) {
		return jedis.get(key);
	}
	
	public void listAdd(String list, String value) {
		jedis.lpush(list, value);
	}
	
	public List<String> getList(String list) {
		return jedis.lrange(list, 0, 50);
	}
	
	public void delete(String key) {
		jedis.del(key);
	}
}
