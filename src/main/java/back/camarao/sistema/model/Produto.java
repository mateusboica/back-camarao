package back.camarao.sistema.model;

import back.camarao.sistema.enums.Categoria;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Document(collection = "produtos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Produto {

    @Id
    private String id;

    @Indexed(unique = true)
    @Field("nome")
    private String nome;

    @Field("preco")
    private BigDecimal preco;

    @Field("descricao")
    private String descricao;

    @Field("is_disponivel")
    private boolean isDisponivel;

    @Field("img")
    private String img;

    @Field("categoria")
    private Categoria categoria;

    @Field("tags")
    private List<String> tags;
    
    @CreatedDate
    @Field("created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private Instant updatedAt;

    @Indexed(unique = true)
    @Field("slug")
    private String slug;
}
