package back.camarao.sistema.model;

import back.camarao.sistema.enums.StatusPedido;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusHistoricoPedido {

    @Field("status")
    private StatusPedido status;

    @Field("alterado_em")
    private Instant alteradoEm;

    @Field("observacao")
    private String observacao;
}
