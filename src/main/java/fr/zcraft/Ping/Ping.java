package fr.zcraft.Ping;

import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.components.i18n.I18n;
import fr.zcraft.zlib.components.rawtext.RawText;
import fr.zcraft.zlib.core.ZPlugin;
import fr.zcraft.zlib.tools.PluginLogger;
import fr.zcraft.zlib.tools.items.ItemStackBuilder;
import fr.zcraft.zlib.tools.reflection.NMSNetwork;
import fr.zcraft.zlib.tools.reflection.Reflection;
import fr.zcraft.zlib.tools.text.RawMessage;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;


public final class Ping extends ZPlugin
{
    private static Class<?> MinecraftServer = null;
    private static Class<?> CraftServer = null;

    static
    {
        try
        {
            MinecraftServer = Reflection.getMinecraftClassByName("MinecraftServer");
            CraftServer = Reflection.getBukkitClassByName("CraftServer");
        }
        catch (ClassNotFoundException e)
        {
            PluginLogger.error("Unable to load classes required to retrieve TPS; disabling TPS support.", e);
        }
    }

    @Override
    public void onEnable()
    {
        saveDefaultConfig();
        loadComponents(I18n.class, PingConfig.class);

        I18n.setPrimaryLocale(PingConfig.LOCALE.get());
    }

    /**
     * Returns a player's latency
     *
     * @param player The player.
     *
     * @return The latency in milli-seconds, or -1 if an error occurred while
     * retrieving it.
     */
    private int getPlayerLatency(final Player player)
    {
        if (player == null) return -1;

        try
        {
            return (int) Reflection.getFieldValue(NMSNetwork.getPlayerHandle(player), "ping");
        }
        catch (InvocationTargetException | IllegalAccessException | NoSuchFieldException e)
        {
            PluginLogger.error("Unable to retrieve {0}'s latency.", e, player.getName());
            return -1;
        }
    }

    /**
     * Retrieves the server's TPS.
     *
     * @return An array of three {@link Double doubles} representing the ticks
     * per seconds of the last minute, 5 minutes and 15 minutes.
     */
    private double[] getServerTPS()
    {
        if (CraftServer == null || MinecraftServer == null) return null;

        try
        {
            Object nmsServer = Reflection.call(CraftServer, Bukkit.getServer(), "getServer");
            return (double[]) Reflection.getFieldValue(MinecraftServer, nmsServer, "recentTps");
        }
        catch (InvocationTargetException | IllegalAccessException | NoSuchFieldException | NoSuchMethodException e)
        {
            PluginLogger.error("Unable to retrieve the last ticks per second.", e);
            return null;
        }
    }

    private String formatLatency(int latency)
    {
        StringBuilder builder = new StringBuilder();

        if (latency > 350)
            builder.append(ChatColor.RED);
        else if (latency > 150)
            builder.append(ChatColor.YELLOW);
        else
            builder.append(ChatColor.GREEN);

        return builder.append(latency).append(" ms").toString();
    }

    private String formatTPS(double[] tps)
    {
        String[] formattedTPS = new String[tps.length];

        for (int i = 0, tpsLength = tps.length; i < tpsLength; i++)
        {
            final double tps_value = tps[i];
            final ChatColor color = tps_value > 18.0 ? ChatColor.GREEN : tps_value > 16.0 ? ChatColor.YELLOW : ChatColor.RED;

            formattedTPS[i] = color + (tps_value >= 20.0 ? "*" : "") + Math.min(Math.round(tps_value * 100.0) / 100.0, 20.0);
        }

        return StringUtils.join(formattedTPS, ChatColor.GRAY + ", " + ChatColor.RESET);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        final Player target;

        if (args.length > 0)
        {
            String playerName = args[0].trim();
            Player playerFound = null;

            for (Player player : Bukkit.getOnlinePlayers())
            {
                if (player.getName().equalsIgnoreCase(playerName))
                {
                    playerFound = player;
                    break;
                }
            }

            if (playerFound == null)
            {
                sender.sendMessage(I.t("{ce}Player {0} not found.", playerName));
                return true;
            }
            else target = playerFound;
        }
        else if (sender instanceof Player)
        {
            target = (Player) sender;
        }
        else if (label.equalsIgnoreCase("ping"))
        {
            sender.sendMessage(I.t("{green}Pong!"));
            return true;
        }
        else
        {
            sender.sendMessage(I.t("{ce}You cannot execute this command from the console."));
            return true;
        }


        final boolean isSelf = sender instanceof Player && ((Player) sender).getUniqueId().equals(target.getUniqueId());

        final int latency = getPlayerLatency(target);
        final double[] tps = getServerTPS();


        sender.sendMessage(ChatColor.GRAY + "⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅");

        if (!isSelf)
        {
            sender.sendMessage(I.t("{green}{bold}Ping, {0}!", target.getName()));
            sender.sendMessage("");
        }
        else if (label.equalsIgnoreCase("ping"))
        {
            sender.sendMessage(I.t("{green}{bold}Pong!"));
            sender.sendMessage("");
        }

        RawMessage.send(sender, new RawText("")
                        .then(I.t("Latency: "))
                        .color(ChatColor.GOLD)
                        .hover(
                                new ItemStackBuilder(Material.DIAMOND)
                                        .title(ChatColor.BOLD + I.t("Latency"))
                                        .longLore(ChatColor.RESET, I.t("The time needed to transfer data from you to the server."), 38)
                                        .loreLine(ChatColor.GREEN, I.t("The lower the better."))
                                .item()
                        )

                        .then(latency != -1 ? formatLatency(latency) : I.t("{gray}(unable to retrieve latency)"))

                        .build()
        );

        RawMessage.send(sender, new RawText("")
                        .then(I.t("Server load: "))
                        .color(ChatColor.GOLD)
                        .hover(
                                new ItemStackBuilder(Material.DIAMOND)
                                        .title(ChatColor.BOLD + I.t("Ticks per second"))
                                        .longLore(ChatColor.RESET, I.t("The number of cycles the server executes per second. The best is 20; under 15, the server is experiencing difficulties."), 38)
                                        .longLore(ChatColor.GRAY, I.t("The three values are the average number of TPS during the last 1, 5 and 15 minutes."), 38)
                                        .loreLine(ChatColor.GREEN, I.t("The closest to 20 the better."))
                                        .item()
                        )

                        .then(tps != null ? formatTPS(tps) : I.t("{gray}(unable to retrieve server load)"))

                        .build()
        );

        sender.sendMessage(ChatColor.GRAY + "⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅");

        return true;
    }
}
