import api.*;
import cli.Admin;

import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;

public class Server {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);

        Account.login(server);
        Account.recoverPassword(server);
        Account.load(server);
        Account.avatar(server);
        Account.search(server);
        Info.load(server);
        Chatroom.loadPV(server);
        Chatroom.loadGroup(server);
        Contacts.Manage(server);
        Messages.Manage(server);
        
        server.setExecutor(null);
        server.start();
        System.out.println("Kaftar Server is running on port 8081...");
        Thread adminCliThread = new Thread(new Admin());
        adminCliThread.start();
    }
}