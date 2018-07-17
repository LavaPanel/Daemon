package com.weeryan17.controller.deamon.util;

import java.util.HashMap;
import java.util.Map;

public class ProcessUtil {
	
	private static Map<Long, ProcessHandle.Info> previous = new HashMap<>();
	
	public static double getCpuUsageSenceLastCheck(long pid, ProcessHandle.Info info) {
		if(previous.containsKey(pid)) {
			ProcessHandle.Info old = previous.get(pid);
			long runtime = getRuntime(info) - getRuntime(old);
			long usage = info.totalCpuDuration().get().toMillis() - old.totalCpuDuration().get().toMillis();
			return usage/runtime;
		} else {
			long runtime = getRuntime(info);
			long usage = info.totalCpuDuration().get().toMillis();
			return usage/runtime;
		}
	}
	
	private static long getRuntime(ProcessHandle.Info info) {
		long startMills = info.startInstant().get().toEpochMilli();
		long currentMills = System.currentTimeMillis();
		return currentMills - startMills;
	}
	
	public static void stoped(long pid) {
		previous.remove(pid);
	}
}
