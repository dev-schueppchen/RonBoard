package com.zekro.discord.bot.ronboard;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.events.message.guild.GenericGuildMessageEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveAllEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEmoteEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class EventListener extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(EventListener.class);
    private final RonBoard ronBoard;
    private final Database database;

    public EventListener(RonBoard ronBoard) {
        this.ronBoard = ronBoard;
        this.database = ronBoard.getDatabase();
    }

    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        if (!event.getReactionEmote().getAsReactionCode().equals("⭐")) {
            return;
        }
        Message message = event.getChannel().retrieveMessageById(event.getMessageIdLong()).complete();
        int count = 0;
        List<Integer> list = message.getReactions().stream()
                .filter(reaction -> reaction.getReactionEmote().getAsReactionCode().equals("⭐"))
                .map(MessageReaction::getCount)
                .collect(Collectors.toList());
        if (!list.isEmpty()) {
            count = list.get(0);
        }

        Optional<StarboardEntry> optional = database.getStarboardEntry(event.getMessageIdLong());
        StarboardEntry starboardEntry;
        if (optional.isEmpty()) {
            starboardEntry = new StarboardEntry(
                    event.getMessageIdLong(),
                    event.getChannel().getIdLong(),
                    0,
                    false
            );
            database.addStarboardEntry(starboardEntry);
        } else {
            starboardEntry = optional.get();
        }

        if (count < ronBoard.getSettings().getBound()) {
            return;
        }

        if (starboardEntry.isPosted()) {
            int finalCount = count;
            ronBoard.getStarboardChannel().retrieveMessageById(starboardEntry.getStarboardId())
                    .flatMap(msg -> msg.editMessage(buildMessage(message, finalCount)))
                    .queue();
            return;
        }
        ronBoard.getStarboardChannel().sendMessage(buildMessage(message, count)).queue(msg -> {
            starboardEntry.setStarboardId(msg.getIdLong());
            starboardEntry.setPosted(true);
            database.updateStarboardEntry(starboardEntry);
        });
    }

    // single reaction removed
    @Override
    public void onGuildMessageReactionRemove(@NotNull GuildMessageReactionRemoveEvent event) {
        if (!event.getReaction().getReactionEmote().getAsReactionCode().equals("⭐")) {
            return;
        }
        Message message = event.getChannel().retrieveMessageById(event.getMessageIdLong()).complete();

        int count = 0;
        List<Integer> list = message.getReactions().stream()
                .filter(reaction -> reaction.getReactionEmote().getAsReactionCode().equals("⭐"))
                .map(MessageReaction::getCount)
                .collect(Collectors.toList());
        if (!list.isEmpty()) {
            count = list.get(0);
        }

        Optional<StarboardEntry> optional = database.getStarboardEntry(event.getMessageIdLong());

        if (count == 0) {
            optional.ifPresent(starboardEntry -> {
                if (starboardEntry.isPosted()) {
                    ronBoard.getStarboardChannel().retrieveMessageById(starboardEntry.getStarboardId()
                    ).flatMap(Message::delete).queue();
                }
                database.deleteStarboardEntry(starboardEntry);
            });
            return;
        }
        StarboardEntry starboardEntry;
        if (optional.isEmpty()) {
            starboardEntry = new StarboardEntry(
                    event.getMessageIdLong(),
                    event.getChannel().getIdLong(),
                    0,
                    false
            );
            database.addStarboardEntry(starboardEntry);
        } else {
            starboardEntry = optional.get();
        }
        if (count < ronBoard.getSettings().getBound() && starboardEntry.isPosted()) {
            ronBoard.getStarboardChannel().retrieveMessageById(starboardEntry.getStarboardId())
                    .flatMap(Message::delete).queue();
            starboardEntry.setPosted(false);
            starboardEntry.setStarboardId(0);
            database.updateStarboardEntry(starboardEntry);
            return;
        }
        int finalCount = count;
        ronBoard.getStarboardChannel().retrieveMessageById(starboardEntry.getStarboardId())
                .flatMap(msg -> msg.editMessage(buildMessage(message, finalCount)))
                .queue();
    }

    // all reactions of a emote removed
    @Override
    public void onGuildMessageReactionRemoveEmote(@NotNull GuildMessageReactionRemoveEmoteEvent event) {
        if (event.getReaction().getReactionEmote().getAsReactionCode().equals("⭐")) {
            removeEntry(event);
        }
    }

    // all reactions removed
    @Override
    public void onGuildMessageDelete(@NotNull GuildMessageDeleteEvent event) {
        removeEntry(event);
    }

    // message deleted
    @Override
    public void onGuildMessageReactionRemoveAll(@NotNull GuildMessageReactionRemoveAllEvent event) {
        removeEntry(event);
    }

    private Message buildMessage(Message message, int count) {
        EmbedBuilder embed = new EmbedBuilder()
                .setAuthor(String.format("%s#%s",
                        message.getAuthor().getName(),
                        message.getAuthor().getDiscriminator()),
                        null,
                        message.getAuthor().getEffectiveAvatarUrl()
                )
                .setTimestamp(message.getTimeCreated());

        String content = message.getContentRaw();

        if (message.getEmbeds().size() > 0) {
            MessageEmbed messageEmbed = message.getEmbeds().get(0);
            if (messageEmbed.getThumbnail() != null) {
                embed.setImage(messageEmbed.getThumbnail().getUrl());
            } else if (messageEmbed.getImage() != null) {
                embed.setImage(messageEmbed.getImage().getUrl());
            }
            String title = messageEmbed.getUrl() == null ? messageEmbed.getTitle() : String.format("[%s](%s)", messageEmbed.getTitle(), messageEmbed.getUrl());
            String description = messageEmbed.getType() == EmbedType.VIDEO ? "" : messageEmbed.getDescription();
            embed.addField(
                    "__Embed Representation:__",
                    String.format("**%s**\n%s", title, description)
                    , false
            );
            for (MessageEmbed.Field field : messageEmbed.getFields()) {
                embed.addField(field);
            }
        } else if (message.getAttachments().size() > 0) {
            embed.setImage(message.getAttachments().get(0).getUrl());
        } else {
            String regex = "(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                embed.setImage(matcher.group(0));
                if (!content.isEmpty()) {
                    content = content.replace(matcher.group(0), "");
                }
            }
        }
        if (message.getReferencedMessage() == null) {
            embed.setDescription(String.format("%s\n\n%s", content, String.format("[*jump to message!*](%s)", message.getJumpUrl())));
        } else {
            Message referencedMessage = message.getReferencedMessage();
            String referenced = String.format("*replying to %s#%s:*\n> *%s* ([*jump*](%s))",
                    referencedMessage.getAuthor().getName(),
                    referencedMessage.getAuthor().getDiscriminator(),
                    referencedMessage.getContentDisplay(),
                    referencedMessage.getJumpUrl());
            embed.setDescription(String.format("%s\n\n%s\n\n%s", referenced, content, String.format("[*jump to message!*](%s)", message.getJumpUrl())));
        }
        return new MessageBuilder()
                .append(String.format("**%d** :star: %s", count, message.getTextChannel().getAsMention()))
                .setEmbed(embed.build()).build();
    }

    private void removeEntry(GenericGuildMessageEvent event) {
        Optional<StarboardEntry> optional = database.getStarboardEntry(event.getMessageIdLong());
        optional.ifPresent(starboardEntry -> {
            ronBoard.getStarboardChannel().retrieveMessageById(starboardEntry.getStarboardId()
            ).flatMap(Message::delete).queue();
            database.deleteStarboardEntry(starboardEntry);
        });
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (event.getAuthor().getIdLong() != 393843637437464588L) {
            return;
        }
        if (!event.getMessage().getContentRaw().startsWith(ronBoard.getSettings().getPrefix() + "stop")) {
            return;
        }
        event.getChannel().sendMessage("https://tenor.com/view/peace-dueces-fadeaway-gif-14327700").queue();
        ronBoard.stop();
    }
}

