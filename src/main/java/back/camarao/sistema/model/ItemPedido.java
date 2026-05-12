package back.camarao.sistema.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemPedido {

    @Field("produto_id")
    private String produtoId;

    @Field("nome_produto")
    private String nomeProduto;

    @Field(value = "preco_unitario", targetType = FieldType.DECIMAL128)
    private BigDecimal precoUnitario;

    @Field("quantidade")
    private Integer quantidade;

    @Field(value = "subtotal", targetType = FieldType.DECIMAL128)
    private BigDecimal subtotal;
}
