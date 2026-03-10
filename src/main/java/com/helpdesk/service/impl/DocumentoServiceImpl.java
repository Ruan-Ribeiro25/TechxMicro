package com.helpdesk.service.impl;

import com.helpdesk.entity.Documento;
import com.helpdesk.repository.DocumentoRepository;
import com.helpdesk.service.DocumentoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DocumentoServiceImpl implements DocumentoService {

    @Autowired
    private DocumentoRepository documentoRepository; // <--- Injeção do Repository

    @Override
    public void save(Documento documento) {
        documentoRepository.save(documento);
    }

    @Override
    public List<Documento> findAll() {
        return documentoRepository.findAll();
    }
}