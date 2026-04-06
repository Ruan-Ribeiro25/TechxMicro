package com.helpdesk.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
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
import java.time.ZoneId; 
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
        Usuario usuario = usuarioRepository.findByUsernameOrEmail(principal.getName());
        if (usuario == null) return "redirect:/login?error=user_sync";
        model.addAttribute("usuario", usuario);
        return "pages/home"; 
    }

    @GetMapping("/helpdesk")
    public String helpdesk(@RequestParam(defaultValue = "0") int page, Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        Usuario usuario = usuarioRepository.findByUsernameOrEmail(principal.getName());
        
        boolean isTechOrAdmin = usuario.getPerfil() != null && (
                usuario.getPerfil().toUpperCase().contains("ADMIN") ||
                usuario.getPerfil().toUpperCase().contains("MASTER") ||
                usuario.getPerfil().toUpperCase().contains("TECNICO")
        );

        boolean isAdmin = usuario.getPerfil() != null && (
                usuario.getPerfil().toUpperCase().contains("ADMIN") ||
                usuario.getPerfil().toUpperCase().contains("MASTER")
        );

        List<Chamado> todosChamados = chamadoRepository.findAll();
        
        long pendentesInfra = todosChamados.stream().filter(c -> c.getStatus() == StatusChamado.PENDENTE && c.getCategoria() == CategoriaChamado.TI).count();
        long pendentesSoftware = todosChamados.stream().filter(c -> c.getStatus() == StatusChamado.PENDENTE && c.getCategoria() == CategoriaChamado.SOFTWARE).count();
        long resolvidos = todosChamados.stream().filter(c -> c.getStatus() == StatusChamado.RESOLVIDO).count();
        long cancelados = todosChamados.stream().filter(c -> c.getStatus() == StatusChamado.CANCELADO).count();

        int[] infraPorHora = new int[24];
        int[] infraPorDia = new int[7];
        int[] infraPorMes = new int[12];
        int[] infraPorAno = new int[10];
        
        int[] softResolvidosTime = new int[4]; 
        int[] softPendentesTime = new int[4];
        int[] softCanceladosTime = new int[4];
        
        int anoAtual = LocalDateTime.now(ZoneId.of("America/Sao_Paulo")).getYear();

        for (Chamado c : todosChamados) {
            if (c.getDataAbertura() != null) {
                LocalDateTime dt = c.getDataAbertura();
                boolean isHoje = dt.toLocalDate().isEqual(LocalDateTime.now(ZoneId.of("America/Sao_Paulo")).toLocalDate());
                boolean isSemanaAtual = dt.getDayOfWeek().getValue() >= 1 && dt.getDayOfWeek().getValue() <= 7 && dt.toLocalDate().isAfter(LocalDateTime.now(ZoneId.of("America/Sao_Paulo")).toLocalDate().minusDays(7));
                boolean isAnoAtual = dt.getYear() == anoAtual;
                int diffAno = anoAtual - dt.getYear();

                if (c.getCategoria() == CategoriaChamado.TI) {
                    if (isHoje) infraPorHora[dt.getHour()]++;
                    if (isSemanaAtual) infraPorDia[dt.getDayOfWeek().getValue() - 1]++;
                    if (isAnoAtual) infraPorMes[dt.getMonthValue() - 1]++;
                    if (diffAno >= 0 && diffAno < 10) infraPorAno[9 - diffAno]++;
                } 
                else if (c.getCategoria() == CategoriaChamado.SOFTWARE) {
                    if (isHoje) {
                        if (c.getStatus() == StatusChamado.RESOLVIDO) softResolvidosTime[0]++;
                        else if (c.getStatus() == StatusChamado.PENDENTE) softPendentesTime[0]++;
                        else if (c.getStatus() == StatusChamado.CANCELADO) softCanceladosTime[0]++;
                    }
                    if (isSemanaAtual) {
                        if (c.getStatus() == StatusChamado.RESOLVIDO) softResolvidosTime[1]++;
                        else if (c.getStatus() == StatusChamado.PENDENTE) softPendentesTime[1]++;
                        else if (c.getStatus() == StatusChamado.CANCELADO) softCanceladosTime[1]++;
                    }
                    if (isAnoAtual) {
                        if (c.getStatus() == StatusChamado.RESOLVIDO) softResolvidosTime[2]++;
                        else if (c.getStatus() == StatusChamado.PENDENTE) softPendentesTime[2]++;
                        else if (c.getStatus() == StatusChamado.CANCELADO) softCanceladosTime[2]++;
                    }
                    if (diffAno >= 0 && diffAno < 10) {
                        if (c.getStatus() == StatusChamado.RESOLVIDO) softResolvidosTime[3]++;
                        else if (c.getStatus() == StatusChamado.PENDENTE) softPendentesTime[3]++;
                        else if (c.getStatus() == StatusChamado.CANCELADO) softCanceladosTime[3]++;
                    }
                }
            }
        }

        Pageable pageable = PageRequest.of(page, 7, Sort.by(Sort.Direction.DESC, "dataAbertura"));
        Page<Chamado> chamadosPage = chamadoRepository.findAll(pageable);

        model.addAttribute("usuario", usuario);
        model.addAttribute("isTechOrAdmin", isTechOrAdmin);
        model.addAttribute("isAdmin", isAdmin); 
        model.addAttribute("chamados", chamadosPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", chamadosPage.getTotalPages());

        model.addAttribute("pendentesInfra", pendentesInfra);
        model.addAttribute("pendentesSoftware", pendentesSoftware);
        model.addAttribute("resolvidos", resolvidos);
        model.addAttribute("cancelados", cancelados);
        
        model.addAttribute("infraPorDia", infraPorDia);
        model.addAttribute("infraPorHora", infraPorHora);
        model.addAttribute("infraPorMes", infraPorMes);
        model.addAttribute("infraPorAno", infraPorAno);
        model.addAttribute("anoAtual", anoAtual);
        
        model.addAttribute("softResolvidosTime", softResolvidosTime);
        model.addAttribute("softPendentesTime", softPendentesTime);
        model.addAttribute("softCanceladosTime", softCanceladosTime);

        return "pages/helpdesk"; 
    }

    @GetMapping("/helpdesk/cadastrar-tecnico")
    public String abrirTelaCadastroTecnico(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        
        Usuario usuario = usuarioRepository.findByUsernameOrEmail(principal.getName());
        model.addAttribute("usuario", usuario);
        
        return "pages/cadastrar-tecnico"; 
    }

    @Transactional
    @PostMapping("/helpdesk/cadastrar-tecnico")
    public String salvarNovoTecnico(
            @RequestParam("nome") String nome,
            @RequestParam("email") String email,
            @RequestParam("qualificacao") String qualificacao,
            @RequestParam("perfilTecnico") String perfilTecnico,
            Principal principal) {
            
        if (principal == null) return "redirect:/login";

        Usuario tecnico = null;
        try {
            tecnico = (Usuario) entityManager.createQuery("SELECT u FROM Usuario u WHERE u.email = :email")
                                             .setParameter("email", email)
                                             .getSingleResult();
        } catch (Exception e) {}

        if (tecnico != null) {
            tecnico.setNome(nome);
            tecnico.setPerfil(perfilTecnico);
            tecnico.setAtivo(false); 
            usuarioRepository.save(tecnico);
        } else {
            Usuario novoTecnico = new Usuario();
            novoTecnico.setNome(nome);
            novoTecnico.setEmail(email);
            novoTecnico.setUsername(email); 
            novoTecnico.setPerfil(perfilTecnico); 
            
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            novoTecnico.setSenha(encoder.encode("Mudar@123")); 
            novoTecnico.setAtivo(false); 

            usuarioRepository.save(novoTecnico);
        }

        return "redirect:/helpdesk/cadastrar-tecnico?sucesso=true";
    }

    @GetMapping("/helpdesk/novo-chamado")
    public String novoChamado(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        Usuario usuario = usuarioRepository.findByUsernameOrEmail(principal.getName());
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
        Usuario solicitante = usuarioRepository.findByUsernameOrEmail(principal.getName());

        Chamado chamado = new Chamado();
        chamado.setCategoria(categoria);
        chamado.setUrgencia(urgencia);
        chamado.setAssunto(assunto);
        chamado.setDescricao(descricao);
        chamado.setSolicitante(solicitante);
        chamado.setStatus(StatusChamado.PENDENTE);
        
        chamado.setDataAbertura(LocalDateTime.now(ZoneId.of("America/Sao_Paulo")));

        chamadoRepository.save(chamado);
        return "redirect:/helpdesk"; 
    }

    @GetMapping("/helpdesk/visualizar-chamado")
    @Transactional(readOnly = true)
    public String visualizarChamado(@RequestParam(value = "id", required = false) Long id, Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        Usuario usuario = usuarioRepository.findByUsernameOrEmail(principal.getName());

        boolean isTechOrAdmin = usuario.getPerfil() != null && (
                usuario.getPerfil().toUpperCase().contains("ADMIN") ||
                usuario.getPerfil().toUpperCase().contains("MASTER") ||
                usuario.getPerfil().toUpperCase().contains("TECNICO")
        );

        if (id == null) return "redirect:/helpdesk";
        Chamado chamado = chamadoRepository.findById(id).orElse(null);
        if (chamado == null) return "redirect:/helpdesk";

        boolean podeResponder = false;
        if (chamado.getSolicitante() != null && chamado.getSolicitante().getId().equals(usuario.getId())) {
            podeResponder = true;
        } else if (chamado.getResponsavel() != null && chamado.getResponsavel().getId().equals(usuario.getId())) {
            podeResponder = true;
        } else if (usuario.getPerfil() != null && (usuario.getPerfil().toUpperCase().contains("ADMIN") || usuario.getPerfil().toUpperCase().contains("MASTER"))) {
            podeResponder = true;
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("chamado", chamado);
        model.addAttribute("isTechOrAdmin", isTechOrAdmin);
        model.addAttribute("podeResponder", podeResponder); 

        return "pages/visualizar-chamado"; 
    }

    @GetMapping("/helpdesk/assumir")
    public String assumirChamado(@RequestParam("id") Long id, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        Usuario tecnico = usuarioRepository.findByUsernameOrEmail(principal.getName());
        Chamado chamado = chamadoRepository.findById(id).orElse(null);

        if (chamado != null && chamado.getResponsavel() == null) {
            String perfil = tecnico.getPerfil() != null ? tecnico.getPerfil().toUpperCase() : "";
            
            boolean isMaster = perfil.contains("ADMIN") || perfil.contains("MASTER");
            boolean isAmbos = perfil.contains("TECNICO_AMBOS");
            boolean podeAssumir = isMaster || isAmbos;

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
                redirectAttributes.addFlashAttribute("erroFila", "Acesso Negado: Sua especialidade não permite assumir chamados desta categoria.");
            }
        }
        return "redirect:/helpdesk"; 
    }

    @PostMapping("/helpdesk/atualizar-status")
    public String atualizarStatus(@RequestParam("id") Long id, 
                                  @RequestParam("status") StatusChamado status, 
                                  Principal principal, 
                                  RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        
        Usuario usuario = usuarioRepository.findByUsernameOrEmail(principal.getName());
        Chamado chamado = chamadoRepository.findById(id).orElse(null);

        if (chamado != null) {
            boolean podeInteragir = false;
            
            if (chamado.getSolicitante() != null && chamado.getSolicitante().getId().equals(usuario.getId())) {
                podeInteragir = true;
            } else if (chamado.getResponsavel() != null && chamado.getResponsavel().getId().equals(usuario.getId())) {
                podeInteragir = true;
            } else if (usuario.getPerfil() != null && (usuario.getPerfil().toUpperCase().contains("ADMIN") || usuario.getPerfil().toUpperCase().contains("MASTER"))) {
                podeInteragir = true;
            }

            if (!podeInteragir) {
                redirectAttributes.addFlashAttribute("erroMensagem", "Acesso Negado: Apenas o solicitante, o técnico responsável ou a administração podem alterar o status deste ticket.");
                return "redirect:/helpdesk/visualizar-chamado?id=" + id;
            }

            chamado.setStatus(status);
            if (status == StatusChamado.RESOLVIDO || status == StatusChamado.CANCELADO) {
                chamado.setDataFechamento(LocalDateTime.now(ZoneId.of("America/Sao_Paulo")));
            }
            chamadoRepository.save(chamado);
        }
        return "redirect:/helpdesk/visualizar-chamado?id=" + id;
    }

    @Transactional
    @PostMapping("/helpdesk/responder")
    public String responderChamado(@RequestParam("chamadoId") Long chamadoId,
                                   @RequestParam("mensagem") String texto,
                                   Principal principal,
                                   RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";

        try {
            Usuario autor = usuarioRepository.findByUsernameOrEmail(principal.getName());
            Chamado chamado = chamadoRepository.findById(chamadoId).orElseThrow(() -> new RuntimeException("Chamado não encontrado"));

            boolean podeResponder = false;
            
            if (chamado.getSolicitante() != null && chamado.getSolicitante().getId().equals(autor.getId())) {
                podeResponder = true;
            } else if (chamado.getResponsavel() != null && chamado.getResponsavel().getId().equals(autor.getId())) {
                podeResponder = true;
            } else if (autor.getPerfil() != null && (autor.getPerfil().toUpperCase().contains("ADMIN") || autor.getPerfil().toUpperCase().contains("MASTER"))) {
                podeResponder = true;
            }

            if (!podeResponder) {
                redirectAttributes.addFlashAttribute("erroMensagem", "Modo Espectador: Apenas o solicitante e o técnico responsável podem enviar mensagens neste ticket.");
                return "redirect:/helpdesk/visualizar-chamado?id=" + chamadoId;
            }

            if (texto != null && !texto.trim().isEmpty()) {
                InteracaoChamado interacao = new InteracaoChamado();
                interacao.setChamado(chamado);
                interacao.setAutor(autor);
                interacao.setTexto(texto);
                
                LocalDateTime dataAtual = LocalDateTime.now(ZoneId.of("America/Sao_Paulo"));
                interacao.setDataEnvio(dataAtual);
                
                interacaoRepository.saveAndFlush(interacao);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao salvar mensagem no banco de dados: " + e.getMessage(), e);
        }

        return "redirect:/helpdesk/visualizar-chamado?id=" + chamadoId + "&t=" + System.currentTimeMillis();
    }
    
    @GetMapping("/helpdesk/auditoria")
    public String auditoriaHelpdesk(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        Usuario usuario = usuarioRepository.findByUsernameOrEmail(principal.getName());
        
        boolean isAdmin = usuario.getPerfil() != null && (
                usuario.getPerfil().toUpperCase().contains("ADMIN") ||
                usuario.getPerfil().toUpperCase().contains("MASTER")
        );
        if (!isAdmin) {
            return "redirect:/helpdesk?erroAuditoria=true";
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("listaLogs", gerarLinhaDoTempoHelpdesk());
        model.addAttribute("tipoRelatorio", "CICLO DE VIDA DOS CHAMADOS");
        
        return "pages/helpdesk-auditoria"; 
    }

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
                sb.append("Gerado em: ").append(LocalDateTime.now(ZoneId.of("America/Sao_Paulo")).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("\n");
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

    private List<Object[]> gerarLinhaDoTempoHelpdesk() {
        List<Chamado> chamados = chamadoRepository.findAll();
        List<Object[]> logsHelpdesk = new ArrayList<>();

        for (Chamado c : chamados) {
            logsHelpdesk.add(new Object[]{
                c.getDataAbertura() != null ? c.getDataAbertura() : LocalDateTime.now(ZoneId.of("America/Sao_Paulo")),
                "ABERTURA DE TICKET",
                "Ticket #" + c.getId() + " - Categoria: " + c.getCategoria() + " | Assunto: " + c.getAssunto(),
                c.getSolicitante() != null ? c.getSolicitante().getNome() : "Usuário Desconhecido"
            });

            if (c.getDataFechamento() != null) {
                String acao = c.getStatus().name().equals("RESOLVIDO") ? "TICKET RESOLVIDO" : "TICKET CANCELADO";
                logsHelpdesk.add(new Object[]{
                    c.getDataFechamento(),
                    acao,
                    "Ticket #" + c.getId() + " finalizado. Status final: " + c.getStatus(),
                    c.getResponsavel() != null ? c.getResponsavel().getNome() : "Sistema"
                });
            }

            if (c.getInteracoes() != null) {
                for (InteracaoChamado i : c.getInteracoes()) {
                    logsHelpdesk.add(new Object[]{
                        i.getDataEnvio() != null ? i.getDataEnvio() : LocalDateTime.now(ZoneId.of("America/Sao_Paulo")),
                        "NOVA INTERAÇÃO",
                        "Ticket #" + c.getId() + " - Resposta adicionada.",
                        i.getAutor() != null ? i.getAutor().getNome() : "Usuário Desconhecido"
                    });
                }
            }
        }

        logsHelpdesk.sort((a, b) -> {
            LocalDateTime dataA = (LocalDateTime) a[0];
            LocalDateTime dataB = (LocalDateTime) b[0];
            return dataB.compareTo(dataA);
        });

        return logsHelpdesk;
    }
}