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
            // CORREÇÃO DA REGRA DE NEGÓCIO: 
            // Apenas o e-mail é validado aqui. O usuário NÃO é ativado automaticamente.
            // Ele permanecerá pendente aguardando a aprovação do Administrador no painel.
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
    // 4. ESQUECI MINHA SENHA (DASHBOARD TECHXMICRO)
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
            
            // Texto do e-mail com Rodapé Profissional e Incentivo ao Helpdesk
            String htmlTexto = 
                "<div style='font-family: Arial, sans-serif; padding: 20px; color: #333;'>" +
                "  <h2 style='color: #008436;'>🔑 Recuperação de Senha - TechxMicro</h2>" +
                "  <p>Olá, <strong>" + usuario.getNome() + "</strong>,</p>" +
                "  <p>Recebemos uma solicitação para redefinir sua senha no ecossistema <strong>TechxMicro</strong>.</p>" +
                "  <div style='background: #f4f4f4; padding: 20px; border-radius: 8px; text-align: center; border: 1px solid #ddd;'>" +
                "    <p style='margin: 0; font-size: 1rem;'>Seu código de segurança é:</p>" +
                "    <b style='font-size: 2.2rem; letter-spacing: 6px; color: #008436;'>" + token + "</b>" +
                "  </div>" +
                "  <p style='margin-top: 25px;'><strong>🚀 Dica do Suporte:</strong></p>" +
                "  <p>Sabia que você pode gerenciar todos os seus chamados em um só lugar? " +
                "      Sempre que precisar de auxílio técnico em Software ou Infraestrutura, utilize nosso Helpdesk.</p>" +
                "  <a href='https://techxmicro.com.br/helpdesk' style='display: inline-block; padding: 12px 20px; " +
                "      background-color: #008436; color: #ffffff; text-decoration: none; border-radius: 5px; font-weight: bold;'>" +
                "      Abrir Chamado no Helpdesk</a>" +
                "  <p style='font-size: 0.85rem; color: #777; margin-top: 30px;'>" +
                "      Se você não reconhece esta ação, por favor ignore este e-mail por segurança.<br>" +
                "      <strong>TechxMicro - Soluções em TI e Desenvolvimento.</strong></p>" +
                "</div>";
                               
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