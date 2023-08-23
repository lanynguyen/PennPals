package org.cis1200;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

public class ServerModelTest {
    private ServerModel model;

    /**
     * Before each test, we initialize model to be
     * a new ServerModel (with all new, empty state)
     */
    @BeforeEach
    public void setUp() {
        // We initialize a fresh ServerModel for each test
        model = new ServerModel();
    }

    /**
     * Here is an example test that checks the functionality of your
     * changeNickname error handling. Each line has commentary directly above
     * it which you can use as a framework for the remainder of your tests.
     */
    @Test
    public void testInvalidNickname() {
        // A user must be registered before their nickname can be changed,
        // so we first register a user with an arbitrarily chosen id of 0.
        model.registerUser(0);

        // We manually create a Command that appropriately tests the case
        // we are checking. In this case, we create a NicknameCommand whose
        // new Nickname is invalid.
        Command command = new NicknameCommand(0, "User0", "!nv@l!d!");

        // We manually create the expected Broadcast using the Broadcast
        // factory methods. In this case, we create an error Broadcast with
        // our command and an INVALID_NAME error.
        Broadcast expected = Broadcast.error(
                command, ServerResponse.INVALID_NAME
        );

        // We then get the actual Broadcast returned by the method we are
        // trying to test. In this case, we use the updateServerModel method
        // of the NicknameCommand.
        Broadcast actual = command.updateServerModel(model);

        // The first assertEquals call tests whether the method returns
        // the appropriate Broadcast.
        assertEquals(expected, actual, "Broadcast");

        // We also want to test whether the state has been correctly
        // changed.In this case, the state that would be affected is
        // the user's Collection.
        Collection<String> users = model.getRegisteredUsers();

        // We now check to see if our command updated the state
        // appropriately. In this case, we first ensure that no
        // additional users have been added.
        assertEquals(1, users.size(), "Number of registered users");

        // We then check if the username was updated to an invalid value
        // (it should not have been).
        assertTrue(users.contains("User0"), "Old nickname still registered");

        // Finally, we check that the id 0 is still associated with the old,
        // unchanged nickname.
        assertEquals(
                "User0", model.getNickname(0),
                "User with id 0 nickname unchanged"
        );
    }

    /*
     * Your TAs will be manually grading the tests that you write below this
     * comment block. Don't forget to test the public methods you have added to
     * your ServerModel class, as well as the behavior of the server in
     * different scenarios.
     * You might find it helpful to take a look at the tests we have already
     * provided you with in Task4Test, Task3Test, and Task5Test.
     */

    @Test
    public void testDeregisterMultipleUsers() {
        model.registerUser(0);
        model.registerUser(1);
        model.deregisterUser(0);
        model.deregisterUser(1);
        assertTrue(model.getRegisteredUsers().isEmpty());
    }

    @Test
    public void testDeregisterOwner() {
        model.registerUser(0);
        model.registerUser(1);
        Command channel = new CreateCommand(0, "User0", "0", false);
        Command join = new JoinCommand(1, "User1", "0");
        channel.updateServerModel(model);
        join.updateServerModel(model);

        model.deregisterUser(0);
        assertTrue(model.getChannels().isEmpty());
    }

    @Test
    public void testOwnerDeregisterMultipleChannels() {
        model.registerUser(0);
        model.registerUser(1);
        model.registerUser(2);
        Command channel1 = new CreateCommand(0, "User0", "1", false);
        Command join1 = new JoinCommand(1, "User1", "1");
        Command join2 = new JoinCommand(2, "User2", "1");
        Command channel2 = new CreateCommand(0, "User0", "2", false);

        channel1.updateServerModel(model);
        channel2.updateServerModel(model);
        join1.updateServerModel(model);
        join2.updateServerModel(model);

        model.deregisterUser(0);
        assertTrue(model.getChannels().isEmpty(), "channel removed by deregistered owner");
    }

    @Test
    public void testChangeOwnerName() {
        model.registerUser(0);
        model.registerUser(1);

        Command channel = new CreateCommand(0, "User0", "0", false);
        Command join = new JoinCommand(1, "User1", "0");
        channel.updateServerModel(model);
        join.updateServerModel(model);

        Command changeName = new NicknameCommand(0, "User0", "cis120");
        channel.updateServerModel(model);
        changeName.updateServerModel(model);

        assertEquals("cis120", model.getOwner("0"));
    }

    @Test
    public void testCreateInvalidChannel() {
        model.registerUser(0);
        Command create = new CreateCommand(0, "User0", "@@", false);
        Broadcast expected = Broadcast.error(create, ServerResponse.INVALID_NAME);
        assertEquals(expected, create.updateServerModel(model), "broadcast");
        assertFalse(model.getChannels().contains("@@"), "channel does not exist");
    }

    @Test
    public void testCreateAlreadyExistChannel() {
        model.registerUser(0);
        Command create = new CreateCommand(0, "User0", "java", false);
        Broadcast expected = Broadcast.okay(create, Collections.singleton("User0"));
        assertEquals(expected, create.updateServerModel(model), "broadcast");

        assertTrue(model.getChannels().contains("java"), "channel exists");
        assertTrue(
                model.getUsersInChannel("java").contains("User0"),
                "channel has creator"
        );
        assertEquals("User0", model.getOwner("java"), "channel has owner");

        Command create2 = new CreateCommand(0, "User0", "java", false);
        Broadcast expected2 = Broadcast.error(create2, ServerResponse.CHANNEL_ALREADY_EXISTS);
        assertEquals(expected2, create2.updateServerModel(model), "broadcast");
    }

    @Test
    public void testJoinChannelDoesNotExistNotMember() {
        model.registerUser(0);

        Command join = new JoinCommand(0, "User0", "java");

        Broadcast expected = Broadcast.error(join, ServerResponse.NO_SUCH_CHANNEL);
        assertEquals(expected, join.updateServerModel(model), "broadcast");
    }

    @Test
    public void testJoinPrivateChannelNotMember() {
        model.registerUser(0);
        model.registerUser(1);
        Command create = new CreateCommand(0, "User0", "java", true);
        create.updateServerModel(model);

        Command join = new JoinCommand(1, "User1", "java");
        Broadcast expected = Broadcast.error(join, ServerResponse.JOIN_PRIVATE_CHANNEL);
        assertEquals(expected, join.updateServerModel(model), "broadcast");

        assertTrue(
                model.getUsersInChannel("java").contains("User0"),
                "User0 in channel"
        );
        assertFalse(
                model.getUsersInChannel("java").contains("User1"),
                "User1 not in channel"
        );
        assertEquals(
                1, model.getUsersInChannel("java").size(),
                "num. users in channel"
        );
    }

    @Test
    public void testMesgChannelDoesNotExist() {
        model.registerUser(0);

        Command mesg = new MessageCommand(0, "User0", "java", "hey whats up hello");

        Broadcast expected = Broadcast.error(mesg, ServerResponse.NO_SUCH_CHANNEL);
        assertEquals(expected, mesg.updateServerModel(model), "broadcast");
    }

    @Test
    public void testMsgPrivateChannelNotMember() {
        model.registerUser(0);
        model.registerUser(1);
        Command create = new CreateCommand(0, "User0", "java", true);
        create.updateServerModel(model);

        Command mesg = new MessageCommand(1, "User1", "java", "hey whats up hello");

        Broadcast expected = Broadcast.error(mesg, ServerResponse.USER_NOT_IN_CHANNEL);
        assertEquals(expected, mesg.updateServerModel(model), "broadcast");
    }

    @Test
    public void testOwnerLeaveChannel() {
        model.registerUser(0);
        model.registerUser(1);
        Command create = new CreateCommand(0, "User0", "java", false);
        create.updateServerModel(model);
        Command join = new JoinCommand(1, "User1", "java");
        join.updateServerModel(model);

        Command leave = new LeaveCommand(0, "User0", "java");
        leave.updateServerModel(model);
        assertTrue(model.getChannels().isEmpty());
    }

    @Test
    public void testMsgPublicChannelNotMember() {
        model.registerUser(0);
        model.registerUser(1);
        Command create = new CreateCommand(0, "User0", "java", false);
        create.updateServerModel(model);

        Command mesg = new MessageCommand(1, "User1", "java", "hey whats up hello");

        Broadcast expected = Broadcast.error(mesg, ServerResponse.USER_NOT_IN_CHANNEL);
        assertEquals(expected, mesg.updateServerModel(model), "broadcast");
    }

    @Test
    public void testLeaveChannelDoesNotExistMember() {
        model.registerUser(0);

        Command leave = new LeaveCommand(1, "User1", "java");

        Broadcast expected = Broadcast.error(leave, ServerResponse.NO_SUCH_CHANNEL);
        assertEquals(expected, leave.updateServerModel(model), "broadcast");
    }

    @Test
    public void testLeaveChannelExistsNotMember() {
        model.registerUser(0);
        model.registerUser(1);
        Command create = new CreateCommand(0, "User0", "java", true);
        create.updateServerModel(model);

        Command leave = new LeaveCommand(1, "User1", "java");
        Broadcast expected = Broadcast.error(leave, ServerResponse.USER_NOT_IN_CHANNEL);
        assertEquals(expected, leave.updateServerModel(model), "broadcast");

        assertTrue(
                model.getUsersInChannel("java").contains("User0"),
                "User0 in channel"
        );
        assertFalse(
                model.getUsersInChannel("java").contains("User1"),
                "User1 not in channel"
        );
        assertEquals(
                1, model.getUsersInChannel("java").size(),
                "num. users in channel"
        );
    }

    @Test
    public void testInviteByOwnerUserDoesNotExists() {
        model.registerUser(0);

        Command create = new CreateCommand(0, "User0", "java", true);
        create.updateServerModel(model);

        Command invite = new InviteCommand(0, "User0", "java", "User1");

        Broadcast expected = Broadcast.error(invite, ServerResponse.NO_SUCH_USER);
        assertEquals(expected, invite.updateServerModel(model), "broadcast");

        assertEquals(1, model.getUsersInChannel("java").size(), "num. users in channel");
        assertTrue(model.getUsersInChannel("java").contains("User0"), "User0 in channel");
        assertFalse(model.getUsersInChannel("java").contains("User1"), "User1 in channel");
    }

    @Test
    public void testInviteByOwnerChannelDoesNotExists() {
        model.registerUser(0);
        model.registerUser(1);

        Command invite = new InviteCommand(0, "User0", "java", "User1");

        Broadcast expected = Broadcast.error(invite, ServerResponse.NO_SUCH_CHANNEL);
        assertEquals(expected, invite.updateServerModel(model), "broadcast");
    }

    @Test
    public void testInviteByOwnerPublicChannel() {
        model.registerUser(0);
        model.registerUser(1);

        Command create = new CreateCommand(0, "User0", "java", false);
        create.updateServerModel(model);

        Command invite = new InviteCommand(0, "User0", "java", "User1");

        Broadcast expected = Broadcast.error(invite, ServerResponse.INVITE_TO_PUBLIC_CHANNEL);
        assertEquals(expected, invite.updateServerModel(model), "broadcast");

        assertEquals(1, model.getUsersInChannel("java").size(), "num. users in channel");
        assertTrue(model.getUsersInChannel("java").contains("User0"), "User0 in channel");
        assertFalse(model.getUsersInChannel("java").contains("User1"), "User1 in channel");
    }

    @Test
    public void testOwnerKicksSelf() {
        model.registerUser(0);
        model.registerUser(1);
        Command create = new CreateCommand(0, "User0", "java", false);
        create.updateServerModel(model);
        Command join = new JoinCommand(1, "User1", "java");
        join.updateServerModel(model);

        Command kick = new KickCommand(0, "User0", "java", "User0");
        kick.updateServerModel(model);
        assertTrue(model.getChannels().isEmpty());
    }

    @Test
    public void testKickedByOwnerUserDoesNotExists() {
        model.registerUser(0);

        Command create = new CreateCommand(0, "User0", "java", true);
        create.updateServerModel(model);

        Command kick = new KickCommand(0, "User0", "java", "User1");

        Broadcast expected = Broadcast.error(kick, ServerResponse.NO_SUCH_USER);
        assertEquals(expected, kick.updateServerModel(model), "broadcast");

        assertEquals(1, model.getUsersInChannel("java").size(), "num. users in channel");
        assertTrue(model.getUsersInChannel("java").contains("User0"), "User0 in channel");
        assertFalse(model.getUsersInChannel("java").contains("User1"), "User1 in channel");
    }

    @Test
    public void testKickedByOwnerChannelDoesNotExist() {
        model.registerUser(0);
        model.registerUser(1);

        Command kick = new KickCommand(0, "User0", "java", "User1");

        Broadcast expected = Broadcast.error(kick, ServerResponse.NO_SUCH_CHANNEL);
        assertEquals(expected, kick.updateServerModel(model), "broadcast");
    }

    @Test
    public void testKickByNonOwner() {
        model.registerUser(0);
        model.registerUser(1);
        model.registerUser(2);
        Command create = new CreateCommand(0, "User0", "java", true);
        create.updateServerModel(model);

        Command invite1 = new InviteCommand(0, "User0", "java", "User1");
        invite1.updateServerModel(model);
        Command invite2 = new InviteCommand(0, "User0", "java", "User2");
        invite2.updateServerModel(model);

        Command kick = new KickCommand(1, "User1", "java", "User2");
        Broadcast expected = Broadcast.error(kick, ServerResponse.USER_NOT_OWNER);
        assertEquals(expected, kick.updateServerModel(model), "error");

        assertEquals(3, model.getUsersInChannel("java").size(), "num. users in channel");
        assertTrue(model.getUsersInChannel("java").contains("User0"), "User0 in channel");
        assertTrue(model.getUsersInChannel("java").contains("User1"), "User1 in channel");
        assertTrue(model.getUsersInChannel("java").contains("User2"), "User2 not in channel");
    }

    @Test
    public void testKickUserNotInChannel() {
        model.registerUser(0);
        model.registerUser(1);
        model.registerUser(2);
        Command create = new CreateCommand(0, "User0", "java", true);
        create.updateServerModel(model);

        Command invite1 = new InviteCommand(0, "User0", "java", "User1");
        invite1.updateServerModel(model);

        Command kick = new KickCommand(0, "User0", "java", "User2");
        Broadcast expected = Broadcast.error(kick, ServerResponse.USER_NOT_IN_CHANNEL);
        assertEquals(expected, kick.updateServerModel(model), "error");

        assertEquals(2, model.getUsersInChannel("java").size(), "num. users in channel");
        assertTrue(model.getUsersInChannel("java").contains("User0"), "User0 in channel");
        assertTrue(model.getUsersInChannel("java").contains("User1"), "User1 in channel");
    }
}
