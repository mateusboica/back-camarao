package back.camarao.sistema.repository;

import back.camarao.sistema.enums.StatusPedido;
import back.camarao.sistema.model.Pedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PedidoRepository extends MongoRepository<Pedido, String> {

    Page<Pedido> findByLojaId(String lojaId, Pageable pageable);

    Page<Pedido> findByStatus(StatusPedido status, Pageable pageable);

    Optional<Pedido> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);
}
