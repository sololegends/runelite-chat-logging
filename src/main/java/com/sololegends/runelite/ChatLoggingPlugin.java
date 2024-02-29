package com.sololegends.runelite;

import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provides;

import ch.qos.logback.classic.*;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;

@Slf4j
@PluginDescriptor(name = "Chat Logging", description = "Logs clan, Friend, Group, Game, and Public chat messages to a folder", tags = {
		"chat", "log", "clan", "group", "friends" })
public class ChatLoggingPlugin extends Plugin {

	private static final String BASE_DIRECTORY = RuneLite.RUNELITE_DIR + "/chatlogs/";

	@Inject
	private Client client;

	@Inject
	private ChatLoggingPluginConfig config;

	// Logger instances
	private Logger public_logger;
	private Logger private_logger;
	private Logger friends_logger;
	private Logger clan_logger;
	private Logger group_logger;
	private Logger game_logger;

	private boolean can_load = false;

	@Override
	protected void startUp() throws Exception {
		log.info("Starting Chat Logging");
	}

	@Override
	protected void shutDown() throws Exception {
		log.info("Stopping Chat Logging!");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event) {
		if (event.getGameState().equals(GameState.LOGGED_IN)) {
			// SO this actually fires BEFORE the player is fully logged in.. sooo we know it
			// is about to happen
			triggerInit();
		}
	}

	@Subscribe
	public void onGameTick(GameTick tick) {
		// If we are logging per player, wait until we have the player login name
		if (can_load && (!config.logChatPerUser() || client.getLocalPlayer().getName() != null)) {
			initLoggers();
			can_load = false;
		}
	}

	private void triggerInit() {
		can_load = true;
	}

	private void initLoggers() {
		public_logger = initLogger("public");
		private_logger = initLogger("private");
		friends_logger = initLogger("friends");
		clan_logger = initLogger("clan");
		group_logger = initLogger("group");
		game_logger = initLogger("game");
	}

	private Logger initLogger(String sub_dir) {
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

		PatternLayoutEncoder encoder = new PatternLayoutEncoder();
		encoder.setContext(context);
		encoder.setPattern("%d{HH:mm:ss} %msg%n");
		encoder.start();

		String directory = BASE_DIRECTORY;

		if (config.logChatPerUser()) {
			directory += client.getLocalPlayer().getName() + "/";
		}

		directory += sub_dir + "/";

		RollingFileAppender<ILoggingEvent> appender = new RollingFileAppender<>();
		appender.setFile(directory + "latest.log");
		appender.setAppend(true);
		appender.setEncoder(encoder);
		appender.setContext(context);

		TimeBasedRollingPolicy<ILoggingEvent> logFilePolicy = new TimeBasedRollingPolicy<>();
		logFilePolicy.setContext(context);
		logFilePolicy.setParent(appender);
		logFilePolicy.setFileNamePattern(directory + "chatlog_%d{yyyy-MM-dd}.log");
		logFilePolicy.setMaxHistory(config.archiveCount());
		logFilePolicy.start();

		appender.setRollingPolicy(logFilePolicy);
		appender.start();

		Logger logger = context.getLogger("logger_" + sub_dir);
		logger.detachAndStopAllAppenders();
		logger.setAdditive(false);
		logger.setLevel(Level.INFO);
		logger.addAppender(appender);

		return logger;
	}

	@Subscribe
	public void onChatMessage(ChatMessage event) {
		switch (event.getType()) {
			case CLAN_GIM_CHAT:
			case CLAN_GIM_MESSAGE:
			case CLAN_GIM_FORM_GROUP:
			case CLAN_GIM_GROUP_WITH:
				if (config.logGroupChat()) {
					if (event.getType() == ChatMessageType.CLAN_GIM_MESSAGE) {
						group_logger.info("{}", event.getMessage());
					} else {
						group_logger.info("{}: {}", nameFormatting(event.getName()), event.getMessage());
					}
				}

			case FRIENDSCHAT:
				if (config.logFriendsChat()) {
					friends_logger.info("[{}] {}: {}", event.getSender(), nameFormatting(event.getName()),
							event.getMessage());
				}

				break;

			case GAMEMESSAGE:
				if (config.logGameChat()) {
					game_logger.info(event.getMessage());
				}

				break;
			case CLAN_CHAT:
			case CLAN_GUEST_CHAT:
			case CLAN_MESSAGE:
			case CLAN_GUEST_MESSAGE:
				if (config.logClanChat()) {
					if (event.getType() == ChatMessageType.CLAN_MESSAGE) {
						clan_logger.info("{}", event.getMessage());
					} else {
						clan_logger.info("{}: {}", nameFormatting(event.getName()), event.getMessage());
					}
				}

				break;
			case PRIVATECHAT:
			case MODPRIVATECHAT:
			case PRIVATECHATOUT:
				if (config.logPrivateChat()) {
					String predicate = event.getType() == ChatMessageType.PRIVATECHATOUT ? "To" : "From";
					private_logger.info("{} {}: {}", predicate, nameFormatting(event.getName()), event.getMessage());
				}
				break;
			case MODCHAT:
			case PUBLICCHAT:
				if (config.logPublicChat()) {
					public_logger.info("{}: {}", nameFormatting(event.getName()), event.getMessage());
				}
				break;
		}
	}

	private String nameFormatting(String name) {
		return Text.removeFormattingTags(Text.toJagexName(name));
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event) {
		if (!ChatLoggingPluginConfig.GROUP.equals(event.getGroup())) {
			return;
		}
		// If we need to reload loggers
		if (event.getKey().equals("per_user") || event.getKey().equals("archive_count")) {
			triggerInit();
		}
	}

	@Provides
	ChatLoggingPluginConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(ChatLoggingPluginConfig.class);
	}
}
