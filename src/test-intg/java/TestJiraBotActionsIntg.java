import org.junit.Before;
import org.junit.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@SuppressWarnings("SimplifiableAssertion")
public class TestJiraBotActionsIntg {

    JiraBotDataManager dataManager;
    JiraBotActions jiraBotActions;

    @Before
    public void setUp() {
        dataManager = new MemorySetDataManager();
        jiraBotActions = new JiraBotActions(dataManager);
    }

    @Test
    public void testFirstTimeCreateProjectCreatesOneAdminUser() throws JiraBotActions.UnauthorisedAccessError {
        // Prove: Test that running registerProject for the first time will create a single admin user

        // Test Setup
        // -- Starting with empty state --

        // Test Execution
        int channelId = jiraBotActions.registerProject("test-channel", "JiraProject1",
                false, "test-user");

        // Test Verification
        // Verify channel settings - Has correct Jira project and restricted settings
        ChannelInfo channel = dataManager.getChannelById(channelId);
        assertEquals("test-channel", channel.channelName);
        assertEquals("JiraProject1", channel.jiraProject);
        assertEquals(false, channel.restricted);

        // Verify channel users - exactly one user, with correct name and admin rights.
        Set<ChannelUser> users = dataManager.getChannelUsers(channelId);
        assertEquals(1, users.size());
        Optional<ChannelUser> data = users.stream().findFirst();
        if (data.isPresent()) {
            ChannelUser user = data.get();
            assertEquals("test-user", user.slackName);
            assertEquals(true, user.isAdmin);
        } else {
            fail("Could not find a valid user");
        }
    }

    @Test
    public void testRegisterProjectThrowsAccessErrorIfUserNotAdmin() {
        // Test Execution
        // Register project (first time)
        try {
            jiraBotActions.registerProject("test-channel", "JiraProject1", false, "test-user");
        } catch (JiraBotActions.UnauthorisedAccessError e) {
            throw new RuntimeException("Unexpected error occurred. This is not the Error we are looking for.");
        }

        // Register project (second time - different user)
        // Check that it throws an UnauthorisedAccessError
        try {
            jiraBotActions.registerProject("test-channel", "JiraProject1", false, "martha");
            fail("Expected an UnauthorisedAccessError here, but none was thrown.");
        } catch (JiraBotActions.UnauthorisedAccessError e) {
            // This is expected! We want the test to end up here.
            assertEquals(JiraBotActions.UnauthorisedAccessError.class, e.getClass());
        }
    }

    @Test
    public void testRegisterProjectAllowsUpdateWhenUserIsAdmin() throws JiraBotActions.UnauthorisedAccessError {
        // Test Execution
        // Register project (first time)
        int channelId = jiraBotActions.registerProject("test-channel", "JiraProject1", false, "test-user");
        // (Mini-verify)
        ChannelInfo channelInfo = dataManager.getChannelById(channelId);
        assertEquals("JiraProject1", channelInfo.jiraProject);
        assertEquals(false, channelInfo.restricted);

        // Register project (second time - same user)
        jiraBotActions.registerProject("test-channel", "JiraProject2", true, "test-user");

        // Verify
        // Check that channel info has now changed
        channelInfo = dataManager.getChannelById(channelId);
        assertEquals("JiraProject2", channelInfo.jiraProject);
        assertEquals(true, channelInfo.restricted);
    }
}
