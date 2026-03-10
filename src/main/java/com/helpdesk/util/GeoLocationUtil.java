package com.helpdesk.util;

import org.springframework.stereotype.Component;

import com.helpdesk.entity.Polo;
import com.helpdesk.entity.Usuario;

@Component
public class GeoLocationUtil {

    private static final String CIDADE_ATUAL_DETECTADA = "São Paulo"; 

    public String getCidadeAtual() {
        return CIDADE_ATUAL_DETECTADA;
    }

    public Long getPoloIdNaRegiaoAtual(Usuario usuario) {
        if (usuario.getPolos() == null || usuario.getPolos().isEmpty()) {
            return null; 
        }
        
        return usuario.getPolos().stream()
                .filter(p -> p.getCidade().equalsIgnoreCase(CIDADE_ATUAL_DETECTADA))
                .map(Polo::getId) // Agora map(Polo::getId) vai funcionar pois Polo tem getId()
                .findFirst()
                .orElse(null);
    }
}