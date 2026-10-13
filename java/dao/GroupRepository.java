package dao;

import entity.*;

import java.sql.*;
import java.util.*;

public class GroupRepository {

    private Group group;

    public GroupRepository(Group group) {
        this.group = group;
    }

    public void changeData(String id, String name, String ownerId) {
        String query = "UPDATE groups SET name = ?, owner_id = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, name);
            pstmt.setString(2, ownerId);
            pstmt.setString(3, id);
            pstmt.executeUpdate();
        }
        catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public void addMessage(Message message) {
        String query = "INSERT INTO messages(sender_id, receiver_id, is_group, text, time, edited, seen, is_deleted) VALUES(?, ?, 1, ?, ?, ?, ?, 0)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            String cleanText = message.getText().replace("║", " ").replace("\n", " ");
            pstmt.setString(1, message.getSenderId());
            pstmt.setString(2, group.getId());
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
        catch(SQLException e) {
           e.printStackTrace();
        } 
    }

    public List<Message> getArrayOfMessages() {
        List<Message> array = new ArrayList<>();
        String query = "SELECT * FROM messages WHERE receiver_id = ? AND is_group = 1 AND is_deleted = 0 ORDER BY time ASC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, group.getId());
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
        catch(SQLException e) {
           e.printStackTrace();
        } 
        return array;
    }

    public List<Message> getArrayOfDeletedMessages() {
        List<Message> array = new ArrayList<>();
        String query = "SELECT * FROM messages WHERE receiver_id = ? AND is_group = 1 AND is_deleted = 1 ORDER BY time ASC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, group.getId());
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
        catch(SQLException e) {
           e.printStackTrace();
        } 
        return array;
    }

    public List<Message> getArrayOfEditedMessages() {
        return getArrayOfMessages(); //Issue Detected
    }

    public void removeMessage(Message mess) {
        String query = "UPDATE messages SET is_deleted = 1 WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, mess.getId());
            pstmt.executeUpdate();
        }
        catch(SQLException e) {
            e.printStackTrace();
        } 
    }

    public void addMember(User member) {
        String query = "INSERT OR IGNORE INTO group_members(group_id, user_id) VALUES(?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, group.getId());
            pstmt.setString(2, member.getId());
            pstmt.executeUpdate();
        }
        catch(SQLException e) {
           e.printStackTrace();
        } 
    }

    public List<String> getArrayOfMembersId() {
        List<String> ids = new ArrayList<>();
        String query = "SELECT user_id FROM group_members WHERE group_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, group.getId());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                   ids.add(rs.getString("user_id"));
                }
            }
        }
        catch(SQLException e) {
           e.printStackTrace();
        }
        return ids;
    }

    public void removeMember(User member) {
        String query = "DELETE FROM group_members WHERE group_id = ? AND user_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, group.getId());
            pstmt.setString(2, member.getId());
            pstmt.executeUpdate();
        }
        catch(SQLException e) {
           e.printStackTrace();
        } 
    }

    public void deleteGroupMessages() {
        String query = "UPDATE messages SET is_deleted = 1 WHERE receiver_id = ? AND is_group = 1";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, group.getId());
            pstmt.executeUpdate();
        }
        catch(SQLException e) {
           e.printStackTrace();
        } 
    }

    public void deleteGroup() {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement pstmt1 = conn.prepareStatement("DELETE FROM group_members WHERE group_id = ?")) {
                pstmt1.setString(1, group.getId());
                pstmt1.executeUpdate();
            }
            try (PreparedStatement pstmt2 = conn.prepareStatement("DELETE FROM groups WHERE id = ?")) {
                pstmt2.setString(1, group.getId());
                pstmt2.executeUpdate();
            }
        }
        catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveAvatar(String base64Image) {
        String query = "UPDATE groups SET avatar = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, base64Image);
            pstmt.setString(2, group.getId());
            pstmt.executeUpdate();
        }
        catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public String getAvatar() {
        String query = "SELECT avatar FROM groups WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, group.getId());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("avatar");
                }
            }
        }
        catch(SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void editMessage(String newText, Message message) {
        String query = "UPDATE messages SET text = ?, edited = 1 WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, newText);
            pstmt.setInt(2, message.getId());
            pstmt.executeUpdate();
        } 
        catch (SQLException e) { e.printStackTrace(); }
    }

    public int getMemberCount() {
        return getArrayOfMembersId().size() + 1;
    }
}