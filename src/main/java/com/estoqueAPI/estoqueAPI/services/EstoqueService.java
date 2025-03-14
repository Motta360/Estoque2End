package com.estoqueAPI.estoqueAPI.services;


import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.estoqueAPI.estoqueAPI.models.Estoque;
import com.estoqueAPI.estoqueAPI.repositories.EstoqueRepository;

@Service
public class EstoqueService {

    @Autowired
    EstoqueRepository estoqueRepository;

    public Estoque getEstoque(Long id){
        Optional<Estoque> estoque =  estoqueRepository.findById(id);
        if (estoque.isPresent()) {
            return estoque.get();
        }else{
            throw new RuntimeException("Não foi encontrado Estoque");
        }
    }
    
}
