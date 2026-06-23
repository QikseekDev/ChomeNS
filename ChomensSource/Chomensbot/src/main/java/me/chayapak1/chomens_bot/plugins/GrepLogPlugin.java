/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.plugins;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.Main;
import me.chayapak1.chomens_bot.command.CommandContext;
import me.chayapak1.chomens_bot.command.CommandException;
import me.chayapak1.chomens_bot.util.FileLoggerUtilities;
import me.chayapak1.chomens_bot.util.StringUtilities;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public class GrepLogPlugin {
    private final Bot bot;
    public Pattern pattern;
    private int count = 0;
    public boolean running = false;

    public GrepLogPlugin(Bot bot) {
        this.bot = bot;
    }

    public void search(CommandContext context, String input, boolean ignoreCase, boolean regex) throws CommandException {
        this.running = true;
        try (Stream<Path> files = Files.list(FileLoggerUtilities.logDirectory);){
            Path[] fileList = (Path[])files.toArray(Path[]::new);
            Arrays.sort(fileList, Comparator.comparing(a -> a.getFileName().toString()));
            StringBuilder result = new StringBuilder();
            for (Path filePath : fileList) {
                BufferedReader bufferedReader;
                if (!this.running) {
                    this.pattern = null;
                    return;
                }
                if (this.count > 1000000) break;
                String fileName = filePath.getFileName().normalize().toString();
                String absolutePath = filePath.toAbsolutePath().normalize().toString();
                if (fileName.endsWith(".txt.gz")) {
                    try (GZIPInputStream gzipInputStream = new GZIPInputStream(new FileInputStream(absolutePath));){
                        bufferedReader = new BufferedReader(new InputStreamReader((InputStream)gzipInputStream, StandardCharsets.UTF_8));
                        try {
                            result.append((CharSequence)this.process(bufferedReader, input, ignoreCase, regex));
                            continue;
                        }
                        finally {
                            bufferedReader.close();
                        }
                    }
                }
                if (!fileName.endsWith(".txt")) continue;
                try (FileInputStream fileInputStream = new FileInputStream(absolutePath);){
                    bufferedReader = new BufferedReader(new InputStreamReader((InputStream)fileInputStream, StandardCharsets.UTF_8));
                    try {
                        result.append((CharSequence)this.process(bufferedReader, input, ignoreCase, regex));
                    }
                    finally {
                        bufferedReader.close();
                    }
                }
            }
            this.pattern = null;
            this.count = 0;
            String stringifiedResult = result.toString();
            long matches = stringifiedResult.lines().count();
            if (matches == 0L) {
                throw new CommandException(Component.translatable("commands.greplog.error.no_matches_found"));
            }
            String channelId = this.bot.options.discordChannelId;
            if (channelId == null) {
                return;
            }
            TextChannel logChannel = Main.discord.jda.getTextChannelById(channelId);
            if (logChannel == null) {
                return;
            }
            ((MessageCreateAction)logChannel.sendMessage("Greplog result:").addFiles(FileUpload.fromData(StringUtilities.truncateToFitUtf8ByteLength(stringifiedResult, 8000000).getBytes(StandardCharsets.UTF_8), String.format("result-%d.txt", System.currentTimeMillis() / 1000L)))).queue(message -> {
                String url = message.getAttachments().getFirst().getUrl();
                DecimalFormat formatter = new DecimalFormat("#,###");
                TranslatableComponent component = ((TranslatableComponent)Component.translatable("commands.greplog.found").color(this.bot.colorPalette.defaultColor)).arguments(Component.text(formatter.format(matches), this.bot.colorPalette.number), Component.text(input, this.bot.colorPalette.string), ((TranslatableComponent)Component.translatable("commands.greplog.here").color(NamedTextColor.GREEN)).clickEvent(ClickEvent.openUrl(url)));
                context.sendOutput(component);
            });
        }
        catch (CommandException e) {
            this.running = false;
            throw e;
        }
        catch (FileNotFoundException e) {
            this.running = false;
            throw new CommandException(Component.text("File not found"));
        }
        catch (NotDirectoryException e) {
            this.running = false;
            throw new CommandException(Component.text("Logger directory is not a directory"));
        }
        catch (PatternSyntaxException e) {
            this.running = false;
            throw new CommandException(Component.text("Pattern is invalid"));
        }
        catch (IOException e) {
            this.running = false;
            throw new CommandException(Component.text("An I/O error has occurred"));
        }
        catch (Exception e) {
            this.bot.logger.error(e);
        }
        this.running = false;
    }

    private StringBuilder process(BufferedReader bufferedReader, String input, boolean ignoreCase, boolean regex) throws IOException, PatternSyntaxException {
        String line;
        if (regex && this.pattern == null) {
            this.pattern = ignoreCase ? Pattern.compile(input, 258) : Pattern.compile(input, 256);
        }
        StringBuilder result = new StringBuilder();
        while ((line = bufferedReader.readLine()) != null) {
            if (!(regex && this.pattern.matcher(line).find() || !ignoreCase && !regex && line.contains(input)) && (!ignoreCase || !StringUtilities.containsIgnoreCase(line, input))) continue;
            result.append(line).append("\n");
            ++this.count;
        }
        return result;
    }
}

