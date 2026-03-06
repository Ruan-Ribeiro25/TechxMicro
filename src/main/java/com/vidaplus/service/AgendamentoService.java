package com.vidaplus.service;

import com.vidaplus.entity.Agendamento;
import java.util.List;

public interface AgendamentoService {
    // Lista todos os agendamentos (usado no /lista)
    List<Agendamento> findAll();

    // Salva ou atualiza um agendamento (usado no /salvar)
    void save(Agendamento agendamento);

    // O MÉTODO QUE FALTAVA (usado na linha 40 do Controller)
    void deleteById(Long id);
}