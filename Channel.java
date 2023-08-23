package org.cis1200;
import java.util.*;
public class Channel implements Comparable {
    // data fields
    private String owner;
    private String channelName;
    private boolean isPrivate;
    private TreeSet<String> clients;

    // constructor
    public Channel(String ownerName, String channelName, boolean isPrivate) {
        this.owner = ownerName;
        this.channelName = channelName;
        this.isPrivate = isPrivate;
        clients = new TreeSet<>();
        clients.add(owner);
    }

    /* get owner of the channel
    * Input: N/A
    * Output: Owner of the channel */
    public String getOwner() {
        return owner;
    }

    public String getChannelName() {
        return channelName;
    }

    /* get privacy of the channel
     * Input: N/A
     * Output: Privacy of the channel */
    public boolean getPrivacy() {
        return isPrivate;
    }

    /* get the client list in the channel
    * Input: N/A
    * Output: Client list of the channel */
    public TreeSet<String> getClients() {
        return clients;
    }

    /* change the owner's name
     * Input: new nickname string
     * Output: N/A */
    public void setOwnerName(String newOwnerName) {
        owner = newOwnerName;
    }

    /* add a client to the channel
    * Input: new client's name string
     * Output: N/A */
    public void addClient(String newClient) {
        clients.add(newClient);
    }

    /* remove a client from the channel
    * Input: nickname string
     * Output: N/A */
    public void removeClient(String clientName) {
        if (clients.contains(clientName)) {
            clients.remove(clientName);
        }
    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Channel other = (Channel) o;
        return  (this.compareTo(other) == 0);
    }
}
