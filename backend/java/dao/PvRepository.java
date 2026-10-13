package dao;

import entity.*;

import java.sql.*;
import java.util.*;

public class PvRepository 
{
    private PV pv;

    public PvRepository(PV pv) {
        this.pv = pv;
    }

    public void deletPvMessages()
    {
        String user1 = pv.getFirstPerson().getId();
        String user2 = pv.getSecondPerson().getId();
        String query = "UPDATE messages SET is_deleted = 1 WHERE is_group = 0 AND ((sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?))";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query))
        {
            pstmt.setString(1, user1);
            pstmt.setString(2, user2);
            pstmt.setString(3, user2);
            pstmt.setString(4, user1);
            pstmt.executeUpdate();
        }
        catch(SQLException e) 
        {
           e.printStackTrace();
        } 
    }

    public boolean deletePv()
    {
        deletPvMessages();
        return true;
    }

    public void addMessage(Message message) {
        String query = "INSERT INTO messages(sender_id, receiver_id, is_group, text, time, edited, seen, is_deleted) VALUES(?, ?, 0, ?, ?, ?, ?, 0)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            String cleanText = message.getText().replace("║", " ").replace("\n", " ");
            pstmt.setString(1, message.getSenderId());
            pstmt.setString(2, message.getReceiverId());
            pstmt.setString(3, cleanText);
            pstmt.setLong(4, message.getTime());
            pstmt.setInt(5, message.getEdited() ? 1 : 0);
            pstmt.setInt(6, message.getSeen() ? 1 : 0);
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    message.setId(generatedKeys.getInt(1));
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Message> getArrayOfMessages() {
        List<Message> array = new ArrayList<>();
        String user1 = pv.getFirstPerson().getId();
        String user2 = pv.getSecondPerson().getId();
        
        String query = "SELECT * FROM messages WHERE is_group = 0 AND is_deleted = 0 AND " +
                       "((sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)) ORDER BY time ASC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, user1);
            pstmt.setString(2, user2);
            pstmt.setString(3, user2);
            pstmt.setString(4, user1);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String sender = rs.getString("sender_id");
                    String receiver = rs.getString("receiver_id");
                    long time = rs.getLong("time");
                    boolean edited = rs.getInt("edited") == 1;
                    boolean seen = rs.getInt("seen") == 1;
                    String text = rs.getString("text");
                    Message newMessage = new Message(id, sender, receiver, text, time, edited, false, seen);
                    array.add(newMessage);
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return array;
    }

    public void removeMessage(Message mess) {
        String query = "UPDATE messages SET is_deleted = 1 WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, mess.getId());
            pstmt.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void editMessage(Message mess, String newText) {
        String query = "UPDATE messages SET text = ?, edited = 1 WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, newText);
            pstmt.setInt(2, mess.getId());
            pstmt.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Message> getArrayOfDeletedMessages() {
        List<Message> array = new ArrayList<>();
        String user1 = pv.getFirstPerson().getId();
        String user2 = pv.getSecondPerson().getId();
        
        String query = "SELECT * FROM messages WHERE is_group = 0 AND is_deleted = 1 AND " +
                       "((sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)) ORDER BY time ASC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, user1);
            pstmt.setString(2, user2);
            pstmt.setString(3, user2);
            pstmt.setString(4, user1);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String sender = rs.getString("sender_id");
                    String receiver = rs.getString("receiver_id");
                    long time = rs.getLong("time");
                    boolean edited = rs.getInt("edited") == 1;
                    boolean seen = rs.getInt("seen") == 1;
                    String text = rs.getString("text");
                    Message newMessage = new Message(id, sender, receiver, text, time, edited, true, seen);
                    array.add(newMessage);
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return array;
    }

    public List<Message> getArrayOfEditedMessages() {
        return getArrayOfMessages();
    }
}