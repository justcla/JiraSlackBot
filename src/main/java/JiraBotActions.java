public class JiraBotActions {

    JiraBotDataManager dataManager;

    public JiraBotActions(JiraBotDataManager jbdm) {
        this.dataManager = jbdm;
    }

    // Define a channel's project
    public int registerProject(String channelName, String jiraProject, boolean isRestricted, String slackUser)
            throws UnauthorisedAccessError {

        // First try to get the channel info
        ChannelInfo channelInfo = dataManager.getChannelByName(channelName);

        // Existing channel
        // If channel info exists, update the channel - if the user is a channel admin user, otherwise throw access error
        if (channelInfo != null) {
            // Abort if user is not authorised.
            if (!dataManager.isChannelAdmin(slackUser, channelInfo.channelId)) {
                throw getUnauthorisedAccessError(slackUser);
            }
            // Update the channel info
            dataManager.updateChannelDetails(channelInfo.channelId, jiraProject, isRestricted);
            return channelInfo.channelId;
        }
        else
        // New channel
        {
            // If new channel, create the channel then create a channel admin user
            int channelId = dataManager.addChannel(channelName, jiraProject, isRestricted);
            // Since it's the first time channel creation, set the calling user as a channel admin
            dataManager.addChannelUser(channelId, slackUser, true);
            return channelId;
        }
    }

    public void addUser(String channel, String slackUser, boolean isAdmin) throws UnauthorisedAccessError, UnregisteredChannelError {
        // Can only add users to a registered channel
        ChannelInfo channelInfo = dataManager.getChannelByName("test-channel");
        if (channelInfo == null) {
            throw new UnregisteredChannelError("This channel has not been registered with the JiraBot. Please call 'project' to register the project before adding extra users.");
        }
        // Only an admin can make changes to this channel
        if (!dataManager.isChannelAdmin(slackUser, channelInfo.channelId)) {
            throw getUnauthorisedAccessError(slackUser);
        }
        // Channel is registered and the caller user is an admin for the channel. Proceed!
        dataManager.addChannelUser(channelInfo.channelId, slackUser, isAdmin);
    }

    private UnauthorisedAccessError getUnauthorisedAccessError(String slackUser) {
        return new UnauthorisedAccessError("Unauthorised access. Admin access required for this feature. " +
                "User '" + slackUser + "' is not an admin of this channel");
    }

    public class UnauthorisedAccessError extends Throwable {
        public UnauthorisedAccessError(String message) {
            super(message);
        }
    }

    public class UnregisteredChannelError extends Throwable {
        public UnregisteredChannelError(String message) {
            super(message);
        }
    }

    // Add a user to a channel's project (Admin function)

    // Remove a user from a channel's project (Admin function)

    // Create a ticket in the channel's project
}
