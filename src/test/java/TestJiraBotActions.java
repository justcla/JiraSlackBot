public class TestJiraBotActions {

    public static void main(String[] args) throws Throwable {

        testFirstTimeCreateProjectCreatesOneAdminUser();

    }

    private static void testFirstTimeCreateProjectCreatesOneAdminUser() throws JiraBotManager.UnauthorisedAccessError {
        // Test that running doProject for the first time will create a single admin user
        JiraBotManager jiraBotManager = JiraBotManager.getInstance();

        jiraBotManager.doProject("test-channel", "JFW",
                false, "justin.clareburt-booking.com");

        // Verify channel users
        System.out.println("Test ran successfully");
    }
}
