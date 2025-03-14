package com.estoqueAPI.estoqueAPI.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.estoqueAPI.estoqueAPI.models.Estoque;
@Repository
public interface EstoqueRepository  extends JpaRepository<Estoque,Long>{
    
}
