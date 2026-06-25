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
                String id = Core.extractJsonValue(body, "id"); 
                String user = Core.extractJsonValue(body, "user"); 
                String fullName = Core.extractJsonValue(body, "fullName");
                String pass = Core.extractJsonValue(body, "pass");

                User existingUser = null;
                if (!id.isEmpty() && userRepo.isExists(id)) {
                    existingUser = userRepo.findUserById(id);
                } else if (!user.isEmpty() || !fullName.isEmpty()) {
                    existingUser = userRepo.findUserByAnyField(user, fullName);
                }

                if (existingUser != null) { // Login
                    String hashedPass = PasswordHasher.hashPassword(pass);
                    if (existingUser.getPassword().equals(hashedPass))
                        Core.sendResponse(exchange, 200, String.format("{\"status\":\"success\",\"message\":\"Login successful\",\"id\":\"%s\"}", existingUser.getId()));
                    else 
                        Core.sendResponse(exchange, 401, "{\"error\":\"Wrong password\"}");
                }
                else { // SignUp
                    if (user.isEmpty() || fullName.isEmpty() || pass.isEmpty()) {
                        Core.sendResponse(exchange, 400, "{\"error\":\"Missing fields\"}");
                        return;
                    }
                    if (!validatePassword(pass, user)) {
                        Core.sendResponse(exchange, 400, "{\"error\":\"Invalid password policy\"}");
                        return;
                    }
                    String hashedPass = PasswordHasher.hashPassword(pass);
                    User newUser = new User(user, fullName, hashedPass);
                    userRepo.save(newUser);
                    Core.sendResponse(exchange, 201, String.format("{\"status\":\"created\",\"message\":\"User registered successfully\",\"id\":\"%s\"}", newUser.getId()));
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
                User currentUser = userRepo.findUserById(userId);
                if (currentUser == null) {
                    Core.sendResponse(exchange, 404, "{\"error\":\"User not found\"}");
                    return;
                }
                StringBuilder json = new StringBuilder();
                json.append("{");
                
                // Basic Data
                String userAvatar = currentUser.getInformationRepository().getAvatar();
                if(userAvatar == null) userAvatar = "";
                json.append(String.format("\"id\":\"%s\",\"userName\":\"%s\",\"fullName\":\"%s\",\"profilePicture\":\"%s\",", 
                    currentUser.getId(), currentUser.getUserName(), currentUser.getFullName(), userAvatar));

                // Private Chats
                json.append("\"pvs\":[");
                List<String> pvIds = currentUser.getInformationRepository().getPvIds();
                for(int i=0 ; i<pvIds.size() ; i++) {
                    String targetId = pvIds.get(i);
                    User target = userRepo.findUserById(targetId);
                    if(target == null) continue;
                    String targetAvatar = target.getInformationRepository().getAvatar();
                    if(targetAvatar == null) targetAvatar = "";
                    json.append(String.format("{\"targetId\":\"%s\",\"targetName\":\"%s\",\"profilePicture\":\"%s\"}", 
                        target.getId(), target.getUserName(), targetAvatar));
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

    public static void avatar(HttpServer server) {
        server.createContext("/api/user/avatar", exchange -> {
            Core.handleCORS(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) return;
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) { // Save
                String body = Core.getRequestBody(exchange);
                String userId = Core.extractJsonValue(body, "userId");
                String base64Image = Core.extractJsonValue(body, "profilePicture");
                User currentUser = userRepo.findUserById(userId);
                if (currentUser == null) {
                    Core.sendResponse(exchange, 404, "{\"error\":\"User not found\"}");
                    return;
                }
                currentUser.setAvatar(base64Image);
                currentUser.getInformationRepository().saveAvatar();
                Core.sendResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Profile picture updated successfully\"}");
            }
            
            else if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) { // Load
                String query = exchange.getRequestURI().getQuery();
                String userId = query != null && query.contains("userId=") ? query.split("userId=")[1] : "";
                User currentUser = userRepo.findUserById(userId);
                if (currentUser == null) {
                    Core.sendResponse(exchange, 404, "{\"error\":\"User not found\"}");
                    return;
                }
                String base64Image = currentUser.getInformationRepository().getAvatar();
                if (base64Image == null) {
                    base64Image = "";
                }
                Core.sendResponse(exchange, 200, String.format("{\"userId\":\"%s\",\"profilePicture\":\"%s\"}", currentUser.getId(), base64Image));
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
                        String usrAvatar = usr.getInformationRepository().getAvatar();
                        if(usrAvatar == null) usrAvatar = "";
                        json.append("{");
                        json.append("\"id\":\"").append(usr.getId()).append("\",");
                        json.append("\"username\":\"").append(usr.getUserName()).append("\",");
                        json.append("\"profilePicture\":\"").append(usrAvatar).append("\"");
                        json.append("}");
                    }
                }
                json.append("]}");
                
                Core.sendResponse(exchange, 200, json.toString());
            }
            else Core.sendResponse(exchange, 405, "Method Not Allowed");
        });
    }
  
    private static boolean validatePassword(String password, String username) {
        if (password.length() < 8 || password.contains(username)) return false;
        String pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!$#@%&^*]).+$";
        return java.util.regex.Pattern.matches(pattern, password);
    }
}
