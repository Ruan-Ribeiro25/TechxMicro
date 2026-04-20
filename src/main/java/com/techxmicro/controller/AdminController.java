package com.techxmicro.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.techxmicro.entity.*;
import com.techxmicro.repository.*;
import com.techxmicro.service.FinanceiroService;

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

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {
	
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PoloRepository poloRepository; 
    @Autowired private ProfissionalRepository profissionalRepository;
    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private AmbulanciaRepository ambulanciaRepository; 
    
    @Autowired private FinanceiroService financeiroService;
    
    @PersistenceContext
    private EntityManager entityManager;

    @GetMapping("/login")
    public String loginAdmin(@RequestParam(value = "error", required = false) String error, Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        Usuario admin = usuarioRepository.findByUsernameOrEmail(principal.getName());
        model.addAttribute("usuario", admin);
        
        if (error != null) {
            if ("denied".equals(error)) {
                model.addAttribute("erro", "Acesso Negado: Você não tem permissão.");
            } else if ("true".equals(error)) {
                model.addAttribute("erro", "Senha incorreta.");
            }
        }
        return "admin/login"; 
    }

    @PostMapping("/login-verificar")
    public String verificarSegundoLogin(@RequestParam String password, Principal principal) {
        Usuario admin = usuarioRepository.findByUsernameOrEmail(principal.getName());
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        if (encoder.matches(password, admin.getPassword())) {
            return "redirect:/admin/painel";
        } else {
            return "redirect:/admin/login?error=true";
        }
    }

    @GetMapping("/painel")
    public String painelAdmin(Model model, Principal principal, @RequestParam(value = "busca", required = false) String busca) {
        if (principal == null) return "redirect:/login";
        
        Usuario admin = usuarioRepository.findByUsernameOrEmail(principal.getName());
        
        if (admin == null || admin.getPerfil() == null || !admin.getPerfil().toUpperCase().contains("ADMIN")) {
            return "redirect:/home?error=access_denied";
        }

        model.addAttribute("usuario", admin); 

        long totalUsuariosComuns = ((Number) entityManager.createQuery("SELECT COUNT(u) FROM Usuario u WHERE u.perfil = 'USUARIO'").getSingleResult()).longValue();
        long totalSuporte = ((Number) entityManager.createQuery("SELECT COUNT(u) FROM Usuario u WHERE UPPER(u.perfil) LIKE '%SUPORTE%' OR UPPER(u.perfil) LIKE '%TECNICO%'").getSingleResult()).longValue();
        long totalDesenvolvedores = ((Number) entityManager.createQuery("SELECT COUNT(u) FROM Usuario u WHERE UPPER(u.perfil) LIKE '%DEV%' OR UPPER(u.perfil) LIKE '%ENGENHEIRO%'").getSingleResult()).longValue();
        long totalAdmins = ((Number) entityManager.createQuery("SELECT COUNT(u) FROM Usuario u WHERE UPPER(u.perfil) LIKE '%ADMIN%'").getSingleResult()).longValue();
        long totalPendentes = ((Number) entityManager.createQuery("SELECT COUNT(u) FROM Usuario u WHERE u.ativo = false").getSingleResult()).longValue();
        
        long totalMotoristas = ((Number) entityManager.createQuery("SELECT COUNT(u) FROM Usuario u WHERE UPPER(u.perfil) LIKE '%MOTORISTA%'").getSingleResult()).longValue();
        long totalTecnicos = ((Number) entityManager.createQuery("SELECT COUNT(u) FROM Usuario u WHERE UPPER(u.perfil) LIKE '%TECNICO%'").getSingleResult()).longValue();
        long totalSvGerais = ((Number) entityManager.createQuery("SELECT COUNT(u) FROM Usuario u WHERE UPPER(u.perfil) LIKE '%GERAIS%' OR UPPER(u.perfil) LIKE '%MANUTENCAO%'").getSingleResult()).longValue();
        long totalRecepcionistas = ((Number) entityManager.createQuery("SELECT COUNT(u) FROM Usuario u WHERE UPPER(u.perfil) LIKE '%RECEPCIONISTA%'").getSingleResult()).longValue();

        long totalMatriz = ((Number) entityManager.createQuery("SELECT COUNT(p) FROM Polo p WHERE p.poloPai IS NULL").getSingleResult()).longValue();
        long totalFiliais = ((Number) entityManager.createQuery("SELECT COUNT(p) FROM Polo p WHERE UPPER(p.tipo) = 'FILIAL'").getSingleResult()).longValue();
        long totalDataCenters = ((Number) entityManager.createQuery("SELECT COUNT(p) FROM Polo p WHERE UPPER(p.tipo) = 'DATACENTER'").getSingleResult()).longValue();
        
        model.addAttribute("totalLaboratorios", totalDataCenters); 

        long totalExcluidos = 0;
        try {
            Number count = (Number) entityManager.createNativeQuery("SELECT COUNT(*) FROM historico_logs WHERE acao = 'EXCLUSAO_USUARIO'").getSingleResult();
            totalExcluidos = count.longValue();
        } catch (Exception e) {}

        long totalLogs = 0;
        try {
            Number countLogs = (Number) entityManager.createNativeQuery("SELECT COUNT(*) FROM historico_logs").getSingleResult();
            totalLogs = countLogs.longValue();
        } catch (Exception e) {}

        long volumeHoje = 0;
        try {
            Number countToday = (Number) entityManager.createNativeQuery("SELECT COUNT(*) FROM historico_logs WHERE DATE(data_hora) = CURDATE()").getSingleResult();
            volumeHoje = countToday.longValue();
        } catch (Exception e) {}

        long totalProdutos = produtoRepository.count();
        long alertaEstoque = 0;
        try { alertaEstoque = produtoRepository.countProdutosBaixoEstoque(); } catch (Exception e) {}
        
        model.addAttribute("totalProdutos", totalProdutos);
        model.addAttribute("alertaEstoque", alertaEstoque);

        List<Ambulancia> frotaTotal = ambulanciaRepository.findAll();
        long totalAmbulancias = frotaTotal.size();
        
        BigDecimal saldoReal = financeiroService.calcularSaldoLiquido();
        model.addAttribute("saldoLiquidoAdmin", saldoReal != null ? saldoReal : BigDecimal.ZERO);
        model.addAttribute("crescimentoFinanceiro", "15");

        model.addAttribute("totalPacientes", totalUsuariosComuns); 
        model.addAttribute("totalMedicos", totalSuporte);
        model.addAttribute("totalEnfermeiros", totalDesenvolvedores);
        model.addAttribute("totalAdmins", totalAdmins);
        model.addAttribute("totalPendentes", totalPendentes);
        model.addAttribute("totalPolos", totalMatriz);
        model.addAttribute("totalExcluidos", totalExcluidos);
        model.addAttribute("totalLogs", totalLogs);
        model.addAttribute("volumeHoje", volumeHoje);

        List<Usuario> listaExibicao;
        if (busca != null && !busca.isEmpty()) {
            listaExibicao = entityManager.createQuery("SELECT u FROM Usuario u WHERE LOWER(u.nome) LIKE LOWER(:b) OR LOWER(u.email) LIKE LOWER(:b)", Usuario.class)
                                         .setParameter("b", "%" + busca + "%")
                                         .setMaxResults(50)
                                         .getResultList();
        } else {
            listaExibicao = entityManager.createQuery("SELECT u FROM Usuario u ORDER BY u.id DESC", Usuario.class)
                                         .setMaxResults(50)
                                         .getResultList();
        }
        
        List<Usuario> inativos = entityManager.createQuery("SELECT u FROM Usuario u WHERE u.ativo = false", Usuario.class).getResultList();
        Set<Usuario> combinedUsuarios = new LinkedHashSet<>(inativos); 
        combinedUsuarios.addAll(listaExibicao);
        model.addAttribute("todosUsuarios", new ArrayList<>(combinedUsuarios));
        
        List<Profissional> listaProfissionais = profissionalRepository.findAll();
        Map<Long, Profissional> mapaProfissionais = listaProfissionais.stream().collect(Collectors.toMap(p -> p.getUsuario().getId(), p -> p));
        
        try {
            List<Object[]> adminsData = entityManager.createNativeQuery("SELECT usuario_id, matricula, 'Administrador' as cargo FROM administrador").getResultList();
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

        model.addAttribute("graficoLabels", Arrays.asList(
            "Usuários Comuns", "Suporte TI", "Desenvolvedores", "Admins", 
            "Matriz", "Excluídos", "Motoristas", "Técnicos", 
            "Filiais", "Data Centers", "Sv. Gerais", "Frota/Veículos",
            "Recepcionistas" 
        ));
        
        model.addAttribute("graficoDados", Arrays.asList(
            totalUsuariosComuns, totalSuporte, totalDesenvolvedores, totalAdmins, 
            totalMatriz, totalExcluidos, totalMotoristas, totalTecnicos, 
            totalFiliais, totalDataCenters, totalSvGerais, totalAmbulancias,
            totalRecepcionistas 
        ));

        return "admin/painel"; 
    }

    @GetMapping("/ambulancias")
    public String listarPolosAmbulancia(Model model, Principal principal) {
        if (principal != null) model.addAttribute("usuario", usuarioRepository.findByUsernameOrEmail(principal.getName()));
        model.addAttribute("polos", poloRepository.findByPoloPaiIsNull());
        return "admin/ambulancias-polos"; 
    }

    @GetMapping("/ambulancias/painel/{id}")
    public String painelFrotaPorPolo(@PathVariable Long id, Model model, Principal principal) {
        if (principal != null) model.addAttribute("usuario", usuarioRepository.findByUsernameOrEmail(principal.getName()));
        Polo polo = poloRepository.findById(id).orElse(null);
        String nomePolo = (polo != null) ? polo.getNome() : "Polo Desconhecido";
        
        List<Ambulancia> frotaFiltrada = new ArrayList<>();
        try {
            String sql = (id == 2L) ? "SELECT * FROM ambulancias WHERE polo_id = :pid OR polo_id IS NULL" : "SELECT * FROM ambulancias WHERE polo_id = :pid";
            frotaFiltrada = entityManager.createNativeQuery(sql, Ambulancia.class).setParameter("pid", id).getResultList();
        } catch (Exception e) {}

        model.addAttribute("listaAmbulancias", frotaFiltrada);
        model.addAttribute("totalAmbulancias", frotaFiltrada.size());
        model.addAttribute("disponiveis", frotaFiltrada.stream().filter(a -> "DISPONIVEL".equals(a.getStatus())).count());
        model.addAttribute("emChamado", frotaFiltrada.stream().filter(a -> "EM_CHAMADO".equals(a.getStatus()) || "SOLICITADO".equals(a.getStatus())).count());
        model.addAttribute("manutencao", frotaFiltrada.stream().filter(a -> "MANUTENCAO".equals(a.getStatus())).count());
        model.addAttribute("nomePolo", nomePolo); 
        model.addAttribute("matrizId", id);
        
        model.addAttribute("corridasAtual", Arrays.asList(45, 52, 38, 60, 55, 70, 65, 58, 62, 80, 95, 88));
        model.addAttribute("corridasAnterior", Arrays.asList(30, 40, 35, 45, 48, 50, 52, 48, 55, 60, 70, 65));
        model.addAttribute("dadosRadar", Arrays.asList(85, 40, 25, 60, 30)); 
        model.addAttribute("manutencaoDados", Arrays.asList(5, 3, 2, 8)); 
        
        return "admin/ambulancias";
    }

    @Transactional
    @PostMapping("/ambulancias/salvar")
    public String salvarAmbulancia(@RequestParam String placa, @RequestParam String tipo, @RequestParam String modelo, @RequestParam(required = false) Long poloId) {
        Ambulancia nova = new Ambulancia();
        nova.setPlaca(placa.toUpperCase()); nova.setTipo(tipo); nova.setModelo(modelo);
        nova.setStatus("DISPONIVEL"); nova.setMotorista("-"); nova.setPrevisaoLiberacao("-");
        Ambulancia salva = ambulanciaRepository.save(nova);
        
        if(salva.getId() != null) {
            try { entityManager.createNativeQuery("UPDATE ambulancias SET polo_id = :pid, data_cadastro = NOW() WHERE id = :aid").setParameter("pid", poloId).setParameter("aid", salva.getId()).executeUpdate(); } catch (Exception e) {}
        }
        return (poloId != null) ? "redirect:/admin/ambulancias/painel/" + poloId : "redirect:/admin/ambulancias";
    }

    @PostMapping("/ambulancias/status")
    public String atualizarStatus(@RequestParam Long id, @RequestParam String acao) { 
        Ambulancia amb = ambulanciaRepository.findById(id).orElse(null);
        Long poloId = null;
        
        if (amb != null) {
            try {
                Object result = entityManager.createNativeQuery("SELECT polo_id FROM ambulancias WHERE id = :id").setParameter("id", id).getSingleResult();
                if(result != null) poloId = ((Number) result).longValue();
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

    @GetMapping("/detalhes/{tipo}")
    public String detalhesPorTipo(@PathVariable String tipo, Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        Usuario admin = usuarioRepository.findByUsernameOrEmail(principal.getName());
        model.addAttribute("usuario", admin);
        model.addAttribute("tipoRelatorio", tipo.toUpperCase());

        if ("POLOS".equalsIgnoreCase(tipo)) return "redirect:/admin/polos"; 
        
        if ("EXCLUIDOS".equalsIgnoreCase(tipo) || "LOGS".equalsIgnoreCase(tipo)) {
            String q = "EXCLUIDOS".equalsIgnoreCase(tipo) ? "SELECT data_hora, acao, detalhes, username_registrado FROM historico_logs WHERE acao = 'EXCLUSAO_USUARIO' ORDER BY data_hora DESC" : "SELECT data_hora, acao, detalhes, username_registrado FROM historico_logs ORDER BY data_hora DESC";
            model.addAttribute("listaLogs", entityManager.createNativeQuery(q).getResultList()); 
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

            model.addAttribute("dadosHora", Arrays.asList(dadosHora)); 
            model.addAttribute("dadosDia", Arrays.asList(dadosDia)); 
            model.addAttribute("dadosMes", Arrays.asList(dadosMes));
            model.addAttribute("sumHoje", Arrays.stream(dadosHora).mapToInt(Integer::intValue).sum());
            model.addAttribute("sumMes", Arrays.stream(dadosDia).mapToInt(Integer::intValue).sum());
            model.addAttribute("sumAno", Arrays.stream(dadosMes).mapToInt(Integer::intValue).sum());
            
            return "admin/lista-detalhes";
        }

        List<Usuario> todos = usuarioRepository.findAll();
        List<Usuario> filtrados = new ArrayList<>();
        if ("SUPORTE".equalsIgnoreCase(tipo)) filtrados = todos.stream().filter(u -> u.getPerfil() != null && u.getPerfil().contains("SUPORTE")).collect(Collectors.toList());
        else if ("DESENVOLVEDORES".equalsIgnoreCase(tipo)) filtrados = todos.stream().filter(u -> u.getPerfil() != null && (u.getPerfil().contains("DEV") || u.getPerfil().contains("ENGENHEIRO"))).collect(Collectors.toList());
        else if ("USUARIOS".equalsIgnoreCase(tipo)) filtrados = todos.stream().filter(u -> u.getPerfil() != null && u.getPerfil().equals("USUARIO")).collect(Collectors.toList());
        else if ("ADMINS".equalsIgnoreCase(tipo)) filtrados = todos.stream().filter(u -> u.getPerfil() != null && u.getPerfil().contains("ADMIN")).collect(Collectors.toList());

        model.addAttribute("listaUsuarios", filtrados);
        
        List<Profissional> listaProfissionais = profissionalRepository.findAll();
        Map<Long, Profissional> mapaProfissionais = listaProfissionais.stream().collect(Collectors.toMap(p -> p.getUsuario().getId(), p -> p));
        
        try {
            List<Object[]> adminsData = entityManager.createNativeQuery("SELECT usuario_id, matricula, 'Administrador' as cargo FROM administrador").getResultList();
            for(Object[] row : adminsData) {
                Long userId = ((Number) row[0]).longValue();
                Profissional profFake = new Profissional();
                profFake.setMatricula((String) row[1]); profFake.setEspecialidade((String) row[2]); profFake.setCrm("CORP"); profFake.setStatusAprovacao("APROVADO");
                mapaProfissionais.putIfAbsent(userId, profFake);
            }
        } catch (Exception e) {}
        model.addAttribute("mapaProfissionais", mapaProfissionais);
        return "admin/lista-detalhes";
    }

    @Transactional
    @PostMapping("/aprovar-profissional")
    public String aprovarProfissional(@RequestParam Long idUsuario) {
        entityManager.createQuery("UPDATE Usuario u SET u.ativo = true WHERE u.id = :id").setParameter("id", idUsuario).executeUpdate();
        return "redirect:/admin/painel?msg=aprovado";
    }
    
    @Transactional
    @PostMapping("/bloquear-usuario")
    public String bloquearUsuario(@RequestParam Long idUsuario) {
        entityManager.createQuery("UPDATE Usuario u SET u.ativo = false WHERE u.id = :id").setParameter("id", idUsuario).executeUpdate();
        return "redirect:/admin/painel?msg=bloqueado";
    }
    
    @Transactional
    @PostMapping("/rejeitar-cadastro")
    public String rejeitarCadastro(@RequestParam Long idUsuario, Principal principal) {
        Usuario u = usuarioRepository.findById(idUsuario).orElse(null);
        if (u != null) {
            String adminResponsavel = principal != null ? principal.getName() : "SISTEMA";
            String detalhes = "Cadastro REJEITADO e removido: " + u.getNome() + " (E-mail: " + u.getEmail() + ")";
            entityManager.createNativeQuery("INSERT INTO historico_logs (acao, data_hora, detalhes, username_registrado) VALUES (?, ?, ?, ?)")
                .setParameter(1, "CADASTRO_REJEITADO").setParameter(2, LocalDateTime.now(java.time.ZoneId.of("America/Sao_Paulo"))).setParameter(3, detalhes).setParameter(4, adminResponsavel).executeUpdate();
            usuarioRepository.deleteById(idUsuario);
        }
        return "redirect:/admin/painel?msg=rejeitado";
    }

    @Transactional
    @PostMapping("/alterar-especialidade")
    public String alterarEspecialidadeTecnico(@RequestParam Long idUsuario, @RequestParam String novaEspecialidade) {
        entityManager.createQuery("UPDATE Usuario u SET u.perfil = :perfil WHERE u.id = :id").setParameter("perfil", novaEspecialidade).setParameter("id", idUsuario).executeUpdate();
        return "redirect:/admin/painel?msg=perfil_atualizado";
    }

    @Transactional
    @PostMapping("/excluir-usuario")
    public String excluirUsuario(@RequestParam("idUsuario") Long idUsuario, @RequestParam("motivo") String motivo, Principal principal) {
        try {
            Usuario u = usuarioRepository.findById(idUsuario).orElse(null);
            if (u == null) return "redirect:/admin/painel?error=erro_desconhecido";
            
            if (u.getPerfil() != null && u.getPerfil().contains("ADMIN")) {
                long adminsCount = usuarioRepository.findAll().stream().filter(user -> user.getPerfil() != null && user.getPerfil().contains("ADMIN")).count();
                if (adminsCount <= 1) return "redirect:/admin/painel?error=ultimo_admin";
            }
            
            try {
                Long chamados = (Long) entityManager.createQuery("SELECT COUNT(c) FROM Chamado c WHERE c.solicitante.id = :uid OR c.responsavel.id = :uid").setParameter("uid", idUsuario).getSingleResult();
                Long mensagens = (Long) entityManager.createQuery("SELECT COUNT(i) FROM InteracaoChamado i WHERE i.autor.id = :uid").setParameter("uid", idUsuario).getSingleResult();
                if (chamados > 0 || mensagens > 0) return "redirect:/admin/painel?error=dependencia_helpdesk";
            } catch (Exception e) {}

            String adminResponsavel = principal != null ? principal.getName() : "SISTEMA";
            entityManager.createNativeQuery("INSERT INTO historico_logs (acao, data_hora, detalhes, username_registrado) VALUES (?, ?, ?, ?)")
                .setParameter(1, "EXCLUSAO_USUARIO").setParameter(2, LocalDateTime.now(java.time.ZoneId.of("America/Sao_Paulo")))
                .setParameter(3, "MOTIVO: " + motivo.toUpperCase() + "\nUsuário: " + u.getNome() + " (E-mail: " + u.getEmail() + ")\n")
                .setParameter(4, adminResponsavel).executeUpdate();

            try { entityManager.createNativeQuery("DELETE FROM usuarios_polos WHERE usuario_id = :uid").setParameter("uid", idUsuario).executeUpdate(); } catch (Exception e) {}
            try { entityManager.createNativeQuery("DELETE FROM administrador WHERE usuario_id = :uid").setParameter("uid", idUsuario).executeUpdate(); } catch (Exception e) {}
            try { entityManager.createNativeQuery("DELETE FROM profissionais WHERE usuario_id = :uid").setParameter("uid", idUsuario).executeUpdate(); } catch (Exception e) {}
            
            if (u.getPolos() != null) u.getPolos().clear(); 
            entityManager.flush();
            entityManager.createNativeQuery("DELETE FROM usuarios WHERE id = :uid").setParameter("uid", idUsuario).executeUpdate();
            
            return "redirect:/admin/painel?msg=excluido";
        } catch (Exception e) { return "redirect:/admin/painel?error=erro_desconhecido"; }
    }

    @GetMapping("/polos")
    public String listarCidades(Model model, Principal principal) {
        if (principal != null) model.addAttribute("usuario", usuarioRepository.findByUsernameOrEmail(principal.getName()));
        List<Polo> cidades = poloRepository.findByPoloPaiIsNull();
        model.addAttribute("polos", cidades);
        
        Map<Long, Map<String, Long>> statsEstrutura = new HashMap<>();
        for(Polo p : cidades) {
            Map<String, Long> counts = new HashMap<>();
            counts.put("MATRIZ", 1L); 
            List<Polo> filiais = poloRepository.findByPoloPai_Id(p.getId());
            counts.put("FILIAIS", filiais.stream().filter(f -> f.getTipo() == null || !"DATACENTER".equalsIgnoreCase(f.getTipo())).count());
            counts.put("DATACENTERS", filiais.stream().filter(f -> "DATACENTER".equalsIgnoreCase(f.getTipo())).count());
            statsEstrutura.put(p.getId(), counts);
        }
        
        model.addAttribute("statsEstrutura", statsEstrutura); 
        model.addAttribute("titulo", "Gestão de Polos (Cidades)"); 
        model.addAttribute("modoFilial", false); 
        model.addAttribute("modoUsuarios", false); 
        return "admin/relatorio-polos"; 
    }

    @GetMapping("/polos/{id}/filiais")
    public String listarFiliais(@PathVariable Long id, Model model, Principal principal) {
        if (principal != null) model.addAttribute("usuario", usuarioRepository.findByUsernameOrEmail(principal.getName()));
        Polo matriz = poloRepository.findById(id).orElse(null);
        List<Polo> filiais = poloRepository.findByPoloPai_Id(id);
        model.addAttribute("polos", filiais);
        
        Map<Long, Map<String, Long>> statsGeral = new HashMap<>();
        try {
            List<Long> ids = filiais.stream().map(Polo::getId).collect(Collectors.toList());
            if(!ids.isEmpty()){
                for (Long cid : ids) { 
                    Map<String, Long> s = new HashMap<>(); s.put("USUARIOS", 0L); s.put("PROFISSIONAIS", 0L); s.put("ADMINS", 0L); statsGeral.put(cid, s); 
                }
                List<Object[]> res = entityManager.createQuery("SELECT p.id, u.perfil, COUNT(u) FROM Usuario u JOIN u.polos p WHERE p.id IN :ids GROUP BY p.id, u.perfil").setParameter("ids", ids).getResultList();
                for (Object[] row : res) {
                    Map<String, Long> s = statsGeral.get((Long) row[0]);
                    String pf = ((String) row[1]).toUpperCase();
                    if (s != null) {
                        if (pf.equals("USUARIO")) s.put("USUARIOS", s.get("USUARIOS") + (Long)row[2]);
                        else if (pf.contains("ADMIN")) s.put("ADMINS", s.get("ADMINS") + (Long)row[2]);
                        else s.put("PROFISSIONAIS", s.get("PROFISSIONAIS") + (Long)row[2]);
                    }
                }
            }
        } catch (Exception e) {}
        
        model.addAttribute("estatisticas", statsGeral);
        model.addAttribute("titulo", "Filiais: " + (matriz != null ? matriz.getCidade() : "")); 
        model.addAttribute("matrizNome", (matriz != null ? matriz.getNome() : "")); 
        model.addAttribute("modoFilial", true); 
        model.addAttribute("modoUsuarios", false); 
        model.addAttribute("matrizId", id);
        return "admin/relatorio-polos";
    }

    @GetMapping("/polos/filial/{id}/usuarios")
    public String listarUsuariosFilial(@PathVariable Long id, @RequestParam(required = false) String busca, Model model, Principal principal) {
        if (principal != null) model.addAttribute("usuario", usuarioRepository.findByUsernameOrEmail(principal.getName()));
        Polo filial = poloRepository.findById(id).orElse(null);
        if (filial == null) return "redirect:/admin/polos";
        
        List<Usuario> usuarios;
        if ("DATACENTER".equalsIgnoreCase(filial.getTipo()) && filial.getPoloPai() != null) {
            String hql = "SELECT DISTINCT u FROM Usuario u JOIN u.polos p WHERE (p.id = :hId OR p.poloPai.id = :hId)";
            if (busca != null && !busca.isEmpty()) hql += " AND (LOWER(u.nome) LIKE LOWER(:busca) OR LOWER(u.email) LIKE LOWER(:busca))";
            var q = entityManager.createQuery(hql, Usuario.class).setParameter("hId", filial.getPoloPai().getId());
            if (busca != null && !busca.isEmpty()) q.setParameter("busca", "%" + busca + "%");
            usuarios = q.getResultList();
            model.addAttribute("avisoDataCenter", "Modo Data Center: Visualizando todos os usuários da rede.");
        } else {
            usuarios = (busca != null && !busca.isEmpty()) ? usuarioRepository.findByPolos_IdAndNomeContainingIgnoreCase(id, busca) : usuarioRepository.findByPolos_Id(id);
        }
        
        model.addAttribute("listaUsuarios", usuarios); 
        model.addAttribute("filial", filial);
        Map<Long, Profissional> mapaProf = profissionalRepository.findAll().stream().collect(Collectors.toMap(p -> p.getUsuario().getId(), p -> p));
        
        try {
            List<Object[]> adminsData = entityManager.createNativeQuery("SELECT usuario_id, matricula, 'Administrador' as cargo FROM administrador").getResultList();
            for(Object[] row : adminsData) {
                Profissional pf = new Profissional(); pf.setMatricula((String) row[1]); pf.setEspecialidade((String) row[2]); pf.setTipoProfissional("ADMINISTRADOR");
                mapaProf.put(((Number) row[0]).longValue(), pf);
            }
        } catch (Exception e) {}
        
        model.addAttribute("mapaProfissionais", mapaProf); 
        model.addAttribute("titulo", "Cadastro: " + filial.getNome()); 
        model.addAttribute("modoFilial", false); 
        model.addAttribute("modoUsuarios", true); 
        model.addAttribute("matrizId", filial.getPoloPai() != null ? filial.getPoloPai().getId() : null); 
        model.addAttribute("filialId", id); 
        model.addAttribute("buscaAtual", busca);
        return "admin/relatorio-polos";
    }

    @GetMapping("/download-relatorio/{formato}")
    public ResponseEntity<ByteArrayResource> downloadAuditoria(@PathVariable String formato, @RequestParam(required = false) String tipo) {
        String conteudo = ""; 
        String filename = "relatorio_" + (tipo != null ? tipo : "GLOBAL") + "_" + System.currentTimeMillis(); 
        MediaType mediaType = MediaType.TEXT_PLAIN;
        
        try {
            List<?> dados = "LOGS".equalsIgnoreCase(tipo) ? entityManager.createNativeQuery("SELECT data_hora, acao, detalhes, username_registrado FROM historico_logs ORDER BY data_hora DESC").getResultList() : "EXCLUIDOS".equalsIgnoreCase(tipo) ? entityManager.createNativeQuery("SELECT data_hora, acao, detalhes, username_registrado FROM historico_logs WHERE acao = 'EXCLUSAO_USUARIO' ORDER BY data_hora DESC").getResultList() : "VOLUME".equalsIgnoreCase(tipo) ? entityManager.createNativeQuery("SELECT data_hora, acao, detalhes, username_registrado FROM historico_logs WHERE DATE(data_hora) = CURDATE() ORDER BY data_hora DESC").getResultList() : usuarioRepository.findAll();
            
            if ("JSON".equalsIgnoreCase(formato)) {
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
                } else { conteudo = mapper.writeValueAsString(dados); }
                filename += ".json"; 
                mediaType = MediaType.APPLICATION_JSON;
                
            } else if ("CSV".equalsIgnoreCase(formato)) {
                StringBuilder sb = new StringBuilder();
                if (!dados.isEmpty() && dados.get(0) instanceof Object[]) {
                    sb.append("DATA_HORA;ACAO;DETALHES;RESPONSAVEL\n");
                    for (Object obj : dados) { 
                        Object[] row = (Object[]) obj; 
                        sb.append(row[0] != null ? row[0].toString() : "").append(";")
                          .append(row[1] != null ? row[1].toString() : "").append(";")
                          .append(row[2] != null ? row[2].toString().replace("\n", " | ").replace(";", ",") : "").append(";")
                          .append(row[3] != null ? row[3].toString() : "").append("\n"); 
                    }
                } else if (!dados.isEmpty() && dados.get(0) instanceof Usuario) {
                    sb.append("ID;NOME;EMAIL;PERFIL;STATUS;POLOS_VINCULADOS\n");
                    for (Object obj : dados) { 
                        Usuario u = (Usuario) obj; 
                        sb.append(u.getId()).append(";")
                          .append(u.getNome()).append(";")
                          .append(u.getEmail()).append(";")
                          .append(u.getPerfil()).append(";")
                          .append(u.isAtivo() ? "ATIVO" : "INATIVO").append(";")
                          .append(u.getPolos() != null ? u.getPolos().stream().map(Polo::getNome).collect(Collectors.joining("|")) : "").append("\n"); 
                    }
                }
                conteudo = sb.toString(); 
                filename += ".csv"; 
                mediaType = MediaType.parseMediaType("text/csv");
                
            } else { 
                StringBuilder sb = new StringBuilder("=== RELATÓRIO DE AUDITORIA TECHXMICRO ===\nGerado em: " + LocalDateTime.now(java.time.ZoneId.of("America/Sao_Paulo")).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + "\nRegistros: " + dados.size() + "\n=========================================\n\n");
                if (!dados.isEmpty() && dados.get(0) instanceof Object[]) {
                    for (Object obj : dados) { 
                        Object[] row = (Object[]) obj; 
                        sb.append("--------------------------------------------------\nDATA: ").append(row[0])
                          .append("\nAÇÃO: ").append(row[1])
                          .append("\nRESPONSÁVEL: ").append(row[3])
                          .append("\nDETALHES:\n").append(row[2]).append("\n"); 
                    }
                } else if (!dados.isEmpty() && dados.get(0) instanceof Usuario) {
                    for (Object obj : dados) { 
                        Usuario u = (Usuario) obj; 
                        sb.append("--------------------------------------------------\nUSUÁRIO: ").append(u.getNome()).append(" (ID: ").append(u.getId())
                          .append(")\nE-MAIL: ").append(u.getEmail()).append(" | PERFIL: ").append(u.getPerfil())
                          .append("\nVÍNCULOS: ").append(u.getPolos() != null ? u.getPolos().stream().map(Polo::getNome).collect(Collectors.joining(", ")) : "Nenhum").append("\n"); 
                    }
                }
                conteudo = sb.toString(); 
                filename += ".txt";
            }
        } catch (Exception e) { conteudo = "Erro: " + e.getMessage(); }
        
        ByteArrayResource resource = new ByteArrayResource(conteudo.getBytes());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + filename)
                .contentType(mediaType)
                .contentLength(resource.contentLength())
                .body(resource);
    }
}