/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.plugins;

import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.command.Command;
import me.chayapak1.chomens_bot.command.CommandContext;
import me.chayapak1.chomens_bot.command.CommandException;
import me.chayapak1.chomens_bot.command.TrustLevel;
import me.chayapak1.chomens_bot.command.contexts.ChomeNSModCommandContext;
import me.chayapak1.chomens_bot.command.contexts.ConsoleCommandContext;
import me.chayapak1.chomens_bot.command.contexts.DiscordCommandContext;
import me.chayapak1.chomens_bot.command.contexts.PlayerCommandContext;
import me.chayapak1.chomens_bot.command.contexts.RemoteCommandContext;
import me.chayapak1.chomens_bot.commands.AuthCommand;
import me.chayapak1.chomens_bot.commands.BotVisibilityCommand;
import me.chayapak1.chomens_bot.commands.BruhifyCommand;
import me.chayapak1.chomens_bot.commands.ClearChatCommand;
import me.chayapak1.chomens_bot.commands.ClearChatQueueCommand;
import me.chayapak1.chomens_bot.commands.CloopCommand;
import me.chayapak1.chomens_bot.commands.CommandBlockCommand;
import me.chayapak1.chomens_bot.commands.ConsoleCommand;
import me.chayapak1.chomens_bot.commands.CowsayCommand;
import me.chayapak1.chomens_bot.commands.EchoCommand;
import me.chayapak1.chomens_bot.commands.EndCommand;
import me.chayapak1.chomens_bot.commands.EvalCommand;
import me.chayapak1.chomens_bot.commands.FilterCommand;
import me.chayapak1.chomens_bot.commands.FindAltsCommand;
import me.chayapak1.chomens_bot.commands.GrepLogCommand;
import me.chayapak1.chomens_bot.commands.HelpCommand;
import me.chayapak1.chomens_bot.commands.IPFilterCommand;
import me.chayapak1.chomens_bot.commands.InfoCommand;
import me.chayapak1.chomens_bot.commands.KickCommand;
import me.chayapak1.chomens_bot.commands.ListCommand;
import me.chayapak1.chomens_bot.commands.MailCommand;
import me.chayapak1.chomens_bot.commands.MusicCommand;
import me.chayapak1.chomens_bot.commands.NetCommandCommand;
import me.chayapak1.chomens_bot.commands.NetMessageCommand;
import me.chayapak1.chomens_bot.commands.RandomTeleportCommand;
import me.chayapak1.chomens_bot.commands.RefillCoreCommand;
import me.chayapak1.chomens_bot.commands.RestartCommand;
import me.chayapak1.chomens_bot.commands.ScreenshareCommand;
import me.chayapak1.chomens_bot.commands.SeenCommand;
import me.chayapak1.chomens_bot.commands.ServerEvalCommand;
import me.chayapak1.chomens_bot.commands.StopCommand;
import me.chayapak1.chomens_bot.commands.TPSBarCommand;
import me.chayapak1.chomens_bot.commands.TestCommand;
import me.chayapak1.chomens_bot.commands.TimeCommand;
import me.chayapak1.chomens_bot.commands.TranslateCommand;
import me.chayapak1.chomens_bot.commands.UUIDCommand;
import me.chayapak1.chomens_bot.commands.UrbanCommand;
import me.chayapak1.chomens_bot.commands.ValidateCommand;
import me.chayapak1.chomens_bot.commands.WeatherCommand;
import me.chayapak1.chomens_bot.commands.WhitelistCommand;
import me.chayapak1.chomens_bot.commands.WikipediaCommand;
import me.chayapak1.chomens_bot.data.chat.ChatPacketType;
import me.chayapak1.chomens_bot.data.listener.Listener;
import me.chayapak1.chomens_bot.util.ExceptionUtilities;
import me.chayapak1.chomens_bot.util.HashingUtilities;
import net.dv8tion.jda.api.entities.Member;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class CommandHandlerPlugin
implements Listener {
    public static final List<Command> COMMANDS = ObjectList.of(new Command[]{new CommandBlockCommand(), new CowsayCommand(), new EchoCommand(), new HelpCommand(), new TestCommand(), new ValidateCommand(), new MusicCommand(), new RandomTeleportCommand(), new BotVisibilityCommand(), new TPSBarCommand(), new NetMessageCommand(), new RefillCoreCommand(), new WikipediaCommand(), new UrbanCommand(), new ClearChatCommand(), new ListCommand(), new ServerEvalCommand(), new UUIDCommand(), new TimeCommand(), new BruhifyCommand(), new EndCommand(), new CloopCommand(), new WeatherCommand(), new TranslateCommand(), new ClearChatQueueCommand(), new FilterCommand(), new MailCommand(), new EvalCommand(), new InfoCommand(), new ConsoleCommand(), new ScreenshareCommand(), new WhitelistCommand(), new SeenCommand(), new IPFilterCommand(), new StopCommand(), new GrepLogCommand(), new FindAltsCommand(), new RestartCommand(), new NetCommandCommand(), new AuthCommand(), new KickCommand()});
    public boolean disabled = false;
    private final Bot bot;
    private final AtomicInteger commandsPerSecond = new AtomicInteger();

    public static Command findCommand(String searchTerm) {
        if (searchTerm.isBlank()) {
            return null;
        }
        for (Command command : COMMANDS) {
            if (!command.name.equals(searchTerm.toLowerCase()) && !Arrays.asList(command.aliases).contains(searchTerm.toLowerCase())) continue;
            return command;
        }
        return null;
    }

    public CommandHandlerPlugin(Bot bot) {
        this.bot = bot;
        bot.listener.addListener(this);
    }

    @Override
    public void onLocalSecondTick() {
        this.commandsPerSecond.set(0);
    }

    public void executeCommand(String input, CommandContext context) {
        String[] args2;
        if (this.commandsPerSecond.get() > 100) {
            return;
        }
        this.commandsPerSecond.getAndIncrement();
        boolean inGame = context instanceof PlayerCommandContext;
        boolean bypass = this.isBypassContext(context);
        String[] splitInput = input.trim().split("\\s+");
        if (splitInput.length == 0) {
            return;
        }
        String commandName = splitInput[0];
        Command command = CommandHandlerPlugin.findCommand(commandName);
        if (command == null) {
            if (!inGame) {
                context.sendOutput(Component.translatable("command_handler.unknown_command", (TextColor)NamedTextColor.RED, Component.text(commandName)));
            }
            return;
        }
        if (!bypass && !this.isCommandAllowed(context, command)) {
            return;
        }
        TrustLevel authenticatedTrustLevel = context.sender.persistingData.authenticatedTrustLevel;
        boolean authenticated = this.isAuthenticated(context, authenticatedTrustLevel);
        boolean needsHash = this.needsHash(command, inGame, authenticated, splitInput.length);
        String userHash = needsHash ? splitInput[1] : "";
        String[] fullArgs = Arrays.copyOfRange(splitInput, 1, splitInput.length);
        String[] stringArray = args2 = needsHash ? Arrays.copyOfRange(splitInput, 2, splitInput.length) : fullArgs;
        if (!this.checkTrustLevel(context, commandName, command, userHash, authenticated, bypass)) {
            return;
        }
        if (!bypass && command.consoleOnly) {
            context.sendOutput(Component.translatable("command_handler.console_only", (TextColor)NamedTextColor.RED));
            return;
        }
        context.fullArgs = fullArgs;
        context.args = args2;
        context.commandName = command.name;
        context.userInputCommandName = commandName;
        this.handleExecution(command, context, inGame);
    }

    private boolean isBypassContext(CommandContext context) {
        return context instanceof ConsoleCommandContext || context instanceof ChomeNSModCommandContext;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private boolean isAuthenticated(CommandContext context, TrustLevel authenticatedTrustLevel) {
        if (!(context instanceof PlayerCommandContext)) return false;
        PlayerCommandContext playerContext = (PlayerCommandContext)context;
        if (playerContext.packetType != ChatPacketType.PLAYER) return false;
        if (authenticatedTrustLevel == TrustLevel.PUBLIC) return false;
        return true;
    }

    private boolean needsHash(Command command, boolean inGame, boolean authenticated, int inputLength) {
        return command.trustLevel != TrustLevel.PUBLIC && inGame && !authenticated && inputLength >= 2;
    }

    private boolean checkTrustLevel(CommandContext context, String userCommandName, Command command, String userHash, boolean authenticated, boolean bypass) {
        TrustLevel requiredLevel = command.trustLevel;
        if (requiredLevel == TrustLevel.PUBLIC || bypass) {
            context.trustLevel = bypass ? TrustLevel.MAX : TrustLevel.PUBLIC;
            return true;
        }
        if (context instanceof RemoteCommandContext) {
            RemoteCommandContext remote = (RemoteCommandContext)context;
            if (remote.source.trustLevel.level < requiredLevel.level) {
                context.sendOutput(Component.translatable("command_handler.not_enough_roles", (TextColor)NamedTextColor.RED));
                return false;
            }
            context.trustLevel = remote.source.trustLevel;
            return true;
        }
        if (context instanceof DiscordCommandContext) {
            DiscordCommandContext discord = (DiscordCommandContext)context;
            Member member = discord.member;
            if (member == null) {
                return false;
            }
            TrustLevel userLevel = TrustLevel.fromDiscordRoles(member.getRoles());
            if (userLevel.level < requiredLevel.level) {
                context.sendOutput(Component.translatable("command_handler.not_enough_roles.trust_level", (TextColor)NamedTextColor.RED, Component.text(requiredLevel.name())));
                return false;
            }
            context.trustLevel = userLevel;
            return true;
        }
        if (authenticated) {
            TrustLevel authLevel = context.sender.persistingData.authenticatedTrustLevel;
            if (authLevel.level < requiredLevel.level) {
                context.sendOutput(Component.translatable("command_handler.not_enough_roles", (TextColor)NamedTextColor.RED));
                return false;
            }
            context.trustLevel = authLevel;
            return true;
        }
        TrustLevel hashLevel = HashingUtilities.getTrustLevel(userHash, userCommandName, context.sender);
        if (hashLevel.level < requiredLevel.level) {
            context.sendOutput(Component.translatable("command_handler.invalid_hash", (TextColor)NamedTextColor.RED, Component.text(requiredLevel.toString())));
            return false;
        }
        context.trustLevel = hashLevel;
        return true;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private boolean isCommandAllowed(CommandContext context, Command command) {
        if (this.disabled) {
            context.sendOutput(Component.translatable("command_handler.disabled", (TextColor)NamedTextColor.RED));
            return false;
        }
        if (!(context instanceof PlayerCommandContext)) return true;
        PlayerCommandContext playerContext = (PlayerCommandContext)context;
        if (command.disallowedPacketTypes == null) return true;
        if (Arrays.asList(command.disallowedPacketTypes).contains((Object)playerContext.packetType)) return false;
        return true;
    }

    private void handleExecution(Command command, CommandContext context, boolean inGame) {
        try {
            Component output = command.execute(context);
            if (output != null) {
                context.sendOutput(output);
            }
        }
        catch (CommandException e) {
            context.sendOutput(e.message.colorIfAbsent(NamedTextColor.RED));
        }
        catch (Exception e) {
            this.bot.logger.error(e);
            String stackTrace = ExceptionUtilities.getStacktrace(e);
            if (!inGame || this.bot.options.useChat || !this.bot.options.useCore) {
                context.sendOutput(Component.text(e.toString(), (TextColor)NamedTextColor.RED));
            }
            context.sendOutput((Component)Component.translatable("command_handler.exception", (TextColor)NamedTextColor.RED).hoverEvent(HoverEvent.showText(Component.text(stackTrace, (TextColor)NamedTextColor.RED))));
        }
    }
}

