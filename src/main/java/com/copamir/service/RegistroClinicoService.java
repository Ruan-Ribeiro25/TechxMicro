package com.copamir.service;

import java.util.List;

import com.copamir.entity.Profissional;
import com.copamir.entity.RegistroClinico;

public interface RegistroClinicoService {

    void salvar(RegistroClinico registro);

    List<RegistroClinico> listarPorProfissional(Profissional profissional);
}