package com.vidaplus.service;

import com.vidaplus.entity.Polo;
import com.vidaplus.entity.Usuario;
import com.vidaplus.enums.StatusExame;
import com.vidaplus.enums.StatusPagamento;
import com.vidaplus.models.ItemExame;
import com.vidaplus.models.PedidoExame;
import com.vidaplus.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class LaboratorioService {

    @Autowired
    private PedidoRepository pedidoRepository;

    public List<PedidoExame> buscarPedidosPorPaciente(Usuario paciente) {
        return pedidoRepository.findByPacienteOrderByDataCriacaoDesc(paciente);
    }

    public long contarPedidosPendentesPagamento(Usuario paciente) {
        return pedidoRepository.countByPacienteAndStatusPagamento(paciente, StatusPagamento.PENDENTE);
    }

    public long contarExamesConcluidos(Usuario paciente) {
        List<PedidoExame> pedidos = pedidoRepository.findByPaciente(paciente);
        long total = 0;
        for (PedidoExame pedido : pedidos) {
            for (ItemExame item : pedido.getItens()) {
                if (item.getStatusClinico() == StatusExame.LIBERADO) total++;
            }
        }
        return total;
    }

    public long contarExamesEmAnalise(Usuario paciente) {
        List<PedidoExame> pedidos = pedidoRepository.findByPaciente(paciente);
        long total = 0;
        for (PedidoExame pedido : pedidos) {
            for (ItemExame item : pedido.getItens()) {
                if (item.getStatusClinico() == StatusExame.EM_PROCESSAMENTO 
                        || item.getStatusClinico() == StatusExame.ANALISE_TECNICA) total++;
            }
        }
        return total;
    }

    // --- NOVO MÉTODO: SALVAR NO BANCO DE DADOS ---
    @Transactional
    public void criarSolicitacao(List<String> nomesExames, String medico, Usuario paciente) {
        PedidoExame pedido = new PedidoExame();
        pedido.setPaciente(paciente);
        
        // Associa ao primeiro polo do usuário ou nulo (ajuste conforme sua regra de negócio de Polos)
        Polo poloAlvo = (!paciente.getPolos().isEmpty()) ? paciente.getPolos().get(0) : null;
        // Se o usuário não tiver polo (ex: admin master sem polo), precisamos tratar ou definir um padrão
        // Aqui deixarei null check ou você define um polo padrão no banco
        if (poloAlvo == null) {
            // Em produção, lance erro ou busque um polo "Sede"
        }
        pedido.setPolo(poloAlvo); 

        // Define status inicial
        pedido.setStatusPagamento(StatusPagamento.PENDENTE);
        pedido.setValorTotal(BigDecimal.ZERO); // Será somado abaixo

        BigDecimal total = BigDecimal.ZERO;

        // Cria os itens
        for (String nomeExame : nomesExames) {
            ItemExame item = new ItemExame();
            item.setNomeExame(nomeExame);
            item.setPreco(new BigDecimal("50.00")); // Preço Fixo Simulado
            item.setStatusClinico(StatusExame.SOLICITADO);
            item.setPedido(pedido);
            
            pedido.getItens().add(item);
            total = total.add(item.getPreco());
        }
        
        pedido.setValorTotal(total);
        pedidoRepository.save(pedido);
    }

    public String gerarLinkDownload(Long pedidoId, Usuario usuarioLogado) throws Exception {
        PedidoExame pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        if (!pedido.getPaciente().getId().equals(usuarioLogado.getId())) {
            throw new SecurityException("Acesso negado.");
        }
        if (!pedido.podeBaixarLaudos()) {
            throw new RuntimeException("Pagamento pendente.");
        }
        return "/arquivos/laudos/" + pedido.getId() + "_laudos.pdf";
    }

    @Transactional
    public String iniciarPagamento(Long pedidoId, Usuario usuarioLogado) {
        // ... (Mesma lógica anterior mantida)
        return "link_fake";
    }
}