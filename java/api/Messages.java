package api;

import dao.*;
import entity.*;

import com.sun.net.httpserver.HttpServer;

public class Messages {
    
    private static final UserRepository userRepo = UserRepository.getInstance();

    public static void Manage (HttpServer server){
        server.createContext("/api/message", exchange -> {
            Core.handleCORS(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) return;
            String body = Core.getRequestBody(exchange);
            String chatType = Core.extractJsonValue(body, "chatType");
            String msgIdStr = Core.extractJsonValue(body, "messageId");
            int messageId = msgIdStr.isEmpty() ? -1 : Integer.parseInt(msgIdStr);
            Message targetMsg = null;
            Chat targetChat = null;
            if ("pv".equalsIgnoreCase(chatType)) {
                String u1 = Core.extractJsonValue(body, "user1");
                String u2 = Core.extractJsonValue(body, "user2");
                User user1 = userRepo.findUserById(u1);
                User user2 = userRepo.findUserById(u2);
                if (user1 != null && user2 != null) {
                    PV pv = Core.findOrCreatePV(user1, user2);
                    targetChat = pv;
                    for (Message m : pv.messages())
                        if (m.getId() == messageId) {
                            targetMsg = m;
                            break;
                        }
                }
            }
            else if ("group".equalsIgnoreCase(chatType)) {
                String groupId = Core.extractJsonValue(body, "groupId");
                Group g = Core.findGroupById(groupId);
                targetChat = g;
                if (g != null) {
                    for (Message m : g.getMessages())
                        if (m.getId() == messageId) {
                            targetMsg = m;
                            break;
                        }
                }
            }
            if (targetMsg == null || targetChat == null) {
                Core.sendResponse(exchange, 404, "{\"error\":\"Message or Chat not found\"}");
                return;
            }

            if ("PUT".equalsIgnoreCase(exchange.getRequestMethod())) {
                String newText = Core.extractJsonValue(body, "newText");
                targetChat.edit(newText, targetMsg);
                Core.sendResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Message edited successfully\"}");
            }

            else if ("DELETE".equalsIgnoreCase(exchange.getRequestMethod())) {
                targetChat.deleteMessage(targetMsg);
                Core.sendResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Message deleted successfully\"}");
            }

            else {
                Core.sendResponse(exchange, 405, "Method Not Allowed");
            }
        });
    }
}