package com.vidaplus.controller;

import com.vidaplus.entity.Usuario;
import com.vidaplus.repository.UsuarioRepository;
import com.vidaplus.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.Base64;

@Controller
public class PerfilController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository; // Injetado para salvar a foto

    @GetMapping("/perfil")
    public String perfil(Model model, Principal principal) {

        model.addAttribute(
            "usuario",
            usuarioService.buscarPorLogin(principal.getName())
        );

        return "perfil";
    }

    // --- NOVO MÉTODO: UPLOAD DE FOTO ---
    @PostMapping("/perfil/upload-foto")
    public String uploadFoto(@RequestParam("image") MultipartFile file, Principal principal) {
        if (principal != null && !file.isEmpty()) {
            try {
                // Busca o usuário logado
                Usuario usuario = usuarioRepository.findByUsernameOrCpf(principal.getName());
                
                // Converte a imagem enviada para Base64 (Texto)
                byte[] bytes = file.getBytes();
                String base64Image = Base64.getEncoder().encodeToString(bytes);
                
                // Atualiza e salva
                usuario.setFotoPerfil(base64Image);
                usuarioRepository.save(usuario);
                
                // Redirecionamento inteligente baseado no perfil
                String perfil = usuario.getPerfil();
                
                if (perfil.toUpperCase().contains("ADMIN")) {
                    return "redirect:/admin/painel";
                } else if (perfil.toUpperCase().contains("MEDICO") || perfil.toUpperCase().contains("ENFERMEIRO")) {
                    return "redirect:/profissional/painel";
                } else if (perfil.toUpperCase().contains("PACIENTE")) {
                    return "redirect:/pacientes"; // Redireciona para a rota padrão de pacientes
                }
                
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // Fallback caso algo falhe ou o perfil não seja identificado
        return "redirect:/home"; 
    }
}