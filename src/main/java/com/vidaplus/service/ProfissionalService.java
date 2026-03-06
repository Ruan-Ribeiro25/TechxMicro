package com.vidaplus.service;

import java.util.List;

import com.vidaplus.entity.Profissional;
import com.vidaplus.entity.RegistroClinico;

public interface ProfissionalService {

    Profissional findById(Long id);

    Profissional buscarPorLogin(String login);

    List<RegistroClinico> listarRegistrosDoMes(String login);
}