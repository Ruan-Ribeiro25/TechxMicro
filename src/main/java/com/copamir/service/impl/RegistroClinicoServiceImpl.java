package com.copamir.service.impl;

import com.copamir.entity.Profissional;
import com.copamir.entity.RegistroClinico;
import com.copamir.repository.RegistroClinicoRepository;
import com.copamir.service.RegistroClinicoService;

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