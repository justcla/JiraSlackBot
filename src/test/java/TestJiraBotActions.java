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
    JiraBotDataManager jbdmMock;

    @Test
    public void testFirstTimeCreateProjectCreatesOneAdminUser() throws JiraBotManager.UnauthorisedAccessError {
        // Prove: Test that running registerProject for the first time will create a single admin user

        // Test Setup
        JiraBotManager jiraBotManager = new JiraBotManager(jbdmMock);
        // Test scenario: Channel does not previously exist
        when(jbdmMock.getChannelByName(any())).thenReturn(null);

        // Test Execution
        int channelId = jiraBotManager.registerProject("test-channel", "JiraProj",
                false, "justin");

        // Verify it called jbdm.createProject()
        verify(jbdmMock).addChannel("test-channel", "JiraProj", false);
        // Verify it called jbdm.addUser();
        verify(jbdmMock).addChannelUser(channelId, "justin", true);
    }

    @Test
    public void testNonAdminUserCannotChangeProject() {
        // Test Setup
        JiraBotManager jiraBotManager = new JiraBotManager(jbdmMock);
        // Test scenario: Channel was previously defined
        when(jbdmMock.getChannelByName(any())).thenReturn(new ChannelInfo());
        when(jbdmMock.isChannelAdmin(any(), anyInt())).thenReturn(false);

        // Test Execution
        int channelId = 0;
        try {
            channelId = jiraBotManager.registerProject("test-channel", "JiraProj",
                    false, "justin");
            fail("Test should have failed by this point due to unauthorised access");
        } catch (JiraBotManager.UnauthorisedAccessError unauthorisedAccessError) {
            // This is expected! Test passed (Can check the error message - optional)
        }

        // Verification
        verify(jbdmMock, times(1)).getChannelByName("test-channel");
        verify(jbdmMock, times(1)).isChannelAdmin("justin", channelId);
        // Verify it never calls jbdm.createProject()
        verify(jbdmMock, never()).addChannel(any(), any(), anyBoolean());
        // Verify it never calls jbdm.addUser();
        verify(jbdmMock, never()).addChannelUser(anyInt(), any(), anyBoolean());
        verifyNoMoreInteractions(jbdmMock);
    }

    @Test
    public void testAdminUserCanChangeProject() throws JiraBotManager.UnauthorisedAccessError {
        // Test Setup
        JiraBotManager jiraBotManager = new JiraBotManager(jbdmMock);
        // Test scenario: Channel was previously defined
        when(jbdmMock.getChannelByName(any())).thenReturn(new ChannelInfo());
        when(jbdmMock.isChannelAdmin(any(), anyInt())).thenReturn(true);

        // Test Execution
        int channelId = jiraBotManager.registerProject("test-channel", "JiraProj",
                false, "justin");

        // Verification
        verify(jbdmMock, times(1)).getChannelByName("test-channel");
        verify(jbdmMock, times(1)).isChannelAdmin("justin", channelId);
        // Verify it calls jbdm.updateProject()
        verify(jbdmMock, times(1)).updateChannelDetails(channelId, "JiraProj", false);
        // Verify it never calls jbdm.addUser();
        verify(jbdmMock, never()).addChannel(any(), any(), anyBoolean());
        verify(jbdmMock, never()).addChannelUser(anyInt(), any(), anyBoolean());
        verifyNoMoreInteractions(jbdmMock);
    }

}
