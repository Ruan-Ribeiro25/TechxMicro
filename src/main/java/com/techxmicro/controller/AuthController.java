package com.techxmicro.controller;

import com.techxmicro.entity.Profissional;
import com.techxmicro.entity.Usuario;
import com.techxmicro.repository.ProfissionalRepository;
import com.techxmicro.repository.UsuarioRepository;
import com.techxmicro.service.EmailService;

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
    // 2. RECUPERAÇÃO E VERIFICAÇÃO DE CONTA
    // =================================================================================

    @GetMapping("/verificar-conta")
    public String verificarConta(@RequestParam(required = false) String email, Model model) {
        if (email != null) model.addAttribute("email", email);
        return "verificar-conta"; 
    }

    @PostMapping("/verificar-conta")
    public String processarVerificacao(@RequestParam("codigo") String codigo, Model model) {
        Usuario usuario = usuarioRepository.findByCodigoVerificacao(codigo);
        if (usuario != null) {
            usuario.setAtivo(true); // Ativando o usuário após verificação
            usuario.setCodigoVerificacao(null);
            usuarioRepository.save(usuario);
            return "redirect:/bem-vindo";
        }
        return "redirect:/verificar-conta?error=true";
    }

    @GetMapping("/bem-vindo")
    public String welcome() { return "welcome"; }
    
    // =================================================================================
    // 3. REGISTRO DE PROFISSIONAL (UPGRADE)
    // =================================================================================
    
    @GetMapping("/register-professional")
    public String registerProfessionalForm(Model model, Principal principal) {
        if (principal == null) return "redirect:/login"; 
        Usuario usuarioLogado = usuarioRepository.findByEmail(principal.getName());
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
        Usuario usuarioExistente = usuarioRepository.findByEmail(principal.getName());

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
    // 4. ESQUECI MINHA SENHA (FAXINA REBRANDING)
    // =================================================================================

    @GetMapping("/forgot-password")
    public String forgotPassword() { return "forgot-password"; }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email, Model model) {
        Usuario usuario = usuarioRepository.findByEmail(email);
        if (usuario != null) {
            String token = String.format("%06d", new Random().nextInt(999999));
            usuario.setTokenReset(token);
            usuarioRepository.save(usuario);
            
            // Texto do e-mail atualizado para TechxMicro
            String htmlTexto = "<div style='font-family: sans-serif; padding: 20px; color: #000;'>" +
                               "<h2 style='color: #008436;'>Recuperação de Acesso TechxMicro</h2>" +
                               "<p>Recebemos uma solicitação para redefinir sua senha no ecossistema de suporte TechxMicro.</p>" +
                               "<p style='background: #f4f4f4; padding: 15px; border-radius: 8px; text-align: center;'>" +
                               "Seu código de segurança é: <b style='font-size: 1.8rem; letter-spacing: 5px; color: #008436;'>" + token + "</b></p>" +
                               "<p>Se você não solicitou esta alteração, por favor ignore este aviso.</p>" +
                               "<hr><small>TechxMicro - Suporte em Infraestrutura e TI desde 2016.</small></div>";
                               
            emailService.enviarEmail(usuario.getEmail(), "TechxMicro - Recuperação de Senha", htmlTexto);
            return "redirect:/enter-code";
        } 
        model.addAttribute("error", "E-mail não encontrado no sistema.");
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