package api;

import dao.*;
import entity.*;
import com.sun.net.httpserver.HttpServer;

public class Info {

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
                            response = String.format(
                                "{\"type\":\"group\", \"name\":\"%s\", \"owner\":\"%s\", \"membersCount\":%d}",
                                g.getName(), g.getOwner().getUserName(), g.getMembers().size()
                            );
                        }
                    } 
                    
                    else if (query.contains("userId=")) 
                    {
                        String uId = query.split("userId=")[1].split("&")[0];
                        User u = UserRepository.getInstance().findUserById(uId);
                        if (u != null) {
                            response = String.format(
                                "{\"type\":\"user\", \"name\":\"%s\", \"bio\":\"%s\", \"id\":\"%s\"}",
                                u.getUserName(), u.getBio(), u.getId()
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
}

