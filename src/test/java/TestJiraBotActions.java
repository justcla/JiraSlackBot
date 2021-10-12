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
        String channelName = "test-channel";
        String callingUser = "calling-user";
        String jiraProject = "JiraProj";
        boolean isRestricted = false;       // <- Creating tickets restricted to registered users?
        // State: Channel DOES NOT previously exist
        when(dataManagerMock.getChannelByName(any())).thenReturn(null);

        // Test Execution
        int channelId = jiraBotActions.registerProject(channelName, jiraProject, isRestricted, callingUser);

        // Verify it called jbdm.createProject()
        verify(dataManagerMock).addChannel(channelName, jiraProject, isRestricted);
        // Verify it called jbdm.addUser();
        verify(dataManagerMock).addChannelUser(channelId, callingUser, true);
    }

    @Test
    public void testBasicUserCannotChangeProject_returnsUnauthorisedAccessError() {
        // Test Setup
        String channelName = "test-channel";
        String callingUser = "calling-user";
        String jiraProject = "JiraProj";
        boolean isRestricted = false;       // <- Creating tickets restricted to registered users?
        // Test scenario: Channel was previously defined
        when(dataManagerMock.getChannelByName(any())).thenReturn(new ChannelInfo());
        when(dataManagerMock.isChannelAdmin(any(), anyInt())).thenReturn(false);

        // Test Execution
        try {
            jiraBotActions.registerProject(channelName, jiraProject, isRestricted, callingUser);
            fail("Test should have failed by this point due to unauthorised access");
        } catch (JiraBotActions.UnauthorisedAccessError e) {
            // This is expected! Test passed (Can check the error message - optional)
        }

        // Verification
//        verify(dataManagerMock, times(1)).getChannelByName("test-channel");
//        verify(dataManagerMock, times(1)).isChannelAdmin("test-user", channelId);
        // Verify it never calls jbdm.createProject()
        verify(dataManagerMock, never()).addChannel(any(), any(), anyBoolean());
        // Verify it never calls jbdm.addUser();
        verify(dataManagerMock, never()).addChannelUser(anyInt(), any(), anyBoolean());
//        verifyNoMoreInteractions(dataManagerMock);
    }

    @Test
    public void testAdminUserCanChangeProject() throws JiraBotActions.UnauthorisedAccessError {
        // Test Setup
        String channelName = "test-channel";
        String callingUser = "calling-user";
        String jiraProject = "JiraProj";
        boolean isRestricted = false;       // <- Creating tickets restricted to registered users?
        // Test scenario: Channel was previously defined
        when(dataManagerMock.getChannelByName(any())).thenReturn(new ChannelInfo());
        when(dataManagerMock.isChannelAdmin(any(), anyInt())).thenReturn(true);

        // Test Execution
        int channelId = jiraBotActions.registerProject(channelName, jiraProject, isRestricted, callingUser);

        // Verification
        verify(dataManagerMock, times(1)).getChannelByName(channelName);
        verify(dataManagerMock, times(1)).isChannelAdmin(callingUser, channelId);
        // Verify it calls UpdateProject
        verify(dataManagerMock, times(1)).updateChannelDetails(channelId, jiraProject, isRestricted);
        // Verify it never calls AddUser or AddChannel;
        verify(dataManagerMock, never()).addChannel(any(), any(), anyBoolean());
        verify(dataManagerMock, never()).addChannelUser(anyInt(), any(), anyBoolean());
        verifyNoMoreInteractions(dataManagerMock);
    }

    //====================================================
    //---- AddUser tests ---
    //====================================================

    @Test
    public void testCannotAddUserOnAnUnregisteredChannel_returnsUnregisteredChannelError() throws Throwable {
        // Test Setup
        // Test scenario: Channel was previously defined. The caller is an admin user.
        String testChannelName = "test-channel";
        String callingUser = "calling-user";
        String newUserName = "new-user";
        when(dataManagerMock.getChannelByName(testChannelName)).thenReturn(null);     // <-- Channel not registered

        // Test Execution
        try {
            jiraBotActions.addUser(testChannelName, callingUser, newUserName, false);
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
    public void testBasicUserCanNotAddUser_returnsUnauthorisedAccessError() throws Throwable {
        // Test Setup
        // Test scenario: Channel was previously defined. The caller is not an admin user.
        int channelId = 1;
        String testChannelName = "test-channel";
        String callingUser = "calling-user";
        String newUserName = "new-user";
        // State: Channel is registered
        ChannelInfo channelInfo = channelInfoObj(channelId, testChannelName);
        when(dataManagerMock.getChannelByName(testChannelName)).thenReturn(channelInfo);
        // State: Calling user is NOT a channel admin
        when(dataManagerMock.isChannelAdmin(callingUser, channelId)).thenReturn(false);

        // Test Execution
        // Attempts to make ADMIN user - expected: UnauthorisedAccess
        try {
            jiraBotActions.addUser(testChannelName, callingUser, newUserName, true);
            fail("Test should have failed by this point due to unauthorised access");
        } catch (JiraBotActions.UnauthorisedAccessError e) {
            // This is expected! Test passed (Can check the error message - optional)
        }
        // Attempts to make BASIC user - expected: UnauthorisedAccess
        try {
            jiraBotActions.addUser(testChannelName, callingUser, newUserName, false);
            fail("Test should have failed by this point due to unauthorised access");
        } catch (JiraBotActions.UnauthorisedAccessError e) {
            // This is expected! Test passed (Can check the error message - optional)
        }

        // What should it do/not do?
        // Verify it never calls AddUser
        verify(dataManagerMock, never()).addChannelUser(anyInt(), any(), anyBoolean());
    }

    @Test
    public void testCannotAddUserForExistingUser_returnsInvalidActionError() throws Throwable {
        // Test Setup
        // Test scenario: Channel was previously defined. The caller is an admin user.
        int channelId = 1;
        String testChannelName = "test-channel";
        String callingUser = "calling-user";
        String newUserName = "new-user";
        ChannelInfo channelInfo = channelInfoObj(channelId, testChannelName);
        when(dataManagerMock.getChannelByName(any())).thenReturn(channelInfo);
        when(dataManagerMock.isChannelAdmin(callingUser, channelId)).thenReturn(true);
        // State: New user IS previously added
        ChannelUser user = new ChannelUser();
        when(dataManagerMock.getChannelUser(channelId, newUserName)).thenReturn(user);

        // Expecting: Cannot add user; user already exists.
        try {
            jiraBotActions.addUser(testChannelName, callingUser, newUserName, false);
            fail("Test should have failed by this point due to invalid action");
        } catch (JiraBotActions.InvalidActionError e) {
            // This is expected! Test passed (Can check the error message - optional)
        }
    }

    @Test
    public void testAdminUserCanAddBasicUser() throws Throwable {
        // Test Setup
        // Test scenario: Channel was previously defined. The caller is an admin user.
        int channelId = 1;
        String testChannelName = "test-channel";
        String callingUser = "calling-user";
        String newUserName = "new-user";
        // State: Channel is registered
        ChannelInfo channelInfo = channelInfoObj(channelId, testChannelName);
        when(dataManagerMock.getChannelByName(testChannelName)).thenReturn(channelInfo);
        // State: Calling user IS a channel admin
        when(dataManagerMock.isChannelAdmin(callingUser, channelId)).thenReturn(true);
        // State: New user NOT previously added
        when(dataManagerMock.getChannelUser(channelId, newUserName)).thenReturn(null);

        // Test Execution
        // Adding a BASIC user (not an admin)
        jiraBotActions.addUser(testChannelName, callingUser, newUserName, false);

        // What should it do?
        // Check if the caller is an admin user.
        verify(dataManagerMock, times(1)).getChannelByName(testChannelName);
        verify(dataManagerMock, times(1)).getChannelUser(channelId, newUserName);
        verify(dataManagerMock, times(1)).isChannelAdmin(callingUser, channelId);
        // Verify it calls jbdm to add the channel user - and verify values;
        // Most important! Check that it adds the user WITHOUT admin rights
        verify(dataManagerMock, times(1)).addChannelUser(channelId, newUserName, false);
        // Verify it never calls addChannel;
        verify(dataManagerMock, never()).addChannel(any(), any(), anyBoolean());
        verifyNoMoreInteractions(dataManagerMock);
    }

    @Test
    public void testAdminUserCanAddAdminUser() throws Throwable {
        // Test Setup
        int channelId = 1;
        String testChannelName = "test-channel";
        String callingUser = "calling-user";
        String newUserName = "new-user";
        // State: Channel is registered
        ChannelInfo channelInfo = channelInfoObj(channelId, testChannelName);
        when(dataManagerMock.getChannelByName(testChannelName)).thenReturn(channelInfo);
        // State: Calling user IS a channel admin
        when(dataManagerMock.isChannelAdmin(callingUser, channelId)).thenReturn(true);
        // State: New user NOT previously added
        when(dataManagerMock.getChannelUser(channelId, newUserName)).thenReturn(null);

        // Test Execution
        // Adding an ADMIN user
        jiraBotActions.addUser(testChannelName, callingUser, newUserName, true);

        // What should it do?
        // Check if the caller is an admin user.
        verify(dataManagerMock, times(1)).getChannelByName(testChannelName);
        verify(dataManagerMock, times(1)).getChannelUser(channelId, newUserName);
        verify(dataManagerMock, times(1)).isChannelAdmin(callingUser, channelId);
        // Verify it calls jbdm to add the channel user - and verify values;
        // Most important! Check that it adds the user WITH admin rights
        verify(dataManagerMock, times(1)).addChannelUser(channelId, newUserName, true);
        // Verify it never calls addChannel;
        verify(dataManagerMock, never()).addChannel(any(), any(), anyBoolean());
        verifyNoMoreInteractions(dataManagerMock);
    }

    @Test
    public void testAdminUserCanUpgradeBasicUserToAdmin() throws Throwable {
        // Test Setup
        int channelId = 1;
        String testChannelName = "test-channel";
        String callingUser = "calling-user";
        String newUserName = "new-user";
        // State: Channel is registered
        ChannelInfo channelInfo = channelInfoObj(channelId, testChannelName);
        when(dataManagerMock.getChannelByName(testChannelName)).thenReturn(channelInfo);
        // State: Calling user IS a channel admin
        when(dataManagerMock.isChannelAdmin(callingUser, channelId)).thenReturn(true);
        // State: New user WAS previously added - as BASIC user
        ChannelUser existingUser = new ChannelUser();
        existingUser.isAdmin = false;   // <-- User starts as a BASIC user (not Admin)
        when(dataManagerMock.getChannelUser(channelId, newUserName)).thenReturn(existingUser);

        // Test Execution
        // Upgrading to an ADMIN user
        jiraBotActions.makeAdmin(testChannelName, callingUser, newUserName);

        // What should it do?
        // Check that it adds the user with ADMIN rights
        verify(dataManagerMock, times(1)).addChannelUser(channelId, newUserName, true);
    }

    @Test
    public void testAdminUserCanDowngradeAdminUserToBasic(){}

    //=================================
    // --- Make Admin
    //=================================

    @Test
    public void testMakeAdminOnUnregProj_returnsUnregisteredProjectError() throws Throwable {
        // Test Setup
        String testChannelName = "test-channel";
        String callingUser = "calling-user";
        String newUserName = "new-user";
        // State: Channel is NOT registered
        when(dataManagerMock.getChannelByName(testChannelName)).thenReturn(null);

        // Test Execution
        // Upgrading to an ADMIN user
        try {
            jiraBotActions.makeAdmin(testChannelName, callingUser, newUserName);
            fail("We should have had a test failure by now. Expecting UnregisteredChannelError.");
        } catch (JiraBotActions.UnregisteredChannelError unregisteredChannelError) {
            // This is what we are expecting!!
        }

        // Verify
        verify(dataManagerMock, never()).isChannelAdmin(any(), anyInt());
        verify(dataManagerMock, never()).addChannelUser(anyInt(), any(), anyBoolean());
    }

    @Test
    public void testBasicUserCannotCallMakeAdmin_returnsUnauthorisedAccessError() throws Throwable {
        // Test Setup
        int channelId = 1;
        String testChannelName = "test-channel";
        String callingUser = "calling-user";
        String newUserName = "new-user";
        String jiraProject = "test-project";
        // State: Channel is registered
        ChannelInfo channelInfo = new ChannelInfo();
        channelInfo.channelId = channelId;
        channelInfo.channelName = testChannelName;
        channelInfo.jiraProject = jiraProject;
        when(dataManagerMock.getChannelByName(testChannelName)).thenReturn(channelInfo);
        when(dataManagerMock.isChannelAdmin(callingUser, channelId)).thenReturn(false); // <- Not a channel admin

        // Test Execution
        // Upgrading to an ADMIN user
        try {
            jiraBotActions.makeAdmin(testChannelName, callingUser, newUserName);
            fail("We should have had a test failure by now. Expecting UnregisteredChannelError.");
        } catch (JiraBotActions.UnauthorisedAccessError e) {
            // This is what we are expecting!!
        }

        // Verify
        verify(dataManagerMock, times(1)).isChannelAdmin(callingUser, channelId);
        verify(dataManagerMock, never()).addChannelUser(anyInt(), any(), anyBoolean());
    }

    //-----------------------------------------------
    // -- RemoveUser tests
    //-----------------------------------------------

    @Test
    public void testCannotRemoveUserFromUnregisteredChannel_returnsUnregisteredChannelError(){}
    @Test
    public void testBasicUserCannotRemoveUser_returnsUnauthorisedAccessError(){}
    @Test
    public void testAdminUserCanRemoveBasicUser(){}
    @Test
    public void testAdminUserCanRemoveAdminUser(){}


    private ChannelInfo channelInfoObj(int channelId, String testChannelName) {
        ChannelInfo channelInfo = new ChannelInfo();
        channelInfo.channelId = channelId;
        channelInfo.channelName = testChannelName;
        return channelInfo;
    }

}
