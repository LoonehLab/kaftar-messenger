package api;

import security.*;
import dao.*;
import entity.*;

import java.util.List;

import com.sun.net.httpserver.HttpServer;

public class Account {

    private static final UserRepository userRepo = UserRepository.getInstance();
    private static final AllGroupsRepository groupRepo = AllGroupsRepository.getInstance();
    
    public static void login(HttpServer server){
        server.createContext("/api/login", exchange -> {
            Core.handleCORS(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) return;
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = Core.getRequestBody(exchange);
                String id = Core.extractJsonValue(body, "id"); //Temp
                String user = Core.extractJsonValue(body, "user");
                String pass = Core.extractJsonValue(body, "pass");
                pass = PasswordHasher.hashPassword(pass);
                if (id.isEmpty() || user.isEmpty() || pass.isEmpty()) {
                    Core.sendResponse(exchange, 400, "{\"error\":\"Missing fields\"}");
                    return;
                }
                if (userRepo.isExists(id)) { //Login
                    User temp = new User(id, user, pass, true);
                    User existingUser = userRepo.findUser(temp);
                    if (existingUser != null && existingUser.getPassword().equals(pass))
                        Core.sendResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Login successful\"}");
                    else Core.sendResponse(exchange, 401, "{\"error\":\"Wrong password\"}");
                }
                else { //SignUp
                    User newUser = new User(id, user, pass, true);
                    userRepo.save(newUser);
                    Core.sendResponse(exchange, 201, "{\"status\":\"created\",\"message\":\"User registered successfully\"}");
                }
            }
            else Core.sendResponse(exchange, 405, "Method Not Allowed");
        });
    }

    public static void load(HttpServer server) {
        server.createContext("/api/user/init", exchange -> {
            Core.handleCORS(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) return;
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                String userId = query != null && query.contains("userId=") ? query.split("userId=")[1] : "";
                User currentUser = userRepo.findUser(new User(userId, "", "", true));
                if (currentUser == null) {
                    Core.sendResponse(exchange, 404, "{\"error\":\"User not found\"}");
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

                Core.sendResponse(exchange, 200, json.toString());
            }
            else Core.sendResponse(exchange, 405, "Method Not Allowed");
        });
    }

    public static void search(HttpServer server) {
        server.createContext("/api/user/search", exchange -> {
            Core.handleCORS(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) return;
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                String searchInput = query != null && query.contains("searchInput=") ? query.split("searchInput=")[1] : "";
                List<User> allUsers = userRepo.getAllUsers();
                StringBuilder json = new StringBuilder();

                json.append("{\"results\":[");
                boolean first = true;
                for(User usr : allUsers) {
                    if (usr.getUserName().startsWith(searchInput)){
                        if (!first) json.append(",");
                        first = false;
                        json.append("{");
                        json.append("\"id\":\"").append(usr.getId()).append("\",");
                        json.append("\"username\":\"").append(usr.getUserName()).append("\"");
                        json.append("}");
                    }
                }
                json.append("]}");
                
                Core.sendResponse(exchange, 200, json.toString());
            }
            else Core.sendResponse(exchange, 405, "Method Not Allowed");
        });
    }
}
