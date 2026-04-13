package com.techxmicro.controller;

import com.techxmicro.entity.Administrador;
import com.techxmicro.entity.Polo;
import com.techxmicro.entity.Usuario;
import com.techxmicro.repository.AdministradorRepository;
import com.techxmicro.repository.PoloRepository;
import com.techxmicro.repository.UsuarioRepository;
import com.techxmicro.service.UsuarioService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
public class RegisterController {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AdministradorRepository administradorRepository;
    @Autowired private UsuarioService usuarioService;
    @Autowired private PoloRepository poloRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    // =========================================================================
    // 1. REGISTRO PÚBLICO (PACIENTES / PROFISSIONAIS)
    // =========================================================================

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "register"; 
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute Usuario usuario, Model model, RedirectAttributes redirectAttributes) {
        // 1. Validação de Duplicidade (Usando E-mail)
        if (usuarioRepository.findByEmail(usuario.getEmail()) != null) {
            model.addAttribute("error", "E-mail já cadastrado!");
            return "register";
        }

        try {
            if (usuario.getPerfil() == null || usuario.getPerfil().isEmpty()) {
                usuario.setPerfil("PACIENTE"); 
            }
            
            // GARANTIA: O E-mail assume a coluna de Username para não dar erro no Spring Security
            usuario.setUsername(usuario.getEmail());

            // 3. LÓGICA INTELIGENTE: VÍNCULO AUTOMÁTICO DE POLO
            Polo clinicaVinculada = vincularPoloAutomatico(usuario);
            
            if (usuario.getPolos() == null) {
                usuario.setPolos(new ArrayList<>());
            }
            
            if (clinicaVinculada != null) {
                usuario.getPolos().add(clinicaVinculada);
            }

            // 4. CHAMADA AO SERVICE
            usuarioService.cadastrar(usuario);

            // 5. REDIRECIONAMENTO
            redirectAttributes.addFlashAttribute("mensagem", "Cadastro realizado! Verifique seu e-mail.");
            return "redirect:/verificar-conta?email=" + usuario.getEmail();
            
        } catch (Exception e) {
            System.err.println("Erro no cadastro: " + e.getMessage()); 
            model.addAttribute("error", "Ocorreu um erro interno ao processar seu cadastro. Verifique os dados ou contate o suporte.");
            return "register";
        }
    }

    private Polo vincularPoloAutomatico(Usuario usuario) {
        String cidade = usuario.getCidade();
        String bairro = usuario.getBairro();
        String cep = usuario.getCep();

        if (cidade == null || bairro == null) return null;

        // Limpo para usar apenas findByUsername
        Usuario responsavelPadrao = usuarioRepository.findByUsername("admin");
        if (responsavelPadrao == null) {
            List<Usuario> users = usuarioRepository.findAll();
            if (!users.isEmpty()) responsavelPadrao = users.get(0);
        }

        Polo hospital = poloRepository.findByPoloPaiIsNull().stream()
                .filter(p -> p.getCidade().equalsIgnoreCase(cidade) && "HOSPITAL".equalsIgnoreCase(p.getTipo()))
                .findFirst()
                .orElse(null);

        if (hospital == null) {
            hospital = new Polo();
            hospital.setNome("Hospital VidaPlus " + cidade);
            hospital.setCidade(cidade);
            hospital.setTipo("HOSPITAL");
            hospital.setCep(cep); 
            hospital.setAtivo(true);
            hospital.setHorarioFuncionamento("24 Horas");
            hospital.setDataInauguracao(LocalDate.now()); 
            hospital.setResponsavel(responsavelPadrao);   
            hospital = poloRepository.save(hospital);
        }

        Polo finalHospital = hospital;
        Polo clinica = poloRepository.findByPoloPai_Id(hospital.getId()).stream()
                .filter(p -> p.getBairro() != null && p.getBairro().equalsIgnoreCase(bairro))
                .findFirst()
                .orElse(null);

        if (clinica == null) {
            clinica = new Polo();
            clinica.setNome("Clínica " + bairro);
            clinica.setCidade(cidade);
            clinica.setBairro(bairro);
            clinica.setTipo("CLINICA");
            clinica.setPoloPai(finalHospital); 
            clinica.setCep(cep);
            clinica.setAtivo(true);
            clinica.setHorarioFuncionamento("08:00 às 18:00");
            clinica.setDataInauguracao(LocalDate.now()); 
            clinica.setResponsavel(responsavelPadrao);   
            clinica.setLogradouro(bairro + ", " + cidade); 
            clinica = poloRepository.save(clinica);
        }

        return clinica; 
    }

    // =========================================================================
    // 2. REGISTRO ADMINISTRATIVO
    // =========================================================================

    @GetMapping("/register-admin")
    public String showAdminRegisterForm(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        // Limpo para usar apenas findByUsername
        Usuario usuario = usuarioRepository.findByUsername(principal.getName());
        model.addAttribute("usuario", usuario);
        
        return "admin/register-admin"; 
    }

    @PostMapping("/register-admin")
    public String processAdminRegister(@RequestParam String matricula,
                                       @RequestParam LocalDate dataMatricula, 
                                       @RequestParam String cargo, 
                                       Principal principal) {
        
        if (principal == null) return "redirect:/login";

        // Limpo para usar apenas findByUsername
        Usuario usuario = usuarioRepository.findByUsername(principal.getName());
        
        if (usuario != null) {
            usuario.setPerfil("ADMIN");
            usuario.setAtivo(false); 
            usuarioRepository.save(usuario);

            Administrador admin = new Administrador();
            admin.setUsuario(usuario);
            admin.setMatricula(matricula);
            admin.setDataMatricula(dataMatricula);
            admin.setNivelAcesso(cargo);
            
            administradorRepository.save(admin);
        }

        return "redirect:/login?pending=true";
    }
}