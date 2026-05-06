package back.camarao.sistema.controller;

import back.camarao.sistema.dto.LojaDTO;
import back.camarao.sistema.service.LojaService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping({"/api/v1/lojas", "/v1/lojas"})
@RequiredArgsConstructor
@Tag(name = "Lojas", description = "Gerenciamento das lojas")
public class LojaController {

    private final LojaService lojaService;

    @GetMapping
    @Operation(summary = "Lista todas as lojas")
    public ResponseEntity<Page<LojaDTO.LojaResponse>> listar(
            @PageableDefault(size = 12, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(lojaService.listarTodos(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca loja por ID")
    @ApiResponse(responseCode = "200", description = "Loja encontrada")
    @ApiResponse(responseCode = "404", description = "Loja nao encontrada")
    public ResponseEntity<LojaDTO.LojaResponse> buscarPorId(@PathVariable String id) {
        return ResponseEntity.ok(lojaService.buscarPorId(id));
    }

    @GetMapping("/nome/{nome}")
    @Operation(summary = "Busca loja por nome")
    public ResponseEntity<LojaDTO.LojaResponse> buscarPorNome(@PathVariable String nome) {
        return ResponseEntity.ok(lojaService.buscarPorNome(nome));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Cadastra uma nova loja")
    @ApiResponse(responseCode = "201", description = "Loja criada com sucesso")
    @ApiResponse(responseCode = "409", description = "Loja com este nome ja existe")
    @ApiResponse(responseCode = "422", description = "Dados invalidos")
    public ResponseEntity<LojaDTO.LojaResponse> criar(@Valid @RequestBody LojaDTO.Request dto) {
        LojaDTO.LojaResponse criada = lojaService.criar(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criada.id())
                .toUri();
        return ResponseEntity.created(location).body(criada);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma loja completa")
    public ResponseEntity<LojaDTO.LojaResponse> atualizar(
            @PathVariable String id,
            @Valid @RequestBody LojaDTO.Request dto) {
        return ResponseEntity.ok(lojaService.atualizar(id, dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    @Operation(summary = "Altera apenas o status aberto/fechado da loja")
    public ResponseEntity<LojaDTO.LojaResponse> alterarStatus(
            @PathVariable String id,
            @Valid @RequestBody LojaDTO.PatchStatus dto) {
        return ResponseEntity.ok(lojaService.alterarStatus(id, dto.aberto()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Remove uma loja")
    @ApiResponse(responseCode = "204", description = "Loja removida")
    public ResponseEntity<Void> deletar(@PathVariable String id) {
        lojaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
