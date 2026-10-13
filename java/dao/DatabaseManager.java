package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:data\\messenger.db"; //DB Address

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        // Creating Folders (If not exist)
        java.io.File folder = new java.io.File("data");
        if (!folder.exists()) folder.mkdir();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Users Table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id TEXT PRIMARY KEY, " +
                    "username TEXT NOT NULL, " +
                    "full_name TEXT NOT NULL, " +
                    "password TEXT NOT NULL, " +
                    "avatar TEXT, " +
                    "bio TEXT, " +
                    "last_seen INTEGER" +
                    ");");

            // Groups Table
            stmt.execute("CREATE TABLE IF NOT EXISTS groups (" +
                    "id TEXT PRIMARY KEY, " +
                    "name TEXT NOT NULL, " +
                    "owner_id TEXT NOT NULL, " +
                    "avatar TEXT, " +
                    "FOREIGN KEY(owner_id) REFERENCES users(id)" +
                    ");");

            // Group Members Table
            stmt.execute("CREATE TABLE IF NOT EXISTS group_members (" +
                    "group_id TEXT, " +
                    "user_id TEXT, " +
                    "PRIMARY KEY(group_id, user_id), " +
                    "FOREIGN KEY(group_id) REFERENCES groups(id), " +
                    "FOREIGN KEY(user_id) REFERENCES users(id)" +
                    ");");

            // Messages Table
            stmt.execute("CREATE TABLE IF NOT EXISTS messages (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "sender_id TEXT NOT NULL, " +
                    "receiver_id TEXT NOT NULL, " + // User/Group ID
                    "is_group INTEGER NOT NULL, " + // 0 = PV | 1 = Group
                    "text TEXT, " +
                    "time INTEGER NOT NULL, " +
                    "edited INTEGER DEFAULT 0, " +
                    "seen INTEGER DEFAULT 0, " +
                    "is_deleted INTEGER DEFAULT 0" +
                    ");");

            // Contacts Table
            stmt.execute("CREATE TABLE IF NOT EXISTS contacts (" +
                    "user_id TEXT, " +
                    "contact_id TEXT, " +
                    "PRIMARY KEY(user_id, contact_id)" +
                    ");");

            // Blocked Table
            stmt.execute("CREATE TABLE IF NOT EXISTS blocked_users (" +
                    "user_id TEXT, " +
                    "blocked_id TEXT, " +
                    "PRIMARY KEY(user_id, blocked_id)" +
                    ");");

            // Chat States
            stmt.execute("CREATE TABLE IF NOT EXISTS chat_states (" +
                    "user_id TEXT, " +
                    "chat_id TEXT, " +
                    "chat_type TEXT, " + // pv / group
                    "state TEXT DEFAULT 'normal', " +
                    "unread_count INTEGER DEFAULT 0, " +
                    "PRIMARY KEY(user_id, chat_id)" +
                    ");");

            // Reports Table
            stmt.execute("CREATE TABLE IF NOT EXISTS reports (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "sender_id TEXT, " +
                    "message_text TEXT, " +
                    "message_id INTEGER" +
                    ");");

            System.out.println("Database and tables initialized successfully.");
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
}