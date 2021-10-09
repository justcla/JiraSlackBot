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

    @Test
    public void testFirstTimeCreateProjectCreatesOneAdminUser() throws JiraBotActions.UnauthorisedAccessError {
        // Prove: Test that running registerProject for the first time will create a single admin user

        // Test Setup
        // Test scenario: Channel does not previously exist
        when(dataManagerMock.getChannelByName(any())).thenReturn(null);

        // Test Execution
        int channelId = jiraBotActions.registerProject("test-channel", "JiraProj",
                false, "justin");

        // Verify it called jbdm.createProject()
        verify(dataManagerMock).addChannel("test-channel", "JiraProj", false);
        // Verify it called jbdm.addUser();
        verify(dataManagerMock).addChannelUser(channelId, "justin", true);
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
                    false, "justin");
            fail("Test should have failed by this point due to unauthorised access");
        } catch (JiraBotActions.UnauthorisedAccessError unauthorisedAccessError) {
            // This is expected! Test passed (Can check the error message - optional)
        }

        // Verification
        verify(dataManagerMock, times(1)).getChannelByName("test-channel");
        verify(dataManagerMock, times(1)).isChannelAdmin("justin", channelId);
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
                false, "justin");

        // Verification
        verify(dataManagerMock, times(1)).getChannelByName("test-channel");
        verify(dataManagerMock, times(1)).isChannelAdmin("justin", channelId);
        // Verify it calls jbdm.updateProject()
        verify(dataManagerMock, times(1)).updateChannelDetails(channelId, "JiraProj", false);
        // Verify it never calls jbdm.addUser();
        verify(dataManagerMock, never()).addChannel(any(), any(), anyBoolean());
        verify(dataManagerMock, never()).addChannelUser(anyInt(), any(), anyBoolean());
        verifyNoMoreInteractions(dataManagerMock);
    }

}
