package com.techxmicro.controller;

import com.techxmicro.entity.Agendamento;
import com.techxmicro.entity.StatusAgendamento;
import com.techxmicro.entity.Usuario;
import com.techxmicro.repository.AgendamentoRepository;
import com.techxmicro.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*; // Inclui PathVariable, GetMapping, etc.
import org.springframework.web.servlet.mvc.support.RedirectAttributes; 

import java.security.Principal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Controller
public class AgendamentoController {

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Lista oficial igual ao cadastro profissional
    private final List<String> ESPECIALIDADES = Arrays.asList(
        "Anestesiologia", "Cardiologia", "Cirurgia Geral", "Clínica Médica",
        "Dermatologia", "Endocrinologia", "Gastroenterologia", "Ginecologia e Obstetrícia",
        "Neurologia", "Oftalmologia", "Ortopedia e Traumatologia", "Otorrinolaringologia",
        "Pediatria", "Psiquiatria", "Radiologia", "Triagem", "Urologia", "UTI Adulto", "UTI Pediátrica"
    );

    // Lista de Feriados Fixos (Dia-Mês)
    private final List<String> FERIADOS = Arrays.asList(
        "01-01", "21-04", "01-05", "07-09", "12-10", "02-11", "15-11", "25-12"
    );

    @GetMapping("/agendamentos")
    public String listarAgendamentos(Model model, Principal principal) {
        if(principal == null) return "redirect:/login";

        String loginUsuario = principal.getName();
        Usuario usuario = usuarioRepository.findByUsername(loginUsuario);
        
        model.addAttribute("usuario", usuario);
        List<Agendamento> lista = agendamentoRepository.findByUsuario(usuario);
        model.addAttribute("agendamentos", lista);

        // --- SISTEMA DE NOTIFICAÇÕES (ITEM 4) ---
        List<String> notificacoes = new ArrayList<>();
        // Correção: Uso do Enum StatusAgendamento.CONFIRMADO em vez de String "Agendado"
        long agendados = lista.stream().filter(a -> a.getStatus() == StatusAgendamento.CONFIRMADO).count();
        
        if (agendados > 0) {
            notificacoes.add("Lembrete: Você tem " + agendados + " consulta(s) agendada(s). Chegue 15 minutos antes.");
        } else {
            notificacoes.add("Dica: Mantenha seus exames em dia. Agende uma consulta!");
        }
        notificacoes.add("Bem-vindo ao VidaPlus! Mantenha seus dados cadastrais atualizados.");
        
        model.addAttribute("notificacoes", notificacoes);
        model.addAttribute("qtdNotificacoes", notificacoes.size());
        // ----------------------------------------

        return "agendamento/agendamentos"; 
    }

    // --- FORMULÁRIO DE AGENDAMENTO ---
    @GetMapping("/agendamentos/novo")
    public String novoAgendamento(Model model) {
        model.addAttribute("agendamento", new Agendamento());
        model.addAttribute("listaEspecialidades", ESPECIALIDADES); 
        return "agendamento/agendamento-form";
    }

    // --- SALVAR COM VALIDAÇÕES DE HORÁRIO E REGRAS ---
    @PostMapping("/agendamentos/salvar")
    public String salvarAgendamento(Agendamento agendamento, 
                                    Principal principal,
                                    @RequestParam(required = false) Long profissionalId,
                                    RedirectAttributes redirectAttributes) { 
        
        String loginUsuario = principal.getName();
        Usuario usuario = usuarioRepository.findByUsername(loginUsuario);
        
        agendamento.setUsuario(usuario);
        // Correção: Define status como CONFIRMADO (Agendado via sistema)
        agendamento.setStatus(StatusAgendamento.CONFIRMADO); 
        
        String nomeMedico = "Profissional Geral";
        if (profissionalId != null) {
            Usuario medico = usuarioRepository.findById(profissionalId).orElse(null);
            if (medico != null) {
                nomeMedico = "Dr(a). " + medico.getNome();
            }
        }
        agendamento.setProfissional(nomeMedico);
        
        // --- INÍCIO DAS VALIDAÇÕES ---
        LocalDateTime dataHora = agendamento.getDataHora();
        if (dataHora == null) dataHora = LocalDateTime.now().plusDays(1);

        // A. Data no Passado
        if (dataHora.isBefore(LocalDateTime.now())) {
            redirectAttributes.addFlashAttribute("erroAgendamento", "Não é possível agendar em uma data retroativa.");
            return "redirect:/agendamentos/novo";
        }

        // B. Fim de Semana
        DayOfWeek diaSemana = dataHora.getDayOfWeek();
        if (diaSemana == DayOfWeek.SATURDAY || diaSemana == DayOfWeek.SUNDAY) {
            redirectAttributes.addFlashAttribute("erroAgendamento", "Atendimentos apenas de Segunda a Sexta-feira.");
            return "redirect:/agendamentos/novo";
        }

        // C. Horário Comercial
        LocalTime hora = dataHora.toLocalTime();
        if (hora.isBefore(LocalTime.of(8, 0)) || hora.isAfter(LocalTime.of(18, 0))) {
            redirectAttributes.addFlashAttribute("erroAgendamento", "Horário de atendimento apenas entre 08:00 e 18:00.");
            return "redirect:/agendamentos/novo";
        }

        // D. Feriados
        String diaMes = String.format("%02d-%02d", dataHora.getDayOfMonth(), dataHora.getMonthValue());
        if (FERIADOS.contains(diaMes)) {
            redirectAttributes.addFlashAttribute("erroAgendamento", "Não há atendimento em feriados nacionais.");
            return "redirect:/agendamentos/novo";
        }

        // E. Conflito (Verifica se já existe agendamento ativo - NÃO CANCELADO)
        boolean ocupado = agendamentoRepository.existsByProfissionalAndDataHoraAndStatusNot(nomeMedico, dataHora, StatusAgendamento.CANCELADO);
        
        if (ocupado) {
            redirectAttributes.addFlashAttribute("erroAgendamento", "Este profissional já possui um agendamento neste horário. Por favor, escolha outro.");
            return "redirect:/agendamentos/novo";
        }
        // --- FIM DAS VALIDAÇÕES ---

        agendamentoRepository.save(agendamento);
        return "redirect:/agendamentos?sucesso=true"; 
    }

    // --- CANCELAMENTO PELO PACIENTE (POST) ---
    @PostMapping("/agendamentos/cancelar")
    public String cancelarAgendamento(@RequestParam("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            Agendamento agendamento = agendamentoRepository.findById(id).orElse(null);
            
            if (agendamento != null) {
                StatusAgendamento statusAtual = agendamento.getStatus();
                
                if (statusAtual != StatusAgendamento.CONCLUIDO && statusAtual != StatusAgendamento.CANCELADO) {
                    agendamento.setStatus(StatusAgendamento.CANCELADO); 
                    agendamentoRepository.save(agendamento);
                    redirectAttributes.addFlashAttribute("sucesso", "Consulta cancelada com sucesso.");
                } else {
                    redirectAttributes.addFlashAttribute("erro", "Esta consulta não pode ser cancelada.");
                }
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao processar cancelamento.");
        }
        return "redirect:/agendamentos";
    }

    // Proteção contra refresh/login no cancelar (Pacientes)
    @GetMapping("/agendamentos/cancelar")
    public String cancelarAgendamentoRedirect() {
        return "redirect:/agendamentos";
    }

    // =========================================================
    // MÉTODOS PARA O PAINEL DA RECEPÇÃO (ITEM 8)
    // =========================================================

    // Ação do botão "Confirmar Chegada" (Check-in)
    @GetMapping("/agendamentos/checkin/{id}")
    public String checkinAgendamento(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Agendamento agendamento = agendamentoRepository.findById(id).orElse(null);
        if (agendamento != null) {
            // Check-in: O paciente chegou e está na sala de espera
            agendamento.setStatus(StatusAgendamento.AGUARDANDO);
            agendamentoRepository.save(agendamento);
            redirectAttributes.addFlashAttribute("msg", "Check-in realizado! Paciente aguardando.");
        }
        return "redirect:/profissional/painel"; // Retorna para o dashboard da recepção
    }

    // Ação do botão "Cancelar" (Recepção) via Link/GET
    @GetMapping("/agendamentos/cancelar/{id}")
    public String cancelarAgendamentoRecepcao(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
         Agendamento agendamento = agendamentoRepository.findById(id).orElse(null);
         if (agendamento != null) {
             agendamento.setStatus(StatusAgendamento.CANCELADO);
             agendamentoRepository.save(agendamento);
             redirectAttributes.addFlashAttribute("msg", "Agendamento cancelado pela recepção.");
         }
         return "redirect:/profissional/painel";
    }
}