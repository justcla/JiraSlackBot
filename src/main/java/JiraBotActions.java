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

    public void addUser(String channel, String callingUser, String newUser, boolean makeAdmin) throws UnauthorisedAccessError, UnregisteredChannelError, InvalidActionError {
        // Can only add users to a registered channel
        ChannelInfo channelInfo = dataManager.getChannelByName(channel);
        if (channelInfo == null) {
            throw getUnregisteredChannelError();
        }
        // Only an admin can make changes to this channel
        if (!dataManager.isChannelAdmin(callingUser, channelInfo.channelId)) {
            throw getUnauthorisedAccessError(newUser);
        }
        // Can only add user if user is not already added
        if (dataManager.getChannelUser(channelInfo.channelId, newUser) != null) {
            throw new InvalidActionError("User is already registered for this channel.");
        }
        // Channel is registered and the caller user is an admin for the channel. Proceed!
        dataManager.addChannelUser(channelInfo.channelId, newUser, makeAdmin);
    }

    public void makeAdmin(String channel, String callingUser, String channelUserName) throws UnauthorisedAccessError, UnregisteredChannelError, InvalidActionError {
        // Can only change users in a registered channel
        ChannelInfo channelInfo = dataManager.getChannelByName(channel);
        if (channelInfo == null) {
            throw getUnregisteredChannelError();
        }
        int channelId = channelInfo.channelId;
        // Only an admin can make changes to this channel
        if (!dataManager.isChannelAdmin(callingUser, channelId)) {
            throw getUnauthorisedAccessError(callingUser);
        }
        // If adding an admin who is already an admin - InvalidAction
        if (dataManager.isChannelAdmin(channelUserName, channelId)) {
            throw new InvalidActionError("User is already an admin for this channel.");
        }

        // Proceed. Will add new, or upgrade existing user.
        dataManager.addChannelUser(channelId, channelUserName, true);
    }

    public void removeAdmin(String channel, String callingUser, String channelUserName) throws UnauthorisedAccessError, UnregisteredChannelError, InvalidActionError {
        // Can only change users in a registered channel
        ChannelInfo channelInfo = dataManager.getChannelByName(channel);
        if (channelInfo == null) {
            throw getUnregisteredChannelError();
        }
        int channelId = channelInfo.channelId;
        // Only an admin can make changes to this channel
        if (!dataManager.isChannelAdmin(callingUser, channelId)) {
            throw getUnauthorisedAccessError(callingUser);
        }
        // If attempting to removing admin access from basic user - InvalidAction
        // Note: This returns the same whether the user is a Basic user, or just not registered
        if (!dataManager.isChannelAdmin(channelUserName, channelId)) {
            throw new InvalidActionError("User is not an admin for this channel.");
        }

        // Proceed. Will add new, or upgrade existing user.
        dataManager.addChannelUser(channelId, channelUserName, true);
    }

    private UnregisteredChannelError getUnregisteredChannelError() {
        return new UnregisteredChannelError("This channel has not been registered with the JiraBot. " +
                "Please call 'project' to register the project first.");
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

    public class InvalidActionError extends Throwable {
        public InvalidActionError(String message) {
            super(message);
        }
    }

    // Add a user to a channel's project (Admin function)

    // Remove a user from a channel's project (Admin function)

    // Create a ticket in the channel's project
}
