package com.vidaplus.service;

import com.vidaplus.entity.RegistroClinico;
import com.vidaplus.entity.Profissional;

import java.util.List;

public interface RegistroClinicoService {

    void salvar(RegistroClinico registro);

    List<RegistroClinico> listarPorProfissional(Profissional profissional);
}