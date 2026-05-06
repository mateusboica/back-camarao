package back.camarao.sistema.repository;

import back.camarao.sistema.model.Loja;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LojaRepository extends MongoRepository<Loja, String> {

    boolean existsByNomeIgnoreCase(String nome);

    Optional<Loja> findByNomeIgnoreCase(String nome);
}
