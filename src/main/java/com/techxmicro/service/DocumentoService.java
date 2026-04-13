package com.techxmicro.service;

import java.util.List;

import com.techxmicro.entity.Documento;

public interface DocumentoService {
    void save(Documento documento);
    List<Documento> findAll();
}