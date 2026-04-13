package com.techxmicro.service;

import java.util.List;

import com.techxmicro.entity.Profissional;
import com.techxmicro.entity.RegistroClinico;

public interface ProfissionalService {

    Profissional findById(Long id);

    Profissional buscarPorLogin(String login);

    List<RegistroClinico> listarRegistrosDoMes(String login);
}