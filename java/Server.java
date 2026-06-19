import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;

public class Server {
    private static final UserRepository userRepo = UserRepository.getInstance();
    private static final AllGroupsRepository groupRepo = AllGroupsRepository.getInstance();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);

        // =========( Login/SignUp )=========
        server.createContext("/api/login", exchange -> {
            handleCORS(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) return;
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = getRequestBody(exchange);
                String id = extractJsonValue(body, "id"); //Temp
                String user = extractJsonValue(body, "user");
                String pass = extractJsonValue(body, "pass");
                pass = PasswordHasher.hashPassword(pass);
                if (id.isEmpty() || user.isEmpty() || pass.isEmpty()) {
                    sendResponse(exchange, 400, "{\"error\":\"Missing fields\"}");
                    return;
                }
                if (userRepo.isExists(id)) { //Login
                    User temp = new User(id, user, pass, true);
                    User existingUser = userRepo.findUser(temp);
                    if (existingUser != null && existingUser.getPassword().equals(pass))
                        sendResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Login successful\"}");
                    else sendResponse(exchange, 401, "{\"error\":\"Wrong password\"}");
                }
                else { //SignUp
                    User newUser = new User(id, user, pass, true);
                    userRepo.save(newUser);
                    sendResponse(exchange, 201, "{\"status\":\"created\",\"message\":\"User registered successfully\"}");
                }
            }
            else sendResponse(exchange, 405, "Method Not Allowed");
        });
        
        // =========( UserInit )=========
        server.createContext("/api/user/init", exchange -> {
            handleCORS(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) return;
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                String userId = query != null && query.contains("userId=") ? query.split("userId=")[1] : "";
                User currentUser = userRepo.findUser(new User(userId, "", "", true));
                if (currentUser == null) {
                    sendResponse(exchange, 404, "{\"error\":\"User not found\"}");
                    return;
                }
                StringBuilder json = new StringBuilder();
                json.append("{");
                
                // Basic Data
                json.append(String.format("\"id\":\"%s\",\"userName\":\"%s\",", currentUser.getId(), currentUser.getUserName()));
                
                // Private Chats
                json.append("\"pvs\":[");
                List<String> pvIds = currentUser.getInformationRepository().getPvIds();
                for(int i=0 ; i<pvIds.size() ; i++) {
                    String targetId = pvIds.get(i);
                    User target = userRepo.findUser(new User(targetId , "" , "" , true));
                    if(target == null) continue;
                    json.append(String.format( "{\"targetId\":\"%s\",\"targetName\":\"%s\"}", target.getId(), target.getUserName()));
                    if(i < pvIds.size()-1) json.append(",");
                }
                json.append("],");

                // Group Chats
                json.append("\"groups\":[");
                List<String> groupIds =currentUser.getInformationRepository().getGroupIds();
                boolean first = true;
                for(String groupId : groupIds) {
                    Group g = groupRepo.findById(groupId);
                    if(g == null) continue;
                    if(!first) json.append(",");
                    first = false;
                    json.append( String.format( "{\"groupId\":\"%s\",\"groupName\":\"%s\"}", g.getId(), g.getName()));
                }
                json.append("]");
                json.append("}");

                sendResponse(exchange, 200, json.toString());
            }
            else sendResponse(exchange, 405, "Method Not Allowed");
        });

        // =========( PV Chatrooms )=========
        server.createContext("/api/pv", exchange -> {
            handleCORS(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) return;
            String method = exchange.getRequestMethod();

            if ("POST".equalsIgnoreCase(method)) { // Send Message
                String body = getRequestBody(exchange);
                String senderId = extractJsonValue(body, "senderId");
                String receiverId = extractJsonValue(body, "receiverId");
                String text = extractJsonValue(body, "text");
                User sender = userRepo.findUser(new User(senderId, "", "", true));
                User receiver = userRepo.findUser(new User(receiverId, "", "", true));
                if (sender == null || receiver == null) {
                    sendResponse(exchange, 404, "{\"error\":\"Sender or Receiver not found\"}");
                    return;
                }
                PV targetPv = findOrCreatePV(sender, receiver);
                Message newMessage = new Message(0, senderId, receiverId, text, System.currentTimeMillis(), false, false);
                targetPv.send(newMessage);
                sendResponse(exchange, 201, "{\"status\":\"success\",\"message\":\"Message sent to PV\"}");
            } 

            else if ("GET".equalsIgnoreCase(method)) { // All Messages
                String query = exchange.getRequestURI().getQuery();
                String user1 = query != null && query.contains("user1=") ? query.split("user1=")[1].split("&")[0] : "";
                String user2 = query != null && query.contains("user2=") ? query.split("user2=")[1] : "";
                User u1 = userRepo.findUser(new User(user1, "", "", true));
                User u2 = userRepo.findUser(new User(user2, "", "", true));
                if (u1 == null || u2 == null) {
                    sendResponse(exchange, 404, "{\"error\":\"Users not found\"}");
                    return;
                }
                PV targetPv = findOrCreatePV(u1, u2);
                sendResponse(exchange, 200, convertMessagesToJson(targetPv.messages()));
            }

            else if ("DELETE".equalsIgnoreCase(method)) { // Delete PV
                String body = getRequestBody(exchange);
                String u1 = extractJsonValue(body, "user1");
                String u2 = extractJsonValue(body, "user2");
                User user1 = userRepo.findUser(new User(u1, "", "", true));
                User user2 = userRepo.findUser(new User(u2, "", "", true));
                if (user1 == null || user2 == null) {
                    sendResponse(exchange, 404, "{\"error\":\"Users not found\"}");
                    return;
                }
                PV targetPv = findOrCreatePV(user1, user2);
                targetPv.deletePv("both");
                sendResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"PV chat history deleted\"}");
                return;
            }
        });

        // =========( Group Chatrooms )=========
        server.createContext("/api/group", exchange -> {
            handleCORS(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) return;
            String method = exchange.getRequestMethod();
            if ("POST".equalsIgnoreCase(method)) { // Create Group or Send Message
                String body = getRequestBody(exchange);
                String action = extractJsonValue(body, "action");

                if ("create".equalsIgnoreCase(action)) { // Create Group
                    String ownerId = extractJsonValue(body, "ownerId");
                    String groupName = extractJsonValue(body, "name");
                    User owner = userRepo.findUser(new User(ownerId, "", "", true));
                    if (owner == null) {
                        sendResponse(exchange, 404, "{\"error\":\"Owner not found\"}");
                        return;
                    }
                    Group newGroup = new Group(owner, groupName, owner);
                    groupRepo.save(newGroup);
                    owner.getInformationRepository().saveGroup(newGroup);
                    sendResponse(exchange, 201, String.format("{\"status\":\"success\",\"groupId\":\"%s\"}", newGroup.getId()));
                } 

                else if ("send".equalsIgnoreCase(action)) { // Send Message
                    String groupId = extractJsonValue(body, "groupId");
                    String senderId = extractJsonValue(body, "senderId");
                    String text = extractJsonValue(body, "text");
                    Group targetGroup = findGroupById(groupId);
                    if (targetGroup == null) {
                        sendResponse(exchange, 404, "{\"error\":\"Group not found\"}");
                        return;
                    }
                    Message groupMessage = new Message(0, senderId, groupId, text, System.currentTimeMillis(), false, false);
                    targetGroup.send(groupMessage);
                    sendResponse(exchange, 201, "{\"status\":\"success\",\"message\":\"Message sent to group\"}");
                }

                else if ("addMember".equalsIgnoreCase(action)) { // Add Member
                    String groupId = extractJsonValue(body, "groupId");
                    String memberId = extractJsonValue(body, "memberId");
                    Group targetGroup = findGroupById(groupId);
                    User newMem = userRepo.findUser(new User(memberId, "", "", true));
                    if (targetGroup != null && newMem != null) {
                        targetGroup.addMember(newMem);
                        newMem.getInformationRepository().saveGroup(targetGroup);
                        newMem.addGroup(targetGroup);
                        sendResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Member added successfully\"}");
                    }
                    else sendResponse(exchange, 404, "{\"error\":\"Group or User not found\"}");
                }

                else if ("removeMember".equalsIgnoreCase(action)) { // Remove Member
                    String groupId = extractJsonValue(body, "groupId");
                    String memberId = extractJsonValue(body, "memberId");
                    Group targetGroup = findGroupById(groupId);
                    User mem = userRepo.findUser(new User(memberId, "", "", true));
                    if (targetGroup != null && mem != null) {
                        targetGroup.leftOrRemove(mem);
                        sendResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Member removed successfully\"}");
                    }
                    else sendResponse(exchange, 404, "{\"error\":\"Group or User not found\"}");
                }
            } 

            else if ("GET".equalsIgnoreCase(method)) { // Group Messages
                String query = exchange.getRequestURI().getQuery();
                String groupId = query != null && query.contains("groupId=") ? query.split("groupId=")[1] : "";
                Group targetGroup = findGroupById(groupId);
                if (targetGroup == null) {
                    sendResponse(exchange, 404, "{\"error\":\"Group not found\"}");
                    return;
                }
                sendResponse(exchange, 200, convertMessagesToJson(targetGroup.getMessages()));
            }

            else if ("DELETE".equalsIgnoreCase(method)) { // Delete Group
                String body = getRequestBody(exchange);
                String groupId = extractJsonValue(body, "groupId");
                Group targetGroup = findGroupById(groupId);
                if (targetGroup != null) {
                    targetGroup.deleteChat();
                    List<User> safeMembers = new ArrayList<>(targetGroup.getMembers());
                    for (User u : safeMembers) {
                        targetGroup.leftOrRemove(u);
                    }
                    targetGroup.deleteGroupForAll();
                    sendResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Group deleted for all\"}");
                } 
                else sendResponse(exchange, 404, "{\"error\":\"Group not found\"}");
            }
        });

        // =========( Contacts Management )=========
        server.createContext("/api/contacts", exchange -> {
            handleCORS(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) return;
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                String userId = "";
                if (query != null && query.contains("userId="))
                    userId = query.split("userId=")[1].split("&")[0];
                User user = userRepo.findUserById(userId);
                if (userId.isEmpty() || user == null) {
                    sendResponse(exchange, 400, "{\"error\":\"User not found\"}");
                    return;
                }
                InformationRepository infoRepo = user.getInformationRepository();
                List<String> contactIds = infoRepo.getContactsId();
                StringBuilder jsonBuilder = new StringBuilder("[");
                for (int i = 0; i < contactIds.size(); i++) {
                    User target = userRepo.findUser(new User(contactIds.get(i) , "" , "" , true));
                    jsonBuilder.append(String.format("{\"userId\":\"%s\",\n\"userName\":\"%s\"}", contactIds.get(i), target.getUserName() ));
                    if (i < contactIds.size() - 1) jsonBuilder.append(",");
                }
                jsonBuilder.append("]");
                sendResponse(exchange, 200, jsonBuilder.toString());
            } 
            
            else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = getRequestBody(exchange);
                String action = extractJsonValue(body, "action");
                String userId = extractJsonValue(body, "userId");
                String contactId = extractJsonValue(body, "contactId");
                if (userId.isEmpty() || contactId.isEmpty() || action.isEmpty()) {
                    sendResponse(exchange, 400, "{\"error\":\"Missing fields\"}");
                    return;
                }
                User user = userRepo.findUserById(userId);
                User contact = userRepo.findUserById(contactId);
                if (user == null || contact == null) {
                    sendResponse(exchange, 404, "{\"error\":\"User or Contact not found\"}");
                    return;
                }
                InformationRepository infoRepo = user.getInformationRepository();
                if ("add".equalsIgnoreCase(action)) {
                    if (infoRepo.getContactsId().contains(contactId)) {
                        sendResponse(exchange, 400, "{\"error\":\"Contact already exists\"}");
                        return;
                    }
                    infoRepo.saveContact(contact);
                    if (!user.getContacts().contains(contact)) user.getContacts().add(contact);
                    sendResponse(exchange, 200, "{\"message\":\"Contact added successfully\"}");
                } 
                
                else if ("remove".equalsIgnoreCase(action)) {
                    infoRepo.removeContact(contact);
                    for (User usr : user.getContacts())
                        if (usr.getId().equals(contactId)) {
                            user.getContacts().remove(usr);
                            break;
                        }
                    sendResponse(exchange, 200, "{\"message\":\"Contact removed successfully\"}");
                } 
                
                else {
                    sendResponse(exchange, 400, "{\"error\":\"Invalid action\"}");
                }
            } 
            
            else {
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        });

        // =========( Message Management )=========
        server.createContext("/api/message", exchange -> {
            handleCORS(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) return;
            String body = getRequestBody(exchange);
            String chatType = extractJsonValue(body, "chatType");
            String msgIdStr = extractJsonValue(body, "messageId");
            int messageId = msgIdStr.isEmpty() ? -1 : Integer.parseInt(msgIdStr);
            Message targetMsg = null;
            Chat targetChat = null;
            if ("pv".equalsIgnoreCase(chatType)) {
                String u1 = extractJsonValue(body, "user1");
                String u2 = extractJsonValue(body, "user2");
                User user1 = userRepo.findUser(new User(u1, "", "", true));
                User user2 = userRepo.findUser(new User(u2, "", "", true));
                if (user1 != null && user2 != null) {
                    PV pv = findOrCreatePV(user1, user2);
                    targetChat = pv;
                    for (Message m : pv.messages())
                        if (m.getId() == messageId) {
                            targetMsg = m;
                            break;
                        }
                }
            }
            else if ("group".equalsIgnoreCase(chatType)) {
                String groupId = extractJsonValue(body, "groupId");
                Group g = findGroupById(groupId);
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
                sendResponse(exchange, 404, "{\"error\":\"Message or Chat not found\"}");
                return;
            }

            if ("PUT".equalsIgnoreCase(exchange.getRequestMethod())) {
                String newText = extractJsonValue(body, "newText");
                targetChat.edit(newText, targetMsg);
                sendResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Message edited successfully\"}");
            }

            else if ("DELETE".equalsIgnoreCase(exchange.getRequestMethod())) {
                targetChat.deleteMessage(targetMsg);
                sendResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Message deleted successfully\"}");
            }

            else {
                sendResponse(exchange, 405, "Method Not Allowed");
            }
        });

        // =========( Initialization )=========
        server.setExecutor(null);
        server.start();
        System.out.println("Whisper Advanced API Server is running on port 8081...");
    }

    // =========( Helper Methods )=========
    private static PV findOrCreatePV(User u1 , User u2){
        PV pv = findPV(u1 , u2);
        if(pv != null) return pv;
        PV newPv;
        if(u1.getId().equals(u2.getId())) newPv = new SavedMessage(u1 , u1);
        else newPv = new PV(u1 , u2);
        u1.getInformationRepository().savePv(newPv);
        u2.getInformationRepository().savePv(newPv);

        return newPv;
    }
    
    private static PV findPV(User u1 , User u2) {
        List<String> ids = u1.getInformationRepository().getPvIds();
        for(String id : ids)
            if(id.equals(u2.getId())) return new PV(u1 , u2);

        return null;
    }

    private static Group findGroupById(String groupId)
    {
        return groupRepo.findById(groupId);
    }

    private static String convertMessagesToJson(List<Message> messages) {
    StringBuilder json = new StringBuilder("[");
    boolean first = true;
    for (int i = 0; i < messages.size(); i++) {
        Message m = messages.get(i);
        if (m.getDeleted()) continue;
        if (!first) json.append(",");
        first = false;
        String safeText = m.getText().replace("\"", "\\\"").replace("\n", "\\n");
        json.append(String.format(
            "{\"id\":%d,\"senderId\":\"%s\",\"receiverId\":\"%s\",\"text\":\"%s\",\"time\":%d,\"edited\":%b}",
            m.getId(), m.getSenderId(), m.getReceiverId(), safeText, m.getTime(), m.getEdited()
        ));
    }
    json.append("]");
    return json.toString();
}

    private static void handleCORS(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
        }
    }

    private static String getRequestBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) { sb.append(line); }
        return sb.toString();
    }

    private static String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\"";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) return "";
        int colonIndex = json.indexOf(":", keyIndex + searchKey.length());
        if (colonIndex == -1) return "";
        int startQuote = json.indexOf("\"", colonIndex);
        if (startQuote == -1) return "";
        int endQuote = json.indexOf("\"", startQuote + 1);
        if (endQuote == -1) return "";
        return json.substring(startQuote + 1, endQuote);
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes("UTF-8");
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}