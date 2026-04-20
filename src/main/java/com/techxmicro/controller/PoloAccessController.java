package com.techxmicro.controller;

import com.techxmicro.entity.Polo;
import com.techxmicro.entity.Usuario;
import com.techxmicro.repository.PoloRepository;
import com.techxmicro.repository.UsuarioRepository;
import com.techxmicro.service.GeolocationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/geo")
public class PoloAccessController {

    @Autowired private GeolocationService geolocationService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PoloRepository poloRepository;

    @PostMapping("/check-in")
    public ResponseEntity<?> verificarLocalizacao(@RequestBody Map<String, Double> coords, Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();

        Double lat = coords.get("latitude");
        Double lon = coords.get("longitude");

        if (lat == null || lon == null) return ResponseEntity.badRequest().body("Coordenadas inválidas");

        // 1. Descobre onde o usuário está
        Map<String, String> local = geolocationService.getCidadeBairro(lat, lon);
        if (local == null) return ResponseEntity.ok("Não foi possível detectar localização precisa.");

        String cidadeAtual = local.get("cidade");
        String bairroAtual = local.get("bairro");

        Usuario usuario = usuarioRepository.findByUsernameOrEmail(principal.getName());

        if (usuario == null) return ResponseEntity.status(404).body("Usuário não encontrado.");

        // 2. Verifica se o usuário JÁ TEM vínculo com este local
        boolean jaPossuiPolo = usuario.getPolos().stream().anyMatch(p -> 
            p.getCidade() != null && p.getCidade().equalsIgnoreCase(cidadeAtual) && 
            p.getBairro() != null && p.getBairro().equalsIgnoreCase(bairroAtual)
        );

        if (jaPossuiPolo) {
            return ResponseEntity.ok("OK: Usuário já vinculado ao polo local.");
        } else {
            // 3. INTELIGÊNCIA: Cria/Vincula o Polo Automaticamente
            Polo novoPolo = vincularPoloAutomatico(cidadeAtual, bairroAtual, usuario.getCep());
            
            usuario.getPolos().add(novoPolo);
            usuarioRepository.save(usuario);
            
            System.out.println(">>> [GEO] Novo vínculo criado: " + usuario.getNome() + " -> " + novoPolo.getNome());
            return ResponseEntity.ok("UPDATE: Novo vínculo criado com " + novoPolo.getNome());
        }
    }

    private Polo vincularPoloAutomatico(String cidade, String bairro, String cep) {
        // Busca um responsável padrão para os novos polos (geralmente o admin)
        Usuario responsavelPadrao = usuarioRepository.findByUsername("admin");
        if (responsavelPadrao == null) {
            List<Usuario> users = usuarioRepository.findAll();
            if (!users.isEmpty()) responsavelPadrao = users.get(0);
        }

        // A. Busca/Cria MATRIZ (Polo Principal - Substitui o 'Hospital')
        Polo matriz = poloRepository.findByPoloPaiIsNull().stream()
                .filter(p -> p.getCidade().equalsIgnoreCase(cidade) && "MATRIZ".equalsIgnoreCase(p.getTipo()))
                .findFirst().orElse(null);

        if (matriz == null) {
            matriz = new Polo();
            matriz.setNome("Polo Central " + cidade);
            matriz.setCidade(cidade);
            matriz.setTipo("MATRIZ");
            matriz.setCep(cep);
            matriz.setHorarioFuncionamento("24 Horas");
            matriz.setAtivo(true);
            matriz.setDataInauguracao(LocalDate.now());
            matriz.setResponsavel(responsavelPadrao);
            matriz = poloRepository.save(matriz);
        }

        // B. Busca/Cria FILIAL (Polo Secundário - Substitui a 'Clinica')
        Polo finalMatriz = matriz;
        Polo filial = poloRepository.findByPoloPai_Id(matriz.getId()).stream()
                .filter(p -> p.getBairro() != null && p.getBairro().equalsIgnoreCase(bairro))
                .findFirst().orElse(null);

        if (filial == null) {
            filial = new Polo();
            filial.setNome("Filial " + bairro);
            filial.setCidade(cidade);
            filial.setBairro(bairro);
            filial.setTipo("FILIAL");
            filial.setPoloPai(finalMatriz);
            filial.setCep(cep);
            filial.setHorarioFuncionamento("08:00 às 18:00");
            filial.setAtivo(true);
            filial.setDataInauguracao(LocalDate.now());
            filial.setResponsavel(responsavelPadrao);
            filial.setLogradouro(bairro + ", " + cidade);
            filial = poloRepository.save(filial);
        }
        return filial;
    }
}