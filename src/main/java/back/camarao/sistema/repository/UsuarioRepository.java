package back.camarao.sistema.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import back.camarao.sistema.model.Usuario;

public interface UsuarioRepository extends MongoRepository<Usuario, String> {

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByEmailIgnoreCase(String email);

    Optional<Usuario> findByNomeIgnoreCase(String nome);

    Optional<Usuario> findByEmailIgnoreCase(String email);
}
