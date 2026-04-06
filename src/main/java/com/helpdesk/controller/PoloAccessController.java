package com.helpdesk.controller;

import com.helpdesk.entity.Polo;
import com.helpdesk.entity.Usuario;
import com.helpdesk.repository.PoloRepository;
import com.helpdesk.repository.UsuarioRepository;
import com.helpdesk.service.GeolocationService;

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

        // CORREÇÃO: Usando o método findByUsernameOrEmail (1 parâmetro)
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

        // A. Busca/Cria Hospital (Pai)
        Polo hospital = poloRepository.findByPoloPaiIsNull().stream()
                .filter(p -> p.getCidade().equalsIgnoreCase(cidade) && "HOSPITAL".equalsIgnoreCase(p.getTipo()))
                .findFirst().orElse(null);

        if (hospital == null) {
            hospital = new Polo();
            hospital.setNome("Hospital VidaPlus " + cidade);
            hospital.setCidade(cidade);
            hospital.setTipo("HOSPITAL");
            hospital.setCep(cep);
            hospital.setHorarioFuncionamento("24 Horas");
            hospital.setAtivo(true);
            hospital.setDataInauguracao(LocalDate.now());
            hospital.setResponsavel(responsavelPadrao);
            hospital = poloRepository.save(hospital);
        }

        // B. Busca/Cria Clínica (Filho)
        Polo finalHospital = hospital;
        Polo clinica = poloRepository.findByPoloPai_Id(hospital.getId()).stream()
                .filter(p -> p.getBairro() != null && p.getBairro().equalsIgnoreCase(bairro))
                .findFirst().orElse(null);

        if (clinica == null) {
            clinica = new Polo();
            clinica.setNome("Clínica " + bairro);
            clinica.setCidade(cidade);
            clinica.setBairro(bairro);
            clinica.setTipo("CLINICA");
            clinica.setPoloPai(finalHospital);
            clinica.setCep(cep);
            clinica.setHorarioFuncionamento("08:00 às 18:00");
            clinica.setAtivo(true);
            clinica.setDataInauguracao(LocalDate.now());
            clinica.setResponsavel(responsavelPadrao);
            clinica.setLogradouro(bairro + ", " + cidade);
            clinica = poloRepository.save(clinica);
        }
        return clinica;
    }
}