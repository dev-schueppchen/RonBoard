package com.zekro.discord.bot.ronboard;

public class StarboardEntry {

    private long messageId;
    private long channelId;
    private long starboardId;
    private boolean isPosted;

    public StarboardEntry() {

    }

    public StarboardEntry(long messageId, long channelId, long starboardId, boolean isPosted) {
        this.messageId = messageId;
        this.channelId = channelId;
        this.starboardId = starboardId;
        this.isPosted = isPosted;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public long getChannelId() {
        return channelId;
    }

    public void setChannelId(long channelId) {
        this.channelId = channelId;
    }

    public long getStarboardId() {
        return starboardId;
    }

    public void setStarboardId(long starboardId) {
        this.starboardId = starboardId;
    }

    public boolean isPosted() {
        return isPosted;
    }

    public void setPosted(boolean posted) {
        isPosted = posted;
    }

    @Override
    public String toString() {
        return "StarboardEntry{" +
                "messageId=" + messageId +
                ", channelId=" + channelId +
                ", reactionCount=" + starboardId +
                ", isPosted=" + isPosted +
                '}';
    }
}
