package com.helpdesk.service;

import java.util.List;

import com.helpdesk.entity.Profissional;
import com.helpdesk.entity.RegistroClinico;

public interface ProfissionalService {

    Profissional findById(Long id);

    Profissional buscarPorLogin(String login);

    List<RegistroClinico> listarRegistrosDoMes(String login);
}