package back.camarao.sistema.repository;

import back.camarao.sistema.enums.Categoria;
import back.camarao.sistema.model.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends MongoRepository<Produto, String> {

    Page<Produto> findByIsDisponivelTrue(Pageable pageable);

    Page<Produto> findByCategoria(Categoria categoria, Pageable pageable);

    Page<Produto> findByCategoriaAndIsDisponivelTrue(Categoria categoria, Pageable pageable);

    @Query("{ 'nome': { $regex: ?0, $options: 'i' } }")
    Page<Produto> findByNomeContainingIgnoreCase(String termo, Pageable pageable);

    boolean existsByNomeIgnoreCase(String nome);

    Optional<Produto> findByNomeIgnoreCase(String nome);

    @Query("{ 'tags': ?0, 'is_disponivel': true }")
    Page<Produto> findDisponivelByTag(String tag, Pageable pageable);

    Optional<Produto> findBySlug(String slug);
}
