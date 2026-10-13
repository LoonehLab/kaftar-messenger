package api;

import dao.*;
import entity.*;

import com.sun.net.httpserver.HttpServer;
import java.util.List;

public class Block {

    private static final UserRepository userRepo = UserRepository.getInstance();

    public static void Manage(HttpServer server) {
        server.createContext("/api/block", exchange -> {
            Core.handleCORS(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) return;
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) { // Check if blocked
                String query = exchange.getRequestURI().getQuery();
                String userId = "";
                String targetId = "";
                if (query != null) {
                    if (query.contains("userId="))
                        userId = query.split("userId=")[1].split("&")[0];
                    if (query.contains("targetId="))
                        targetId = query.split("targetId=")[1].split("&")[0];
                }
                if (userId.isEmpty() || targetId.isEmpty()) {
                    Core.sendResponse(exchange, 400, "{\"error\":\"Missing userId or targetId\"}");
                    return;
                }
                User user = userRepo.findUserById(userId);
                User target = userRepo.findUserById(targetId);
                if (user == null || target == null) {
                    Core.sendResponse(exchange, 404, "{\"error\":\"User or Target not found\"}");
                    return;
                }
                InformationRepository infoRepo = user.getInformationRepository();
                List<String> blockedIds = infoRepo.getBlockedUsersId();
                boolean isBlocked = blockedIds.contains(targetId);
                String responseJson = String.format("{\"isBlocked\": %b}", isBlocked);
                Core.sendResponse(exchange, 200, responseJson);
            } 
            
            else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) { // Block / Unblock
                String body = Core.getRequestBody(exchange);
                String action = Core.extractJsonValue(body, "action"); // block/unblock
                String userId = Core.extractJsonValue(body, "userId");
                String targetId = Core.extractJsonValue(body, "targetId");
                if (userId.isEmpty() || targetId.isEmpty() || action.isEmpty()) {
                    Core.sendResponse(exchange, 400, "{\"error\":\"Missing fields\"}");
                    return;
                }
                User user = userRepo.findUserById(userId);
                User target = userRepo.findUserById(targetId);
                if (user == null || target == null) {
                    Core.sendResponse(exchange, 404, "{\"error\":\"User or Target not found\"}");
                    return;
                }
                InformationRepository infoRepo = user.getInformationRepository();

                if ("block".equalsIgnoreCase(action)) { // Block
                    if (infoRepo.getBlockedUsersId().contains(targetId)) {
                        Core.sendResponse(exchange, 400, "{\"error\":\"User is already blocked\"}");
                        return;
                    }
                    infoRepo.saveBlockedUser(target);
                    Core.sendResponse(exchange, 200, "{\"message\":\"User blocked successfully\"}");
                }
                else if ("unblock".equalsIgnoreCase(action)) { // Unblock
                    if (!infoRepo.getBlockedUsersId().contains(targetId)) {
                        Core.sendResponse(exchange, 400, "{\"error\":\"User is not blocked\"}");
                        return;
                    }
                    infoRepo.removeBlockedUser(target);
                    Core.sendResponse(exchange, 200, "{\"message\":\"User unblocked successfully\"}");
                }
                else Core.sendResponse(exchange, 400, "{\"error\":\"Invalid action\"}");
            } 
            else Core.sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
        });
    }
}