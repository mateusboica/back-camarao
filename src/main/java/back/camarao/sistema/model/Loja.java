package back.camarao.sistema.model;

import back.camarao.sistema.features.HorarioFuncionamento;
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

@Document(collection = "lojas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loja {

    @Id
    private String id;

    @Indexed(unique = true)
    @Field("nome")
    private String nome;

    @Field("descricao")
    private String descricao;

    @Field("endereco")
    private String endereco;

    @Field("telefone")
    private String telefone;

    @Field("aberto")
    private Boolean aberto;

    @Field("logo_url")
    private String logoUrl;

    @Field(value = "taxa_servico", targetType = FieldType.DECIMAL128)
    private BigDecimal taxaServico;

    @Field(value = "taxa_entrega", targetType = FieldType.DECIMAL128)
    private BigDecimal taxaEntrega;

    @Field("horario_funcionamento")
    private List<HorarioFuncionamento> horarioFuncionamento;

    @CreatedDate
    @Field("created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private Instant updatedAt;
}
