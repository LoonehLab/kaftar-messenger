package api;

import entity.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;

import dao.AllGroupsRepository;

public class Core {
    private static final AllGroupsRepository groupRepo = AllGroupsRepository.getInstance();
    protected static PV findOrCreatePV(User u1 , User u2){
        PV pv = findPV(u1 , u2);
        if(pv != null) return pv;
        PV newPv;
        if(u1.getId().equals(u2.getId())) newPv = new SavedMessage(u1 , u1);
        else newPv = new PV(u1 , u2);
        u1.getInformationRepository().savePv(newPv);
        u2.getInformationRepository().savePv(newPv);

        return newPv;
    }
    
    protected static PV findPV(User u1 , User u2) {
        List<String> ids = u1.getInformationRepository().getPvIds();
        for(String id : ids)
            if(id.equals(u2.getId())) return new PV(u1 , u2);

        return null;
    }

    protected static Group findGroupById(String groupId) {
        return groupRepo.findById(groupId);
    }

    protected static String convertMessagesToJson(List<Message> messages) {
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

    protected static void handleCORS(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
        }
    }

    protected static String getRequestBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) { sb.append(line); }
        return sb.toString();
    }

    protected static String extractJsonValue(String json, String key) {
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

    protected static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes("UTF-8");
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}
