/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.plugins;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.Configuration;
import me.chayapak1.chomens_bot.Main;
import me.chayapak1.chomens_bot.command.Command;
import me.chayapak1.chomens_bot.command.contexts.ConsoleCommandContext;
import me.chayapak1.chomens_bot.plugins.CommandHandlerPlugin;
import me.chayapak1.chomens_bot.util.ChatMessageUtilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.SelectorComponent;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.StyleSetter;
import net.kyori.adventure.text.renderer.TranslatableComponentRenderer;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.ParsedLine;

public class ConsolePlugin
implements Completer {
    private static final Path HISTORY_PATH = Path.of(".console_history", new String[0]);
    private static final ConsoleFormatRenderer RENDERER = new ConsoleFormatRenderer();
    private final List<Bot> allBots = Main.bots;
    public final LineReader reader;
    public String consoleServer = "all";
    private final String prefix;
    private final Component format;

    public ConsolePlugin(Configuration config) {
        this.format = GsonComponentSerializer.gson().deserialize(config.consoleChatFormat);
        this.prefix = config.consoleCommandPrefix;
        this.reader = LineReaderBuilder.builder().completer(this).variable("history-file", HISTORY_PATH).option(LineReader.Option.DISABLE_EVENT_EXPANSION, true).build();
        Thread thread2 = new Thread(() -> {
            while (true) {
                String line = null;
                try {
                    line = this.reader.readLine(this.getPrompt());
                }
                catch (Exception e) {
                    System.exit(1);
                }
                this.handleLine(line);
                try {
                    this.reader.getHistory().save();
                }
                catch (IOException iOException) {
                }
            }
        }, "Console Thread");
        thread2.start();
    }

    private String getPrompt() {
        return String.format("[%s] > ", this.consoleServer);
    }

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        if (!line.line().startsWith(this.prefix)) {
            return;
        }
        String command = line.line().substring(this.prefix.length());
        List<Command> commands = CommandHandlerPlugin.COMMANDS;
        List<String> commandNames = commands.stream().map(eachCommand -> eachCommand.name).toList();
        List<Candidate> filteredCommands = commandNames.stream().filter(eachCommand -> eachCommand.startsWith(command)).map(eachCommand -> new Candidate(this.prefix + eachCommand)).toList();
        candidates.addAll(filteredCommands);
    }

    private void handleLine(String line) {
        if (line == null) {
            return;
        }
        for (Bot bot : this.allBots) {
            String server = bot.getServerString(true);
            if (!server.equals(this.consoleServer) && !this.consoleServer.equalsIgnoreCase("all")) continue;
            if (line.startsWith(this.prefix)) {
                ConsoleCommandContext context = new ConsoleCommandContext(bot, this.prefix);
                bot.commandHandler.executeCommand(line.substring(this.prefix.length()), context);
                continue;
            }
            if (!bot.loggedIn) continue;
            Component stylizedMessage = ChatMessageUtilities.applyChatMessageStyling(line);
            Component rendered = RENDERER.render(this.format, new ConsoleFormatContext(bot.profile.getIdAsString(), stylizedMessage, Map.of("MESSAGE", line, "USERNAME", bot.profile.getName(), "OWNER_NAME", bot.config.ownerName)));
            bot.chat.tellraw(rendered);
        }
    }

    private static final class ConsoleFormatRenderer
    extends TranslatableComponentRenderer<ConsoleFormatContext> {
        private ConsoleFormatRenderer() {
        }

        @Override
        @NotNull
        protected Component renderSelector(@NotNull SelectorComponent component, @NotNull ConsoleFormatContext context) {
            String pattern = component.pattern();
            if (pattern.equals("@s")) {
                SelectorComponent.Builder builder = Component.selector().pattern(context.uuid());
                return this.mergeStyleAndOptionallyDeepRender(component, builder, context);
            }
            return super.renderSelector(component, context);
        }

        @Override
        @NotNull
        protected Component renderText(@NotNull TextComponent component, @NotNull ConsoleFormatContext context) {
            String content = component.content();
            if (content.equals("MESSAGE")) {
                return this.mergeMessage(component, context.message(), context);
            }
            String arg = context.args().get(component.content());
            if (arg != null) {
                TextComponent.Builder builder = Component.text().content(arg);
                return this.mergeStyleAndOptionallyDeepRender(component, builder, context);
            }
            return super.renderText(component, context);
        }

        @Override
        protected <B extends ComponentBuilder<?, ?>> void mergeStyle(Component component, B builder, ConsoleFormatContext context) {
            super.mergeStyle(component, builder, context);
            builder.clickEvent(this.mergeClickEvent(component.clickEvent(), context));
        }

        private Component mergeMessage(Component root, Component msg, ConsoleFormatContext context) {
            HoverEvent<?> hoverEvent;
            StyleSetter<Component> result = msg.applyFallbackStyle(root.style());
            ClickEvent clickEvent = result.clickEvent();
            if (clickEvent != null) {
                result = result.clickEvent(this.mergeClickEvent(clickEvent, context));
            }
            if ((hoverEvent = result.hoverEvent()) != null) {
                result = result.hoverEvent(hoverEvent.withRenderedValue(this, context));
            }
            return result;
        }

        private ClickEvent mergeClickEvent(ClickEvent clickEvent, ConsoleFormatContext context) {
            if (clickEvent == null) {
                return null;
            }
            if (!clickEvent.action().supports(ClickEvent.Payload.string(""))) {
                return clickEvent;
            }
            String value = ((ClickEvent.Payload.Text)clickEvent.payload()).value();
            String arg = context.args().get(value);
            if (arg == null) {
                return clickEvent;
            }
            return switch (clickEvent.action()) {
                case ClickEvent.Action.OPEN_URL -> ClickEvent.openUrl(arg);
                case ClickEvent.Action.OPEN_FILE -> ClickEvent.openFile(arg);
                case ClickEvent.Action.RUN_COMMAND -> ClickEvent.runCommand(arg);
                case ClickEvent.Action.SUGGEST_COMMAND -> ClickEvent.suggestCommand(arg);
                case ClickEvent.Action.COPY_TO_CLIPBOARD -> ClickEvent.copyToClipboard(arg);
                default -> clickEvent;
            };
        }
    }

    private record ConsoleFormatContext(String uuid, Component message, Map<String, String> args) {
    }
}

