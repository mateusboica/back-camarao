package back.camarao.sistema.model;

import back.camarao.sistema.enums.StatusPedido;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Document(collection = "pedidos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido {

    @Id
    private String id;

    @Field("loja_id")
    private String lojaId;

    @Field("nome_cliente")
    private String nomeCliente;

    @Field("telefone_cliente")
    private String telefoneCliente;

    @Field("endereco_entrega")
    private String enderecoEntrega;

    @Field("cep_entrega")
    private String cepEntrega;

    @Field("rua_entrega")
    private String ruaEntrega;

    @Field("numero_entrega")
    private String numeroEntrega;

    @Field("bairro_entrega")
    private String bairroEntrega;

    @Field("complemento_entrega")
    private String complementoEntrega;

    @Field("referencia_entrega")
    private String referenciaEntrega;

    @Field("metodo_pagamento")
    private String metodoPagamento;

    @Field(value = "troco_para", targetType = FieldType.DECIMAL128)
    private BigDecimal trocoPara;

    @Field("observacao")
    private String observacao;

    @Field("itens")
    private List<ItemPedido> itens;

    @Field(value = "subtotal", targetType = FieldType.DECIMAL128)
    private BigDecimal subtotal;

    @Field(value = "taxa_servico", targetType = FieldType.DECIMAL128)
    private BigDecimal taxaServico;

    @Field(value = "taxa_entrega", targetType = FieldType.DECIMAL128)
    private BigDecimal taxaEntrega;

    @Field(value = "total", targetType = FieldType.DECIMAL128)
    private BigDecimal total;

    @Field("status")
    private StatusPedido status;

    @CreatedDate
    @Field("created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private Instant updatedAt;
}
