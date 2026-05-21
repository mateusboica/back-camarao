package back.camarao.sistema.model;

import back.camarao.sistema.enums.StatusPedido;
import back.camarao.sistema.enums.StatusPagamento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
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

    @Field("usuario_id")
    private String usuarioId;

    @Indexed(unique = true, sparse = true)
    @Field("access_slug")
    private String accessSlug;

    @Field("nome_cliente")
    private String nomeCliente;

    @Field("telefone_cliente")
    private String telefoneCliente;

    @Field("endereco_entrega")
    private String enderecoEntrega;

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

    @Field("status_pagamento") 
    private StatusPagamento statusPagamento;

    @CreatedDate
    @Field("created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private Instant updatedAt;
}
