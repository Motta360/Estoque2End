package com.estoqueAPI.estoqueAPI.models;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String pedido;
    private String agencia;
    private LocalDate data;
    private Boolean executado;
    private String GestorSTD;
    private String GestorResponsavel;
    private String Fornecedor;
    private String DataPrevista;
    private String email;
    private String Entrada;

    public Pedido() {
        data = LocalDate.now();
        executado = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPedido() {
        return pedido;
    }

    public void setPedido(String pedido) {
        this.pedido = pedido;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    
    public String getAgencia() {
        return agencia;
    }

    public void setAgencia(String agencia) {
        this.agencia = agencia;
    }

    
    public Boolean getExecutado() {
        return executado;
    }

    public void setExecutado(Boolean executado) {
        this.executado = executado;
    }

    public String getGestorSTD() {
        return GestorSTD;
    }

    public void setGestorSTD(String gestorSTD) {
        GestorSTD = gestorSTD;
    }

    public String getGestorResponsavel() {
        return GestorResponsavel;
    }

    public void setGestorResponsavel(String gestorResponsavel) {
        GestorResponsavel = gestorResponsavel;
    }

    public String getFornecedor() {
        return Fornecedor;
    }

    public void setFornecedor(String fornecedor) {
        Fornecedor = fornecedor;
    }

    public String getDataPrevista() {
        return DataPrevista;
    }

    public void setDataPrevista(String dataPrevista) {
        DataPrevista = dataPrevista;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEntrada() {
        return Entrada;
    }

    public void setEntrada(String entrada) {
        Entrada = entrada;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((pedido == null) ? 0 : pedido.hashCode());
        result = prime * result + ((data == null) ? 0 : data.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Pedido other = (Pedido) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (pedido == null) {
            if (other.pedido != null)
                return false;
        } else if (!pedido.equals(other.pedido))
            return false;
        if (data == null) {
            if (other.data != null)
                return false;
        } else if (!data.equals(other.data))
            return false;
        return true;
    }

   
}
