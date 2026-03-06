package com.vidaplus.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vidaplus.entity.Profissional;
import com.vidaplus.entity.RegistroClinico; // Certifique-se que esta classe existe
import com.vidaplus.repository.ProfissionalRepository;
import com.vidaplus.repository.RegistroClinicoRepository; // Certifique-se que esta classe existe
import com.vidaplus.service.ProfissionalService;

@Service
public class ProfissionalServiceImpl implements ProfissionalService {

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private RegistroClinicoRepository registroClinicoRepository;

    @Override
    public Profissional findById(Long id) {
        return profissionalRepository.findById(id).orElse(null);
    }

    @Override
    public Profissional buscarPorLogin(String login) {
        // CORREÇÃO: Usando o método correto com underscore definido no Repository
        return profissionalRepository.findByUsuario_Username(login);
    }

    @Override
    public List<RegistroClinico> listarRegistrosDoMes(String login) {

        // CORREÇÃO: Chamada corrigida aqui também
        Profissional profissional = profissionalRepository.findByUsuario_Username(login);

        if (profissional == null) {
            return List.of();
        }

        LocalDateTime inicio = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime fim = LocalDate.now().plusDays(1).atStartOfDay().minusSeconds(1);

        return registroClinicoRepository.findByProfissionalAndDataHoraBetween(
                profissional,
                inicio,
                fim
        );
    }
}