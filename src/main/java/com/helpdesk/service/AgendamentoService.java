package com.helpdesk.service;

import java.util.List;

import com.helpdesk.entity.Agendamento;

public interface AgendamentoService {
    // Lista todos os agendamentos (usado no /lista)
    List<Agendamento> findAll();

    // Salva ou atualiza um agendamento (usado no /salvar)
    void save(Agendamento agendamento);

    // O MÉTODO QUE FALTAVA (usado na linha 40 do Controller)
    void deleteById(Long id);
}