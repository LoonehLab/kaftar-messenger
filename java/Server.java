import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;

public class Server {
    private static final UserRepository userRepo = new UserRepository();
    // Chats: (TEMP)
    private static final List<PV> activePVs = new ArrayList<>();
    private static final List<Group> activeGroups = new ArrayList<>();

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
                if (id.isEmpty() || user.isEmpty() || pass.isEmpty()) {
                    sendResponse(exchange, 400, "{\"error\":\"Missing fields\"}");
                    return;
                }
                if (userRepo.isExists(id)) { //Login
                    User temp = new User(id, user, pass);
                    User existingUser = userRepo.findUser(temp);
                    if (existingUser != null && existingUser.getPassword().equals(pass))
                        sendResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Login successful\"}");
                    else sendResponse(exchange, 401, "{\"error\":\"Wrong password\"}");
                }
                else { //SignUp
                    User newUser = new User(id, user, pass);
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
                User currentUser = userRepo.findUser(new User(userId, "", ""));
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
                List<PV> userPVs = new ArrayList<>();
                for (PV p : activePVs)
                    if (p.getFirstPerson().getId().equals(userId) || p.getSecondPerson().getId().equals(userId)) userPVs.add(p);
                for (int i = 0; i < userPVs.size(); i++) {
                    PV p = userPVs.get(i);
                    User target = p.getFirstPerson().getId().equals(userId) ? p.getSecondPerson() : p.getFirstPerson();
                    json.append(String.format("{\"targetId\":\"%s\",\"targetName\":\"%s\"}", target.getId(), target.getUserName()));
                    if (i < userPVs.size() - 1) json.append(",");
                }
                json.append("],");

                // Group Chats
                json.append("\"groups\":[");
                List<Group> userGroups = new ArrayList<>();
                for (Group g : activeGroups) { //All Gropus (TEMP)
                    userGroups.add(g);
                }
                for (int i = 0; i < userGroups.size(); i++) {
                    Group g = userGroups.get(i);
                    json.append(String.format("{\"groupId\":\"%s\",\"groupName\":\"%s\"}", g.getId(), g.getName()));
                    if (i < userGroups.size() - 1) json.append(",");
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
                User sender = userRepo.findUser(new User(senderId, "", ""));
                User receiver = userRepo.findUser(new User(receiverId, "", ""));
                if (sender == null || receiver == null) {
                    sendResponse(exchange, 404, "{\"error\":\"Sender or Receiver not found\"}");
                    return;
                }
                PV targetPv = findOrCreatePV(sender, receiver);
                Message newMessage = new Message(0, senderId, receiverId, text, System.currentTimeMillis(), false, 0, false);
                targetPv.send(newMessage);
                sendResponse(exchange, 201, "{\"status\":\"success\",\"message\":\"Message sent to PV\"}");
            }

            else if ("GET".equalsIgnoreCase(method)) { // All Messages
                String query = exchange.getRequestURI().getQuery();
                String user1 = query != null && query.contains("user1=") ? query.split("user1=")[1].split("&")[0] : "";
                String user2 = query != null && query.contains("user2=") ? query.split("user2=")[1] : "";
                User u1 = userRepo.findUser(new User(user1, "", ""));
                User u2 = userRepo.findUser(new User(user2, "", ""));
                if (u1 == null || u2 == null) {
                    sendResponse(exchange, 404, "{\"error\":\"Users not found\"}");
                    return;
                }
                PV targetPv = findOrCreatePV(u1, u2);
                sendResponse(exchange, 200, convertMessagesToJson(targetPv.messages()));
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
                    User owner = userRepo.findUser(new User(ownerId, "", ""));
                    if (owner == null) {
                        sendResponse(exchange, 404, "{\"error\":\"Owner not found\"}");
                        return;
                    }
                    Group newGroup = new Group(owner, groupName, owner);
                    activeGroups.add(newGroup);
                    sendResponse(exchange, 201, String.format("{\"status\":\"success\",\"groupId\":\"%s\"}", newGroup.getId()));
                }

                else if ("send".equalsIgnoreCase(action)) { // Send Message
                    String groupId = extractJsonValue(body, "groupId");
                    String senderId = extractJsonValue(body, "senderId");
                    String text = extractJsonValue(body, "text");
                    Group targetGroup = null;
                    for (Group g : activeGroups) 
                        if (g.getId().equals(groupId)) { 
                            targetGroup = g; 
                            break; 
                        }
                    if (targetGroup == null) {
                        sendResponse(exchange, 404, "{\"error\":\"Group not found\"}");
                        return;
                    }
                    Message groupMessage = new Message(0, senderId, groupId, text, System.currentTimeMillis(), false, 0, false);
                    targetGroup.send(groupMessage);
                    sendResponse(exchange, 201, "{\"status\":\"success\",\"message\":\"Message sent to group\"}");
                }
            }
            
            else if ("GET".equalsIgnoreCase(method)) { // Group Messages
                String query = exchange.getRequestURI().getQuery();
                String groupId = query != null && query.contains("groupId=") ? query.split("groupId=")[1] : "";
                Group targetGroup = null;
                for (Group g : activeGroups)
                    if (g.getId().equals(groupId)) {
                        targetGroup = g;
                        break;
                    }
                if (targetGroup == null) {
                    sendResponse(exchange, 404, "{\"error\":\"Group not found\"}");
                    return;
                }
                sendResponse(exchange, 200, convertMessagesToJson(targetGroup.getMessages()));
            }
        });

        // =========( Initialization )=========
        server.setExecutor(null);
        server.start();
        System.out.println("Whisper Advanced API Server is running on port 8081...");
    }

    // =========( Helper Methods )=========
    private static PV findOrCreatePV(User u1, User u2) {
        for (PV p : activePVs)
            if ((p.getFirstPerson().getId().equals(u1.getId()) && p.getSecondPerson().getId().equals(u2.getId())) ||
                (p.getFirstPerson().getId().equals(u2.getId()) && p.getSecondPerson().getId().equals(u1.getId()))) {
                return p;
            }
        PV newPv = new PV(u1, u2);
        activePVs.add(newPv);
        return newPv;
    }

    private static String convertMessagesToJson(List<Message> messages) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < messages.size(); i++) {
            Message m = messages.get(i);
            if (m.getDeleted()) continue;
            String safeText = m.getText().replace("\"", "\\\"").replace("\n", "\\n");
            json.append(String.format(
                "{\"id\":%d,\"senderId\":\"%s\",\"receiverId\":\"%s\",\"text\":\"%s\",\"time\":%d,\"edited\":%b}",
                m.getId(), m.getSenderId(), m.getReceiverId(), safeText, m.getTime(), m.getEdited()
            ));
            if (i < messages.size() - 1) json.append(",");
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