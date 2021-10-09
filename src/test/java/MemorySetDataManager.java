import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class MemorySetDataManager implements JiraBotDataManager {

    private static MemorySetDataManager instance;

    // Data store
    Set<ChannelInfo> channels = new HashSet<>();
    Set<ChannelUser> users = new HashSet<>();
    int lastChannelId = 0;

    @Override
    public ChannelInfo getChannelByName(String channelName) {
        return channels.stream().filter(c -> channelName.equals(c.channelName)).findFirst().orElse(null);
    }

    @Override
    public ChannelInfo getChannelById(int channelId) {
        return channels.stream().filter(c -> channelId == channelId).findFirst().orElse(null);
    }

    @Override
    public boolean isExistingChannel(String channelName) {
        return channels.stream().anyMatch(c -> c.channelName.equals(channelName));
    }

    @Override
    public boolean isChannelAdmin(String slackUser, int channelId) {
        return users.stream().anyMatch(user -> slackUser.equals(user.slackName)
                && user.channelId == channelId
                && user.isAdmin);
    }

    /**
     * Registers a new Slack channel for a Jira project
     * @param channelName The name of the Slack channel
     * @param jiraProject The name of the Jira Project to associate with this Slack channel
     * @param isRestricted If true, only authorised users can create ticket in the channel
     */
    @Override
    public int addChannel(String channelName, String jiraProject, boolean isRestricted) {
        ChannelInfo channelInfo = new ChannelInfo();
        channelInfo.channelName = channelName;
        channelInfo.jiraProject = jiraProject;
        channelInfo.restricted = isRestricted;
        channelInfo.channelId = getNewChannelId();
        channels.add(channelInfo);
        return channelInfo.channelId;
    }

    /**
     * Updates the data store to record an entry for the Channel
     * @param channelId The internal channelId of the channel
     * @param jiraProject The name of the Jira Project to associate with this Slack channel
     * @param isRestricted If true, only authorised users can create ticket in this channel
     */
    @Override
    public void updateChannelDetails(int channelId, String jiraProject, boolean isRestricted) {
        // If it already exists, update it
        Optional<ChannelInfo> data = channels.stream().filter(c -> c.channelId == channelId).findFirst();
        ChannelInfo channelInfo;
        if (data.isEmpty()) {
            throw new RuntimeException("Unable to update channel info. Data not found in data store for channel: " + channelId);
        }
        channelInfo = data.get();
        // Update the values that can be changed.
        channelInfo.jiraProject = jiraProject;
        channelInfo.restricted = isRestricted;
    }

    @Override
    public void addChannelUser(int channelId, String slackUser, boolean isAdmin) {
        ChannelUser user = new ChannelUser();
        user.channelId = channelId;
        user.slackName = slackUser;
        user.isAdmin = isAdmin;
        users.add(user);
    }

    @Override
    public Set<ChannelUser> getChannelUsers(int channelId) {
        return users.stream().filter(u -> u.channelId == channelId).collect(Collectors.toSet());
    }

    private int getNewChannelId() {
        return ++lastChannelId;
    }


}
