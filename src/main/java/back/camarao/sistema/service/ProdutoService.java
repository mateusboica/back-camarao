package back.camarao.sistema.service;

import back.camarao.sistema.dto.ProdutoDTO;
import back.camarao.sistema.enums.Categoria;
import back.camarao.sistema.exception.ResourceAlreadyExistsException;
import back.camarao.sistema.exception.ResourceNotFoundException;
import back.camarao.sistema.model.Produto;
import back.camarao.sistema.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    // ── Listagens ─────────────────────────────────────────────────────────────

    public Page<ProdutoDTO.Response> listarTodos(Pageable pageable) {
        log.debug("Listando todos os produtos – página {}", pageable.getPageNumber());
        return produtoRepository.findAll(pageable).map(ProdutoDTO.Response::from);
    }

    public Page<ProdutoDTO.Response> listarDisponiveis(Pageable pageable) {
        return produtoRepository.findByIsDisponivelTrue(pageable).map(ProdutoDTO.Response::from);
    }

    public Page<ProdutoDTO.Response> listarPorCategoria(Categoria categoria, boolean somenteDisponiveis,
            Pageable pageable) {
        Page<Produto> page = somenteDisponiveis
                ? produtoRepository.findByCategoriaAndIsDisponivelTrue(categoria, pageable)
                : produtoRepository.findByCategoria(categoria, pageable);
        return page.map(ProdutoDTO.Response::from);
    }

    public Page<ProdutoDTO.Response> buscarPorNome(String termo, Pageable pageable) {
        return produtoRepository.findByNomeContainingIgnoreCase(termo, pageable)
                .map(ProdutoDTO.Response::from);
    }

    public Page<ProdutoDTO.Response> listarPorTag(String tag, Pageable pageable) {
        return produtoRepository.findDisponivelByTag(tag, pageable)
                .map(ProdutoDTO.Response::from);
    }

    public ProdutoDTO.Response buscarPorSlug(String slug) {
        Produto produto = produtoRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Produto", slug));
        return ProdutoDTO.Response.from(produto);
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    public ProdutoDTO.Response buscarPorId(String id) {
        return ProdutoDTO.Response.from(encontrarOuLancar(id));
    }

    public ProdutoDTO.Response criar(ProdutoDTO.Request dto) {
        if (produtoRepository.existsByNomeIgnoreCase(dto.nome())) {
            throw new ResourceAlreadyExistsException(
                    "Já existe um produto com o nome '%s'".formatted(dto.nome()));
        }

        String slug = dto.nome()
                .toLowerCase()
                .trim()
                .replaceAll("[áàãâä]", "a")
                .replaceAll("[éèêë]", "e")
                .replaceAll("[íìîï]", "i")
                .replaceAll("[óòõôö]", "o")
                .replaceAll("[úùûü]", "u")
                .replaceAll("[ç]", "c")
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-");

        Produto produto = Produto.builder()
                .nome(dto.nome().trim())
                .slug(slug)
                .preco(dto.preco())
                .descricao(dto.descricao().trim())
                .isDisponivel(dto.isDisponivel())
                .img(dto.img())
                .categoria(dto.categoria())
                .tags(dto.tags())
                .build();

        Produto salvo = produtoRepository.save(produto);
        log.info("Produto criado: id={}, nome={}", salvo.getId(), salvo.getNome());
        return ProdutoDTO.Response.from(salvo);
    }

    public ProdutoDTO.Response atualizar(String id, ProdutoDTO.Request dto) {
        Produto existente = encontrarOuLancar(id);
        boolean nomeMudou = !existente.getNome().equalsIgnoreCase(dto.nome().trim());
        if (nomeMudou && produtoRepository.existsByNomeIgnoreCase(dto.nome())) {
            throw new ResourceAlreadyExistsException(
                    "Já existe outro produto com o nome '%s'".formatted(dto.nome()));
        }

        existente.setNome(dto.nome().trim());
        existente.setPreco(dto.preco());
        existente.setDescricao(dto.descricao().trim());
        existente.setDisponivel(dto.isDisponivel());
        existente.setImg(dto.img());
        existente.setCategoria(dto.categoria());
        existente.setTags(dto.tags());

        Produto atualizado = produtoRepository.save(existente);
        log.info("Produto atualizado: id={}", id);
        return ProdutoDTO.Response.from(atualizado);
    }

    public ProdutoDTO.Response alterarDisponibilidade(String id, boolean disponivel) {
        Produto produto = encontrarOuLancar(id);
        produto.setDisponivel(disponivel);
        return ProdutoDTO.Response.from(produtoRepository.save(produto));
    }

    public void deletar(String id) {
        encontrarOuLancar(id);
        produtoRepository.deleteById(id);
        log.info("Produto deletado: id={}", id);
    }

    public Produto encontrarOuLancar(String id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto", id));
    }
}
