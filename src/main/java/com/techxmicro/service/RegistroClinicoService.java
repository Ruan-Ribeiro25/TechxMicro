package com.techxmicro.service;

import java.util.List;

import com.techxmicro.entity.Profissional;
import com.techxmicro.entity.RegistroClinico;

public interface RegistroClinicoService {

    void salvar(RegistroClinico registro);

    List<RegistroClinico> listarPorProfissional(Profissional profissional);
}