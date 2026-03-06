package com.vidaplus.service.impl;

import com.vidaplus.entity.Documento;
import com.vidaplus.repository.DocumentoRepository; // <--- Mudamos de DAO para Repository
import com.vidaplus.service.DocumentoService;
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