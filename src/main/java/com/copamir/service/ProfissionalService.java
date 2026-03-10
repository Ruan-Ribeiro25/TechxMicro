package com.copamir.service;

import java.util.List;

import com.copamir.entity.Profissional;
import com.copamir.entity.RegistroClinico;

public interface ProfissionalService {

    Profissional findById(Long id);

    Profissional buscarPorLogin(String login);

    List<RegistroClinico> listarRegistrosDoMes(String login);
}