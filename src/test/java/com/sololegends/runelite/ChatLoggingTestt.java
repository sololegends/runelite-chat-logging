package com.sololegends.runelite;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ChatLoggingTestt {
	public static void main(String[] args) throws Exception {
		ExternalPluginManager.loadBuiltin(ChatLoggingPlugin.class);
		RuneLite.main(args);
	}
}