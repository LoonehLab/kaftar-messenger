package dao;

import entity.*;

import java.util.*;
import java.sql.*;

public class ReportRepository 
{
    private ReportRepository() {}

    private static class ReportRepositoryInner
    {
        private static final ReportRepository INSTANCE = new ReportRepository();
    }

    public static ReportRepository getInstance()
    {
        return ReportRepositoryInner.INSTANCE;
    }

    public void saveReport(Message msg)
    {
        String query = "INSERT INTO reports(sender_id, message_text, message_id) VALUES(?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query))
        {
            pstmt.setString(1, msg.getSenderId());
            pstmt.setString(2, msg.getText());
            pstmt.setInt(3, msg.getId());
            pstmt.executeUpdate();
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
    }

    public Map<Integer , String[]> getReports()
    {
        int counter = 1;
        Map<Integer , String[]> reports = new HashMap<>();
        String query = "SELECT sender_id, message_text, message_id FROM reports";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query))
        {
            while(rs.next())
            {
                String[] data = new String[] {
                    rs.getString("sender_id"),
                    rs.getString("message_text"),
                    String.valueOf(rs.getInt("message_id"))
                };
                reports.put(counter, data);
                counter++;
            }
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
        return reports;
    }

    public void delete() {
        String query = "DELETE FROM reports";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(query);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
}