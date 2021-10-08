public class JiraBotManager {

    private static JiraBotManager instance;


    JiraBotDataManager jbdm;

    public static JiraBotManager getInstance() {
        if (instance ==null) {
            instance = new JiraBotManager(JiraBotDataManager.getInstance());
        }
        return instance;
    }

    public JiraBotManager(JiraBotDataManager jbdm) {
        this.jbdm = jbdm;
    }

    // Define a channel's project
    public int registerProject(String channelName, String jiraProject, boolean isRestricted, String slackUser)
            throws UnauthorisedAccessError {

        // First try to get the channel info
        ChannelInfo channelInfo = jbdm.getChannelByName(channelName);

        // Existing channel
        // If channel info exists, update the channel - if the user is a channel admin user, otherwise throw access error
        if (channelInfo != null) {
            // Abort if user is not authorised.
            if (!jbdm.isChannelAdmin(slackUser, channelInfo.channelId)) {
                throw new UnauthorisedAccessError("Unauthorised access. Admin access required for this feature. " +
                        "User '" + slackUser + "' is not an admin of this channel");
            }
            // Update the channel info
            jbdm.updateChannelDetails(channelInfo.channelId, jiraProject, isRestricted);
            return channelInfo.channelId;
        }
        else
        // New channel
        {
            // If new channel, create the channel then create a channel admin user
            int channelId = jbdm.addChannel(channelName, jiraProject, isRestricted);
            // Since it's the first time channel creation, set the calling user as a channel admin
            jbdm.addChannelUser(channelId, slackUser, true);
            return channelId;
        }
    }

    public class UnauthorisedAccessError extends Throwable {
        public UnauthorisedAccessError(String message) {
            super(message);
        }
    }

    // Add a user to a channel's project (Admin function)

    // Remove a user from a channel's project (Admin function)

    // Create a ticket in the channel's project
}
