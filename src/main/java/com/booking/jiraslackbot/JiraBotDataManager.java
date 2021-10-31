package com.booking.jiraslackbot;

import java.util.Set;

public interface JiraBotDataManager {
    
    ChannelInfo getChannelByName(String channelName);

    ChannelInfo getChannelById(int channelId);

    boolean isExistingChannel(String channelName);

    boolean isChannelAdmin(String slackUser, int channelId);

    /**
     * Registers a new Slack channel for a Jira project
     * @param channelName The name of the Slack channel
     * @param jiraProject The name of the Jira Project to associate with this Slack channel
     * @param isRestricted If true, only authorised users can create ticket in the channel
     */
    int addChannel(String channelName, String jiraProject, boolean isRestricted);

    /**
     * Updates the data store to record an entry for the Channel
     * @param channelId The internal channelId of the channel
     * @param jiraProject The name of the Jira Project to associate with this Slack channel
     * @param isRestricted If true, only authorised users can create ticket in this channel
     */
    void updateChannelDetails(int channelId, String jiraProject, boolean isRestricted);

    /**
     * Adds or updates Channel User data. Key: channelId, slackUser
     */
    void addChannelUser(int channelId, String slackUser, boolean isAdmin);

    Set<ChannelUser> getChannelUsers(int channelId);

    ChannelUser getChannelUser(int channelId, String slackUser);
}
