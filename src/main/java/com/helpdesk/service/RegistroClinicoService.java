package com.helpdesk.service;

import java.util.List;

import com.helpdesk.entity.Profissional;
import com.helpdesk.entity.RegistroClinico;

public interface RegistroClinicoService {

    void salvar(RegistroClinico registro);

    List<RegistroClinico> listarPorProfissional(Profissional profissional);
}