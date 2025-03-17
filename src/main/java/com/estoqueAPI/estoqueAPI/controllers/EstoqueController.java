package com.estoqueAPI.estoqueAPI.controllers;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.estoqueAPI.estoqueAPI.models.Estoque;
import com.estoqueAPI.estoqueAPI.models.Movel;
import com.estoqueAPI.estoqueAPI.models.Pedido;
import com.estoqueAPI.estoqueAPI.repositories.EstoqueRepository;
import com.estoqueAPI.estoqueAPI.repositories.MovelRepository;
import com.estoqueAPI.estoqueAPI.repositories.PedidoRepository;
import com.estoqueAPI.estoqueAPI.services.EstoqueService;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@RestController
@CrossOrigin(origins = "*")
public class EstoqueController {

    @Autowired
    private EstoqueService estoqueService;

    @Autowired
    private EstoqueRepository estoqueRepository;

    @Autowired
    private MovelRepository movelRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private S3Client s3Client;

    @Value("${aws.bucketName}")
    private String bucketName;

    @GetMapping("/{id}/moveis")
    public ResponseEntity<List<Movel>> getMoveis(@PathVariable Long id) {
        try {
            Estoque estoque = estoqueService.getEstoque(id);
            Collections.sort(estoque.getMoveis());
            return ResponseEntity.ok(estoque.getMoveis());
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Estoque não encontrado", e);
        }
    }

    @GetMapping("/{id}/{movel}")
    public ResponseEntity<Movel> getMovel(@PathVariable Long id, @PathVariable String movel) {
        try {
            Estoque estoque = estoqueService.getEstoque(id);
            for (Movel movel_ : estoque.getMoveis()) {
                if (movel_.getName().equalsIgnoreCase(movel)) {
                    return ResponseEntity.ok(movel_);
                }
            }
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Móvel específico não encontrado");
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Movel especifico não encontrado", e);
        }
    }

    @DeleteMapping("/{id}/delete/{movel}")
    public ResponseEntity<Movel> deleteMovel(@PathVariable Long id, @PathVariable String movel) {
        Estoque estoque = estoqueService.getEstoque(id);

        for (Movel movel_ : estoque.getMoveis()) {
            if (movel_.getName().equalsIgnoreCase(movel)) {
                String imgUrl = movel_.getImgUrl();
                if (imgUrl != null && !imgUrl.isEmpty()) {
                    try {
                        String fileName = imgUrl.substring(imgUrl.lastIndexOf('/') + 1);

                        s3Client.deleteObject(DeleteObjectRequest.builder()
                                .bucket(bucketName)
                                .key(fileName)
                                .build());

                        System.out.println("Imagem deletada com sucesso do S3: " + fileName);
                    } catch (S3Exception e) {
                        System.err.println("Erro ao deletar a imagem do S3: " + e.awsErrorDetails().errorMessage());
                        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao deletar a imagem do S3");
                    }
                }

                estoque.getMoveis().remove(movel_);
                movelRepository.deleteById(movel_.getId());
                estoqueRepository.save(estoque);

                return ResponseEntity.ok(movel_);
            }
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Móvel específico não encontrado");
    }

    @PostMapping("/{id}/moveis/add")
    public ResponseEntity<Movel> criarMovel(@RequestBody Movel movel, @PathVariable Long id) {
        Movel novoMovel = movelRepository.save(movel);
        Estoque e1 = estoqueRepository.findById(id).get();
        e1.getMoveis().add(novoMovel);
        estoqueRepository.save(e1);
        return ResponseEntity.ok(novoMovel);
    }

    @PostMapping("/pedidos")
    public ResponseEntity<Pedido> criarPedido(@RequestBody Pedido pedido) {
        Pedido novoPedido = pedidoRepository.save(pedido);
        mandadorDeEmail(novoPedido);
        return ResponseEntity.ok(novoPedido);
    }

    @GetMapping("/pedidos")
    public ResponseEntity<List<Pedido>> getPedidos() {
        List<Pedido> enviar = pedidoRepository.findAll();
        Collections.reverse(enviar);
        return ResponseEntity.ok(enviar);
    }

    @GetMapping("/pedidos/{id}")
    public ResponseEntity<Pedido> getPedidoPorId(@PathVariable Long id) {
        return pedidoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado"));
    }

    @PostMapping("/pedidos/{id}/executar")
    public ResponseEntity<Map<String, String>> executarPedido(@PathVariable Long id, @RequestBody List<Movel> itens) {
        try {
            Pedido pedido = pedidoRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado"));

            for (Movel item : itens) {
                Optional<Movel> optionalMovel = movelRepository.findByName(item.getName());
                if (optionalMovel.isPresent()) {
                    Movel movel = optionalMovel.get();
                    System.out.println("Movel encontrado: " + movel);

                    if (pedido.getEntrada().equalsIgnoreCase("Saida")) {
                        if (movel.getQuantidade() >= item.getQuantidade()) {
                            movel.setQuantidade(movel.getQuantidade() - item.getQuantidade());
                            movelRepository.save(movel);
                        } else {
                            Map<String, String> errorResponse = new HashMap<>();
                            errorResponse.put("error", "Estoque insuficiente para o item: " + item.getName());
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
                        }
                    } else {
                        movel.setQuantidade(movel.getQuantidade() + item.getQuantidade());
                        movelRepository.save(movel);
                    }
                } else {
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("error", "Item não encontrado: " + item.getName());
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
                }
            }

            pedido.setExecutado(true);
            pedidoRepository.save(pedido);

            Map<String, String> successResponse = new HashMap<>();
            successResponse.put("message", "Pedido executado com sucesso!");
            return ResponseEntity.ok(successResponse);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erro ao executar pedido: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    public void mandadorDeEmail(Pedido pedido) {
        String mensagem = "Olá, \n Foi feito um novo pedido para o estoque, \n Agencia: " + pedido.getAgencia()
                + "\n Gestor: " + pedido.getGestorSTD() + "\n\n Atenciosamente, \n\n Lucas Motta Teixeira\n" +
                " Departamento de Projetos\n" +
                "www.foxengenharia.com.br\n" +
                "+55 (11) 3791 8747\n" +
                "lucas.teixeira@foxengenharia.com.br\n" +
                "Rua Amália de Noronha, 151, Sala 404 Villa Offices,\n" +
                "Pinheiros, CEP 05410-010\n" +
                "São Paulo/SP\n";

        String host = "smtp.zoho.com";
        String port = "587";
        String username = "PedidosEstoqueFox@zohomail.com";
        String password = "if707070=false70denovo";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("lara.gaia@foxengenharia.com.br"));
            message.setSubject("Novo Pedido foi feito ao estoque");
            message.setText(mensagem);
            Transport.send(message);

            System.out.println("E-mail enviado com sucesso!");
        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("Erro ao enviar o e-mail: " + e.getMessage());
        }
    }
}