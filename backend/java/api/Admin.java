package api;

import dao.*;
import entity.*;

import java.util.List;
import java.util.Map;
import com.sun.net.httpserver.HttpServer;

public class Admin {

    private static final UserRepository userRepo = UserRepository.getInstance();
    private static final AllGroupsRepository groupRepo = AllGroupsRepository.getInstance();
    private static final ReportRepository reportRepo = ReportRepository.getInstance();

    private static final String ADMIN_USER = "poori";
    private static final String ADMIN_PASS = "popopo";

    public static void Manage(HttpServer server) {

        server.createContext("/api/admin/login", exchange -> {
            Core.handleCORS(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) return;
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = Core.getRequestBody(exchange);
                String username = Core.extractJsonValue(body, "username");
                String password = Core.extractJsonValue(body, "password");
                if (ADMIN_USER.equals(username) && ADMIN_PASS.equals(password))
                    Core.sendResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Welcome Admin\"}");
                else Core.sendResponse(exchange, 401, "{\"error\":\"Invalid Admin Credentials\"}");
            }
            else Core.sendResponse(exchange, 405, "Method Not Allowed");
        });

        server.createContext("/api/admin/users", exchange -> {
            Core.handleCORS(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) return;
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                List<User> users = userRepo.getAllUsers();
                StringBuilder json = new StringBuilder("[");
                for (int i=0; i<users.size(); i++) {
                    if (i>0) json.append(",");
                    User u = users.get(i);
                    json.append(String.format(
                        "{\"id\":\"%s\",\"username\":\"%s\",\"fullName\":\"%s\",\"phoneNumber\":\"%s\"}",
                        u.getId(), u.getUserName(), u.getFullName(), 
                        u.getPhoneNumber() != null ? u.getPhoneNumber() : ""
                    ));
                }
                json.append("]");
                Core.sendResponse(exchange, 200, json.toString());
            }
            else Core.sendResponse(exchange, 405, "Method Not Allowed");
        });

        server.createContext("/api/admin/groups", exchange -> {
            Core.handleCORS(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) return;
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                List<Group> allGroups = groupRepo.findAll();
                StringBuilder json = new StringBuilder("[");
                for (int i=0; i<allGroups.size(); i++) {
                    if (i>0) json.append(",");
                    Group g = allGroups.get(i);
                    json.append("{");
                    json.append(String.format("\"id\":\"%s\",\"name\":\"%s\",\"owner\":\"%s\",", 
                        g.getId(), g.getName(), g.getOwner() != null ? g.getOwner().getUserName() : "Unknown"));
                    json.append("\"members\":[");
                    List<User> members = g.getMembers();
                    for (int j=0; j<members.size(); j++) {
                        if (j>0) json.append(",");
                        User member = members.get(j);
                        json.append(String.format("{\"id\":\"%s\",\"username\":\"%s\",\"fullName\":\"%s\"}", 
                            member.getId(), member.getUserName(), member.getFullName()));
                    }
                    json.append("]");
                    json.append("}");
                }
                json.append("]");
                Core.sendResponse(exchange, 200, json.toString());
            }
            else Core.sendResponse(exchange, 405, "Method Not Allowed");
        });

        server.createContext("/api/admin/reports", exchange -> {
            Core.handleCORS(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) return;
            String method = exchange.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                Map<Integer, String[]> reports = reportRepo.getReports();
                StringBuilder json = new StringBuilder("[");
                if (reports != null && !reports.isEmpty()) {
                    boolean first = true;
                    for (Map.Entry<Integer, String[]> entry : reports.entrySet()) {
                        if (!first) json.append(",");
                        first = false;
                        String[] reportData = entry.getValue();
                        String sender = (reportData != null && reportData.length > 0) ? reportData[0] : "Unknown";
                        String content = (reportData != null && reportData.length > 1) ? reportData[1] : "No Content";
                        String realID = (reportData != null && reportData.length > 2) ? reportData[2].trim() : "0";
                        String cleanText = content.replace("\"", "\\\"").replace("\n", "\\n");
                        json.append(String.format(
                            "{\"messageId\":\"%s\",\"senderId\":\"%s\",\"content\":\"%s\"}",
                            realID, sender, cleanText
                        ));
                    }
                }
                json.append("]");
                Core.sendResponse(exchange, 200, json.toString());
            } 
            
            else if ("DELETE".equalsIgnoreCase(method)) {
                try {
                    reportRepo.delete(); 
                    Core.sendResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"All reports cleared successfully\"}");
                }
                catch (Exception e) {
                    Core.sendResponse(exchange, 500, "{\"error\":\"Failed to clear reports file\"}");
                }
            }
            else Core.sendResponse(exchange, 405, "Method Not Allowed");
        });
    }
}