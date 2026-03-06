package com.vidaplus.service.impl;

import com.vidaplus.entity.RegistroClinico;
import com.vidaplus.entity.Profissional;
import com.vidaplus.repository.RegistroClinicoRepository;
import com.vidaplus.service.RegistroClinicoService;
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