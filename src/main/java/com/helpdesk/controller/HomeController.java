package com.helpdesk.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.core.io.ByteArrayResource;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import com.helpdesk.entity.Chamado;
import com.helpdesk.entity.InteracaoChamado;
import com.helpdesk.entity.Usuario;
import com.helpdesk.enums.CategoriaChamado;
import com.helpdesk.enums.StatusChamado;
import com.helpdesk.enums.UrgenciaChamado;
import com.helpdesk.repository.ChamadoRepository;
import com.helpdesk.repository.InteracaoChamadoRepository;
import com.helpdesk.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class HomeController {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ChamadoRepository chamadoRepository;
    @Autowired private InteracaoChamadoRepository interacaoRepository;
    @PersistenceContext
    private EntityManager entityManager;

    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }

    @GetMapping("/home")
    public String home(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        Usuario usuario = usuarioRepository.findByUsernameOrCpf(principal.getName());
        if (usuario == null) return "redirect:/login?error=user_sync";
        model.addAttribute("usuario", usuario);
        return "pages/home"; 
    }

    @GetMapping("/helpdesk")
    public String helpdesk(@RequestParam(defaultValue = "0") int page, Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        Usuario usuario = usuarioRepository.findByUsernameOrCpf(principal.getName());
        
        boolean isTechOrAdmin = usuario.getPerfil() != null && (
                usuario.getPerfil().toUpperCase().contains("ADMIN") ||
                usuario.getPerfil().toUpperCase().contains("TI") ||
                usuario.getPerfil().toUpperCase().contains("MASTER")
        );

        List<Chamado> todosChamados = chamadoRepository.findAll();
        
        long pendentesInfra = todosChamados.stream().filter(c -> c.getStatus() == StatusChamado.PENDENTE && c.getCategoria() == CategoriaChamado.TI).count();
        long pendentesSoftware = todosChamados.stream().filter(c -> c.getStatus() == StatusChamado.PENDENTE && c.getCategoria() == CategoriaChamado.SOFTWARE).count();
        long resolvidos = todosChamados.stream().filter(c -> c.getStatus() == StatusChamado.RESOLVIDO).count();
        long cancelados = todosChamados.stream().filter(c -> c.getStatus() == StatusChamado.CANCELADO).count();

     // PREPARAÇÃO DOS DADOS PARA A LINHA DO TEMPO DO GRÁFICO
        int[] infraPorHora = new int[24];
        int[] infraPorDia = new int[7];
        int[] infraPorMes = new int[12];
        int[] infraPorAno = new int[10];
        
        int anoAtual = LocalDateTime.now().getYear();

        for (Chamado c : todosChamados) {
            if (c.getCategoria() == CategoriaChamado.TI && c.getDataAbertura() != null) {
                LocalDateTime dt = c.getDataAbertura();
                
                // 1. DIA (Hoje - Por hora)
                if (dt.toLocalDate().isEqual(LocalDateTime.now().toLocalDate())) {
                    infraPorHora[dt.getHour()]++;
                }
                
                // 2. SEMANA (Por dia da semana)
                int diaDaSemana = dt.getDayOfWeek().getValue();
                if (diaDaSemana >= 1 && diaDaSemana <= 7) { 
                    infraPorDia[diaDaSemana - 1]++;
                }
                
                // 3. 1 ANO (Por mês dentro do ano atual)
                if (dt.getYear() == anoAtual) {
                    infraPorMes[dt.getMonthValue() - 1]++;
                }
                
                // 4. 10 ANOS (Por ano na última década)
                int diffAno = anoAtual - dt.getYear();
                if (diffAno >= 0 && diffAno < 10) {
                    infraPorAno[9 - diffAno]++;
                }
            }
        }
        long softResolvidos = todosChamados.stream().filter(c -> c.getCategoria() == CategoriaChamado.SOFTWARE && c.getStatus() == StatusChamado.RESOLVIDO).count();
        long softPendentes = todosChamados.stream().filter(c -> c.getCategoria() == CategoriaChamado.SOFTWARE && c.getStatus() == StatusChamado.PENDENTE).count();
        long softCancelados = todosChamados.stream().filter(c -> c.getCategoria() == CategoriaChamado.SOFTWARE && c.getStatus() == StatusChamado.CANCELADO).count();

        Pageable pageable = PageRequest.of(page, 7, Sort.by(Sort.Direction.DESC, "dataAbertura"));
        Page<Chamado> chamadosPage = chamadoRepository.findAll(pageable);

        model.addAttribute("usuario", usuario);
        model.addAttribute("isTechOrAdmin", isTechOrAdmin);
        model.addAttribute("chamados", chamadosPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", chamadosPage.getTotalPages());

        model.addAttribute("pendentesInfra", pendentesInfra);
        model.addAttribute("pendentesSoftware", pendentesSoftware);
        model.addAttribute("resolvidos", resolvidos);
        model.addAttribute("cancelados", cancelados);
        
        model.addAttribute("infraPorDia", infraPorDia); // Você já tem essa linha
        model.addAttribute("infraPorHora", infraPorHora);
        model.addAttribute("infraPorMes", infraPorMes);
        model.addAttribute("infraPorAno", infraPorAno);
        model.addAttribute("anoAtual", anoAtual);
        model.addAttribute("softResolvidos", softResolvidos);
        model.addAttribute("softPendentes", softPendentes);
        model.addAttribute("softCancelados", softCancelados);

        return "pages/helpdesk"; 
    }

    @GetMapping("/helpdesk/cadastrar-tecnico")
    public String abrirTelaCadastroTecnico(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        
        Usuario usuario = usuarioRepository.findByUsernameOrCpf(principal.getName());
        model.addAttribute("usuario", usuario);
        
        return "pages/cadastrar-tecnico"; 
    }

    // ==================== NOVA ROTA DE REGISTO DO TÉCNICO ====================
    @PostMapping("/helpdesk/cadastrar-tecnico")
    public String salvarNovoTecnico(
            @RequestParam("nome") String nome,
            @RequestParam("cpf") String cpf,
            @RequestParam("email") String email,
            @RequestParam("senha") String senha,
            @RequestParam("perfilTecnico") String perfilTecnico,
            Principal principal) {
            
        if (principal == null) return "redirect:/login";

        Usuario novoTecnico = new Usuario();
        novoTecnico.setNome(nome);
        novoTecnico.setCpf(cpf);
        novoTecnico.setUsername(cpf); // Define o CPF como nome de utilizador para login
        novoTecnico.setEmail(email);
        
        // CORREÇÃO 1: Seta o perfil diretamente como String
        novoTecnico.setPerfil(perfilTecnico); 

        // CORREÇÃO 2: Instancia o encoder e utiliza setSenha() (conforme seu Usuario.java)
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        novoTecnico.setSenha(encoder.encode(senha)); 
        
        // REGRA DE NEGÓCIO: O técnico nasce INATIVO para forçar a aprovação do Administrador
        novoTecnico.setAtivo(false); 

        usuarioRepository.save(novoTecnico);

        // Redireciona devolvendo a variável de sucesso para ativar o alerta verde no HTML
        return "redirect:/helpdesk/cadastrar-tecnico?sucesso=true";
    }
    // =========================================================================

    @GetMapping("/helpdesk/novo-chamado")
    public String novoChamado(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        Usuario usuario = usuarioRepository.findByUsernameOrCpf(principal.getName());
        model.addAttribute("usuario", usuario);
        return "pages/novo-chamado"; 
    }

    @PostMapping("/helpdesk/novo-chamado")
    public String salvarChamado(
            @RequestParam("categoria") CategoriaChamado categoria,
            @RequestParam("urgencia") UrgenciaChamado urgencia,
            @RequestParam("assunto") String assunto,
            @RequestParam("descricao") String descricao,
            Principal principal) {
        if (principal == null) return "redirect:/login";
        Usuario solicitante = usuarioRepository.findByUsernameOrCpf(principal.getName());

        Chamado chamado = new Chamado();
        chamado.setCategoria(categoria);
        chamado.setUrgencia(urgencia);
        chamado.setAssunto(assunto);
        chamado.setDescricao(descricao);
        chamado.setSolicitante(solicitante);
        chamado.setStatus(StatusChamado.PENDENTE);
        chamado.setDataAbertura(LocalDateTime.now());

        chamadoRepository.save(chamado);
        return "redirect:/helpdesk"; 
    }

    @GetMapping("/helpdesk/visualizar-chamado")
    @Transactional(readOnly = true)
    public String visualizarChamado(@RequestParam(value = "id", required = false) Long id, Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        Usuario usuario = usuarioRepository.findByUsernameOrCpf(principal.getName());

        boolean isTechOrAdmin = usuario.getPerfil() != null && (
                usuario.getPerfil().toUpperCase().contains("ADMIN") ||
                usuario.getPerfil().toUpperCase().contains("TI") ||
                usuario.getPerfil().toUpperCase().contains("MASTER")
        );

        if (id == null) return "redirect:/helpdesk";
        Chamado chamado = chamadoRepository.findById(id).orElse(null);
        if (chamado == null) return "redirect:/helpdesk";

        model.addAttribute("usuario", usuario);
        model.addAttribute("chamado", chamado);
        model.addAttribute("isTechOrAdmin", isTechOrAdmin);

        return "pages/visualizar-chamado"; 
    }

    @GetMapping("/helpdesk/assumir")
    public String assumirChamado(@RequestParam("id") Long id, Principal principal, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        Usuario tecnico = usuarioRepository.findByUsernameOrCpf(principal.getName());
        Chamado chamado = chamadoRepository.findById(id).orElse(null);

        if (chamado != null && chamado.getResponsavel() == null) {
            String perfil = tecnico.getPerfil() != null ? tecnico.getPerfil().toUpperCase() : "";
            
            // Regras de liberação
            boolean isMaster = perfil.contains("ADMIN") || perfil.contains("MASTER");
            boolean isAmbos = perfil.contains("TECNICO_AMBOS");
            boolean podeAssumir = isMaster || isAmbos;

            // Validação cruzada
            if (!podeAssumir) {
                if (chamado.getCategoria() == CategoriaChamado.TI && perfil.contains("TECNICO_TI")) {
                    podeAssumir = true;
                } else if (chamado.getCategoria() == CategoriaChamado.SOFTWARE && perfil.contains("TECNICO_SOFTWARE")) {
                    podeAssumir = true;
                }
            }

            if (podeAssumir) {
                chamado.setResponsavel(tecnico);
                chamadoRepository.save(chamado);
            } else {
                // Devolve um erro na tela se ele tentar pegar da fila errada
                redirectAttributes.addFlashAttribute("erroFila", "Acesso Negado: Sua especialidade não permite assumir chamados desta categoria.");
            }
        }
        return "redirect:/helpdesk"; 
    }

    @PostMapping("/helpdesk/atualizar-status")
    public String atualizarStatus(@RequestParam("id") Long id, @RequestParam("status") StatusChamado status, Principal principal) {
        if (principal == null) return "redirect:/login";
        Chamado chamado = chamadoRepository.findById(id).orElse(null);

        if (chamado != null) {
            chamado.setStatus(status);
            if (status == StatusChamado.RESOLVIDO || status == StatusChamado.CANCELADO) {
                chamado.setDataFechamento(LocalDateTime.now());
            }
            chamadoRepository.save(chamado);
        }
        return "redirect:/helpdesk/visualizar-chamado?id=" + id;
    }

    @Transactional
    @PostMapping("/helpdesk/responder")
    public String responderChamado(@RequestParam("chamadoId") Long chamadoId,
                                   @RequestParam("mensagem") String texto,
                                   Principal principal) {
        if (principal == null) return "redirect:/login";

        try {
            Usuario autor = usuarioRepository.findByUsernameOrCpf(principal.getName());
            Chamado chamado = chamadoRepository.findById(chamadoId).orElseThrow(() -> new RuntimeException("Chamado não encontrado"));

            if (texto != null && !texto.trim().isEmpty()) {
                InteracaoChamado interacao = new InteracaoChamado();
                interacao.setChamado(chamado);
                interacao.setAutor(autor);
                interacao.setTexto(texto);
                interacao.setDataEnvio(LocalDateTime.now());
                
                // O saveAndFlush obriga o Java a injetar o dado no MySQL neste exato milissegundo
                interacaoRepository.saveAndFlush(interacao);
            }
        } catch (Exception e) {
            // Se houver falha no banco, o sistema vai estourar um erro na sua tela e não vai mais esconder!
            throw new RuntimeException("Erro ao salvar mensagem no banco de dados: " + e.getMessage(), e);
        }

        // O segredo do Cache: Adicionar o timestamp (&t=...) no link obriga o navegador/Cloudflare a carregar a página fresca!
        return "redirect:/helpdesk/visualizar-chamado?id=" + chamadoId + "&t=" + System.currentTimeMillis();
    }
    
 // =================================================================================
    // MÓDULO DE AUDITORIA EXCLUSIVA DO HELPDESK (CHAMADOS E INTERAÇÕES)
    // =================================================================================

    @GetMapping("/helpdesk/auditoria")
    public String auditoriaHelpdesk(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        Usuario usuario = usuarioRepository.findByUsernameOrCpf(principal.getName());
        
        boolean isTechOrAdmin = usuario.getPerfil() != null && (
                usuario.getPerfil().toUpperCase().contains("ADMIN") ||
                usuario.getPerfil().toUpperCase().contains("TI") ||
                usuario.getPerfil().toUpperCase().contains("MASTER")
        );
        if (!isTechOrAdmin) return "redirect:/helpdesk";

        model.addAttribute("usuario", usuario);
        model.addAttribute("listaLogs", gerarLinhaDoTempoHelpdesk());
        model.addAttribute("tipoRelatorio", "CICLO DE VIDA DOS CHAMADOS");
        
        return "pages/helpdesk-auditoria"; 
    }

    // --- ROTA PARA DOWNLOAD DO RELATÓRIO DO HELPDESK ---
    @GetMapping("/helpdesk/download-auditoria/{formato}")
    public ResponseEntity<ByteArrayResource> downloadAuditoriaHelpdesk(@PathVariable String formato, Principal principal) {
        List<Object[]> dados = gerarLinhaDoTempoHelpdesk();
        String conteudo = "";
        String filename = "auditoria_chamados_" + System.currentTimeMillis();
        MediaType mediaType = MediaType.TEXT_PLAIN;

        try {
            if ("JSON".equalsIgnoreCase(formato)) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
                mapper.enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT);
                
                List<java.util.Map<String, Object>> dadosJson = new ArrayList<>();
                for (Object[] row : dados) {
                    java.util.Map<String, Object> map = new java.util.LinkedHashMap<>();
                    map.put("data_hora", row[0].toString());
                    map.put("acao", row[1]);
                    map.put("detalhes", row[2]);
                    map.put("usuario_responsavel", row[3]);
                    dadosJson.add(map);
                }
                conteudo = mapper.writeValueAsString(dadosJson);
                filename += ".json";
                mediaType = MediaType.APPLICATION_JSON;

            } else if ("CSV".equalsIgnoreCase(formato)) {
                StringBuilder sb = new StringBuilder();
                sb.append("DATA_HORA;ACAO;DETALHES;USUARIO_RESPONSAVEL\n");
                for (Object[] row : dados) {
                    sb.append(row[0]).append(";")
                      .append(row[1]).append(";")
                      .append(row[2].toString().replace(";", ",")).append(";")
                      .append(row[3]).append("\n");
                }
                conteudo = sb.toString();
                filename += ".csv";
                mediaType = MediaType.parseMediaType("text/csv");

            } else { 
                StringBuilder sb = new StringBuilder();
                sb.append("=== AUDITORIA DE CHAMADOS PIXEL TI ===\n");
                sb.append("Gerado em: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("\n");
                sb.append("Total de Eventos: ").append(dados.size()).append("\n");
                sb.append("=========================================\n\n");
                for (Object[] row : dados) {
                    sb.append("DATA/HORA: ").append(row[0]).append("\n");
                    sb.append("AÇÃO: ").append(row[1]).append("\n");
                    sb.append("USUÁRIO: ").append(row[3]).append("\n");
                    sb.append("DETALHES: ").append(row[2]).append("\n");
                    sb.append("--------------------------------------------------\n");
                }
                conteudo = sb.toString();
                filename += ".txt";
            }
        } catch (Exception e) {
            conteudo = "Erro ao gerar arquivo de auditoria.";
        }

        ByteArrayResource resource = new ByteArrayResource(conteudo.getBytes());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + filename)
                .contentType(mediaType)
                .contentLength(resource.contentLength())
                .body(resource);
    }

    // --- MOTOR QUE VARRE O BANCO E GERA A LINHA DO TEMPO ---
    private List<Object[]> gerarLinhaDoTempoHelpdesk() {
        List<Chamado> chamados = chamadoRepository.findAll();
        List<Object[]> logsHelpdesk = new ArrayList<>();

        for (Chamado c : chamados) {
            // 1. Registro de Abertura do Chamado
            logsHelpdesk.add(new Object[]{
                c.getDataAbertura() != null ? c.getDataAbertura() : LocalDateTime.now(),
                "ABERTURA DE TICKET",
                "Ticket #" + c.getId() + " - Categoria: " + c.getCategoria() + " | Assunto: " + c.getAssunto(),
                c.getSolicitante() != null ? c.getSolicitante().getNome() : "Usuário Desconhecido"
            });

            // 2. Registro de Fechamento (Resolvido ou Cancelado)
            if (c.getDataFechamento() != null) {
                String acao = c.getStatus().name().equals("RESOLVIDO") ? "TICKET RESOLVIDO" : "TICKET CANCELADO";
                logsHelpdesk.add(new Object[]{
                    c.getDataFechamento(),
                    acao,
                    "Ticket #" + c.getId() + " finalizado. Status final: " + c.getStatus(),
                    c.getResponsavel() != null ? c.getResponsavel().getNome() : "Sistema"
                });
            }

            // 3. Registro de Interações (Respostas no Chat)
            if (c.getInteracoes() != null) {
                for (InteracaoChamado i : c.getInteracoes()) {
                    logsHelpdesk.add(new Object[]{
                        i.getDataEnvio() != null ? i.getDataEnvio() : LocalDateTime.now(),
                        "NOVA INTERAÇÃO",
                        "Ticket #" + c.getId() + " - Resposta adicionada.",
                        i.getAutor() != null ? i.getAutor().getNome() : "Usuário Desconhecido"
                    });
                }
            }
        }

        // Ordenar do mais recente para o mais antigo
        logsHelpdesk.sort((a, b) -> {
            LocalDateTime dataA = (LocalDateTime) a[0];
            LocalDateTime dataB = (LocalDateTime) b[0];
            return dataB.compareTo(dataA);
        });

        return logsHelpdesk;
    }
}