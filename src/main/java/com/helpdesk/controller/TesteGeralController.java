package com.helpdesk.controller;

import com.helpdesk.entity.*;
import com.helpdesk.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.util.Optional;

@RestController
@RequestMapping("/api/teste")
public class TesteGeralController {

    @Autowired private PoloRepository poloRepo;
    @Autowired private LogRepository logRepo;
    @Autowired private AmbulanciaRepository ambuRepo;
    @Autowired private LeitoRepository leitoRepo;      
    @Autowired private LaboratorioRepository labRepo;  
    @Autowired private TransacaoFinanceiraRepository finRepo; 
    @Autowired private ProdutoRepository prodRepo;
    @Autowired private ProntuarioRepository prontuarioRepo;
    
    // NOVO REPOSITÓRIO DE AGENDAMENTOS
    @Autowired private AgendamentoRepository agendamentoRepo;
    
    @Autowired private JdbcTemplate jdbcTemplate;

    // --- MÁGICA V3 (O "Cérebro" que preenche tudo) ---
    private void preencherAutomaticamente(Object destino, Map<String, Object> origem) {
        Method[] metodos = destino.getClass().getMethods();
        for (Method m : metodos) {
            if (m.getName().startsWith("set") && m.getParameterCount() == 1) {
                String atributo = m.getName().substring(3).toLowerCase(); 
                for (String key : origem.keySet()) {
                    String keyLimpa = key.replace("_", "").toLowerCase();
                    if (keyLimpa.equals(atributo) || atributo.startsWith(keyLimpa)) {
                        try {
                            Object valor = origem.get(key);
                            Class<?> tipoParametro = m.getParameterTypes()[0];
                            if (tipoParametro == BigDecimal.class) {
                                m.invoke(destino, new BigDecimal(valor.toString()));
                            } else if (tipoParametro.isEnum() && valor instanceof String) {
                                for(Object objEnum : tipoParametro.getEnumConstants()) {
                                    if(objEnum.toString().equalsIgnoreCase((String)valor)) {
                                        m.invoke(destino, objEnum); break;
                                    }
                                }
                            } else if (valor instanceof Integer && tipoParametro == Double.class) {
                                m.invoke(destino, ((Integer) valor).doubleValue());
                            } else {
                                m.invoke(destino, valor);
                            }
                        } catch (Exception e) { }
                    }
                }
            }
        }
    }

    private Map<String, Object> simplificar(Object obj) {
        Map<String, Object> map = new HashMap<>();
        if (obj == null) return map;
        try {
            try { map.put("id", obj.getClass().getMethod("getId").invoke(obj)); } catch (Exception e) {}
            map.put("tipo_objeto", obj.getClass().getSimpleName());
            try { map.put("info", obj.toString()); } catch (Exception e) {}
        } catch (Exception e) { map.put("erro_visualizacao", e.getMessage()); }
        return map;
    }

    // ================== AGENDAMENTOS (NOVO CRUD) ==================
    
    // 1. GET (Listar)
    @GetMapping("/agendamentos/listar")
    public List<Map<String, Object>> listarAgendamentos() { 
        return agendamentoRepo.findAll().stream().map(this::simplificar).collect(Collectors.toList()); 
    }
    
    // 2. POST (Criar)
    @PostMapping("/agendamentos/criar")
    public Object criarAgendamento(@RequestBody Map<String, Object> dados) {
        Agendamento a = new Agendamento();
        try {
            preencherAutomaticamente(a, dados);
            
            // Lógica Inteligente de Data: Se não vier data, coloca "Agora + 1 hora"
            try {
                Method getData = encontrarMetodoGetter(a, "Data");
                if (getData != null && getData.invoke(a) == null) {
                    Method setData = encontrarMetodoSetter(a, "Data", LocalDateTime.class);
                    if (setData != null) setData.invoke(a, LocalDateTime.now().plusHours(1));
                }
            } catch (Exception ex) {}

            return simplificar(agendamentoRepo.save(a)); 
        } catch (Exception e) { return criarErroDetalhado(e, a); }
    }

    // 3. PUT (Editar)
    @PutMapping("/agendamentos/editar/{id}")
    public Object editarAgendamento(@PathVariable Long id, @RequestBody Map<String, Object> dados) {
        try {
            Optional<Agendamento> op = agendamentoRepo.findById(id);
            if (op.isPresent()) {
                Agendamento a = op.get();
                preencherAutomaticamente(a, dados);
                return simplificar(agendamentoRepo.save(a));
            } else {
                Map<String, String> erro = new HashMap<>();
                erro.put("mensagem", "Agendamento não encontrado com ID " + id);
                return erro;
            }
        } catch (Exception e) { return criarErroDetalhado(e, new Agendamento()); }
    }

    // 4. DELETE (Excluir)
    @DeleteMapping("/agendamentos/excluir/{id}")
    public String deletarAgendamento(@PathVariable Long id) { 
        agendamentoRepo.deleteById(id); 
        return "Agendamento excluído com sucesso!"; 
    }

    // ================== OUTROS CRUDS MANTIDOS (PRONTUARIOS, ESTOQUE, ETC) ==================
    
    @GetMapping("/prontuarios/listar")
    public List<Map<String, Object>> listarProntuarios() { return prontuarioRepo.findAll().stream().map(this::simplificar).collect(Collectors.toList()); }
    @PostMapping("/prontuarios/criar")
    public Object criarProntuario(@RequestBody Map<String, Object> dados) { try { Prontuario p = new Prontuario(); preencherAutomaticamente(p, dados); return simplificar(prontuarioRepo.save(p)); } catch (Exception e) { return criarErroDetalhado(e, new Prontuario()); } }
    @PutMapping("/prontuarios/editar/{id}")
    public Object editarProntuario(@PathVariable Long id, @RequestBody Map<String, Object> dados) { Optional<Prontuario> op = prontuarioRepo.findById(id); if (op.isPresent()) { preencherAutomaticamente(op.get(), dados); return simplificar(prontuarioRepo.save(op.get())); } return null; }
    @DeleteMapping("/prontuarios/excluir/{id}")
    public String deletarProntuario(@PathVariable Long id) { prontuarioRepo.deleteById(id); return "Excluído"; }

    @GetMapping("/estoque/listar")
    public List<Map<String, Object>> listarEstoque() { return prodRepo.findAll().stream().map(this::simplificar).collect(Collectors.toList()); }
    @PostMapping("/estoque/criar")
    public Object criarProduto(@RequestBody Map<String, Object> dados) {
        Produto p = new Produto();
        try { preencherAutomaticamente(p, dados); try { if (p.getClass().getMethod("getEan").invoke(p) == null) p.getClass().getMethod("setEan", String.class).invoke(p, "GTIN-"+System.currentTimeMillis()); } catch(Exception e){} return simplificar(prodRepo.save(p)); 
        } catch (Exception e) { if(e.getMessage().toLowerCase().contains("codigo_barras")) { try { jdbcTemplate.execute("ALTER TABLE produtos MODIFY codigo_barras VARCHAR(255) NULL"); return simplificar(prodRepo.save(p)); } catch(Exception ex){} } return criarErroDetalhado(e, p); }
    }
    @DeleteMapping("/estoque/excluir/{id}")
    public String deletarProduto(@PathVariable Long id) { prodRepo.deleteById(id); return "Excluído"; }

    @GetMapping("/financeiro/listar")
    public List<Map<String, Object>> listarFin() { return finRepo.findAll().stream().map(this::simplificar).collect(Collectors.toList()); }
    @PostMapping("/financeiro/criar")
    public Object criarFin(@RequestBody Map<String, Object> dados) {
        TransacaoFinanceira f = new TransacaoFinanceira();
        try { preencherAutomaticamente(f, dados); 
              for(Method m : f.getClass().getMethods()) { if(m.getName().startsWith("set") && m.getParameterCount()==1 && m.getParameterTypes()[0].isEnum() && verificarSeNulo(f, m.getName())) setarEnumPadrao(f, m); }
              return simplificar(finRepo.save(f)); 
        } catch (Exception e) { return criarErroDetalhado(e, f); }
    }
    @DeleteMapping("/financeiro/excluir/{id}")
    public String deletarFin(@PathVariable Long id) { finRepo.deleteById(id); return "Excluído"; }

    // (Outros métodos de Ambulancia, Polo, etc. mantidos resumidos para caber, mas o Java aceita)
    @GetMapping("/ambulancias/listar") public List<Map<String, Object>> lAmb() { return ambuRepo.findAll().stream().map(this::simplificar).collect(Collectors.toList()); }
    @PostMapping("/ambulancias/criar") public Object cAmb(@RequestBody Map<String, Object> d) { Ambulancia a=new Ambulancia(); preencherAutomaticamente(a,d); if(d.get("status")==null) try{a.setStatus("DISPONIVEL");}catch(Exception e){} return simplificar(ambuRepo.save(a)); }
    @DeleteMapping("/ambulancias/excluir/{id}") public String dAmb(@PathVariable Long id) { ambuRepo.deleteById(id); return "Ok"; }
    
    // --- HELPERS ---
    private Method encontrarMetodoGetter(Object obj, String parteNome) { for(Method m : obj.getClass().getMethods()) if(m.getName().startsWith("get") && m.getName().contains(parteNome)) return m; return null; }
    private Method encontrarMetodoSetter(Object obj, String parteNome, Class<?> tipo) { for(Method m : obj.getClass().getMethods()) if(m.getName().startsWith("set") && m.getName().contains(parteNome) && m.getParameterTypes()[0] == tipo) return m; return null; }
    private boolean verificarSeNulo(Object obj, String nomeSetter) { try { String nomeGetter = "get" + nomeSetter.substring(3); Method getter = obj.getClass().getMethod(nomeGetter); return getter.invoke(obj) == null; } catch (Exception e) { return true; } }
    private void setarEnumPadrao(Object obj, Method setter) { try { Object[] enums = setter.getParameterTypes()[0].getEnumConstants(); if(enums.length > 0) setter.invoke(obj, enums[0]); } catch (Exception e) {} }
    
    private Object criarErroDetalhado(Exception e, Object entidade) {
        Map<String, String> erro = new HashMap<>();
        erro.put("status", "erro");
        erro.put("mensagem", e.getMessage());
        List<String> metodos = new ArrayList<>();
        for(Method m : entidade.getClass().getMethods()) if(m.getName().startsWith("set")) metodos.add(m.getName());
        erro.put("dica_debug_metodos_disponiveis", metodos.toString());
        return erro;
    }
}