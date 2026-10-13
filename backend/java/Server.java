import api.*;
import dao.DatabaseManager;

import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;

public class Server {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);

        DatabaseManager.initializeDatabase();
        
        Account.login(server);
        Account.recoverPassword(server);
        Account.deleteAccount(server);
        Account.load(server);
        Account.search(server);
        Info.load(server);
        Info.updateProfile(server);
        Info.updateGroupInfo(server);
        Info.userAvatar(server);
        Info.groupAvatar(server);
        Chatroom.setState(server);
        Chatroom.loadPV(server);
        Chatroom.loadGroup(server);
        Chatroom.loadGroupLogs(server);
        Contacts.Manage(server);
        Block.Manage(server);
        Messages.Manage(server);

        Admin.Manage(server);
        
        server.setExecutor(null);
        server.start();
        System.out.println("Kaftar Server is running on port 8081...");
        // Thread adminCliThread = new Thread(new Admin());
        // adminCliThread.start();
    }
}