package com.techxmicro.service.impl;

import com.techxmicro.entity.Profissional;
import com.techxmicro.entity.RegistroClinico;
import com.techxmicro.repository.RegistroClinicoRepository;
import com.techxmicro.service.RegistroClinicoService;

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