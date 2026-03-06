package com.vidaplus.service;

import com.vidaplus.entity.Documento;
import java.util.List;

public interface DocumentoService {
    void save(Documento documento);
    List<Documento> findAll();
}