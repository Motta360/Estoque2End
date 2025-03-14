package com.estoqueAPI.estoqueAPI.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.estoqueAPI.estoqueAPI.models.Pedido;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido,Long> {
    
}
