package api;

import dao.*;
import entity.*;

import java.util.ArrayList;
import java.util.List;

import com.sun.net.httpserver.HttpServer;

public class Chatroom {

    private static final UserRepository userRepo = UserRepository.getInstance();
    private static final AllGroupsRepository groupRepo = AllGroupsRepository.getInstance();

    public static void loadPV(HttpServer server) {
        server.createContext("/api/pv", exchange -> {
            Core.handleCORS(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) return;
            String method = exchange.getRequestMethod();

            if ("POST".equalsIgnoreCase(method)) { // Send Message
                String body = Core.getRequestBody(exchange);
                String senderId = Core.extractJsonValue(body, "senderId");
                String receiverId = Core.extractJsonValue(body, "receiverId");
                String text = Core.extractJsonValue(body, "text");
                User sender = userRepo.findUserById(senderId);
                sender.updateLastSeen();
                User receiver = userRepo.findUserById(receiverId);
                if (sender == null || receiver == null) {
                    Core.sendResponse(exchange, 404, "{\"error\":\"Sender or Receiver not found\"}");
                    return;
                }
                PV targetPv = Core.findOrCreatePV(sender, receiver);
                Message newMessage = new Message(0, senderId, receiverId, text, System.currentTimeMillis(), false, false);
                targetPv.send(newMessage);
                Core.sendResponse(exchange, 201, "{\"status\":\"success\",\"message\":\"Message sent to PV\"}");
            } 

            else if ("GET".equalsIgnoreCase(method)) { // All Messages
                String query = exchange.getRequestURI().getQuery();
                String user1 = query != null && query.contains("user1=") ? query.split("user1=")[1].split("&")[0] : "";
                String user2 = query != null && query.contains("user2=") ? query.split("user2=")[1] : "";
                User u1 = userRepo.findUserById(user1);
                User u2 = userRepo.findUserById(user2);
                if (u1 == null || u2 == null) {
                    Core.sendResponse(exchange, 404, "{\"error\":\"Users not found\"}");
                    return;
                }
                PV targetPv = Core.findOrCreatePV(u1, u2);
                Core.sendResponse(exchange, 200, Core.convertMessagesToJson(targetPv.messages()));
            }

            else if ("DELETE".equalsIgnoreCase(method)) { // Delete PV
                String body = Core.getRequestBody(exchange);
                String u1 = Core.extractJsonValue(body, "user1");
                String u2 = Core.extractJsonValue(body, "user2");
                User user1 = userRepo.findUserById(u1);
                User user2 = userRepo.findUserById(u2);
                if (user1 == null || user2 == null) {
                    Core.sendResponse(exchange, 404, "{\"error\":\"Users not found\"}");
                    return;
                }
                PV targetPv = Core.findOrCreatePV(user1, user2);
                targetPv.deletePv("both"); 
                user1.removePv(targetPv);
                user2.removePv(targetPv);
                user1.getInformationRepository().removePv(targetPv);
                user2.getInformationRepository().removePv(targetPv);
                Core.sendResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"PV chat history deleted\"}");
                return;
            }
        });
    }

    public static void loadGroup(HttpServer server) {
        server.createContext("/api/group", exchange -> {
            Core.handleCORS(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) return;
            String method = exchange.getRequestMethod();
            if ("POST".equalsIgnoreCase(method)) { // Create Group or Send Message
                String body = Core.getRequestBody(exchange);
                String action = Core.extractJsonValue(body, "action");

                if ("create".equalsIgnoreCase(action)) { // Create Group
                    String ownerId = Core.extractJsonValue(body, "ownerId");
                    String groupName = Core.extractJsonValue(body, "name");
                    User owner = userRepo.findUserById(ownerId);
                    if (owner == null) {
                        Core.sendResponse(exchange, 404, "{\"error\":\"Owner not found\"}");
                        return;
                    }
                    Group newGroup = new Group(owner, groupName, owner);
                    groupRepo.save(newGroup);
                    owner.getInformationRepository().saveGroup(newGroup);
                    Core.sendResponse(exchange, 201, String.format("{\"status\":\"success\",\"groupId\":\"%s\"}", newGroup.getId()));
                } 

                else if ("send".equalsIgnoreCase(action)) { // Send Message
                    String groupId = Core.extractJsonValue(body, "groupId");
                    String senderId = Core.extractJsonValue(body, "senderId");
                    String text = Core.extractJsonValue(body, "text");

                    User sender = userRepo.findUserById(senderId);
                    if (sender != null) {
                        sender.updateLastSeen();
                    }

                    Group targetGroup = Core.findGroupById(groupId);
                    if (targetGroup == null) {
                        Core.sendResponse(exchange, 404, "{\"error\":\"Group not found\"}");
                        return;
                    }
                    Message groupMessage = new Message(0, senderId, groupId, text, System.currentTimeMillis(), false, false);
                    targetGroup.send(groupMessage);
                    Core.sendResponse(exchange, 201, "{\"status\":\"success\",\"message\":\"Message sent to group\"}");
                }

                else if ("addMember".equalsIgnoreCase(action)) { // Add Member
                    String groupId = Core.extractJsonValue(body, "groupId");
                    String memberId = Core.extractJsonValue(body, "memberId");
                    Group targetGroup = Core.findGroupById(groupId);
                    User newMem = userRepo.findUserById(memberId);
                    if (targetGroup != null && newMem != null) {
                        targetGroup.addMember(newMem);
                        newMem.getInformationRepository().saveGroup(targetGroup);
                        newMem.addGroup(targetGroup);
                        Core.sendResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Member added successfully\"}");
                    }
                    else Core.sendResponse(exchange, 404, "{\"error\":\"Group or User not found\"}");
                }

                else if ("removeMember".equalsIgnoreCase(action)) { // Remove Member
                    String groupId = Core.extractJsonValue(body, "groupId");
                    String memberId = Core.extractJsonValue(body, "memberId");
                    Group targetGroup = Core.findGroupById(groupId);
                    User mem = userRepo.findUserByAnyField(memberId,"");
                    if (targetGroup != null && mem != null) {
                        targetGroup.leftOrRemove(mem);
                        Core.sendResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Member removed successfully\"}");
                    }
                    else Core.sendResponse(exchange, 404, "{\"error\":\"Group or User not found\"}");
                }
            } 

            else if ("GET".equalsIgnoreCase(method)) { // Group Messages
                String query = exchange.getRequestURI().getQuery();
                String groupId = query != null && query.contains("groupId=") ? query.split("groupId=")[1] : "";
                Group targetGroup = Core.findGroupById(groupId);
                if (targetGroup == null) {
                    Core.sendResponse(exchange, 404, "{\"error\":\"Group not found\"}");
                    return;
                }

                String action = "";
                if (query != null && query.contains("action=")) {
                    action = query.split("action=")[1].split("&")[0];
                }

                if ("members".equalsIgnoreCase(action)) {
                    int count = targetGroup.getGroupRepository().getMemberCount();
                    Core.sendResponse(exchange, 200, "{\"count\":" + count + "}");
                    return;
                }
                Core.sendResponse(exchange, 200, Core.convertMessagesToJson(targetGroup.getMessages()));
            }

            else if ("DELETE".equalsIgnoreCase(method)) { // Delete Group
                String body = Core.getRequestBody(exchange);
                String groupId = Core.extractJsonValue(body, "groupId");
                Group targetGroup = Core.findGroupById(groupId);
                if (targetGroup != null) {
                    targetGroup.deleteChat();
                    List<User> safeMembers = new ArrayList<>(targetGroup.getMembers());
                    for (User u : safeMembers) {
                        u.removeGroup(targetGroup); 
                        u.getInformationRepository().removeGroup(targetGroup); 
                        targetGroup.leftOrRemove(u); 
                    }
                    targetGroup.deleteGroupForAll(); 
                    groupRepo.delete(targetGroup);

                    Core.sendResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Group deleted for all\"}");
                } 
                else Core.sendResponse(exchange, 404, "{\"error\":\"Group not found\"}");
            }
        });
    }

    public static void setState(HttpServer server) {
        server.createContext("/api/chatroom/state", exchange -> {
            Core.handleCORS(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) return;
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = Core.getRequestBody(exchange);
                String userId = Core.extractJsonValue(body, "userId");
                String chatId = Core.extractJsonValue(body, "chatId");
                String type = Core.extractJsonValue(body, "type");
                String action = Core.extractJsonValue(body, "action");
                User user = userRepo.findUserById(userId);
                if (user == null) {
                    Core.sendResponse(exchange, 404, "{\"error\":\"User not found\"}");
                    return;
                }
                Chat targetChat = null;
                if ("pv".equalsIgnoreCase(type)) {
                    User receiver = userRepo.findUserById(chatId);
                    if (receiver != null) targetChat = Core.findOrCreatePV(user, receiver);
                }
                else if ("group".equalsIgnoreCase(type)) targetChat = Core.findGroupById(chatId);
                if (targetChat == null) {
                    Core.sendResponse(exchange, 404, "{\"error\":\"Chat room not found\"}");
                    return;
                }
                String newState = "normal";
                if ("pin".equalsIgnoreCase(action)) newState = "pinned";
                else if ("archive".equalsIgnoreCase(action)) newState = "archived";
                else if ("unpin".equalsIgnoreCase(action) || "unarchive".equalsIgnoreCase(action)) newState = "normal";
                else {
                    Core.sendResponse(exchange, 400, "{\"error\":\"Invalid action. Choose pin, unpin, archive, or unarchive.\"}");
                    return;
                }
                targetChat.setChatState(user, type, chatId, newState);
                Core.sendResponse(exchange, 200, String.format(
                    "{\"status\":\"success\",\"message\":\"Chat status updated to %s successfully\"}", newState));
            }
            else Core.sendResponse(exchange, 405, "Method Not Allowed");
        });
    }
}
