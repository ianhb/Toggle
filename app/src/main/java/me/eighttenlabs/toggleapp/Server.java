package me.eighttenlabs.toggleapp;

/**
 * Server Object to store and compare servers on network
 * <p/>
 * Created by Ian on 1/5/2015.
 */
public class Server {
    String name;
    String ip;

    public Server(String name, String ip) {
        this.name = name;
        this.ip = ip;
    }

    @Override
    public boolean equals(Object o) {
        return ((o instanceof Server) && (((Server) o).ip.equals(ip)));
    }
}
