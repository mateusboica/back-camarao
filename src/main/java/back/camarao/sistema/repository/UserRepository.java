package back.camarao.sistema.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import back.camarao.sistema.model.User;

public interface UserRepository extends MongoRepository<User, String> {

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByEmailIgnoreCase(String email);

    Optional<User> findByNomeIgnoreCase(String nome);

    Optional<User> findByEmailIgnoreCase(String email);
}
