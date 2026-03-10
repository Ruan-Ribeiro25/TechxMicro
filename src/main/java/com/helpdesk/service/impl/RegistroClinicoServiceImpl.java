package com.helpdesk.service.impl;

import com.helpdesk.entity.Profissional;
import com.helpdesk.entity.RegistroClinico;
import com.helpdesk.repository.RegistroClinicoRepository;
import com.helpdesk.service.RegistroClinicoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RegistroClinicoServiceImpl implements RegistroClinicoService {

    @Autowired
    private RegistroClinicoRepository repository;

    @Override
    public void salvar(RegistroClinico registro) {
        repository.save(registro);
    }

    @Override
    public List<RegistroClinico> listarPorProfissional(Profissional profissional) {
        return repository.findByProfissional(profissional);
    }
}