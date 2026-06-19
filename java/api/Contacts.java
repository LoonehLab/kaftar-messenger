package api;

import dao.*;
import entity.*;

import java.util.List;
import com.sun.net.httpserver.HttpServer;

public class Contacts {

    private static final UserRepository userRepo = UserRepository.getInstance();

    public static void Manage (HttpServer server) {
        server.createContext("/api/contacts", exchange -> {
            Core.handleCORS(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) return;
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                String userId = "";
                if (query != null && query.contains("userId="))
                    userId = query.split("userId=")[1].split("&")[0];
                User user = userRepo.findUserById(userId);
                if (userId.isEmpty() || user == null) {
                    Core.sendResponse(exchange, 400, "{\"error\":\"User not found\"}");
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
                Core.sendResponse(exchange, 200, jsonBuilder.toString());
            } 
            
            else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = Core.getRequestBody(exchange);
                String action = Core.extractJsonValue(body, "action");
                String userId = Core.extractJsonValue(body, "userId");
                String contactId = Core.extractJsonValue(body, "contactId");
                if (userId.isEmpty() || contactId.isEmpty() || action.isEmpty()) {
                    Core.sendResponse(exchange, 400, "{\"error\":\"Missing fields\"}");
                    return;
                }
                User user = userRepo.findUserById(userId);
                User contact = userRepo.findUserById(contactId);
                if (user == null || contact == null) {
                    Core.sendResponse(exchange, 404, "{\"error\":\"User or Contact not found\"}");
                    return;
                }
                InformationRepository infoRepo = user.getInformationRepository();
                if ("add".equalsIgnoreCase(action)) {
                    if (infoRepo.getContactsId().contains(contactId)) {
                        Core.sendResponse(exchange, 400, "{\"error\":\"Contact already exists\"}");
                        return;
                    }
                    infoRepo.saveContact(contact);
                    if (!user.getContacts().contains(contact)) user.getContacts().add(contact);
                    Core.sendResponse(exchange, 200, "{\"message\":\"Contact added successfully\"}");
                } 
                
                else if ("remove".equalsIgnoreCase(action)) {
                    infoRepo.removeContact(contact);
                    for (User usr : user.getContacts())
                        if (usr.getId().equals(contactId)) {
                            user.getContacts().remove(usr);
                            break;
                        }
                    Core.sendResponse(exchange, 200, "{\"message\":\"Contact removed successfully\"}");
                } 
                
                else {
                    Core.sendResponse(exchange, 400, "{\"error\":\"Invalid action\"}");
                }
            } 
            
            else {
                Core.sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        });
    }
}
