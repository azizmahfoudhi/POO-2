package com.ihec.club.gui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

/**
 * Client HTTP utilitaire pour communiquer avec l'API REST Spring Boot.
 * Utilisé par les panneaux Swing pour envoyer et recevoir des données.
 */
public class ApiClient {

    private static final String BASE_URL = "http://localhost:8081/api";
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    /** Requête GET — retourne le JSON brut */
    public static String get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new RuntimeException("Erreur HTTP " + response.statusCode() + ": " + response.body());
        }
        return response.body();
    }

    /** Requête GET — désérialise en objet Java */
    public static <T> T get(String path, Class<T> type) throws Exception {
        String json = get(path);
        return mapper.readValue(json, type);
    }

    /** Requête GET — désérialise en liste */
    public static <T> List<T> getList(String path, TypeReference<List<T>> typeRef) throws Exception {
        String json = get(path);
        return mapper.readValue(json, typeRef);
    }

    /** Requête POST — envoie un body JSON */
    public static String post(String path, Object body) throws Exception {
        String json = mapper.writeValueAsString(body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new RuntimeException("Erreur: " + response.body());
        }
        return response.body();
    }

    /** Requête PUT — envoie un body JSON */
    public static String put(String path, Object body) throws Exception {
        String json = mapper.writeValueAsString(body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new RuntimeException("Erreur: " + response.body());
        }
        return response.body();
    }

    /** Requête DELETE */
    public static void delete(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new RuntimeException("Erreur: " + response.body());
        }
    }

    /** Utilitaire : parse un JSON en Map */
    public static Map<String, Object> parseMap(String json) throws Exception {
        return mapper.readValue(json, new TypeReference<Map<String, Object>>() {});
    }

    /** Utilitaire : parse un JSON en liste de Maps */
    public static List<Map<String, Object>> parseList(String json) throws Exception {
        return mapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
    }
}
