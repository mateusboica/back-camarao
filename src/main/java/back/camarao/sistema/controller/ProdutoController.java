package back.camarao.sistema.controller;

import back.camarao.sistema.dto.ProdutoDTO;
import back.camarao.sistema.enums.Categoria;
import back.camarao.sistema.service.ProdutoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;


/**
 * Endpoints REST para gerenciamento do cardápio.
 * <p>
 * Base path: {@code /api/v1/produtos}
 */
@RestController
@RequestMapping({"/api/v1/produtos", "/v1/produtos"})
@RequiredArgsConstructor
@Tag(name = "Produtos", description = "Gerenciamento do cardápio – Maré Artisanal")
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    // ── GET /api/v1/produtos ──────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @Operation(summary = "Lista todos os produtos", description = "Retorna todos os produtos com paginação. " +
            "Use ?disponivel=true para filtrar apenas os disponíveis.")
    public ResponseEntity<Page<ProdutoDTO.Response>> listar(
            @RequestParam(required = false) Boolean disponivel,
            @PageableDefault(size = 12, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<ProdutoDTO.Response> page = Boolean.TRUE.equals(disponivel)
                ? produtoService.listarDisponiveis(pageable)
                : produtoService.listarTodos(pageable);
        return ResponseEntity.ok(page);
    }

    // ── GET /api/v1/produtos/{id} ─────────────────────────────────────────────

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping("/{id}")
    @Operation(summary = "Busca produto por ID")
    @ApiResponse(responseCode = "200", description = "Produto encontrado")
    @ApiResponse(responseCode = "404", description = "Produto não encontrado", content = @Content(schema = @Schema(implementation = Void.class)))
    public ResponseEntity<ProdutoDTO.Response> buscarPorId(@PathVariable String id) {
        return ResponseEntity.ok(produtoService.buscarPorId(id));
    }

    // ── GET /api/v1/produtos/{slug}

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping("/slug/{slug}/")
    public ResponseEntity<ProdutoDTO.Response> buscarPorSlug(@PathVariable String slug) {
        return ResponseEntity.ok(produtoService.buscarPorSlug(slug));
    }

    // ── GET /api/v1/produtos/categoria/{categoria} ────────────────────────────

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping("/categoria/{categoria}")
    @Operation(summary = "Lista produtos por categoria")
    public ResponseEntity<Page<ProdutoDTO.Response>> listarPorCategoria(
            @PathVariable Categoria categoria,
            @RequestParam(defaultValue = "false") boolean somenteDisponiveis,
            @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(
                produtoService.listarPorCategoria(categoria, somenteDisponiveis, pageable));
    }

    // ── GET /api/v1/produtos/busca?termo= ────────────────────────────────────

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping("/busca")
    @Operation(summary = "Busca produtos por nome")
    public ResponseEntity<Page<ProdutoDTO.Response>> buscarPorNome(
            @Parameter(description = "Termo de busca") @RequestParam String termo,
            @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(produtoService.buscarPorNome(termo, pageable));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    // ── GET /api/v1/produtos/tag/{tag} ────────────────────────────────────────

    @GetMapping("/tag/{tag}")
    @Operation(summary = "Lista produtos disponíveis por tag (ex: 'vegano', 'sem_gluten')")
    public ResponseEntity<Page<ProdutoDTO.Response>> listarPorTag(@PathVariable String tag, @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(produtoService.listarPorTag(tag, pageable));
    }

    // ── POST /api/v1/produtos ─────────────────────────────────────────────────
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Cadastra um novo produto")
    @ApiResponse(responseCode = "201", description = "Produto criado com sucesso")
    @ApiResponse(responseCode = "409", description = "Produto com este nome já existe")
    @ApiResponse(responseCode = "422", description = "Dados inválidos")
    public ResponseEntity<ProdutoDTO.Response> criar(@Valid @RequestBody ProdutoDTO.Request dto) {
        ProdutoDTO.Response criado = produtoService.criar(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.id())
                .toUri();
        return ResponseEntity.created(location).body(criado);
    }

    // ── PUT /api/v1/produtos/{id} ─────────────────────────────────────────────
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um produto completo (substituição total)")
    public ResponseEntity<ProdutoDTO.Response> atualizar(
            @PathVariable String id,
            @Valid @RequestBody ProdutoDTO.Request dto) {
        return ResponseEntity.ok(produtoService.atualizar(id, dto));
    }

    // ── PATCH /api/v1/produtos/{id}/disponibilidade ───────────────────────────
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/disponibilidade")
    @Operation(summary = "Altera apenas a disponibilidade do produto (liga/desliga no cardápio)")
    public ResponseEntity<ProdutoDTO.Response> alterarDisponibilidade(
            @PathVariable String id,
            @Valid @RequestBody ProdutoDTO.PatchDisponibilidade dto) {
        return ResponseEntity.ok(produtoService.alterarDisponibilidade(id, dto.isDisponivel()));
    }

    // ── DELETE /api/v1/produtos/{id} ──────────────────────────────────────────
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um produto do cardápio")
    @ApiResponse(responseCode = "204", description = "Produto removido")
    public ResponseEntity<Void> deletar(@PathVariable String id) {
        produtoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
