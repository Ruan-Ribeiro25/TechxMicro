package com.copamir.service;

import java.util.List;

import com.copamir.entity.Documento;

public interface DocumentoService {
    void save(Documento documento);
    List<Documento> findAll();
}