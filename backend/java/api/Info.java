package api;

import dao.*;
import entity.*;

import java.util.ArrayList;
import java.util.List;

import com.sun.net.httpserver.HttpServer;

public class Info {

    private static final UserRepository userRepo = UserRepository.getInstance();
    private static final AllGroupsRepository groupRepo = AllGroupsRepository.getInstance();

    public static void load(HttpServer server) 
    {

        server.createContext("/api/details", exchange -> {
            Core.handleCORS(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) return;

            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery(); //groupId=5 or userId=10
                String response = "";

                if (query != null) 
                {
                    if (query.contains("groupId=")) 
                    {
                        String gId = query.split("groupId=")[1].split("&")[0];
                        Group g = AllGroupsRepository.getInstance().findById(gId);
                        if (g != null) 
                        {
                            List<String> usernameList = new ArrayList<>();
                            for (User u : g.getMembers()) usernameList.add(u.getUserName());
                            String membersJoined = String.join(",", usernameList);
                            response = String.format(
                                "{\"name\":\"%s\", \"owner\":\"%s\", \"ownerId\":\"%s\", \"membersCount\":%d, \"membersList\":\"%s\"}",
                                g.getName(), g.getOwner().getUserName(), g.getOwner().getId(), g.getMembers().size(), membersJoined);
                        }
                    }
                    
                    else if (query.contains("userId=")) 
                    {
                        String uId = query.split("userId=")[1].split("&")[0];
                        User u = UserRepository.getInstance().findUserById(uId);
                        if (u != null) {
                            response = String.format(
                                "{\"userName\":\"%s\", \"fullName\":\"%s\", \"bio\":\"%s\"}",
                                u.getUserName(), u.getFullName(), u.getBio()
                            );
                        }
                    }
                }

                if (response.isEmpty()) 
                {
                    Core.sendResponse(exchange, 404, "{\"error\":\"Not Found\"}");
                } 
                
                else 
                {
                    Core.sendResponse(exchange, 200, response);
                }
            }
        });
    }

    public static void updateProfile(HttpServer server) {
        server.createContext("/api/user/update", exchange -> {
            Core.handleCORS(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) return;
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = Core.getRequestBody(exchange);
                String userId = Core.extractJsonValue(body, "userId");
                String newFullName = Core.extractJsonValue(body, "fullName");
                String newBio = Core.extractJsonValue(body, "bio");
                User currentUser = userRepo.findUserById(userId);
                if (currentUser == null) {
                    Core.sendResponse(exchange, 404, "{\"error\":\"User not found\"}");
                    return;
                }
                if (!newFullName.isEmpty()) {
                    currentUser.setFullName(newFullName);
                    currentUser.changeFullNameAndSave(newFullName);
                }
                if (!newBio.isEmpty())currentUser.setBio(newBio);
                List<User> allUsers = userRepo.getAllUsers();
                try (java.io.FileWriter writer = new java.io.FileWriter(".." + java.io.File.separator + "data" + java.io.File.separator + "users.txt", false)) {
                    for (User u : allUsers)
                        writer.write(u.getId() + " , " + u.getUserName() + " , " + u.getFullName() + " , " + u.getPassword() + "\n");
                }
                catch (java.io.IOException e) {
                    e.printStackTrace();
                    Core.sendResponse(exchange, 500, "{\"error\":\"Database error while saving user\"}");
                    return;
                } 
                currentUser.getInformationRepository().saveBioAndOtherData();
                Core.sendResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Profile updated successfully\"}");
            } 
            else Core.sendResponse(exchange, 405, "Method Not Allowed");
        });
    }

    public static void updateGroupInfo(HttpServer server) {
        server.createContext("/api/group/update", exchange -> {
            Core.handleCORS(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) return;
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = Core.getRequestBody(exchange);
                String groupId = Core.extractJsonValue(body, "groupId");
                String newName = Core.extractJsonValue(body, "name");
                Group targetGroup = Core.findGroupById(groupId);
                if (targetGroup == null) {
                    Core.sendResponse(exchange, 404, "{\"error\":\"Group not found\"}");
                    return;
                }
                if (newName.isEmpty()) {
                    Core.sendResponse(exchange, 400, "{\"error\":\"Group name cannot be empty\"}");
                    return;
                }
                targetGroup.setName(newName);
                GroupRepository groupRepoLocal = new GroupRepository(targetGroup);
                groupRepoLocal.changeData(targetGroup.getId(), newName, targetGroup.getOwner().getId());
                List<Group> allGroups = groupRepo.findAll();
                try (java.io.FileWriter writer = new java.io.FileWriter(".." + java.io.File.separator + "data" + java.io.File.separator + "groups.txt", false)) {
                    for (Group g : allGroups) {
                        String gName = g.getId().equals(groupId) ? newName : g.getName();
                        writer.write(g.getId() + " , " + gName + " , " + g.getOwner().getId() + "\n");
                    }
                }
                catch (java.io.IOException e) {
                    e.printStackTrace();
                    Core.sendResponse(exchange, 500, "{\"error\":\"Database error while updating groups file\"}");
                    return;
                }

                Core.sendResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Group name updated successfully\"}");
            }
            else Core.sendResponse(exchange, 405, "Method Not Allowed");
        });
    }

    public static void userAvatar(HttpServer server) {
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

    public static void groupAvatar(HttpServer server) {
        server.createContext("/api/group/avatar", exchange -> {
            Core.handleCORS(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) return;
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) { //SAVE
                String body = Core.getRequestBody(exchange);
                String groupId = Core.extractJsonValue(body, "groupId");
                String base64Image = Core.extractJsonValue(body, "groupPicture");
                Group targetGroup = Core.findGroupById(groupId);
                if (targetGroup == null) {
                    Core.sendResponse(exchange, 404, "{\"error\":\"Group not found\"}");
                    return;
                }
                targetGroup.setAvatar(base64Image);
                dao.GroupRepository groupRepoLocal = new dao.GroupRepository(targetGroup);
                groupRepoLocal.saveAvatar(base64Image);
                Core.sendResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Group picture updated successfully\"}");
            }
            else if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) { // GET
                String query = exchange.getRequestURI().getQuery();
                String groupId = query != null && query.contains("groupId=") ? query.split("groupId=")[1] : "";
                Group targetGroup = Core.findGroupById(groupId);
                if (targetGroup == null) {
                    Core.sendResponse(exchange, 404, "{\"error\":\"Group not found\"}");
                    return;
                }
                String base64Image = targetGroup.getAvatar();
                if (base64Image == null) base64Image = "";
                Core.sendResponse(exchange, 200, String.format("{\"groupId\":\"%s\",\"groupPicture\":\"%s\"}", targetGroup.getId(), base64Image));
            }
            else Core.sendResponse(exchange, 405, "Method Not Allowed");
        });
    }
}

