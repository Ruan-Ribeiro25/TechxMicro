package com.helpdesk.service.impl;

import com.helpdesk.entity.Agendamento;
import com.helpdesk.repository.AgendamentoRepository;
import com.helpdesk.service.AgendamentoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgendamentoServiceImpl implements AgendamentoService {

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Override
    public List<Agendamento> findAll() {
        return agendamentoRepository.findAll();
    }

    @Override
    public void save(Agendamento agendamento) {
        // Lógica simples de salvar
        agendamentoRepository.save(agendamento);
    }

    @Override
    public void deleteById(Long id) {
        // A implementação real que remove do banco
        agendamentoRepository.deleteById(id);
    }
}