/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.commands;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.command.Command;
import me.chayapak1.chomens_bot.command.CommandContext;
import me.chayapak1.chomens_bot.command.CommandException;
import me.chayapak1.chomens_bot.command.TrustLevel;
import me.chayapak1.chomens_bot.util.StringUtilities;
import me.chayapak1.chomens_bot.util.TimeUtilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class InfoCommand
extends Command {
    public static final String ORIGINAL_REPOSITORY_URL = "https://code.chipmunk.land/ChomeNS/chomens-bot-java";
    public static final Properties BUILD_INFO = new Properties();

    public InfoCommand() {
        super("info", new String[]{"", "creator", "discord", "server", "botuser", "botlogintime", "uptime"}, new String[]{"creator", "discord", "botuser", "botlogintime", "uptime"}, TrustLevel.PUBLIC);
    }

    @Override
    public Component execute(CommandContext context) throws CommandException {
        String action;
        context.checkOverloadArgs(1);
        Bot bot = context.bot;
        switch (action = !context.userInputCommandName.equalsIgnoreCase(this.name) ? context.userInputCommandName.toLowerCase() : context.getString(false, false, true)) {
            case "creator": {
                return Component.translatable("commands.info.creator.output", bot.colorPalette.defaultColor, Component.text("ChomeNS Bot", bot.colorPalette.primary), Component.text("chayapak", bot.colorPalette.ownerName));
            }
            case "discord": {
                String link = bot.config.discord.inviteLink;
                return Component.translatable("commands.info.discord.output", bot.colorPalette.defaultColor, Component.text(link, (TextColor)NamedTextColor.BLUE).clickEvent(ClickEvent.openUrl(link)));
            }
            case "server": {
                MemoryUsage heapUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
                OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
                StringBuilder builder = new StringBuilder();
                try {
                    RandomAccessFile file = new RandomAccessFile("/proc/cpuinfo", "r");
                    FileChannel channel = file.getChannel();
                    ByteBuffer buffer = ByteBuffer.allocate(0x100000);
                    long bytesRead = channel.read(buffer);
                    while (bytesRead != -1L) {
                        buffer.flip();
                        while (buffer.hasRemaining()) {
                            builder.append((char)buffer.get());
                        }
                        buffer.clear();
                        bytesRead = channel.read(buffer);
                    }
                    channel.close();
                    file.close();
                }
                catch (IOException file) {
                    // empty catch block
                }
                TextColor color = bot.colorPalette.string;
                String[] lines = builder.toString().split("\n");
                Optional<String> modelName = Arrays.stream(lines).filter(line -> line.startsWith("model name")).findFirst();
                Component cpuModel = modelName.map(s2 -> Component.text(s2.split("\t: ")[1], color)).orElseGet(() -> Component.text("N/A", color));
                InetAddress localHost = null;
                try {
                    localHost = InetAddress.getLocalHost();
                }
                catch (UnknownHostException unknownHostException) {
                    // empty catch block
                }
                TranslatableComponent component = Component.translatable("commands.info.server.output", bot.colorPalette.secondary, Component.text(localHost == null ? "N/A" : localHost.getHostName(), color), Component.text(System.getProperty("user.dir"), color), Component.text(os.getArch(), color), Component.text(os.getVersion(), color), Component.text(os.getName(), color), Component.text(String.valueOf(Runtime.getRuntime().availableProcessors()), color), cpuModel, Component.text(String.valueOf(Thread.activeCount()), color), Component.translatable("%s MB / %s MB", color, Component.text(heapUsage.getUsed() / 1024L / 1024L), Component.text(heapUsage.getMax() / 1024L / 1024L)), Component.translatable("%s MB / %s MB", color, Component.text((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024L / 1024L), Component.text(Runtime.getRuntime().totalMemory() / 1024L / 1024L)));
                return component;
            }
            case "botuser": {
                String username = bot.username;
                String uuid = bot.profile.getIdAsString();
                return Component.translatable("commands.info.botuser.output", bot.colorPalette.defaultColor, ((TextComponent)Component.text(username, bot.colorPalette.username).hoverEvent(HoverEvent.showText(Component.translatable("commands.generic.click_to_copy_username", (TextColor)NamedTextColor.GREEN)))).clickEvent(ClickEvent.copyToClipboard(username)), ((TextComponent)Component.text(uuid, bot.colorPalette.uuid).hoverEvent(HoverEvent.showText(Component.translatable("commands.generic.click_to_copy_uuid", (TextColor)NamedTextColor.GREEN)))).clickEvent(ClickEvent.copyToClipboard(uuid)));
            }
            case "botlogintime": {
                long loginTime = bot.loginTime;
                ZoneId zoneId = ZoneId.of("UTC");
                String formattedLoginTime = TimeUtilities.formatTime(loginTime, "MMMM d, yyyy, hh:mm:ss a Z", zoneId);
                SimpleDateFormat timeSinceFormatter = new SimpleDateFormat("HH 'hours' mm 'minutes' ss 'seconds'");
                timeSinceFormatter.setTimeZone(TimeZone.getTimeZone(zoneId));
                String formattedTimeSince = timeSinceFormatter.format(new Date(System.currentTimeMillis() - loginTime));
                return Component.translatable("commands.info.botlogintime.output", bot.colorPalette.defaultColor, Component.text(formattedLoginTime, bot.colorPalette.string), Component.text(formattedTimeSince, bot.colorPalette.string));
            }
            case "uptime": {
                long uptime = ManagementFactory.getRuntimeMXBean().getUptime() / 1000L;
                long days = TimeUnit.SECONDS.toDays(uptime);
                long hours = TimeUnit.SECONDS.toHours(uptime) - days * 24L;
                long minutes = TimeUnit.SECONDS.toMinutes(uptime) - TimeUnit.SECONDS.toHours(uptime) * 60L;
                long seconds = TimeUnit.SECONDS.toSeconds(uptime) - TimeUnit.SECONDS.toMinutes(uptime) * 60L;
                return Component.translatable("commands.info.uptime.output", bot.colorPalette.defaultColor, Component.translatable("%s %s, %s %s, %s %s, %s %s", (TextColor)NamedTextColor.GREEN, Component.text(days), Component.text(StringUtilities.addPlural(days, "day")), Component.text(hours), Component.text(StringUtilities.addPlural(hours, "hour")), Component.text(minutes), Component.text(StringUtilities.addPlural(minutes, "minute")), Component.text(seconds), Component.text(StringUtilities.addPlural(seconds, "second"))));
            }
        }
        return ((TranslatableComponent)((TranslatableComponent)((TranslatableComponent)((TranslatableComponent)((TranslatableComponent)((TranslatableComponent)Component.translatable("commands.info.default.main_output", bot.colorPalette.defaultColor, Component.text("ChomeNS Bot", (TextColor)NamedTextColor.YELLOW), Component.text("Kaboom").style(Style.style().color(NamedTextColor.GRAY).decorate(TextDecoration.BOLD))).append(Component.newline())).append(Component.translatable("commands.info.default.original_repository", bot.colorPalette.defaultColor, ((TextComponent)Component.text(ORIGINAL_REPOSITORY_URL).color(bot.colorPalette.string)).clickEvent(ClickEvent.openUrl(ORIGINAL_REPOSITORY_URL))))).append(Component.newline())).append(Component.translatable("commands.info.default.compiled_at", bot.colorPalette.defaultColor, Component.text(BUILD_INFO.getProperty("build.date", "unknown")).color(bot.colorPalette.string)))).append(Component.translatable("commands.info.default.git_commit", bot.colorPalette.defaultColor, Component.text(BUILD_INFO.getProperty("build.git.commit.hash", "unknown")).color(bot.colorPalette.string), Component.text(BUILD_INFO.getProperty("build.git.commit.count", "unknown")).color(bot.colorPalette.number)))).append(Component.newline())).append(Component.translatable("commands.info.default.build", bot.colorPalette.defaultColor, Component.text(BUILD_INFO.getProperty("build.number", "unknown")).color(bot.colorPalette.number)));
    }

    static {
        try (InputStream input = ClassLoader.getSystemClassLoader().getResourceAsStream("application.properties");){
            BUILD_INFO.load(input);
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }
}

