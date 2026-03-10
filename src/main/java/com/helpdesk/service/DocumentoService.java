package com.helpdesk.service;

import java.util.List;

import com.helpdesk.entity.Documento;

public interface DocumentoService {
    void save(Documento documento);
    List<Documento> findAll();
}