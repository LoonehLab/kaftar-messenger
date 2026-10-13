package dao;

import entity.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {
   
   private UserRepository() {
      // Singleton
   }

   private static class UserRepositoryInner {
      private static final UserRepository INSTANCE = new UserRepository();
   }

   public static UserRepository getInstance() { 
      return UserRepositoryInner.INSTANCE; 
   }

   public void save(User user) {
      String query = "INSERT INTO users(id, username, full_name, password, avatar, bio, last_seen) VALUES(?, ?, ?, ?, ?, ?, ?)";
      try (Connection conn = DatabaseManager.getConnection();
           PreparedStatement pstmt = conn.prepareStatement(query)) {
         pstmt.setString(1, user.getId());
         pstmt.setString(2, user.getUserName());
         pstmt.setString(3, user.getFullName());
         pstmt.setString(4, user.getPassword());
         pstmt.setString(5, user.getAvatar());
         pstmt.setString(6, user.getBio());
         pstmt.setLong(7, 0); // Default = 0
         pstmt.executeUpdate();
      }
      catch (SQLException e) {
         e.printStackTrace();
      }
   }

   public void delete(User user) {
      if (user == null || user.getId() == null) return;
      String query = "DELETE FROM users WHERE id = ?";
      try (Connection conn = DatabaseManager.getConnection();
           PreparedStatement pstmt = conn.prepareStatement(query)) {
         pstmt.setString(1, user.getId());
         pstmt.executeUpdate();
      }
      catch (SQLException e) {
         e.printStackTrace();
      }
   }

   public void changeFullName(String userId, String newName) {
      String query = "UPDATE users SET full_name = ? WHERE id = ?";
      try (Connection conn = DatabaseManager.getConnection();
           PreparedStatement pstmt = conn.prepareStatement(query)) {
         pstmt.setString(1, newName);
         pstmt.setString(2, userId);
         pstmt.executeUpdate();
      }
      catch (SQLException e) {
         e.printStackTrace();
      }
   }

   public void changePassword(String userId, String newPassword) {
      String query = "UPDATE users SET password = ? WHERE id = ?";
      try (Connection conn = DatabaseManager.getConnection();
           PreparedStatement pstmt = conn.prepareStatement(query)) {
         pstmt.setString(1, newPassword);
         pstmt.setString(2, userId);
         pstmt.executeUpdate();
      }
      catch (SQLException e) {
         e.printStackTrace();
      }
   }

   public boolean isExists(String id) {
      String query = "SELECT 1 FROM users WHERE id = ?";
      try (Connection conn = DatabaseManager.getConnection();
           PreparedStatement pstmt = conn.prepareStatement(query)) {
         pstmt.setString(1, id);
         try (ResultSet rs = pstmt.executeQuery()) {
            return rs.next();
         }
      }
      catch (SQLException e) {
         e.printStackTrace();
      }
      return false;
   }

   public User findUserById(String id) {
      String query = "SELECT * FROM users WHERE id = ?";
      try (Connection conn = DatabaseManager.getConnection();
           PreparedStatement pstmt = conn.prepareStatement(query)) {
         pstmt.setString(1, id);
         try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
               User user = new User(
                  rs.getString("id"),
                  rs.getString("username"),
                  rs.getString("full_name"),
                  rs.getString("password"),
                  true
               );
               user.setAvatar(rs.getString("avatar"));
               user.setBio(rs.getString("bio"));
               return user;
            }
         }
      }
      catch (SQLException e) {
         e.printStackTrace();
      }
      return null;
   }

   public List<User> getAllUsers() {
      List<User> users = new ArrayList<>();
      String query = "SELECT * FROM users";
      try (Connection conn = DatabaseManager.getConnection();
           Statement stmt = conn.createStatement();
           ResultSet rs = stmt.executeQuery(query)) {
         while (rs.next()) {
            User user = new User(
               rs.getString("id"),
               rs.getString("username"),
               rs.getString("full_name"),
               rs.getString("password"),
               true
            );
            user.setAvatar(rs.getString("avatar"));
            user.setBio(rs.getString("bio"));
            users.add(user);
         }
      } catch (SQLException e) {
         e.printStackTrace();
      }
      return users;
   }

   public User findUserByAnyField(String username, String fullName) {
      String query = "SELECT * FROM users WHERE username = ? OR full_name = ?";
      try (Connection conn = DatabaseManager.getConnection();
           PreparedStatement pstmt = conn.prepareStatement(query)) {
         pstmt.setString(1, username != null ? username : "");
         pstmt.setString(2, fullName != null ? fullName : "");
         try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
               User user = new User(
                  rs.getString("id"),
                  rs.getString("username"),
                  rs.getString("full_name"),
                  rs.getString("password"),
                  true
               );
               user.setAvatar(rs.getString("avatar"));
               user.setBio(rs.getString("bio"));
               return user;
            }
         }
      } catch (SQLException e) {
         e.printStackTrace();
      }
      return null;
   }
}
