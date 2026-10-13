package dao;

import entity.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AllGroupsRepository {
    
    private AllGroupsRepository() {}

    private static class GroupRepositoryInner {
        private static final AllGroupsRepository INSTANCE = new AllGroupsRepository();
    }

    public static AllGroupsRepository getInstance() {
        return GroupRepositoryInner.INSTANCE;
    }

    public void save(Group group) {
        String query = "INSERT OR REPLACE INTO groups(id, name, owner_id) VALUES(?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, group.getId());
            pstmt.setString(2, group.getName());
            pstmt.setString(3, group.getOwner().getId());
            pstmt.executeUpdate();
        }
        catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(Group group) {
        if (group == null || group.getId() == null) return;
        String query = "DELETE FROM groups WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, group.getId());
            pstmt.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public Group findById(String groupId) {
        String query = "SELECT * FROM groups WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, groupId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if(rs.next()) {
                    User owner = UserRepository.getInstance().findUserById(rs.getString("owner_id"));
                    if(owner == null) return null;
                    Group group = new Group(rs.getString("id"), owner, rs.getString("name"), owner);
                    group.setAvatar(rs.getString("avatar"));
                    return group;
                }
            }
        }
        catch(SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Group> findAll() {
        List<Group> groups = new ArrayList<>();
        String query = "SELECT * FROM groups";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while(rs.next()) {
                User owner = UserRepository.getInstance().findUserById(rs.getString("owner_id"));
                if(owner != null) {
                    Group group = new Group(rs.getString("id"), owner, rs.getString("name"), owner);
                    group.setAvatar(rs.getString("avatar"));
                    groups.add(group);
                }
            }
        }
        catch(SQLException e) {
            e.printStackTrace();
        }
        return groups;
    }
}