package de.nickkel.lupobot.core.internal.commands;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import de.nickkel.lupobot.core.util.LupoColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

import java.util.ArrayList;
import java.util.List;

@CommandInfo(name = "installplugin", permissions = Permission.ADMINISTRATOR, cooldown = 5, category = "core")
public class InstallPluginCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        if(context.getArgs().length == 1) {
            if(LupoBot.getInstance().getPlugins().size() == 0) {
                context.getChannel().sendMessage("**Error:** No plugins loaded! Nothing to install").queue();
                return;
            }
            boolean match = false;
            int plugins = 0;
            for(LupoPlugin plugin : LupoBot.getInstance().getPlugins()) {
                plugins++;
                if(context.getArgs()[0].equalsIgnoreCase(plugin.getInfo().name()) || context.getArgs()[0].equalsIgnoreCase(context.getServer().translatePluginName(plugin))) {
                    List<Long> guilds = new ArrayList<>();
                    for(long l : plugin.getInfo().guilds()) {
                        guilds.add(l);
                    }
                    if(guilds.size() == 0 || guilds.contains(context.getGuild().getIdLong())) {
                        match = true;
                        if(context.getServer().getPlugins().contains(plugin)) {
                            EmbedBuilder builder = new EmbedBuilder();
                            builder.setColor(LupoColor.RED.getColor());
                            builder.setAuthor(context.getGuild().getName() + " (" + context.getGuild().getId() + ")", null, context.getGuild().getIconUrl());
                            builder.setDescription(context.getServer().translate(null, "core_plugin-already-installed", context.getServer().translatePluginName(plugin)));
                            builder.setTimestamp(context.getMessage().getTimeCreated());
                            context.getChannel().sendMessage(builder.build()).queue();
                            return;
                        } else {
                            context.getServer().installPlugin(plugin);
                            EmbedBuilder builder = new EmbedBuilder();
                            builder.setColor(LupoColor.GREEN.getColor());
                            builder.setAuthor(context.getGuild().getName() + " (" + context.getGuild().getId() + ")", null, context.getGuild().getIconUrl());
                            builder.setDescription(context.getServer().translate(null, "core_plugin-installed", context.getServer().translatePluginName(plugin)));
                            builder.addField(context.getServer().translate(null, "core_plugin"), context.getServer().translatePluginName(plugin), false);
                            builder.addField(context.getServer().translate(null, "core_description"), context.getServer().translate(plugin, plugin.getInfo().name() + "_description"), false);
                            context.getChannel().sendMessage(builder.build()).queue();
                            return;
                        }
                    }
                }
            }
            if(!match && plugins == LupoBot.getInstance().getPlugins().size()) {
                sendSyntaxError(context, "core_installplugin-invalid-plugin");
            }
        } else {
            sendHelp(context);
        }
    }
}
