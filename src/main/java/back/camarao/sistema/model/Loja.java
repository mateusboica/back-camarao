package back.camarao.sistema.model;

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

import java.time.Instant;

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

    @Field("taxa_servico")
    private String taxaServico;

    @Field("taxa_entrega")
    private String taxaEntrega;

    @Field("horario_funcionamento")
    private String horarioFuncionamento;

    @CreatedDate
    @Field("created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private Instant updatedAt;
}
