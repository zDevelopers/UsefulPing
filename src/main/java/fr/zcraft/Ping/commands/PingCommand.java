/*
 * Copyright or © or Copr. AmauryCarrade (2015)
 * 
 * http://amaury.carrade.eu
 * 
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software.  You can  use, 
 * modify and/ or redistribute the software under the terms of the CeCILL-B
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info". 
 * 
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability. 
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or 
 * data to be ensured and,  more generally, to use and operate it in the 
 * same conditions as regards security. 
 * 
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */
package fr.zcraft.Ping.commands;

import fr.zcraft.Ping.Pinger;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.components.rawtext.RawText;
import fr.zcraft.zlib.tools.text.RawMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class PingCommand implements CommandExecutor
{
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

        final int latency = Pinger.getPlayerLatency(target);
        final double[] tps = Pinger.getServerTPS();


        if (sender instanceof Player)
            sender.sendMessage(ChatColor.GRAY + "⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅");

        if (!isSelf)
        {
            sender.sendMessage(" " + I.t("{green}{bold}{0}'s ping", target.getName()));
            sender.sendMessage("");
        }
        else if (label.toLowerCase().endsWith("ping"))
        {
            if (sender.hasPermission("ping.toggleping"))
                RawMessage.send(sender, new RawText(" " + I.t("{green}{bold}Pong!"))
                        .then("  ")
                        .then(I.t("(keep displayed)"))
                            .color(ChatColor.GRAY)
                            .hover(new RawText(I.t("Click here to display your ping continuously")))
                            .command("/toggleping")
                        .build()
                );
            else
                sender.sendMessage(" " + I.t("{green}{bold}Pong!"));

            sender.sendMessage("");
        }

        RawMessage.send(sender, new RawText(" ")
                        .then(I.t("Latency: "))
                            .color(ChatColor.GOLD)
                            .hover(
                                    new RawText()
                                    .then(I.t("Latency")).style(ChatColor.BOLD).then("\n")
                                    .then(
                                            isSelf
                                                    ? I.t("The time needed to transfer data from you to the server.")
                                                    : I.t("The time needed to transfer data from {0} to the server.", target.getName())
                                    ).then("\n")
                                    .then(I.t("The lower the better.")).color(ChatColor.GREEN)
                            )

                        .then(latency != -1 ? Pinger.formatLatency(latency) : I.t("{gray}(unable to retrieve latency)"))

                        .build()
        );

        RawMessage.send(sender, new RawText(" ")
                        .then(I.t("Server load: "))
                            .color(ChatColor.GOLD)
                            .hover(
                                    new RawText()
                                    .then(I.t("Ticks per second")).style(ChatColor.BOLD).then("\n")
                                    .then(I.t("The number of cycles the server executes per second. The best is 20; under 15, the server is experiencing difficulties.")).then("\n")
                                    .then(I.t("The three values are the average number of TPS during the last 1, 5 and 15 minutes.")).color(ChatColor.GRAY).then("\n")
                                    .then(I.t("The closest to 20 the better.")).color(ChatColor.GREEN)
                            )

                        .then(tps != null ? Pinger.formatTPS(tps) : I.t("{gray}(unable to retrieve server load)"))

                        .build()
        );

        final boolean highLatency = latency > 150;
        final boolean lowTPS = tps != null && tps[0] < 16.5;

        if ((highLatency || lowTPS) && sender.hasPermission("ping.diagnostic"))
        {
            sender.sendMessage("");

            if (isSelf)
            {
                if (lowTPS && highLatency)
                {
                    sendWarning(sender, I.t("Both your latency and the server load are high."), I.t("If you are experiencing poor performances, it may come from your internet connection, the server, or both."));
                }
                else if (highLatency)
                {
                    sendWarning(sender, I.t("Your latency is high."), I.t("If you are experiencing poor performances, it probably comes from your internet connection."));
                }
                else
                {
                    sendWarning(sender, I.t("The server load is high."), I.t("If you are experiencing poor performances, it probably comes from the server."));
                }
            }
            else
            {
                if (lowTPS && highLatency)
                {
                    sendWarning(sender, I.t("Both {0}'s latency and the server load are high.", target.getName()), I.t("If they are experiencing poor performances, it may come from their internet connection, the server, or both."));
                }
                else if (highLatency)
                {
                    sendWarning(sender, I.t("{0}'s latency is high.", target.getName()), I.t("If they are experiencing poor performances, it probably comes from their internet connection."));
                }
                else
                {
                    sendWarning(sender, I.t("The server load is high."), I.t("If {0} is experiencing poor performances, it probably comes from the server.", target.getName()));
                }
            }
        }

        if (sender instanceof Player)
            sender.sendMessage(ChatColor.GRAY + "⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅");

        return true;
    }

    private void sendWarning(final CommandSender receiver, final String title, final String explanation)
    {
        receiver.sendMessage(ChatColor.RED + " \u26A0 " + ChatColor.GRAY + ChatColor.BOLD + title + " " + ChatColor.GRAY + explanation);
    }
}
