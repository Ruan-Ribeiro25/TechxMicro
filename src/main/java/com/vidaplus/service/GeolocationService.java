package com.vidaplus.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

@Service
public class GeolocationService {

    // API Gratuita do OpenStreetMap (Nominatim)
    private final String NOMINATIM_API = "https://nominatim.openstreetmap.org/reverse?format=json&lat={lat}&lon={lon}";

    public Map<String, String> getCidadeBairro(double lat, double lon) {
        RestTemplate restTemplate = new RestTemplate();
        Map<String, String> localizacao = new HashMap<>();

        try {
            // Nominatim exige um User-Agent válido
            HttpHeaders headers = new HttpHeaders();
            headers.add("User-Agent", "VidaPlusApp/1.0");
            HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

            Map<String, String> params = new HashMap<>();
            params.put("lat", String.valueOf(lat));
            params.put("lon", String.valueOf(lon));

            ResponseEntity<String> response = restTemplate.exchange(
                NOMINATIM_API, HttpMethod.GET, entity, String.class, params
            );

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            JsonNode address = root.path("address");

            // Tenta capturar a cidade (pode vir como city, town ou village)
            String cidade = "";
            if (address.has("city")) cidade = address.get("city").asText();
            else if (address.has("town")) cidade = address.get("town").asText();
            else if (address.has("village")) cidade = address.get("village").asText();
            else if (address.has("municipality")) cidade = address.get("municipality").asText();

            // Tenta capturar o bairro (suburb, neighbourhood, quarter)
            String bairro = "";
            if (address.has("suburb")) bairro = address.get("suburb").asText();
            else if (address.has("neighbourhood")) bairro = address.get("neighbourhood").asText();
            else if (address.has("quarter")) bairro = address.get("quarter").asText();
            else if (address.has("city_district")) bairro = address.get("city_district").asText();
            
            // Fallback se bairro for nulo (usa cidade ou "Centro")
            if (bairro.isEmpty()) bairro = "Centro";

            localizacao.put("cidade", cidade);
            localizacao.put("bairro", bairro);
            
            System.out.println(">>> GEOLOCALIZAÇÃO DETECTADA: " + cidade + " - " + bairro);

        } catch (Exception e) {
            e.printStackTrace();
            // Retorna nulo em caso de erro para não travar o login
            return null; 
        }
        return localizacao;
    }
}