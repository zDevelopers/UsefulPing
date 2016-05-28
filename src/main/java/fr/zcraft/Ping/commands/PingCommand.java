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
import fr.zcraft.zlib.tools.items.ItemStackBuilder;
import fr.zcraft.zlib.tools.text.RawMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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
            sender.sendMessage(I.t("{green}{bold}{0}'s ping", target.getName()));
            sender.sendMessage("");
        }
        else if (label.toLowerCase().endsWith("ping"))
        {
            if (sender.hasPermission("ping.toggleping"))
                RawMessage.send(sender, new RawText(I.t("{green}{bold}Pong!"))
                        .then("  ")
                        .then(I.t("(keep displayed)"))
                            .color(ChatColor.GRAY)
                            .hover(new RawText(I.t("Click here to display your ping continuously")))
                            .command("/toggleping")
                        .build()
                );
            else
                sender.sendMessage(I.t("{green}{bold}Pong!"));

            sender.sendMessage("");
        }

        RawMessage.send(sender, new RawText("")
                        .then(I.t("Latency: "))
                            .color(ChatColor.GOLD)
                            .hover(
                                new ItemStackBuilder(Material.POTATO_ITEM)
                                        .title(ChatColor.BOLD + I.t("Latency"))
                                        .longLore(ChatColor.RESET, isSelf
                                                ? I.t("The time needed to transfer data from you to the server.")
                                                : I.t("The time needed to transfer data from {0} to the server.", target.getName()), 38)
                                        .loreLine(ChatColor.GREEN, I.t("The lower the better."))
                                        .hideAttributes()
                                .item()
                            )

                        .then(latency != -1 ? Pinger.formatLatency(latency) : I.t("{gray}(unable to retrieve latency)"))

                        .build()
        );

        RawMessage.send(sender, new RawText("")
                        .then(I.t("Server load: "))
                            .color(ChatColor.GOLD)
                            .hover(
                                new ItemStackBuilder(Material.POTATO_ITEM)
                                        .title(ChatColor.BOLD + I.t("Ticks per second"))
                                        .longLore(ChatColor.RESET, I.t("The number of cycles the server executes per second. The best is 20; under 15, the server is experiencing difficulties."), 38)
                                        .longLore(ChatColor.GRAY, I.t("The three values are the average number of TPS during the last 1, 5 and 15 minutes."), 38)
                                        .loreLine(ChatColor.GREEN, I.t("The closest to 20 the better."))
                                        .hideAttributes()
                                .item()
                            )

                        .then(tps != null ? Pinger.formatTPS(tps) : I.t("{gray}(unable to retrieve server load)"))

                        .build()
        );

        if (sender instanceof Player)
            sender.sendMessage(ChatColor.GRAY + "⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅");

        return true;
    }
}
