import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TestJiraBotActions {

    @Mock
    MemorySetDataManager dataManagerMock;

    JiraBotActions jiraBotActions;

    @Before
    public void setUp() {
        jiraBotActions = new JiraBotActions(dataManagerMock);
    }

    //====================================================
    //---- RegisterProject tests
    //====================================================

    @Test
    public void testFirstTimeCreateProjectCreatesOneAdminUser() throws JiraBotActions.UnauthorisedAccessError {
        // Prove: Test that running registerProject for the first time will create a single admin user

        // Test Setup
        // Test scenario: Channel does not previously exist
        when(dataManagerMock.getChannelByName(any())).thenReturn(null);

        // Test Execution
        int channelId = jiraBotActions.registerProject("test-channel", "JiraProj",
                false, "test-user");

        // Verify it called jbdm.createProject()
        verify(dataManagerMock).addChannel("test-channel", "JiraProj", false);
        // Verify it called jbdm.addUser();
        verify(dataManagerMock).addChannelUser(channelId, "test-user", true);
    }

    @Test
    public void testNonAdminUserCannotChangeProject() {
        // Test Setup
        // Test scenario: Channel was previously defined
        when(dataManagerMock.getChannelByName(any())).thenReturn(new ChannelInfo());
        when(dataManagerMock.isChannelAdmin(any(), anyInt())).thenReturn(false);

        // Test Execution
        int channelId = 0;
        try {
            channelId = jiraBotActions.registerProject("test-channel", "JiraProj",
                    false, "test-user");
            fail("Test should have failed by this point due to unauthorised access");
        } catch (JiraBotActions.UnauthorisedAccessError unauthorisedAccessError) {
            // This is expected! Test passed (Can check the error message - optional)
        }

        // Verification
        verify(dataManagerMock, times(1)).getChannelByName("test-channel");
        verify(dataManagerMock, times(1)).isChannelAdmin("test-user", channelId);
        // Verify it never calls jbdm.createProject()
        verify(dataManagerMock, never()).addChannel(any(), any(), anyBoolean());
        // Verify it never calls jbdm.addUser();
        verify(dataManagerMock, never()).addChannelUser(anyInt(), any(), anyBoolean());
        verifyNoMoreInteractions(dataManagerMock);
    }

    @Test
    public void testAdminUserCanChangeProject() throws JiraBotActions.UnauthorisedAccessError {
        // Test Setup
        // Test scenario: Channel was previously defined
        when(dataManagerMock.getChannelByName(any())).thenReturn(new ChannelInfo());
        when(dataManagerMock.isChannelAdmin(any(), anyInt())).thenReturn(true);

        // Test Execution
        int channelId = jiraBotActions.registerProject("test-channel", "JiraProj",
                false, "test-user");

        // Verification
        verify(dataManagerMock, times(1)).getChannelByName("test-channel");
        verify(dataManagerMock, times(1)).isChannelAdmin("test-user", channelId);
        // Verify it calls jbdm.updateProject()
        verify(dataManagerMock, times(1)).updateChannelDetails(channelId, "JiraProj", false);
        // Verify it never calls jbdm.addUser or addChannel;
        verify(dataManagerMock, never()).addChannel(any(), any(), anyBoolean());
        verify(dataManagerMock, never()).addChannelUser(anyInt(), any(), anyBoolean());
        verifyNoMoreInteractions(dataManagerMock);
    }

    //====================================================
    //---- AddUser tests ---
    //====================================================

    @Test
    public void testCallingAddUserOnAnUnregisteredChannelWillReturnError() throws JiraBotActions.UnauthorisedAccessError {
        // Test Setup
        // Test scenario: Channel was previously defined. The caller is an admin user.
        int channelId = 1;
        String testChannelName = "test-channel";
        String testUserName = "test-user";
        ChannelInfo channelInfo = channelInfoObj(channelId, testChannelName);
        when(dataManagerMock.getChannelByName(any())).thenReturn(null);     // <-- Channel not registered

        // Test Execution
        try {
            jiraBotActions.addUser(testChannelName, testUserName, false);
            fail("Test should have failed by this point due to unregistered channel");
        } catch (JiraBotActions.UnregisteredChannelError e) {
            // This is expected! Test passed (Can check the error message - optional)
        }

        // What should it do?
        // Check if the channel is registered
        verify(dataManagerMock, times(1)).getChannelByName(testChannelName);
        // It will not check if the user is a channel admin
        verify(dataManagerMock, never()).isChannelAdmin(any(), anyInt());
        // Verify it never calls addChannel or addUser;
        verify(dataManagerMock, never()).addChannel(any(), any(), anyBoolean());
        verify(dataManagerMock, never()).addChannelUser(anyInt(), any(), anyBoolean());
        verifyNoMoreInteractions(dataManagerMock);
    }

    @Test
    public void testNonAdminUserCanNotAddUser() throws JiraBotActions.UnregisteredChannelError {
        // Test Setup
        // Test scenario: Channel was previously defined. The caller is not an admin user.
        int channelId = 1;
        String testChannelName = "test-channel";
        String testUserName = "test-user";
        ChannelInfo channelInfo = channelInfoObj(channelId, testChannelName);
        when(dataManagerMock.getChannelByName(any())).thenReturn(channelInfo);
        when(dataManagerMock.isChannelAdmin(any(), anyInt())).thenReturn(false);

        // Test Execution
        try {
            jiraBotActions.addUser(testChannelName, testUserName, true);
            fail("Test should have failed by this point due to unauthorised access");
        } catch (JiraBotActions.UnauthorisedAccessError e) {
            // This is expected! Test passed (Can check the error message - optional)
        }

        // What should it do?
        // Check if the caller is an admin user.
        verify(dataManagerMock, times(1)).getChannelByName(testChannelName);
        verify(dataManagerMock, times(1)).isChannelAdmin(testUserName, channelId);
        // Verify it never calls jbdm.addUser();
        verify(dataManagerMock, never()).addChannelUser(anyInt(), any(), anyBoolean());
        verifyNoMoreInteractions(dataManagerMock);
    }

    @Test
    public void testAdminUserCanAddUser() throws JiraBotActions.UnauthorisedAccessError, JiraBotActions.UnregisteredChannelError {
        // Test Setup
        // Test scenario: Channel was previously defined. The caller is an admin user.
        int channelId = 1;
        String testChannelName = "test-channel";
        String testUserName = "test-user";
        ChannelInfo channelInfo = channelInfoObj(channelId, testChannelName);
        when(dataManagerMock.getChannelByName(any())).thenReturn(channelInfo);
        when(dataManagerMock.isChannelAdmin(any(), anyInt())).thenReturn(true);

        // Test Execution
        jiraBotActions.addUser(testChannelName, testUserName, true);

        // What should it do?
        // Check if the caller is an admin user.
        verify(dataManagerMock, times(1)).getChannelByName(testChannelName);
        verify(dataManagerMock, times(1)).isChannelAdmin(testUserName, channelId);
        // Verify it calls jbdm to add the channel user - and verify values;
        verify(dataManagerMock, times(1)).addChannelUser(channelId, testUserName, true);
        // Verify it never calls addChannel;
        verify(dataManagerMock, never()).addChannel(any(), any(), anyBoolean());
        verifyNoMoreInteractions(dataManagerMock);
    }

    private ChannelInfo channelInfoObj(int channelId, String testChannelName) {
        ChannelInfo channelInfo = new ChannelInfo();
        channelInfo.channelId = channelId;
        channelInfo.channelName = testChannelName;
        return channelInfo;
    }

}
