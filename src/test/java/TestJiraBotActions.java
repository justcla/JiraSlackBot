import org.junit.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class TestJiraBotActions {

    @Test
    public void testFirstTimeCreateProjectCreatesOneAdminUser() throws JiraBotManager.UnauthorisedAccessError {
        // Prove: Test that running registerProject for the first time will create a single admin user

        // Test Setup
        JiraBotManager jiraBotManager = JiraBotManager.getInstance();
        JiraBotDataManager jbdm = JiraBotDataManager.getInstance();

        // Test Execution
        int channelId = jiraBotManager.registerProject("test-channel", "JFW",
                false, "justin");

        // Test Verification
        // Verify channel settings - Has correct Jira project and restricted settings
        ChannelInfo channel = jbdm.getChannelById(channelId);
        assertEquals("test-channel", channel.channelName);
        assertEquals("JFW", channel.jiraProject);
        assertEquals(false, channel.restricted);

        // Verify channel users - exactly one user, with correct name and admin rights.
        Set<ChannelUser> users = jbdm.getChannelUsers(channelId);
        assertEquals(1, users.size());
        Optional<ChannelUser> data = users.stream().findFirst();
        if (data.isPresent()) {
            ChannelUser user = data.get();
            assertEquals("justin", user.slackName);
            assertEquals(true, user.isAdmin);
        }
    }

    @Test
    public void testRegisterProjectThrowsAccessErrorIfUserNotAdmin() {
        // Test Setup
        JiraBotManager jiraBotManager = JiraBotManager.getInstance();
        JiraBotDataManager jbdm = JiraBotDataManager.getInstance();

        // Test Execution
        // Register project (first time)
        int channelId = 0;
        try {
            channelId = jiraBotManager.registerProject("test-channel", "JFW", false, "justin");
        } catch (JiraBotManager.UnauthorisedAccessError e) {
            throw new RuntimeException("Unexpected error occurred. This is not the Error we are looking for.");
        }

        // Register project (second time - different user)
        // Check that it throws an UnauthorisedAccessError
        try {
            jiraBotManager.registerProject("test-channel", "JFW", false, "martha");
        } catch (JiraBotManager.UnauthorisedAccessError e) {
            // This is expected! We want the test to end up here.
            assertEquals(JiraBotManager.UnauthorisedAccessError.class, e.getClass());
        }
    }

    @Test
    public void testRegisterProjectAllowsUpdateWhenUserIsAdmin() throws JiraBotManager.UnauthorisedAccessError {
        // Test Setup
        JiraBotManager jiraBotManager = JiraBotManager.getInstance();
        JiraBotDataManager jbdm = JiraBotDataManager.getInstance();

        // Test Execution
        // Register project (first time)
        int channelId = jiraBotManager.registerProject("test-channel", "JFW", false, "justin");
        // (Mini-verify)
        ChannelInfo channelInfo = jbdm.getChannelById(channelId);
        assertEquals("JFW", channelInfo.jiraProject);
        assertEquals(false, channelInfo.restricted);

        // Register project (second time - same user)
        jiraBotManager.registerProject("test-channel", "PFW", true, "justin");

        // Verify
        // Check that channel info has now changed
        channelInfo = jbdm.getChannelById(channelId);
        assertEquals("PFW", channelInfo.jiraProject);
        assertEquals(true, channelInfo.restricted);

    }
}
