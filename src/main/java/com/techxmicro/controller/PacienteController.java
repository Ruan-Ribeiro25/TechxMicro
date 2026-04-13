package com.techxmicro.controller;

import com.techxmicro.entity.Prontuario;
import com.techxmicro.entity.Usuario;
import com.techxmicro.models.PedidoExame;
import com.techxmicro.repository.AgendamentoRepository;
import com.techxmicro.repository.ProntuarioRepository;
import com.techxmicro.repository.SinaisVitaisRepository;
import com.techxmicro.repository.UsuarioRepository;
import com.techxmicro.service.LaboratorioService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;
import java.util.List;

@Controller
public class PacienteController {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AgendamentoRepository agendamentoRepository;
    @Autowired private ProntuarioRepository prontuarioRepository;
    
    // --- REPOSITÓRIO DE TRIAGEM ---
    @Autowired private SinaisVitaisRepository sinaisVitaisRepository;

    // --- SERVIÇO DE LABORATÓRIO [NOVO] ---
    @Autowired private LaboratorioService laboratorioService;

    // --- ROTA 1: Painel do Paciente ---
    @GetMapping("/pacientes")
    public String areaPaciente(Model model, Principal principal) {
        if(principal == null) return "redirect:/login";
        
        Usuario paciente = usuarioRepository.findByUsername(principal.getName());
        model.addAttribute("paciente", paciente);
        
        // 1. Histórico de Agendamentos
        model.addAttribute("historico", agendamentoRepository.findByUsuario(paciente));
        
        // 2. Histórico Clínico (Médico - Verde)
        List<Prontuario> prontuarios = prontuarioRepository.findByPacienteOrderByDataHoraDesc(paciente);
        model.addAttribute("historicoClinico", prontuarios);
        
        // 3. Histórico de Triagem (Enfermeiro - Azul)
        model.addAttribute("historicoTriagem", sinaisVitaisRepository.findByPacienteOrderByDataHoraDesc(paciente));
        
        // 4. Histórico de Exames Laboratoriais [NOVO]
        // Busca os exames deste paciente para exibir no dashboard
        List<PedidoExame> meusExames = laboratorioService.buscarPedidosPorPaciente(paciente);
        model.addAttribute("meusExames", meusExames);

        return "paciente/pacientes"; 
    }

    // --- ROTA 2: Abrir Formulário de Edição ---
    @GetMapping("/pacientes/editar")
    public String editarDados(Model model, Principal principal) {
        if(principal == null) return "redirect:/login";

        Usuario paciente = usuarioRepository.findByUsername(principal.getName());
        model.addAttribute("usuario", paciente);

        return "paciente/paciente-editar"; 
    }

    // --- ROTA 3: Salvar Alterações ---
    @PostMapping("/pacientes/salvar-dados")
    public String salvarDados(@ModelAttribute Usuario formUsuario, Principal principal) {
        if(principal == null) return "redirect:/login";

        Usuario usuarioBanco = usuarioRepository.findByUsername(principal.getName());

        if (usuarioBanco != null) {
            usuarioBanco.setTelefone(formUsuario.getTelefone());
            usuarioBanco.setCep(formUsuario.getCep());
            usuarioBanco.setCidade(formUsuario.getCidade());
            usuarioBanco.setLogradouro(formUsuario.getLogradouro());
            usuarioBanco.setNumero(formUsuario.getNumero());
            usuarioBanco.setBairro(formUsuario.getBairro());
            usuarioBanco.setUf(formUsuario.getUf());
            
            usuarioRepository.save(usuarioBanco);
        }

        return "redirect:/pacientes?atualizado=true";
    }
}