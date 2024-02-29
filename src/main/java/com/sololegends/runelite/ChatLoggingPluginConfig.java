package com.sololegends.runelite;

import net.runelite.client.config.*;

@ConfigGroup("Chat Logging")
public interface ChatLoggingPluginConfig extends Config {
	public String GROUP = "Chat Logging";

	@ConfigSection(name = "Channel Select", description = "Enable/Disable chat channels", position = 0)
	public String CHANNEL_SECT = "Channel Select";
	@ConfigSection(name = "Logging Options", description = "Change Logging behaviour", position = 10)
	public String LOGGING_SECT = "Logging Options";

	@ConfigItem(section = CHANNEL_SECT, keyName = "game", name = "Game Chat", description = "Enable game chat logging")
	default boolean logGameChat() {
		return false;
	}

	@ConfigItem(section = CHANNEL_SECT, keyName = "public", name = "Public Chat", description = "Enable public chat logging")
	default boolean logPublicChat() {
		return false;
	}

	@ConfigItem(section = CHANNEL_SECT, keyName = "private", name = "Private Chat", description = "Enable private chat logging")
	default boolean logPrivateChat() {
		return true;
	}

	@ConfigItem(section = CHANNEL_SECT, keyName = "friends", name = "Friends Chat", description = "Enable friends chat logging")
	default boolean logFriendsChat() {
		return true;
	}

	@ConfigItem(section = CHANNEL_SECT, keyName = "clan", name = "Clan Chat", description = "Enable clan chat logging")
	default boolean logClanChat() {
		return true;
	}

	@ConfigItem(section = CHANNEL_SECT, keyName = "group_iron", name = "Group Iron Chat", description = "Enable logging of the group iron-man chat")
	default boolean logGroupChat() {
		return true;
	}

	@ConfigItem(section = LOGGING_SECT, keyName = "per_user", name = "Folder Per User", description = "Splits chats up into folders per logged in user")
	default boolean logChatPerUser() {
		return true;
	}

	@ConfigItem(section = LOGGING_SECT, keyName = "archive_count", name = "Archive Count", description = "Number of archived days of chat to save (0 for infinite)")
	default int archiveCount() {
		return 30;
	}
}
