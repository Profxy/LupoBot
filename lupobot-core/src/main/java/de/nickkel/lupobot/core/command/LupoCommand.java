package de.nickkel.lupobot.core.command;

import de.nickkel.lupobot.core.data.LupoServer;
import de.nickkel.lupobot.core.pagination.method.Pages;
import de.nickkel.lupobot.core.pagination.model.Page;
import de.nickkel.lupobot.core.pagination.type.PageType;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import de.nickkel.lupobot.core.util.LupoColor;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public abstract class LupoCommand {

    @Getter
    private final CommandInfo info = this.getClass().getAnnotation(CommandInfo.class);

    public abstract void onCommand(CommandContext context);

    public void sendHelp(CommandContext context) {
        context.getChannel().sendMessage(getHelpBuilder(context).build()).queue();
    }

    public void sendSyntaxError(CommandContext context, String errorKey, Object... params) {
        LupoServer server = LupoServer.getByGuild(context.getGuild());
        LupoPlugin plugin = context.getPlugin();

        ArrayList<Page> pages = new ArrayList<>();

        // Error page
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(LupoColor.RED.getColor());
        builder.setAuthor(context.getMember().getUser().getAsTag() + " (" + context.getMember().getId() + ")", null, context.getMember().getUser().getAvatarUrl());
        builder.setFooter(server.translate(null, "core_used-command", server.getPrefix() + context.getLabel()));
        builder.setDescription(server.translate(plugin, errorKey, params));
        pages.add(new Page(PageType.EMBED, builder.build()));

        // Help page
        pages.add(new Page(PageType.EMBED, getHelpBuilder(context).build()));

        context.getChannel().sendMessage((MessageEmbed) pages.get(0).getContent()).queue(success -> {
            Pages.paginate(success, pages, 60, TimeUnit.SECONDS, user -> context.getUser().getId() == user.getIdLong());
        });
    }

    public EmbedBuilder getHelpBuilder(CommandContext context) {
        LupoServer server = LupoServer.getByGuild(context.getGuild());
        LupoPlugin plugin = context.getPlugin();

        String title = server.getPrefix() + this.info.name();
        if(this.info.aliases().length != 0) {
            for(String alias : this.info.aliases()) {
                title = title + " / " + server.getPrefix() + alias;
            }
        }
        String permissions = "/";
        if(this.info.permissions().length != 0) {
            permissions = "";
            for(Permission permission : this.info.permissions()) {
                permissions = permissions + "\n" + permission.toString();
            }
        }

        String pluginName = "core";
        if(plugin != null) {
            pluginName = plugin.getInfo().name();
        }
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.decode("#6495ED"));
        builder.setTitle(title);
        builder.setDescription(server.translate(context.getPlugin(), pluginName + "_" + this.getInfo().name() + "-description"));
        builder.addField(server.translate(null, "core_command-usage"), server.translate(plugin, pluginName + "_" + this.getInfo().name() + "-usage"), false);
        builder.addField(server.translate(null, "core_command-permission"), permissions, false);
        builder.addField(server.translate(null, "core_command-example"), server.translate(plugin, pluginName + "_" + this.getInfo().name() + "-example"), false);
        builder.setFooter(server.translate(null, "core_command-plugin") + ": " +
                server.translatePluginName(plugin));

        return builder;
    }
}
