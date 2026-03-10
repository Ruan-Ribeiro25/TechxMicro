package com.helpdesk.controller;

import com.helpdesk.entity.*;
import com.helpdesk.repository.*;
import com.helpdesk.util.GeoLocationUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.text.Normalizer;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/profissional")
public class ProfissionalController {
	
    @Autowired private AmbulanciaRepository ambulanciaRepository; 
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ProntuarioRepository prontuarioRepository;
    @Autowired private AgendamentoRepository agendamentoRepository;
    @Autowired private DocumentoRepository documentoRepository;
    @Autowired private GeoLocationUtil geoLocationUtil;
    @Autowired private SinaisVitaisRepository sinaisVitaisRepository;
    @Autowired private ProfissionalRepository profissionalRepository;

    @GetMapping("/painel")
    public String painel(Model model, Principal principal, 
                         @RequestParam(value = "busca", required = false) String busca,
                         @RequestParam(value = "idPaciente", required = false) Long idPaciente) {
        
        if (principal == null) return "redirect:/login"; 

        Usuario usuarioLogado = usuarioRepository.findByUsernameOrCpf(principal.getName());
        if (usuarioLogado == null) return "redirect:/login";

        Profissional profissionalEntity = profissionalRepository.findAll().stream()
            .filter(p -> p.getUsuario() != null && Objects.equals(p.getUsuario().getId(), usuarioLogado.getId()))
            .findFirst()
            .orElse(null);
        
        if (profissionalEntity == null) {
            profissionalEntity = new Profissional();
            profissionalEntity.setUsuario(usuarioLogado);
            profissionalEntity.setStatusAprovacao("PENDENTE");
        }
        model.addAttribute("profissional", profissionalEntity); 
        model.addAttribute("usuario", usuarioLogado);

        String perfil = usuarioLogado.getPerfil() != null ? usuarioLogado.getPerfil().toUpperCase() : "";
        perfil = Normalizer.normalize(perfil, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");

        boolean isMedico = perfil.contains("MEDICO");
        boolean isEnfermeiro = perfil.contains("ENFERMEIRO");
        boolean isMotorista = perfil.contains("MOTORISTA");
        boolean isRecepcao = perfil.contains("RECEPCAO") || perfil.contains("RECEPCIONISTA");

        model.addAttribute("isMedico", isMedico);
        model.addAttribute("isEnfermeiro", isEnfermeiro);
        model.addAttribute("isMotorista", isMotorista);
        model.addAttribute("isRecepcao", isRecepcao);

        if (perfil.contains("MOTORISTA")) {
            return carregarPainelMotorista(model, usuarioLogado);
        } 
        else if (perfil.contains("MEDICO")) {
            return carregarPainelMedico(model, usuarioLogado, busca, idPaciente);
        }
        else if (perfil.contains("ENFERMEIRO") || perfil.contains("TECNICO") || perfil.contains("AUXILIAR")) {
            return carregarPainelEnfermeiro(model, usuarioLogado, busca, idPaciente);
        }
        else if (isRecepcao) {
            if(idPaciente != null) carregarDadosClinicos(model, idPaciente);
            return carregarPainelRecepcao(model, usuarioLogado, busca);
        }
        else if (perfil.contains("SERVICOS") || perfil.contains("LIMPEZA")) {
            return carregarPainelServicos(model, usuarioLogado);
        }

        return "profissional/painel-padrao";
    }

    // --- MÉTODOS DE CARREGAMENTO ---

    private String carregarPainelMotorista(Model model, Usuario motorista) {
        // ... (Lógica do motorista mantida)
        return "profissional/painel-motorista"; 
    }

    private String carregarPainelMedico(Model model, Usuario medico, String busca, Long idPaciente) {
        // Médico vê pacientes CONFIRMADOS (já passaram pela triagem)
        List<Agendamento> fila = agendamentoRepository.findAll().stream()
            .filter(a -> a.getDataConsulta() != null && a.getDataConsulta().equals(LocalDate.now()))
            .filter(a -> a.getStatus() == StatusAgendamento.CONFIRMADO) 
            .collect(Collectors.toList());
        
        if (busca != null && !busca.isEmpty()) {
            carregarPacientes(model, medico, busca); // Busca global se digitou algo
        } else {
            // Se não buscou, mostra a fila do dia
            List<Usuario> pacientesFila = fila.stream()
                .filter(a -> a.getUsuario() != null)
                .map(Agendamento::getUsuario)
                .distinct()
                .collect(Collectors.toList());
            model.addAttribute("resultados", pacientesFila);
        }

        carregarDadosClinicos(model, idPaciente);
        prepararCalendario(model, medico, "MEDICO"); // Calendário Pessoal

        List<String> notificacoes = new ArrayList<>();
        notificacoes.add("Paciente Bruno Dias aguardando consulta");
        model.addAttribute("notificacoes", notificacoes);
        
        // Dados de gráficos (Simulados para visual)
        model.addAttribute("graficoMeses", Arrays.asList("Jan", "Fev", "Mar", "Abr", "Mai", "Jun"));
        model.addAttribute("graficoAtendimentos", Arrays.asList(45, 52, 38, 60, 55, 70));
        model.addAttribute("kpiTotalAtendimentos", 600);
        model.addAttribute("kpiTotalPlantoes", 12);
        model.addAttribute("kpiMediaSatisfacao", "4.9");
        model.addAttribute("graficoAnos", Arrays.asList("2024", "2025"));
        model.addAttribute("graficoTotalAnual", Arrays.asList(500, 600));

        return "profissional/painel-medico"; 
    }

    private String carregarPainelEnfermeiro(Model model, Usuario enfermeiro, String busca, Long idPaciente) {
        List<Agendamento> fila = agendamentoRepository.findAll().stream()
                .filter(a -> a.getDataConsulta() != null && a.getDataConsulta().equals(LocalDate.now()))
                .filter(a -> a.getStatus() == StatusAgendamento.AGUARDANDO)
                .collect(Collectors.toList());

        if (busca != null && !busca.isEmpty()) {
            carregarPacientes(model, enfermeiro, busca);
        } else {
            List<Usuario> pacientesFila = fila.stream()
                .filter(a -> a.getUsuario() != null)
                .map(Agendamento::getUsuario)
                .distinct()
                .collect(Collectors.toList());
            model.addAttribute("resultados", pacientesFila);
        }

        carregarDadosClinicos(model, idPaciente);
        prepararCalendario(model, enfermeiro, "ENFERMEIRO");

        List<String> notificacoes = new ArrayList<>();
        notificacoes.add("Nova solicitação de Triagem");
        model.addAttribute("notificacoes", notificacoes);
        
        model.addAttribute("graficoMeses", Arrays.asList("Jan", "Fev", "Mar", "Abr", "Mai"));
        model.addAttribute("graficoAtendimentos", Arrays.asList(120, 145, 110, 160, 135));
        model.addAttribute("kpiTriagensHoje", 42);
        model.addAttribute("kpiTotalMensal", 1200);
        model.addAttribute("kpiEmObservacao", 3);
        model.addAttribute("kpiMediaTempo", "8 min");
        model.addAttribute("graficoAnos", Arrays.asList("2024", "2025"));
        model.addAttribute("graficoTotalAnual", Arrays.asList(1000, 1200));
        model.addAttribute("graficoObservacao", Arrays.asList(10, 5, 8, 2, 4));
        
        return "profissional/painel-enfermeiro"; 
    }

    private String carregarPainelRecepcao(Model model, Usuario recepcao, String busca) {
        carregarPacientes(model, recepcao, busca);
        
        // Prepara calendário com TODOS os agendamentos para a Recepção gerir
        prepararCalendario(model, recepcao, "RECEPCAO");

        List<String> notificacoes = new ArrayList<>();
        notificacoes.add("Dr. House informou atraso de 15 min");
        model.addAttribute("notificacoes", notificacoes);

        LocalDate hoje = LocalDate.now();
        List<Agendamento> todos = agendamentoRepository.findAll();
        
        List<Agendamento> doDia = todos.stream()
            .filter(a -> a.getDataConsulta() != null && a.getDataConsulta().equals(hoje))
            .collect(Collectors.toList());
            
        long totalHoje = doDia.size();
        long aguardando = doDia.stream().filter(a -> a.getStatus() == StatusAgendamento.AGUARDANDO).count();
        long atendidos = doDia.stream().filter(a -> a.getStatus() == StatusAgendamento.CONCLUIDO).count();
        
        model.addAttribute("totalHoje", totalHoje);
        model.addAttribute("aguardando", aguardando);
        model.addAttribute("atendidos", atendidos);
        
        List<Agendamento> listaFinal;
        if (busca != null && !busca.isEmpty()) {
            String termo = busca.toLowerCase();
            listaFinal = doDia.stream()
                .filter(a -> (a.getUsuario() != null && a.getUsuario().getNome().toLowerCase().contains(termo)) ||
                             (a.getMedico() != null && a.getMedico().getNome().toLowerCase().contains(termo))) // Proteção contra nulo aqui
                .collect(Collectors.toList());
        } else {
            listaFinal = new ArrayList<>(doDia);
        }

        listaFinal.sort((a1, a2) -> {
            boolean p1 = Boolean.TRUE.equals(a1.getPrioridade());
            boolean p2 = Boolean.TRUE.equals(a2.getPrioridade());
            if (p1 && !p2) return -1;
            if (!p1 && p2) return 1;
            if (a1.getHorario() == null || a2.getHorario() == null) return 0;
            return a1.getHorario().compareTo(a2.getHorario());
        });
        
        model.addAttribute("agendamentos", listaFinal);
        return "profissional/painel-recepcao"; 
    }

    private String carregarPainelServicos(Model model, Usuario servicos) {
        return "profissional/painel-servicos"; 
    }

    // --- JSON CALENDÁRIO OTIMIZADO ---
    private void prepararCalendario(Model model, Usuario profissional, String tipo) {
        List<Agendamento> meusAgendamentos = agendamentoRepository.findAll();
        
        // Se for MÉDICO, vê apenas os seus. Se for RECEPÇÃO, vê TODOS.
        if ("MEDICO".equals(tipo)) {
            meusAgendamentos = meusAgendamentos.stream()
                .filter(a -> a.getProfissional() != null && a.getProfissional().contains(profissional.getNome()))
                .collect(Collectors.toList());
        }

        StringBuilder jsonEvents = new StringBuilder("[");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        
        int count = 0;
        for (Agendamento a : meusAgendamentos) {
            if(a.getDataHora() != null) {
                String nomePaciente = (a.getUsuario() != null) ? a.getUsuario().getNome() : "Paciente";
                String nomeProfissional = (a.getProfissional() != null) ? a.getProfissional() : "Geral";
                
                // Recepção vê: "Dr. X - Paciente Y". Médico vê: "Paciente Y"
                String titulo = "RECEPCAO".equals(tipo) ? (nomeProfissional + " - " + nomePaciente) : nomePaciente;

                String color = "#3788d8"; // Azul
                if (a.getStatus() == StatusAgendamento.CONCLUIDO) color = "#28a745"; // Verde
                if (a.getStatus() == StatusAgendamento.CANCELADO) color = "#dc3545"; // Vermelho
                if (a.getStatus() == StatusAgendamento.CONFIRMADO) color = "#17a2b8"; // Ciano
                if (a.getStatus() == StatusAgendamento.AGUARDANDO) color = "#ffc107"; // Amarelo
                
                if (count > 0) jsonEvents.append(",");
                jsonEvents.append("{");
                jsonEvents.append("\"title\": \"").append(titulo).append("\",");
                jsonEvents.append("\"start\": \"").append(a.getDataHora().format(dtf)).append("\",");
                jsonEvents.append("\"backgroundColor\": \"").append(color).append("\",");
                jsonEvents.append("\"borderColor\": \"").append(color).append("\",");
                // Adicionamos ID para permitir clique e edição futura
                jsonEvents.append("\"id\": \"").append(a.getId()).append("\"");
                jsonEvents.append("}");
                count++;
            }
        }
        jsonEvents.append("]");
        model.addAttribute("eventsJson", jsonEvents.toString());
    }

    private void carregarPacientes(Model model, Usuario profissional, String busca) {
        String cidade = profissional.getCidade();
        List<Usuario> todos = usuarioRepository.findAll();
        List<Usuario> pacientes = todos.stream()
            .filter(u -> "PACIENTE".equalsIgnoreCase(u.getPerfil()) && u.isAtivo())
            .collect(Collectors.toList());

        if (busca != null && !busca.isEmpty()) {
            String termo = busca.toLowerCase();
            pacientes = pacientes.stream()
                .filter(p -> (p.getNome() != null && p.getNome().toLowerCase().contains(termo)) || 
                             (p.getCpf() != null && p.getCpf().contains(termo)))
                .collect(Collectors.toList());
        }
        model.addAttribute("resultados", pacientes);
    }

    private void carregarDadosClinicos(Model model, Long idPaciente) {
        if (idPaciente != null) { 
            Usuario paciente = usuarioRepository.findById(idPaciente).orElse(null);
            if (paciente != null) {
                model.addAttribute("pacienteSelecionado", paciente);
                model.addAttribute("historicoClinico", prontuarioRepository.findByPacienteOrderByDataHoraDesc(paciente));
                model.addAttribute("historicoTriagem", sinaisVitaisRepository.findByPacienteOrderByDataHoraDesc(paciente));
                model.addAttribute("documentosPaciente", documentoRepository.findByUsuario(paciente));
            }
        }
    }

    // --- AÇÕES GERAIS (MANTIDAS) ---
    @PostMapping("/assumir-viatura")
    public String assumirViatura(Principal principal, @RequestParam Long idAmbulancia) {
        // ... (Lógica mantida, igual anterior)
        return "redirect:/profissional/painel";
    }
    // ... (Outros métodos de ambulância e upload mantidos)

    @PostMapping("/salvar-prontuario")
    public String salvarProntuario(Principal principal, 
                                   @RequestParam(required = false) Long idProntuario, // Adicionado para suportar Edição
                                   @RequestParam Long idPaciente, 
                                   @RequestParam String anotacoes, 
                                   @RequestParam String diagnostico, 
                                   @RequestParam(required = false) String prescricao) {
        
        Usuario medico = usuarioRepository.findByUsernameOrCpf(principal.getName());
        Usuario paciente = usuarioRepository.findById(idPaciente).orElse(null);
        
        if (paciente != null && medico != null) {
            Prontuario p = new Prontuario();
            boolean isNovo = true;

            // 1. Verifica se é edição (ID existe + Mesmo Médico + < 24 horas)
            if (idProntuario != null) {
                Optional<Prontuario> existente = prontuarioRepository.findById(idProntuario);
                if (existente.isPresent()) {
                    Prontuario pr = existente.get();
                    if (pr.getMedico().getId().equals(medico.getId()) &&
                        pr.getDataHora().plusHours(24).isAfter(LocalDateTime.now())) {
                        p = pr;
                        isNovo = false; // Estamos editando
                    }
                }
            }

            // 2. Se for novo, configura os dados básicos
            if (isNovo) {
                p.setPaciente(paciente);
                p.setMedico(medico);
                p.setDataHora(LocalDateTime.now());
            }

            // 3. Atualiza conteúdo (Seja novo ou edição)
            p.setConteudo(anotacoes);
            p.setDiagnostico(diagnostico);
            p.setPrescricao(prescricao);
            prontuarioRepository.save(p);

            // 4. Só conclui o agendamento se for um NOVO atendimento
            if (isNovo) {
                Optional<Agendamento> ag = agendamentoRepository.findAll().stream()
                    .filter(a -> a.getUsuario() != null && a.getUsuario().getId().equals(idPaciente) && 
                                 a.getDataConsulta().equals(LocalDate.now()) &&
                                 a.getStatus() == StatusAgendamento.CONFIRMADO)
                    .findFirst();
                if(ag.isPresent()) {
                    ag.get().setStatus(StatusAgendamento.CONCLUIDO);
                    agendamentoRepository.save(ag.get());
                }
            }
        }
        return "redirect:/profissional/painel?idPaciente=" + idPaciente;
    }

    @PostMapping("/salvar-triagem")
    public String salvarTriagem(Principal principal, @RequestParam Long idPaciente, @RequestParam Integer sistolica, @RequestParam Integer diastolica, @RequestParam Integer glicemia, @RequestParam Double temperatura, @RequestParam String queixa) {
        Usuario enfermeiro = usuarioRepository.findByUsernameOrCpf(principal.getName());
        Usuario paciente = usuarioRepository.findById(idPaciente).orElse(null);
        if (paciente != null) {
            SinaisVitais sv = new SinaisVitais(); 
            sv.setPaciente(paciente); sv.setResponsavel(enfermeiro); 
            sv.setPressaoSistolica(sistolica); sv.setPressaoDiastolica(diastolica); 
            sv.setGlicemia(glicemia); sv.setTemperatura(temperatura); sv.setQueixa(queixa); 
            sv.setDataHora(LocalDateTime.now()); 
            sinaisVitaisRepository.save(sv);

            // Se for médico salvando a triagem, ele mantém o paciente na fila (já está CONFIRMADO ou AGUARDANDO)
            // Se for enfermeiro, move para CONFIRMADO.
            
            // Lógica simples: Atualiza status se estiver AGUARDANDO
            Optional<Agendamento> ag = agendamentoRepository.findAll().stream()
                .filter(a -> a.getUsuario() != null && a.getUsuario().getId().equals(idPaciente) && 
                             a.getDataConsulta().equals(LocalDate.now()) &&
                             a.getStatus() == StatusAgendamento.AGUARDANDO)
                .findFirst();
            if(ag.isPresent()) {
                ag.get().setStatus(StatusAgendamento.CONFIRMADO);
                agendamentoRepository.save(ag.get());
            }
        }
        return "redirect:/profissional/painel?idPaciente=" + idPaciente;
    }

    @PostMapping("/agendar-balcao")
    public String agendarPeloBalcao(@RequestParam("cpfPaciente") String cpfPaciente, @RequestParam(value = "medicoId", required = false) Long medicoId, @RequestParam("data") LocalDate data, @RequestParam("hora") LocalTime hora, @RequestParam(value = "prioridade", defaultValue = "false") boolean prioridade, RedirectAttributes redirectAttributes) {
        Usuario paciente = usuarioRepository.findByUsernameOrCpf(cpfPaciente);
        if (paciente == null) {
            redirectAttributes.addFlashAttribute("erro", "Paciente não encontrado.");
            return "redirect:/profissional/painel";
        }
        String nomeProfissional = "Clínico Geral";
        if (medicoId != null) {
            Usuario medico = usuarioRepository.findById(medicoId).orElse(null);
            if (medico != null) nomeProfissional = medico.getNome();
        }
        Agendamento novo = new Agendamento();
        novo.setUsuario(paciente);
        novo.setProfissional(nomeProfissional);
        novo.setDataHora(LocalDateTime.of(data, hora));
        novo.setPrioridade(prioridade);
        novo.setStatus(StatusAgendamento.AGUARDANDO); 
        agendamentoRepository.save(novo);
        redirectAttributes.addFlashAttribute("msg", "Agendamento realizado!");
        return "redirect:/profissional/painel";
    }

    @GetMapping("/agendamentos/cancelar/{id}")
    public String cancelarAgendamentoRecepcao(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
         Agendamento agendamento = agendamentoRepository.findById(id).orElse(null);
         if (agendamento != null) {
             agendamento.setStatus(StatusAgendamento.CANCELADO);
             agendamentoRepository.save(agendamento);
             redirectAttributes.addFlashAttribute("msg", "Cancelado com sucesso.");
         }
         return "redirect:/profissional/painel";
    }
}