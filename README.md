# PennPals

This assignment involves building an internet chat server in Java. Your code will model the internal state of the server and handle communication with chat users.

Server

A computer system designed to provide a service to many connected clients at once.

Client

An end user of a service like our chat server.

In this assignment, a client is a user who has connected to the server to chat with other users.
Each client is assigned a unique integer ID when they connect to the server. This integer cannot be changed, and it persists as long as the client is connected. If a client later reconnects, they will get a new integer ID.
Each client also has a nickname - a string that other users can see. A user can change their own nickname at any time.
We use “client” and “user” interchangeably in these instructions.

Channel

A group of users (clients) connected to the server. Every channel has an owner – the user who created the channel.

A user in a channel receives all messages sent to that channel after they join.
A user in a channel can leave the channel at any time. After they leave, they will no longer get messages from that channel.
At any given time, a user connected to the server can be in any number of channels (including none).
Owner
The owner of a channel can remove anyone from that channel (including themselves). If the owner leaves for any reason, the entire channel is removed.

Channel Privacy
A channel is either public or private. If it is public, any user who knows the name of the channel can join. If it is private, new users are only added if the owner invites them, in which case they automatically join.

Protocol
A standardized set of instructions for communicating over the Internet by requesting and transmitting data.

In this assignment, clients communicate with the server by sending commands that instruct the server to create new channels, send messages on channels, etc. We represent this communication protocol in our code base using subclasses of an abstract class Command:

NicknameCommand changes the sender’s nickname.
CreateCommand creates a new channel with the sender as its owner.
JoinCommand lets the sender join a public channel.
InviteCommand adds a target user to a private channel owned by the sender.
MessageCommand sends a message to all users in a specific channel.
LeaveCommand lets the sender leave a channel they are in.
KickCommand removes a target user from a public or private channel owned by the sender.
