package back.camarao.sistema.controller;

import back.camarao.sistema.dto.ProdutoDTO;
import back.camarao.sistema.enums.Categoria;
import back.camarao.sistema.service.ProdutoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping({"/api/v1/produtos", "/v1/produtos"})
@RequiredArgsConstructor
@Tag(name = "Produtos", description = "Gerenciamento do cardapio")
public class ProdutoController {

    private final ProdutoService produtoService;

    @GetMapping
    @Operation(summary = "Lista produtos")
    public ResponseEntity<Page<ProdutoDTO.Response>> listar(
            @RequestParam(required = false) Boolean disponivel,
            @PageableDefault(size = 12, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<ProdutoDTO.Response> page = Boolean.TRUE.equals(disponivel)
                ? produtoService.listarDisponiveis(pageable)
                : produtoService.listarTodos(pageable);

        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca produto por ID")
    public ResponseEntity<ProdutoDTO.Response> buscarPorId(@PathVariable String id) {
        return ResponseEntity.ok(produtoService.buscarPorId(id));
    }

    @GetMapping({"/slug/{slug}", "/slug/{slug}/"})
    @Operation(summary = "Busca produto por slug")
    public ResponseEntity<ProdutoDTO.Response> buscarPorSlug(@PathVariable String slug) {
        return ResponseEntity.ok(produtoService.buscarPorSlug(slug));
    }

    @GetMapping("/categoria/{categoria}")
    @Operation(summary = "Lista produtos por categoria")
    public ResponseEntity<Page<ProdutoDTO.Response>> listarPorCategoria(
            @PathVariable Categoria categoria,
            @RequestParam(defaultValue = "false") boolean somenteDisponiveis,
            @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(
                produtoService.listarPorCategoria(categoria, somenteDisponiveis, pageable));
    }

    @GetMapping("/busca")
    @Operation(summary = "Busca produtos por nome")
    public ResponseEntity<Page<ProdutoDTO.Response>> buscarPorNome(
            @Parameter(description = "Termo de busca") @RequestParam String termo,
            @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(produtoService.buscarPorNome(termo, pageable));
    }

    @GetMapping("/tag/{tag}")
    @Operation(summary = "Lista produtos disponiveis por tag")
    public ResponseEntity<Page<ProdutoDTO.Response>> listarPorTag(
            @PathVariable String tag,
            @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(produtoService.listarPorTag(tag, pageable));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Cadastra um novo produto")
    @ApiResponse(responseCode = "201", description = "Produto criado com sucesso")
    public ResponseEntity<ProdutoDTO.Response> criar(@Valid @RequestBody ProdutoDTO.Request dto) {
        ProdutoDTO.Response criado = produtoService.criar(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.id())
                .toUri();

        return ResponseEntity.created(location).body(criado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um produto completo")
    public ResponseEntity<ProdutoDTO.Response> atualizar(
            @PathVariable String id,
            @Valid @RequestBody ProdutoDTO.Request dto) {
        return ResponseEntity.ok(produtoService.atualizar(id, dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/disponibilidade")
    @Operation(summary = "Altera a disponibilidade do produto")
    public ResponseEntity<ProdutoDTO.Response> alterarDisponibilidade(
            @PathVariable String id,
            @Valid @RequestBody ProdutoDTO.PatchDisponibilidade dto) {
        return ResponseEntity.ok(produtoService.alterarDisponibilidade(id, dto.isDisponivel()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um produto")
    public ResponseEntity<Void> deletar(@PathVariable String id) {
        produtoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
