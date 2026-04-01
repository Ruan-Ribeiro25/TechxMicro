package com.helpdesk.controller;

import com.helpdesk.entity.Profissional;
import com.helpdesk.entity.Usuario;
import com.helpdesk.repository.ProfissionalRepository;
import com.helpdesk.repository.UsuarioRepository;
import com.helpdesk.service.EmailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Random;

@Controller
public class AuthController {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ProfissionalRepository profissionalRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    
    @Autowired private EmailService emailService; 

    // =================================================================================
    // 1. LOGIN E REDIRECIONAMENTOS
    // =================================================================================

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/login-professional")
    public String loginProfessional() { 
        return "redirect:/acesso-profissional"; 
    }

    // =================================================================================
    // 2. RECUPERAÇÃO DE SENHA E VERIFICAÇÃO (CRÍTICO PARA O CADASTRO)
    // =================================================================================

    @GetMapping("/verificar-conta")
    public String verificarConta(@RequestParam(required = false) String email, Model model) {
        // Passamos o e-mail para a view saber quem estamos esperando
        if (email != null) {
            model.addAttribute("email", email);
        }
        return "verificar-conta"; 
    }

    @PostMapping("/verificar-conta")
    public String processarVerificacao(@RequestParam("codigo") String codigo, Model model) {
        Usuario usuario = usuarioRepository.findByCodigoVerificacao(codigo);
        
        if (usuario != null) {
            // Ativa o usuário
            usuario.setAtivo(true);
            // Limpa o código para evitar reuso
            usuario.setCodigoVerificacao(null);
            usuarioRepository.save(usuario);
            
            // ATUALIZAÇÃO: Redireciona para a tela de Boas-Vindas
            return "redirect:/bem-vindo";
        }
        
        // Se errou o código, volta para a tela com erro
        return "redirect:/verificar-conta?error=true";
    }

    // NOVO: Endpoint para renderizar a tela de boas-vindas
    @GetMapping("/bem-vindo")
    public String welcome() {
        return "welcome";
    }
    
    // =================================================================================
    // 3. REGISTRO DE PROFISSIONAL (UPGRADE DE CONTA)
    // =================================================================================
    
    @GetMapping("/register-professional")
    public String registerProfessionalForm(Model model, Principal principal) {
        if (principal == null) return "redirect:/login"; 
        Usuario usuarioLogado = usuarioRepository.findByUsernameOrCpf(principal.getName());
        if (usuarioLogado == null) return "redirect:/login?error=user_not_found";
        model.addAttribute("usuario", usuarioLogado);
        return "register-professional"; 
    }

    @PostMapping("/register-professional")
    public String registerProfessionalAction(Principal principal,
                                             @RequestParam String tipoProfissional, 
                                             @RequestParam String registroConselho, 
                                             @RequestParam String matricula,
                                             @RequestParam LocalDate dataMatricula,
                                             @RequestParam String especialidade) {
        if (principal == null) return "redirect:/login";
        Usuario usuarioExistente = usuarioRepository.findByUsernameOrCpf(principal.getName());

        usuarioExistente.setPerfil(tipoProfissional);
        usuarioRepository.save(usuarioExistente);

        Profissional prof = new Profissional();
        prof.setUsuario(usuarioExistente);
        prof.setTipoProfissional(tipoProfissional);
        prof.setMatricula(matricula);
        prof.setDataMatricula(dataMatricula);
        prof.setEspecialidade(especialidade);
        prof.setStatusAprovacao("PENDENTE");

        if (tipoProfissional != null && tipoProfissional.toUpperCase().contains("MEDICO")) {
            prof.setCrm(registroConselho);
        } else {
            prof.setCoren(registroConselho);
        }

        profissionalRepository.save(prof);
        return "redirect:/acesso-profissional?upgrade=success";
    }

    // =================================================================================
    // 4. ESQUECI MINHA SENHA
    // =================================================================================

    @GetMapping("/forgot-password")
    public String forgotPassword() { return "forgot-password"; }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String cpf, Model model) {
        Usuario usuario = usuarioRepository.findByCpf(cpf);
        if (usuario != null) {
            String token = String.format("%06d", new Random().nextInt(999999));
            usuario.setTokenReset(token);
            usuarioRepository.save(usuario);
            
            // Injeção de visual HTML
            String htmlTexto = "<h2 style='color: #f07d35;'>Recuperação de Acesso</h2>" +
                               "<p>Recebemos uma solicitação para redefinir sua senha na plataforma PIXEL TI.</p>" +
                               "<p>Seu código de segurança é: <b style='font-size: 1.5rem; letter-spacing: 3px; color: #b6d441;'>" + token + "</b></p>" +
                               "<p>Se você não solicitou esta alteração, por favor ignore este aviso.</p>";
                               
            emailService.enviarEmail(usuario.getEmail(), "PIXEL TI - Recuperação de Senha", htmlTexto);
            
            return "redirect:/enter-code";
        } // <--- A CHAVE QUE FALTAVA ESTÁ AQUI!
        
        model.addAttribute("error", "CPF não encontrado.");
        return "forgot-password";
    }
    
    @GetMapping("/enter-code")
    public String enterCode() { return "enter-code"; }
    
    @PostMapping("/verify-reset-code")
    public String verifyResetCode(@RequestParam String token) {
        Usuario usuario = usuarioRepository.findByTokenReset(token);
        return (usuario != null) ? "redirect:/update-password?token=" + token : "redirect:/enter-code?error=invalid";
    }
    
    @GetMapping("/update-password")
    public String updatePasswordForm(@RequestParam String token, Model model) {
        model.addAttribute("tokenValidado", token);
        return "update-password";
    }
    
    @PostMapping("/update-password")
    public String updatePasswordAction(@RequestParam String token, @RequestParam String senha, @RequestParam String confirmarSenha) {
        Usuario usuario = usuarioRepository.findByTokenReset(token);
        if (usuario != null && senha.equals(confirmarSenha)) {
            usuario.setSenha(passwordEncoder.encode(senha));
            usuario.setTokenReset(null);
            usuarioRepository.save(usuario);
            return "redirect:/login?reset=success";
        }
        return "redirect:/update-password?error=true";
    }
}