package com.copamir.controller;

import com.copamir.entity.Polo;
import com.copamir.repository.PoloRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Controller
public class RedeCredenciadaController {

    @Autowired
    private PoloRepository poloRepository;

    @GetMapping("/rede-credenciada")
    public String abrirRedeCredenciada(Model model) {
        // 1. Busca todas as unidades no banco
        List<Polo> todasUnidades = poloRepository.findAll();

        // 2. Agrupa as unidades por Cidade (TreeMap mantém ordem alfabética das chaves/cidades)
        Map<String, List<Polo>> redePorCidade = todasUnidades.stream()
                .collect(Collectors.groupingBy(
                        Polo::getCidade, 
                        TreeMap::new, 
                        Collectors.toList()
                ));

        // 3. Envia o mapa para o HTML
        model.addAttribute("redePorCidade", redePorCidade);
        
        // Envia contagem total para exibir no topo
        model.addAttribute("totalUnidades", todasUnidades.size());

        return "rede-credenciada";
    }
}