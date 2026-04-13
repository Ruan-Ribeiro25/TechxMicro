package com.techxmicro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.techxmicro.entity.Produto;

import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    // Método existente para o Scanner
    Optional<Produto> findByEan(String ean);

    // --- CORREÇÃO DO ERRO ---
    // Cria a consulta SQL personalizada para contar itens onde a quantidade é menor ou igual ao mínimo
    @Query("SELECT COUNT(p) FROM Produto p WHERE p.quantidade <= p.quantidadeMinima")
    long countProdutosBaixoEstoque();
    
    // Opcional: Se precisar contar os críticos (zerados) também
    @Query("SELECT COUNT(p) FROM Produto p WHERE p.quantidade <= 0")
    long countProdutosCriticos();
}