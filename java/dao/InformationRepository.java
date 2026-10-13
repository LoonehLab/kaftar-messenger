package dao;

import entity.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InformationRepository {
    private User user;

    public InformationRepository(User user)
    {
        this.user = user;
    }

    public void saveBlockedUser(User blockedUser)
    {
        String query = "INSERT OR IGNORE INTO blocked_users(user_id, blocked_id) VALUES(?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query))
        {
            pstmt.setString(1, user.getId());
            pstmt.setString(2, blockedUser.getId());
            pstmt.executeUpdate();
        }
        catch(SQLException e) 
        {
            e.printStackTrace();
        }
    }

    public void saveLastSeen(long lastSeen)
    {
        String query = "UPDATE users SET last_seen = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query))
        {
            pstmt.setLong(1, lastSeen);
            pstmt.setString(2, user.getId());
            pstmt.executeUpdate();
        }
        catch(SQLException e) 
        {
            e.printStackTrace();
        }
    }

    public long getLastSeen()
    {
        long result = 0;
        String query = "SELECT last_seen FROM users WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query))
        {
            pstmt.setString(1, user.getId());
            try (ResultSet rs = pstmt.executeQuery())
            {
                if (rs.next()) 
                {
                    result = rs.getLong("last_seen");
                }
            }
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
        return result;
    }

    public void unreadMessagesPlusOne(Chat chat)
    {
        String chatId = (chat instanceof PV) ? 
            (((PV)chat).getFirstPerson().getId().equals(user.getId()) ? ((PV)chat).getSecondPerson().getId() : ((PV)chat).getFirstPerson().getId()) : 
            ((Group)chat).getId();
        String chatType = (chat instanceof PV) ? "pv" : "group";

        String query = "UPDATE chat_states SET unread_count = unread_count + 1 WHERE user_id = ? AND chat_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query))
        {
            pstmt.setString(1, user.getId());
            pstmt.setString(2, chatId);
            int affected = pstmt.executeUpdate();
            
            if (affected == 0) {
                String insertQuery = "INSERT INTO chat_states(user_id, chat_id, chat_type, state, unread_count) VALUES(?, ?, ?, 'normal', 1)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                    insertStmt.setString(1, user.getId());
                    insertStmt.setString(2, chatId);
                    insertStmt.setString(3, chatType);
                    insertStmt.executeUpdate();
                }
            }
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
    }

    public void readMessages(Chat chat)
    {
        String chatId = (chat instanceof PV) ? 
            (((PV)chat).getFirstPerson().getId().equals(user.getId()) ? ((PV)chat).getSecondPerson().getId() : ((PV)chat).getFirstPerson().getId()) : 
            ((Group)chat).getId();

        String query = "UPDATE chat_states SET unread_count = 0 WHERE user_id = ? AND chat_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query))
        {
            pstmt.setString(1, user.getId());
            pstmt.setString(2, chatId);
            pstmt.executeUpdate();
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
    }

    public int getUreadMessagesCount(Chat chat)
    {
        String chatId = (chat instanceof PV) ? 
            (((PV)chat).getFirstPerson().getId().equals(user.getId()) ? ((PV)chat).getSecondPerson().getId() : ((PV)chat).getFirstPerson().getId()) : 
            ((Group)chat).getId();

        String query = "SELECT unread_count FROM chat_states WHERE user_id = ? AND chat_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query))
        {
            pstmt.setString(1, user.getId());
            pstmt.setString(2, chatId);
            try (ResultSet rs = pstmt.executeQuery())
            {
                if (rs.next())
                {
                    return rs.getInt("unread_count");
                }
            }
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
        return 0;
    }
    
    public void savePv(PV pv)
    {
        String targetId = pv.getFirstPerson().getId().equals(user.getId()) ? pv.getSecondPerson().getId() : pv.getFirstPerson().getId();
        saveChatState(targetId, "pv", "normal");
    }

    public void saveGroup(Group group)
    {
        saveChatState(group.getId(), "group", "normal");
    }

    public void saveAvatar() {
        String query = "UPDATE users SET avatar = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query))
        {
            pstmt.setString(1, user.getAvatar() != null ? user.getAvatar() : "");
            pstmt.setString(2, user.getId());
            pstmt.executeUpdate();
        } 
        catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveBioAndOtherData()
    {
        String query = "UPDATE users SET bio = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query))
        {
            pstmt.setString(1, user.getBio() != null ? user.getBio() : "");
            pstmt.setString(2, user.getId());
            pstmt.executeUpdate();
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
    }

    public void saveContact(User contact)
    {
        String query = "INSERT OR IGNORE INTO contacts(user_id, contact_id) VALUES(?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query))
        {
            pstmt.setString(1, user.getId());
            pstmt.setString(2, contact.getId());
            pstmt.executeUpdate();
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
    }

    public void saveChatState(String chatId, String type, String state) {
        String query = "INSERT INTO chat_states(user_id, chat_id, chat_type, state, unread_count) VALUES(?, ?, ?, ?, COALESCE((SELECT unread_count FROM chat_states WHERE user_id = ? AND chat_id = ?), 0)) " +
                       "ON CONFLICT(user_id, chat_id) DO UPDATE SET state = excluded.state, chat_type = excluded.chat_type";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query))
        {
            pstmt.setString(1, user.getId());
            pstmt.setString(2, chatId);
            pstmt.setString(3, type);
            pstmt.setString(4, state);
            pstmt.setString(5, user.getId());
            pstmt.setString(6, chatId);
            pstmt.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getChatState(String chatId, String type) {
        String query = "SELECT state FROM chat_states WHERE user_id = ? AND chat_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query))
        {
            pstmt.setString(1, user.getId());
            pstmt.setString(2, chatId);
            try (ResultSet rs = pstmt.executeQuery())
            {
                if (rs.next())
                {
                    return rs.getString("state");
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return "normal";
    }

    public List<String> getPvIds()
    {
        List<String> ids = new ArrayList<>();
        String query = "SELECT chat_id FROM chat_states WHERE user_id = ? AND chat_type = 'pv'";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query))
        {
            pstmt.setString(1, user.getId());
            try (ResultSet rs = pstmt.executeQuery())
            {
                while (rs.next()) 
                {
                    ids.add(rs.getString("chat_id"));
                }
            }
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
        return ids;
    }

    public List<String> getGroupIds()
    {
        List<String> ids = new ArrayList<>();
        String query = "SELECT chat_id FROM chat_states WHERE user_id = ? AND chat_type = 'group'";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query))
        {
            pstmt.setString(1, user.getId());
            try (ResultSet rs = pstmt.executeQuery())
            {
                while (rs.next()) 
                {
                    ids.add(rs.getString("chat_id"));
                }
            }
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
        return ids;
    }

    public String getAvatar() {
        String query = "SELECT avatar FROM users WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query))
        {
            pstmt.setString(1, user.getId());
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

    public String getBio()
    {
        String bio = null;
        String query = "SELECT bio FROM users WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query))
        {
            pstmt.setString(1, user.getId());
            try (ResultSet rs = pstmt.executeQuery())
            {
                if (rs.next())
                {
                    bio = rs.getString("bio");
                }
            }
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
        return bio;
    }

    public List<String> getContactsId()
    {
        List<String> ids = new ArrayList<>();
        String query = "SELECT contact_id FROM contacts WHERE user_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query))
        {
            pstmt.setString(1, user.getId());
            try (ResultSet rs = pstmt.executeQuery())
            {
                while (rs.next()) 
                {
                    ids.add(rs.getString("contact_id"));
                }
            }
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
        return ids;
    }

    public List<String> getBlockedUsersId()
    {
        List<String> ids = new ArrayList<>();
        String query = "SELECT blocked_id FROM blocked_users WHERE user_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query))
        {
            pstmt.setString(1, user.getId());
            try (ResultSet rs = pstmt.executeQuery())
            {
                while (rs.next()) 
                {
                    ids.add(rs.getString("blocked_id"));
                }
            }
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
        return ids;
    }

    public void removePv(PV pv)
    {
        String targetId = pv.getFirstPerson().getId().equals(user.getId()) ? pv.getSecondPerson().getId() : pv.getFirstPerson().getId();
        String query = "DELETE FROM chat_states WHERE user_id = ? AND chat_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query))
        {
            pstmt.setString(1, user.getId());
            pstmt.setString(2, targetId);
            pstmt.executeUpdate();
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
    }

    public void removeGroup(Group group)
    {
        if (group == null || group.getId() == null) return;
        String query = "DELETE FROM chat_states WHERE user_id = ? AND chat_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query))
        {
            pstmt.setString(1, user.getId());
            pstmt.setString(2, group.getId());
            pstmt.executeUpdate();
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
    }

    public void removeContact(User contact)
    {
        String query = "DELETE FROM contacts WHERE user_id = ? AND contact_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query))
        {
            pstmt.setString(1, user.getId());
            pstmt.setString(2, contact.getId());
            pstmt.executeUpdate();
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
    }

    public void removeBlockedUser(User blockedUser)
    {
        String query = "DELETE FROM blocked_users WHERE user_id = ? AND blocked_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query))
        {
            pstmt.setString(1, user.getId());
            pstmt.setString(2, blockedUser.getId());
            pstmt.executeUpdate();
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
    }

    public void deleteUserDirectory() {
        // No Need in SQL
    }
}