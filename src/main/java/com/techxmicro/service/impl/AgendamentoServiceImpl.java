package com.techxmicro.service.impl;

import com.techxmicro.entity.Agendamento;
import com.techxmicro.repository.AgendamentoRepository;
import com.techxmicro.service.AgendamentoService;

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