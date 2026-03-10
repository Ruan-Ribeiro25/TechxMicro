package com.copamir.controller;

import com.copamir.entity.*;
import com.copamir.models.PedidoExame;
import com.copamir.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional; 
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {
	
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AgendamentoRepository agendamentoRepository;
    @Autowired private PoloRepository poloRepository; 
    @Autowired private ProfissionalRepository profissionalRepository;
    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private AmbulanciaRepository ambulanciaRepository;
    
    @PersistenceContext
    private EntityManager entityManager;

    // =================================================================================
    // 1. LOGIN E PAINEL GERAL
    // =================================================================================

    @GetMapping("/login")
    public String loginAdmin(@RequestParam(value = "error", required = false) String error, Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        Usuario admin = usuarioRepository.findByUsernameOrCpf(principal.getName());
        model.addAttribute("usuario", admin);
        if (error != null) {
            if ("denied".equals(error)) model.addAttribute("erro", "Acesso Negado: Você não tem permissão.");
            else if ("true".equals(error)) model.addAttribute("erro", "Senha incorreta.");
        }
        return "admin/login"; 
    }

    @PostMapping("/login-verificar")
    public String verificarSegundoLogin(@RequestParam String password, Principal principal) {
        Usuario admin = usuarioRepository.findByUsernameOrCpf(principal.getName());
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (encoder.matches(password, admin.getPassword())) return "redirect:/admin/painel";
        else return "redirect:/admin/login?error=true";
    }

    @GetMapping("/painel")
    public String painelAdmin(Model model, Principal principal, @RequestParam(value = "busca", required = false) String busca) {
        if (principal == null) return "redirect:/login";
        Usuario admin = usuarioRepository.findByUsernameOrCpf(principal.getName());
        if (admin == null || !"ADMIN".equals(admin.getPerfil())) return "redirect:/home?error=access_denied";

        model.addAttribute("usuario", admin); 

        // --- CARREGAMENTO E CONTAGEM DE USUÁRIOS ---
        List<Usuario> todosUsuarios = usuarioRepository.findAll();
        
        long totalPacientes = todosUsuarios.stream().filter(u -> u.getPerfil() != null && u.getPerfil().toUpperCase().contains("PACIENTE")).count();
        long totalMedicos = todosUsuarios.stream().filter(u -> u.getPerfil() != null && u.getPerfil().toUpperCase().contains("MEDICO")).count();
        long totalEnfermeiros = todosUsuarios.stream().filter(u -> u.getPerfil() != null && u.getPerfil().toUpperCase().contains("ENFERMEIRO")).count();
        long totalAdmins = todosUsuarios.stream().filter(u -> u.getPerfil() != null && u.getPerfil().toUpperCase().contains("ADMIN")).count();
        long totalPendentes = todosUsuarios.stream().filter(u -> !u.isAtivo()).count();
        
        long totalMotoristas = todosUsuarios.stream().filter(u -> u.getPerfil() != null && u.getPerfil().toUpperCase().contains("MOTORISTA")).count();
        long totalTecnicos = todosUsuarios.stream().filter(u -> u.getPerfil() != null && u.getPerfil().toUpperCase().contains("TECNICO")).count();
        long totalSvGerais = todosUsuarios.stream().filter(u -> u.getPerfil() != null && (u.getPerfil().toUpperCase().contains("GERAIS") || u.getPerfil().toUpperCase().contains("LIMPEZA") || u.getPerfil().toUpperCase().contains("MANUTENCAO"))).count();
        
        // --- NOVO: CONTAGEM DE RECEPCIONISTAS ---
     // LINHA NOVA CORRIGIDA (Cobre "RECEPCIONISTA" e "RECEPCAO")
        long totalRecepcionistas = todosUsuarios.stream().filter(u -> u.getPerfil() != null && (u.getPerfil().toUpperCase().contains("RECEPCIONISTA") || u.getPerfil().toUpperCase().contains("RECEPCAO"))).count();

        // --- CONTAGEM DE POLOS (UNIDADES) ---
        long totalPolos = poloRepository.findByPoloPaiIsNull().size();
        
        List<Polo> todosPolosDb = poloRepository.findAll();
        long totalClinicas = todosPolosDb.stream().filter(p -> p.getTipo() != null && "CLINICA".equalsIgnoreCase(p.getTipo())).count();
        long totalLaboratorios = todosPolosDb.stream().filter(p -> p.getTipo() != null && "LABORATORIO".equalsIgnoreCase(p.getTipo())).count();
        model.addAttribute("totalLaboratorios", totalLaboratorios);

        // --- CONTAGEM DE EXCLUÍDOS ---
        long totalExcluidos = 0;
        try {
            Number count = (Number) entityManager.createNativeQuery("SELECT COUNT(*) FROM historico_logs WHERE acao = 'EXCLUSAO_USUARIO'").getSingleResult();
            totalExcluidos = count.longValue();
        } catch (Exception e) { totalExcluidos = 0; }

        // --- CONTAGEM DE LOGS ---
        long totalLogs = 0;
        try {
            Number countLogs = (Number) entityManager.createNativeQuery("SELECT COUNT(*) FROM historico_logs").getSingleResult();
            totalLogs = countLogs.longValue();
        } catch (Exception e) { totalLogs = 0; }

        // --- TRÁFEGO HOJE ---
        long volumeHoje = 0;
        try {
            Number countToday = (Number) entityManager.createNativeQuery("SELECT COUNT(*) FROM historico_logs WHERE DATE(data_hora) = CURDATE()").getSingleResult();
            volumeHoje = countToday.longValue();
        } catch (Exception e) { volumeHoje = 0; }

        // --- PRODUTOS ---
        long totalProdutos = produtoRepository.count();
        long alertaEstoque = 0;
        try { alertaEstoque = produtoRepository.countProdutosBaixoEstoque(); } catch (Exception e) { alertaEstoque = 0; }
        
        model.addAttribute("totalProdutos", totalProdutos);
        model.addAttribute("alertaEstoque", alertaEstoque);

        // --- AMBULÂNCIAS (PAINEL GERAL) ---
        List<Ambulancia> frotaTotal = ambulanciaRepository.findAll();
        
        long totalAmbulancias = frotaTotal.size();
        long ambEmChamado = frotaTotal.stream().filter(a -> "EM_CHAMADO".equals(a.getStatus()) || "SOLICITADO".equals(a.getStatus())).count();
        long ambNaBase = frotaTotal.stream().filter(a -> "DISPONIVEL".equals(a.getStatus())).count();
        
        model.addAttribute("totalAmbulancias", totalAmbulancias);
        model.addAttribute("ambEmChamado", ambEmChamado);
        model.addAttribute("ambNaBase", ambNaBase);
        
        // Dados Dashboard
        model.addAttribute("faturamentoMensal", "1.2M");
        model.addAttribute("crescimentoFinanceiro", "15");
        model.addAttribute("leitosOcupados", 42);
        model.addAttribute("leitosVagos", 8);
        model.addAttribute("leitosTotal", 50);
        model.addAttribute("leitosOcupacaoPerc", 84);
        model.addAttribute("previsaoAlta", 5);
        model.addAttribute("atendimentosHoje", 128);
        model.addAttribute("filaEspera", 12);

        // Atributos KPI
        model.addAttribute("totalPacientes", totalPacientes);
        model.addAttribute("totalMedicos", totalMedicos);
        model.addAttribute("totalEnfermeiros", totalEnfermeiros);
        model.addAttribute("totalAdmins", totalAdmins);
        model.addAttribute("totalPendentes", totalPendentes);
        model.addAttribute("totalPolos", totalPolos);
        model.addAttribute("totalExcluidos", totalExcluidos);
        model.addAttribute("totalLogs", totalLogs);
        model.addAttribute("volumeHoje", volumeHoje);

        // Busca
        List<Usuario> listaExibicao;
        if (busca != null && !busca.isEmpty()) {
            listaExibicao = todosUsuarios.stream().filter(u -> u.getNome().toLowerCase().contains(busca.toLowerCase()) || u.getCpf().contains(busca)).collect(Collectors.toList());
        } else {
            listaExibicao = todosUsuarios.stream().limit(50).collect(Collectors.toList());
        }
        model.addAttribute("todosUsuarios", listaExibicao);
        
        // Profissionais
        List<Profissional> listaProfissionais = profissionalRepository.findAll();
        Map<Long, Profissional> mapaProfissionais = listaProfissionais.stream().collect(Collectors.toMap(p -> p.getUsuario().getId(), p -> p));
        try {
            List<Object[]> adminsData = entityManager.createNativeQuery("SELECT usuario_id, matricula, cargo FROM administrador").getResultList();
            for(Object[] row : adminsData) {
                Long userId = ((Number) row[0]).longValue();
                Profissional profFake = new Profissional();
                profFake.setMatricula((String) row[1]);
                profFake.setEspecialidade((String) row[2]); 
                profFake.setCrm("CORP"); 
                profFake.setStatusAprovacao("APROVADO");
                mapaProfissionais.putIfAbsent(userId, profFake);
            }
        } catch (Exception e) {}
        model.addAttribute("mapaProfissionais", mapaProfissionais);

        // Gráfico (Agora com 13 Itens, incluindo Recepcionistas)
        model.addAttribute("graficoLabels", Arrays.asList(
            "Pacientes", "Médicos", "Enfermeiros", "Admins", 
            "Polos", "Excluídos", "Motoristas", "Técnicos", 
            "Clínicas", "Laboratórios", "Sv. Gerais", "Ambulâncias",
            "Recepcionistas" // <--- ADICIONADO NO ARRAY DE LABELS
        ));
        model.addAttribute("graficoDados", Arrays.asList(
            totalPacientes, totalMedicos, totalEnfermeiros, totalAdmins, 
            totalPolos, totalExcluidos, totalMotoristas, totalTecnicos, 
            totalClinicas, totalLaboratorios, totalSvGerais, totalAmbulancias,
            totalRecepcionistas // <--- ADICIONADO NO ARRAY DE DADOS
        ));

        return "admin/painel"; 
    }

    // =================================================================================
    // 2. GESTÃO DE AMBULÂNCIAS (FLUXO CORRIGIDO - INDIVIDUALIZADO)
    // =================================================================================

    @GetMapping("/ambulancias")
    public String listarPolosAmbulancia(Model model, Principal principal) {
        adicionarUsuarioAoModel(model, principal);
        List<Polo> cidades = poloRepository.findByPoloPaiIsNull();
        model.addAttribute("polos", cidades);
        return "admin/ambulancias-polos"; 
    }

    @GetMapping("/ambulancias/painel/{id}")
    public String painelFrotaPorPolo(@PathVariable Long id, Model model, Principal principal) {
        adicionarUsuarioAoModel(model, principal);
        
        Polo polo = poloRepository.findById(id).orElse(null);
        String nomePolo = (polo != null) ? polo.getNome() : "Polo Desconhecido";
        
        List<Ambulancia> frotaFiltrada = new ArrayList<>();
        try {
            String sql;
            if (id == 2L) {
                sql = "SELECT * FROM ambulancias WHERE polo_id = :pid OR polo_id IS NULL";
            } else {
                sql = "SELECT * FROM ambulancias WHERE polo_id = :pid";
            }
            
            Query query = entityManager.createNativeQuery(sql, Ambulancia.class);
            query.setParameter("pid", id);
            frotaFiltrada = query.getResultList();
        } catch (Exception e) {
            frotaFiltrada = new ArrayList<>();
        }

        long total = frotaFiltrada.size();
        long disponiveis = frotaFiltrada.stream().filter(a -> "DISPONIVEL".equals(a.getStatus())).count();
        long emOcorrencia = frotaFiltrada.stream().filter(a -> "EM_CHAMADO".equals(a.getStatus()) || "SOLICITADO".equals(a.getStatus())).count();
        long manutencao = frotaFiltrada.stream().filter(a -> "MANUTENCAO".equals(a.getStatus())).count();

        model.addAttribute("listaAmbulancias", frotaFiltrada);
        model.addAttribute("totalAmbulancias", total);
        model.addAttribute("disponiveis", disponiveis);
        model.addAttribute("emChamado", emOcorrencia);
        model.addAttribute("manutencao", manutencao);
        model.addAttribute("nomePolo", nomePolo); 
        model.addAttribute("hospitalId", id);

        model.addAttribute("corridasAtual", Arrays.asList(45, 52, 38, 60, 55, 70, 65, 58, 62, 80, 95, 88));
        model.addAttribute("corridasAnterior", Arrays.asList(30, 40, 35, 45, 48, 50, 52, 48, 55, 60, 70, 65));
        model.addAttribute("dadosRadar", Arrays.asList(85, 40, 25, 60, 30)); 
        model.addAttribute("manutencaoDados", Arrays.asList(5, 3, 2, 8)); 
        
        return "admin/ambulancias";
    }

    @Transactional
    @PostMapping("/ambulancias/salvar")
    public String salvarAmbulancia(
            @RequestParam String placa, 
            @RequestParam String tipo, 
            @RequestParam String modelo,
            @RequestParam(required = false) Long poloId 
    ) {
        Ambulancia nova = new Ambulancia();
        nova.setPlaca(placa.toUpperCase());
        nova.setTipo(tipo);
        nova.setModelo(modelo);
        nova.setStatus("DISPONIVEL");
        nova.setMotorista("-");
        nova.setPrevisaoLiberacao("-");
        
        Ambulancia salva = ambulanciaRepository.save(nova);
        
        if(salva.getId() != null) {
            try {
                String sql = "UPDATE ambulancias SET polo_id = :pid, data_cadastro = NOW() WHERE id = :aid";
                Query query = entityManager.createNativeQuery(sql);
                query.setParameter("pid", poloId);
                query.setParameter("aid", salva.getId());
                query.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return (poloId != null) ? "redirect:/admin/ambulancias/painel/" + poloId : "redirect:/admin/ambulancias";
    }

    @PostMapping("/ambulancias/status")
    public String atualizarStatus(@RequestParam Long id, @RequestParam String acao) { 
        Ambulancia amb = ambulanciaRepository.findById(id).orElse(null);
        Long poloId = null;
        
        if (amb != null) {
            try {
                Query query = entityManager.createNativeQuery("SELECT polo_id FROM ambulancias WHERE id = :id");
                query.setParameter("id", id);
                Object result = query.getSingleResult();
                if(result != null) {
                    poloId = ((Number) result).longValue();
                }
            } catch (Exception e) {}

            switch (acao) {
                case "solicitar": amb.setStatus("SOLICITADO"); break;
                case "aceitar": amb.setStatus("EM_CHAMADO"); if ("-".equals(amb.getMotorista())) amb.setMotorista("Mot. Vinculado"); amb.setPrevisaoLiberacao("Em rota..."); break;
                case "despachar": amb.setStatus("EM_CHAMADO"); if ("-".equals(amb.getMotorista())) amb.setMotorista("Plantão"); amb.setPrevisaoLiberacao("Em andamento"); break;
                case "finalizar": amb.setStatus("DISPONIVEL"); amb.setMotorista("-"); amb.setPrevisaoLiberacao("-"); break;
                case "manutencao": amb.setStatus("MANUTENCAO"); amb.setMotorista("Oficina"); amb.setPrevisaoLiberacao("Indefinido"); break;
                case "ativar": amb.setStatus("DISPONIVEL"); amb.setMotorista("-"); amb.setPrevisaoLiberacao("-"); break;
            }
            ambulanciaRepository.save(amb);
        }
        return (poloId != null) ? "redirect:/admin/ambulancias/painel/" + poloId : "redirect:/admin/ambulancias";
    }

    // =================================================================================
    // 3. FUNCIONALIDADES AUXILIARES (DETALHES, ETC)
    // =================================================================================

    @GetMapping("/detalhes/{tipo}")
    public String detalhesPorTipo(@PathVariable String tipo, Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        Usuario admin = usuarioRepository.findByUsernameOrCpf(principal.getName());
        model.addAttribute("usuario", admin);
        model.addAttribute("tipoRelatorio", tipo.toUpperCase());

        if ("POLOS".equalsIgnoreCase(tipo)) return "redirect:/admin/polos"; 
        
        if ("EXCLUIDOS".equalsIgnoreCase(tipo)) {
            List<Object[]> logs = entityManager.createNativeQuery("SELECT data_hora, acao, detalhes, username_registrado FROM historico_logs WHERE acao = 'EXCLUSAO_USUARIO' ORDER BY data_hora DESC").getResultList();
            model.addAttribute("listaLogs", logs);
            return "admin/lista-detalhes";
        }

        if ("LOGS".equalsIgnoreCase(tipo)) {
            List<Object[]> logs = entityManager.createNativeQuery("SELECT data_hora, acao, detalhes, username_registrado FROM historico_logs ORDER BY data_hora DESC").getResultList();
            model.addAttribute("listaLogs", logs);
            return "admin/lista-detalhes";
        }

        if ("VOLUME".equalsIgnoreCase(tipo)) {
            List<Object[]> porHora = entityManager.createNativeQuery("SELECT HOUR(data_hora), COUNT(*) FROM historico_logs WHERE DATE(data_hora) = CURDATE() GROUP BY HOUR(data_hora)").getResultList();
            Integer[] dadosHora = new Integer[24]; Arrays.fill(dadosHora, 0);
            for(Object[] obj : porHora) dadosHora[((Number) obj[0]).intValue()] = ((Number) obj[1]).intValue();
            
            List<Object[]> porDia = entityManager.createNativeQuery("SELECT DAY(data_hora), COUNT(*) FROM historico_logs WHERE MONTH(data_hora) = MONTH(CURDATE()) AND YEAR(data_hora) = YEAR(CURDATE()) GROUP BY DAY(data_hora)").getResultList();
            Integer[] dadosDia = new Integer[31]; Arrays.fill(dadosDia, 0);
            for(Object[] obj : porDia) dadosDia[((Number) obj[0]).intValue() - 1] = ((Number) obj[1]).intValue();

            List<Object[]> porMes = entityManager.createNativeQuery("SELECT MONTH(data_hora), COUNT(*) FROM historico_logs WHERE YEAR(data_hora) = YEAR(CURDATE()) GROUP BY MONTH(data_hora)").getResultList();
            Integer[] dadosMes = new Integer[12]; Arrays.fill(dadosMes, 0);
            for(Object[] obj : porMes) dadosMes[((Number) obj[0]).intValue() - 1] = ((Number) obj[1]).intValue();

            long sumHoje = Arrays.stream(dadosHora).mapToInt(Integer::intValue).sum();
            long sumMes = Arrays.stream(dadosDia).mapToInt(Integer::intValue).sum();
            long sumAno = Arrays.stream(dadosMes).mapToInt(Integer::intValue).sum();

            model.addAttribute("dadosHora", Arrays.asList(dadosHora));
            model.addAttribute("dadosDia", Arrays.asList(dadosDia));
            model.addAttribute("dadosMes", Arrays.asList(dadosMes));
            model.addAttribute("sumHoje", sumHoje);
            model.addAttribute("sumMes", sumMes);
            model.addAttribute("sumAno", sumAno);
            
            return "admin/lista-detalhes";
        }

        List<Usuario> todos = usuarioRepository.findAll();
        List<Usuario> filtrados = new ArrayList<>();
        if ("MEDICOS".equalsIgnoreCase(tipo)) filtrados = todos.stream().filter(u -> u.getPerfil().contains("MEDICO")).collect(Collectors.toList());
        else if ("ENFERMEIROS".equalsIgnoreCase(tipo)) filtrados = todos.stream().filter(u -> u.getPerfil().contains("ENFERMEIRO")).collect(Collectors.toList());
        else if ("PACIENTES".equalsIgnoreCase(tipo)) filtrados = todos.stream().filter(u -> u.getPerfil().contains("PACIENTE")).collect(Collectors.toList());
        else if ("ADMINS".equalsIgnoreCase(tipo)) filtrados = todos.stream().filter(u -> u.getPerfil().contains("ADMIN")).collect(Collectors.toList());

        model.addAttribute("listaUsuarios", filtrados);
        
        List<Profissional> listaProfissionais = profissionalRepository.findAll();
        Map<Long, Profissional> mapaProfissionais = listaProfissionais.stream().collect(Collectors.toMap(p -> p.getUsuario().getId(), p -> p));
        
        try {
            List<Object[]> adminsData = entityManager.createNativeQuery("SELECT usuario_id, matricula, cargo FROM administrador").getResultList();
            for(Object[] row : adminsData) {
                Long userId = ((Number) row[0]).longValue();
                Profissional profFake = new Profissional();
                profFake.setMatricula((String) row[1]);
                profFake.setEspecialidade((String) row[2]); 
                profFake.setCrm("CORP"); 
                profFake.setStatusAprovacao("APROVADO");
                mapaProfissionais.putIfAbsent(userId, profFake);
            }
        } catch (Exception e) {}

        model.addAttribute("mapaProfissionais", mapaProfissionais);
        return "admin/lista-detalhes";
    }

    @GetMapping("/laboratorio")
    public String painelLaboratorio(Model model, Principal principal) {
        // 1. Segurança e Dados do Usuário
        if (principal == null) return "redirect:/login";
        Usuario admin = usuarioRepository.findByUsernameOrCpf(principal.getName());
        model.addAttribute("usuario", admin);

        // 2. CORREÇÃO: Declara a lista vazia PRIMEIRO
        List<PedidoExame> todosPedidos = new ArrayList<>();
        
        try {
            // Tenta buscar os dados
            String hql = "SELECT p FROM PedidoExame p ORDER BY p.dataCriacao DESC";
            todosPedidos = entityManager.createQuery(hql, PedidoExame.class)
                                        .setMaxResults(100)
                                        .getResultList();
        } catch (Exception e) {
            // Se der erro (ex: PedidoExame não for uma Entidade mapeada), o painel abre vazio sem quebrar
            System.err.println("Aviso: Não foi possível carregar os pedidos via JPA. Erro: " + e.getMessage());
        }
        
        model.addAttribute("listaTodosPedidos", todosPedidos);

        // 3. Calcular KPIs (Com proteção contra nulos)
        long totalHoje = todosPedidos.stream()
                .filter(p -> p.getDataCriacao() != null && p.getDataCriacao().toLocalDate().isEqual(java.time.LocalDate.now()))
                .count();
        
        long pendentes = todosPedidos.stream()
                .filter(p -> p.getStatusPagamento() != null && "PENDENTE".equals(p.getStatusPagamento().name()))
                .count();
        
        long emAnalise = todosPedidos.stream()
                .filter(p -> p.getStatusPagamento() != null && ("PROCESSANDO".equals(p.getStatusPagamento().name()) || "ANALISE".equals(p.getStatusPagamento().name())))
                .count();

        double faturamento = todosPedidos.stream()
                .filter(p -> p.getStatusPagamento() != null && "APROVADO".equals(p.getStatusPagamento().name()))
                .mapToDouble(p -> 50.00) 
                .sum();

        model.addAttribute("kpiTotalExames", totalHoje > 0 ? totalHoje : todosPedidos.size());
        model.addAttribute("kpiPendentes", pendentes);
        model.addAttribute("kpiEmAnalise", emAnalise);
        model.addAttribute("kpiFaturamento", String.format("%.2f", faturamento));

        // 4. Lista Lateral de Laboratórios
        List<Polo> laboratorios = poloRepository.findAll().stream()
                .filter(p -> "LABORATORIO".equalsIgnoreCase(p.getTipo()))
                .collect(Collectors.toList());
        
        model.addAttribute("listaLaboratorios", laboratorios);
        model.addAttribute("totalLaboratorios", laboratorios.size());

        return "admin/gestao-laboratorio";
    }
    
    // =================================================================================
    // 5. AÇÕES DE GERENCIAMENTO (APROVAR, BLOQUEAR, EXCLUIR)
    // =================================================================================

    @PostMapping("/aprovar-profissional")
    public String aprovarProfissional(@RequestParam Long idUsuario) {
        Usuario u = usuarioRepository.findById(idUsuario).orElse(null);
        if (u != null) { u.setAtivo(true); usuarioRepository.save(u); }
        return "redirect:/admin/painel?msg=aprovado";
    }
    
    @PostMapping("/bloquear-usuario")
    public String bloquearUsuario(@RequestParam Long idUsuario) {
        Usuario u = usuarioRepository.findById(idUsuario).orElse(null);
        if (u != null) { u.setAtivo(false); usuarioRepository.save(u); }
        return "redirect:/admin/painel?msg=bloqueado";
    }

    @Transactional
    @PostMapping("/excluir-usuario")
    public String excluirUsuario(@RequestParam Long idUsuario, @RequestParam("motivo") String motivo, Principal principal) {
        try {
            Usuario u = usuarioRepository.findById(idUsuario).orElse(null);
            if (u != null) {
                if ("ADMIN".equalsIgnoreCase(u.getPerfil())) {
                    long adminsCount = usuarioRepository.findAll().stream().filter(user -> "ADMIN".equalsIgnoreCase(user.getPerfil())).count();
                    if (adminsCount <= 1) return "redirect:/admin/painel?error=ultimo_admin";
                }
                String adminResponsavel = principal != null ? principal.getName() : "SISTEMA";
                
                StringBuilder detalhes = new StringBuilder();
                detalhes.append("MOTIVO: ").append(motivo.toUpperCase()).append("\n");
                detalhes.append("Usuário: ").append(u.getNome()).append(" (CPF: ").append(u.getCpf()).append(")\n");
                
                entityManager.createNativeQuery("INSERT INTO historico_logs (acao, data_hora, detalhes, username_registrado) VALUES (?, ?, ?, ?)")
                    .setParameter(1, "EXCLUSAO_USUARIO")
                    .setParameter(2, LocalDateTime.now())
                    .setParameter(3, detalhes.toString())
                    .setParameter(4, adminResponsavel)
                    .executeUpdate();

                limparTabela("usuarios_polos", "usuario_id", idUsuario);
                limparTabela("administrador", "usuario_id", idUsuario);
                limparTabela("profissionais", "usuario_id", idUsuario);
                limparTabela("prontuarios", "paciente_id", idUsuario);
                limparTabela("prontuarios", "medico_id", idUsuario);
                limparTabela("sinais_vitais", "paciente_id", idUsuario);
                limparTabela("sinais_vitais", "responsavel_id", idUsuario);
                limparTabela("registro_clinico", "paciente_id", idUsuario);
                
                entityManager.createNativeQuery("DELETE FROM historico_logs WHERE usuario_id = :uid").setParameter("uid", idUsuario).executeUpdate();
                limparTabela("documentos", "usuario_id", idUsuario);
                limparTabela("agendamentos", "usuario_id", idUsuario);
                
                if (u.getPolos() != null && !u.getPolos().isEmpty()) { for (Polo p : u.getPolos()) { p.setUsuario(null); poloRepository.save(p); } u.getPolos().clear(); }
                
                usuarioRepository.deleteById(idUsuario);
            }
            return "redirect:/admin/painel?msg=excluido";
        } catch (Exception e) { 
            return "redirect:/admin/painel?error=erro_desconhecido"; 
        }
    }

    private void limparTabela(String tabela, String coluna, Long id) {
        try { entityManager.createNativeQuery("DELETE FROM " + tabela + " WHERE " + coluna + " = :uid").setParameter("uid", id).executeUpdate(); } catch (Exception e) {}
    }

    // =================================================================================
    // 6. GESTÃO DE POLOS CENTRALIZADA
    // =================================================================================

    @GetMapping("/polos")
    public String listarCidades(Model model, Principal principal) {
        adicionarUsuarioAoModel(model, principal);
        
        List<Polo> cidades = poloRepository.findByPoloPaiIsNull();
        model.addAttribute("polos", cidades);
        
        Map<Long, Map<String, Long>> statsEstrutura = new HashMap<>();
        
        for(Polo p : cidades) {
            Map<String, Long> counts = new HashMap<>();
            
            counts.put("HOSPITAIS", 1L); 
            
            List<Polo> filiais = poloRepository.findByPoloPai_Id(p.getId());
            long qtdClinicas = filiais.stream().filter(f -> f.getTipo() == null || !"LABORATORIO".equalsIgnoreCase(f.getTipo())).count();
            long qtdLabs = filiais.stream().filter(f -> "LABORATORIO".equalsIgnoreCase(f.getTipo())).count();
            
            counts.put("CLINICAS", qtdClinicas);
            counts.put("LABORATORIOS", qtdLabs);
            
            statsEstrutura.put(p.getId(), counts);
        }
        
        model.addAttribute("statsEstrutura", statsEstrutura);
        model.addAttribute("titulo", "Gestão de Polos (Cidades)");
        model.addAttribute("modoClinica", false);
        model.addAttribute("modoUsuarios", false); 
        
        return "admin/relatorio-polos"; 
    }

    @GetMapping("/polos/{id}/clinicas")
    public String listarClinicas(@PathVariable Long id, Model model, Principal principal) {
        adicionarUsuarioAoModel(model, principal);
        
        Polo hospital = poloRepository.findById(id).orElse(null);
        List<Polo> clinicas = poloRepository.findByPoloPai_Id(id);
        
        model.addAttribute("polos", clinicas);
        model.addAttribute("estatisticas", calcularEstatisticasPolos(clinicas));
        
        model.addAttribute("titulo", "Filiais (Clínicas e Labs): " + (hospital != null ? hospital.getCidade() : ""));
        model.addAttribute("hospitalNome", (hospital != null ? hospital.getNome() : "")); 
        model.addAttribute("modoClinica", true);
        model.addAttribute("modoUsuarios", false);
        model.addAttribute("hospitalId", id);
        
        return "admin/relatorio-polos";
    }

    @GetMapping("/polos/clinica/{id}/usuarios")
    public String listarUsuariosClinica(@PathVariable Long id, 
                                        @RequestParam(required = false) String busca,
                                        Model model, 
                                        Principal principal) {
        adicionarUsuarioAoModel(model, principal);
        
        Polo clinica = poloRepository.findById(id).orElse(null);
        if (clinica == null) return "redirect:/admin/polos";

        List<Usuario> usuarios;
        if ("LABORATORIO".equalsIgnoreCase(clinica.getTipo()) && clinica.getPoloPai() != null) {
            Long hospitalId = clinica.getPoloPai().getId();
            String hql = "SELECT DISTINCT u FROM Usuario u JOIN u.polos p WHERE (p.id = :hId OR p.poloPai.id = :hId)";
            if (busca != null && !busca.isEmpty()) {
                hql += " AND (LOWER(u.nome) LIKE LOWER(:busca) OR u.cpf LIKE :busca)";
            }
            var query = entityManager.createQuery(hql, Usuario.class).setParameter("hId", hospitalId);
            if (busca != null && !busca.isEmpty()) query.setParameter("busca", "%" + busca + "%");
            usuarios = query.getResultList();
            model.addAttribute("avisoLaboratorio", "Modo Laboratório: Visualizando todos os pacientes da rede.");
        } else {
            if (busca != null && !busca.isEmpty()) {
                usuarios = usuarioRepository.findByPolos_IdAndNomeContainingIgnoreCase(id, busca);
            } else {
                usuarios = usuarioRepository.findByPolos_Id(id);
            }
        }

        model.addAttribute("listaUsuarios", usuarios);
        model.addAttribute("clinica", clinica);
        
        Map<Long, Profissional> mapaProf = profissionalRepository.findAll().stream()
                .collect(Collectors.toMap(p -> p.getUsuario().getId(), p -> p));
        
        try {
            List<Object[]> adminsData = entityManager.createNativeQuery("SELECT usuario_id, matricula, cargo FROM administrador").getResultList();
            for(Object[] row : adminsData) {
                Long userId = ((Number) row[0]).longValue();
                String matricula = (String) row[1];
                String cargo = (String) row[2];
                Profissional profFake = new Profissional();
                profFake.setMatricula(matricula);
                profFake.setEspecialidade(cargo); 
                profFake.setTipoProfissional("ADMINISTRADOR");
                mapaProf.put(userId, profFake);
            }
        } catch (Exception e) {}

        model.addAttribute("mapaProfissionais", mapaProf);
        model.addAttribute("titulo", "Cadastro: " + clinica.getNome());
        model.addAttribute("modoClinica", false);
        model.addAttribute("modoUsuarios", true); 
        model.addAttribute("hospitalId", clinica.getPoloPai() != null ? clinica.getPoloPai().getId() : null);
        model.addAttribute("clinicaId", id); 
        model.addAttribute("buscaAtual", busca);

        return "admin/relatorio-polos";
    }

    // --- MÉTODOS AUXILIARES GERAIS ---

    private void adicionarUsuarioAoModel(Model model, Principal principal) {
        if (principal != null) {
            model.addAttribute("usuario", usuarioRepository.findByUsernameOrCpf(principal.getName()));
        }
    }

    private Map<Long, Map<String, Long>> calcularEstatisticasPolos(List<Polo> polos) {
        Map<Long, Map<String, Long>> statsGeral = new HashMap<>();
        if (polos == null || polos.isEmpty()) return statsGeral;
        List<Long> ids = polos.stream().map(Polo::getId).collect(Collectors.toList());
        
        for (Long id : ids) {
            Map<String, Long> s = new HashMap<>();
            s.put("PACIENTES", 0L); s.put("PROFISSIONAIS", 0L); s.put("ADMINS", 0L);
            statsGeral.put(id, s);
        }

        try {
            String hql = "SELECT p.id, u.perfil, COUNT(u) FROM Usuario u JOIN u.polos p WHERE p.id IN :ids GROUP BY p.id, u.perfil";
            List<Object[]> resultados = entityManager.createQuery(hql).setParameter("ids", ids).getResultList();
            for (Object[] row : resultados) {
                Long poloId = (Long) row[0];
                String perfil = ((String) row[1]).toUpperCase();
                Long qtd = (Long) row[2];
                Map<String, Long> s = statsGeral.get(poloId);
                if (s != null) {
                    if (perfil.contains("PACIENTE")) s.put("PACIENTES", s.get("PACIENTES") + qtd);
                    else if (perfil.contains("ADMIN")) s.put("ADMINS", s.get("ADMINS") + qtd);
                    else s.put("PROFISSIONAIS", s.get("PROFISSIONAIS") + qtd);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        long totalPacientesRegiao = 0;
        long totalProfissionaisRegiao = 0;
        long totalAdminsRegiao = 0;

        for (Map<String, Long> stats : statsGeral.values()) {
            totalPacientesRegiao += stats.get("PACIENTES");
            totalProfissionaisRegiao += stats.get("PROFISSIONAIS");
            totalAdminsRegiao += stats.get("ADMINS");
        }

        for (Polo p : polos) {
            if ("LABORATORIO".equalsIgnoreCase(p.getTipo())) {
                Map<String, Long> s = statsGeral.get(p.getId());
                if (s != null) {
                    s.put("PACIENTES", totalPacientesRegiao);
                    s.put("PROFISSIONAIS", totalProfissionaisRegiao);
                    s.put("ADMINS", totalAdminsRegiao);
                }
            }
        }

        return statsGeral;
    }

    // =================================================================================
    // 7. GESTÃO DE LEITOS (NOVO DASHBOARD)
    // =================================================================================
    
    @GetMapping("/leitos")
    public String dashboardLeitos(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        Usuario admin = usuarioRepository.findByUsernameOrCpf(principal.getName());
        model.addAttribute("usuario", admin);

        // --- MOCK DATA PARA LEITOS (Para popular o dashboard imediatamente) ---
        // Em produção, isso viria de leitoRepository.findAll()
        List<Map<String, Object>> listaLeitos = new ArrayList<>();
        
        // Simulação de Alas: UTI, Internação, Pediátria
        String[] alas = {"UTI Adulto", "Internação Clínica", "Pediatria", "Cirúrgica"};
        String[] statusPossiveis = {"DISPONIVEL", "OCUPADO", "HIGIENIZACAO", "MANUTENCAO", "ALTA_PREVISTA"};
        Random random = new Random();

        int totalLeitos = 50;
        int ocupados = 0;
        int disponiveis = 0;
        int higienizacao = 0;
        int manutencao = 0;
        int altaPrevista = 0;

        for (int i = 1; i <= totalLeitos; i++) {
            Map<String, Object> leito = new HashMap<>();
            leito.put("numero", "L-" + String.format("%03d", i));
            
            // Distribuição de Alas
            String ala;
            if (i <= 10) ala = alas[0]; // UTI
            else if (i <= 30) ala = alas[1]; // Internação
            else if (i <= 40) ala = alas[2]; // Pediatria
            else ala = alas[3]; // Cirúrgica
            leito.put("ala", ala);

            // Sorteio de Status (Ponderado)
            int r = random.nextInt(100);
            String status;
            if (r < 60) status = "OCUPADO";
            else if (r < 80) status = "DISPONIVEL";
            else if (r < 90) status = "ALTA_PREVISTA";
            else if (r < 95) status = "HIGIENIZACAO";
            else status = "MANUTENCAO";
            
            leito.put("status", status);

            // Dados do Paciente (se ocupado/alta)
            if (status.equals("OCUPADO") || status.equals("ALTA_PREVISTA")) {
                leito.put("paciente", "Paciente Exemplo " + i);
                leito.put("sexo", random.nextBoolean() ? "M" : "F");
                leito.put("idade", 20 + random.nextInt(60));
                leito.put("entrada", LocalDateTime.now().minusDays(random.nextInt(10)).format(DateTimeFormatter.ofPattern("dd/MM HH:mm")));
                leito.put("medico", "Dr. Plantonista");
            }

            listaLeitos.add(leito);

            // Contagem
            switch(status) {
                case "OCUPADO": ocupados++; break;
                case "DISPONIVEL": disponiveis++; break;
                case "HIGIENIZACAO": higienizacao++; break;
                case "MANUTENCAO": manutencao++; break;
                case "ALTA_PREVISTA": altaPrevista++; break;
            }
        }

        // Agrupar por Ala para a View
        Map<String, List<Map<String, Object>>> leitosPorAla = listaLeitos.stream()
            .collect(Collectors.groupingBy(l -> (String) l.get("ala")));

        model.addAttribute("leitosPorAla", leitosPorAla);
        model.addAttribute("totalLeitos", totalLeitos);
        model.addAttribute("ocupados", ocupados);
        model.addAttribute("disponiveis", disponiveis);
        model.addAttribute("higienizacao", higienizacao);
        model.addAttribute("manutencao", manutencao);
        model.addAttribute("altaPrevista", altaPrevista);
        
        // Percentual de Ocupação
        long percOcupacao = Math.round(((double)(ocupados + altaPrevista) / totalLeitos) * 100);
        model.addAttribute("percOcupacao", percOcupacao);

        // Dados Gráficos (Chart.js)
        model.addAttribute("dadosStatus", Arrays.asList(ocupados, disponiveis, higienizacao, manutencao, altaPrevista));
        model.addAttribute("dadosAlas", Arrays.asList(
            listaLeitos.stream().filter(l -> l.get("ala").equals("UTI Adulto") && l.get("status").equals("OCUPADO")).count(),
            listaLeitos.stream().filter(l -> l.get("ala").equals("Internação Clínica") && l.get("status").equals("OCUPADO")).count(),
            listaLeitos.stream().filter(l -> l.get("ala").equals("Pediatria") && l.get("status").equals("OCUPADO")).count(),
            listaLeitos.stream().filter(l -> l.get("ala").equals("Cirúrgica") && l.get("status").equals("OCUPADO")).count()
        ));

        return "admin/leitos";
    }

    // =================================================================================
    // 8. AUDITORIA E EXPORTAÇÃO (CORRIGIDA)
    // =================================================================================

    @GetMapping("/download-relatorio/{formato}")
    public ResponseEntity<ByteArrayResource> downloadAuditoria(
            @PathVariable String formato,
            @RequestParam(required = false) String tipo, 
            @RequestParam(required = false) Long clinicaId 
    ) {
        String conteudo = "";
        String filename = "relatorio_" + (tipo != null ? tipo : "GLOBAL") + "_" + System.currentTimeMillis();
        MediaType mediaType = MediaType.TEXT_PLAIN;

        try {
            List<?> dados = carregarDadosGlobais(tipo);

            if ("JSON".equalsIgnoreCase(formato)) {
                // CORREÇÃO: Converte Object[] em Map para JSON legível
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                mapper.enable(SerializationFeature.INDENT_OUTPUT);

                if (!dados.isEmpty() && dados.get(0) instanceof Object[]) {
                    List<Map<String, Object>> dadosJson = new ArrayList<>();
                    for (Object obj : dados) {
                        Object[] row = (Object[]) obj;
                        Map<String, Object> map = new LinkedHashMap<>();
                        map.put("data_hora", row[0]);
                        map.put("acao", row[1]);
                        map.put("detalhes", row[2]);
                        map.put("responsavel", row[3]);
                        dadosJson.add(map);
                    }
                    conteudo = mapper.writeValueAsString(dadosJson);
                } else {
                    conteudo = mapper.writeValueAsString(dados);
                }
                
                filename += ".json";
                mediaType = MediaType.APPLICATION_JSON;

            } else if ("CSV".equalsIgnoreCase(formato)) {
                conteudo = gerarCSV(dados);
                filename += ".csv";
                mediaType = MediaType.parseMediaType("text/csv");

            } else { 
                conteudo = gerarTXT(dados);
                filename += ".txt";
            }

        } catch (Exception e) {
            conteudo = "Erro ao gerar relatório: " + e.getMessage();
        }

        ByteArrayResource resource = new ByteArrayResource(conteudo.getBytes());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + filename)
                .contentType(mediaType)
                .contentLength(resource.contentLength())
                .body(resource);
    }

    private List<?> carregarDadosGlobais(String tipo) {
        // Seleção EXPLÍCITA de colunas para garantir ordem
        if ("LOGS".equalsIgnoreCase(tipo)) {
            return entityManager.createNativeQuery("SELECT data_hora, acao, detalhes, username_registrado FROM historico_logs ORDER BY data_hora DESC").getResultList();
        }
        if ("EXCLUIDOS".equalsIgnoreCase(tipo)) {
            return entityManager.createNativeQuery("SELECT data_hora, acao, detalhes, username_registrado FROM historico_logs WHERE acao = 'EXCLUSAO_USUARIO' ORDER BY data_hora DESC").getResultList();
        }
        if ("VOLUME".equalsIgnoreCase(tipo)) {
             return entityManager.createNativeQuery("SELECT data_hora, acao, detalhes, username_registrado FROM historico_logs WHERE DATE(data_hora) = CURDATE() ORDER BY data_hora DESC").getResultList();
        }
        return usuarioRepository.findAll();
    }

    private String gerarCSV(List<?> dados) {
        StringBuilder sb = new StringBuilder();
        if (dados.isEmpty()) return "Nenhum dado encontrado";
        
        Object primeiro = dados.get(0);

        // LOGS (Array de Objetos)
        if (primeiro instanceof Object[]) {
            sb.append("DATA_HORA;ACAO;DETALHES;RESPONSAVEL\n");
            for (Object obj : dados) {
                Object[] row = (Object[]) obj;
                String data = row[0] != null ? row[0].toString() : "";
                String acao = row[1] != null ? row[1].toString() : "";
                // Remove quebras de linha para não quebrar o CSV
                String detalhes = row[2] != null ? row[2].toString().replace("\n", " | ").replace(";", ",") : "";
                String resp = row[3] != null ? row[3].toString() : "";
                
                sb.append(data).append(";")
                  .append(acao).append(";")
                  .append(detalhes).append(";")
                  .append(resp).append("\n");
            }
        } 
        // USUÁRIOS (Entidade)
        else if (primeiro instanceof Usuario) {
            sb.append("ID;NOME;CPF;PERFIL;EMAIL;STATUS;POLOS_VINCULADOS\n");
            for (Object obj : dados) {
                Usuario u = (Usuario) obj;
                String polos = u.getPolos() != null ? u.getPolos().stream().map(Polo::getNome).collect(Collectors.joining("|")) : "";
                sb.append(u.getId()).append(";")
                  .append(u.getNome()).append(";")
                  .append(u.getCpf()).append(";")
                  .append(u.getPerfil()).append(";")
                  .append(u.getEmail()).append(";")
                  .append(u.isAtivo() ? "ATIVO" : "INATIVO").append(";")
                  .append(polos).append("\n");
            }
        } 
        else {
            sb.append("DADOS_NAO_RECONHECIDOS\n").append(dados.toString());
        }
        return sb.toString();
    }

    private String gerarTXT(List<?> dados) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== RELATÓRIO DE AUDITORIA VIDAPLUS ===\n");
        sb.append("Gerado em: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("\n");
        sb.append("Registros: ").append(dados.size()).append("\n");
        sb.append("=========================================\n\n");

        if (dados.isEmpty()) {
            sb.append("Nenhum registro encontrado.\n");
            return sb.toString();
        }

        Object primeiro = dados.get(0);

        if (primeiro instanceof Object[]) {
            for (Object obj : dados) {
                Object[] row = (Object[]) obj;
                sb.append("--------------------------------------------------\n");
                sb.append("DATA: ").append(row[0]).append("\n");
                sb.append("AÇÃO: ").append(row[1]).append("\n");
                sb.append("RESPONSÁVEL: ").append(row[3]).append("\n");
                sb.append("DETALHES:\n").append(row[2]).append("\n");
            }
        }
        else if (primeiro instanceof Usuario) {
            for (Object obj : dados) {
                Usuario u = (Usuario) obj;
                sb.append("--------------------------------------------------\n");
                sb.append("USUÁRIO: ").append(u.getNome()).append(" (ID: ").append(u.getId()).append(")\n");
                sb.append("CPF: ").append(u.getCpf()).append(" | PERFIL: ").append(u.getPerfil()).append("\n");
                String polos = u.getPolos() != null ? u.getPolos().stream().map(Polo::getNome).collect(Collectors.joining(", ")) : "Nenhum";
                sb.append("VÍNCULOS: ").append(polos).append("\n");
            }
        }

        return sb.toString();
    }
}