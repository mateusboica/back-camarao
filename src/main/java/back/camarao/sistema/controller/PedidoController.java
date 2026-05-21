package back.camarao.sistema.controller;

import back.camarao.sistema.dto.PedidoDTO;
import back.camarao.sistema.enums.StatusPedido;
import back.camarao.sistema.security.AuthenticatedUser;
import back.camarao.sistema.service.PedidoService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping({"/api/v1/pedidos", "/v1/pedidos"})
@RequiredArgsConstructor
@Tag(name = "Pedidos", description = "Gerenciamento de pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    @Operation(summary = "Lista pedidos")
    public ResponseEntity<Page<PedidoDTO.Response>> listar(
            @RequestParam(required = false) String lojaId,
            @RequestParam(required = false) StatusPedido status,
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        if (lojaId != null && !lojaId.isBlank()) {
            return ResponseEntity.ok(pedidoService.listarPorLoja(lojaId, pageable));
        }
        if (status != null) {
            return ResponseEntity.ok(pedidoService.listarPorStatus(status, pageable));
        }
        return ResponseEntity.ok(pedidoService.listarTodos(pageable));
    }

<<<<<<< HEAD
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/minha-conta")
    @Operation(summary = "Lista pedidos da conta autenticada")
    public ResponseEntity<Page<PedidoDTO.Response>> listarMinhaConta(
            @AuthenticationPrincipal AuthenticatedUser usuario,
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(pedidoService.listarPorUsuario(usuario.getUser().getId(), pageable));
    }

    @GetMapping("/publico/{accessSlug}")
    @Operation(summary = "Busca pedido pelo identificador publico")
    public ResponseEntity<PedidoDTO.Response> buscarPorSlugPublico(@PathVariable String accessSlug) {
        return ResponseEntity.ok(pedidoService.buscarPorSlugPublico(accessSlug));
    }

    @GetMapping("/publico/local/{id}")
    @Operation(summary = "Busca pedido sem login usando dados salvos no navegador")
    public ResponseEntity<PedidoDTO.Response> buscarPorDadosLocais(
            @PathVariable String id,
            @RequestParam String telefone) {
        return ResponseEntity.ok(pedidoService.buscarPorIdETelefonePublico(id, telefone));
=======
    @GetMapping("/cep/{cep}")
    @Operation(summary = "Busca endereco de entrega por CEP")
    @ApiResponse(responseCode = "200", description = "Endereco encontrado")
    @ApiResponse(responseCode = "409", description = "CEP invalido ou nao encontrado")
    public ResponseEntity<PedidoDTO.CepResponse> buscarEnderecoPorCep(@PathVariable String cep) {
        return ResponseEntity.ok(pedidoService.buscarEnderecoPorCep(cep));
    }

    @GetMapping("/frete")
    @Operation(summary = "Calcula o frete por CEP")
    @ApiResponse(responseCode = "200", description = "Frete calculado")
    @ApiResponse(responseCode = "404", description = "Loja nao encontrada")
    @ApiResponse(responseCode = "409", description = "CEP invalido ou frete indisponivel")
    public ResponseEntity<PedidoDTO.FreteResponse> calcularFrete(
            @RequestParam String lojaId,
            @RequestParam String cep) {
        return ResponseEntity.ok(pedidoService.calcularFrete(lojaId, cep));
    }

    @GetMapping("/acompanhamento/{codigo}")
    @Operation(summary = "Acompanha um pedido por codigo e telefone")
    public ResponseEntity<PedidoDTO.Response> acompanharPorCodigo(
            @PathVariable String codigo,
            @RequestParam String telefone) {
        return ResponseEntity.ok(pedidoService.acompanharPorCodigo(codigo, telefone));
>>>>>>> 5f56ce829bc12a91f2d6f4314131ed60cf49bd31
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    @Operation(summary = "Busca pedido por ID")
    public ResponseEntity<PedidoDTO.Response> buscarPorId(@PathVariable String id) {
        return ResponseEntity.ok(pedidoService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Cria um novo pedido")
    @ApiResponse(responseCode = "201", description = "Pedido criado com sucesso")
    @ApiResponse(responseCode = "409", description = "Loja fechada ou produto indisponivel")
    @ApiResponse(responseCode = "422", description = "Dados invalidos")
    public ResponseEntity<PedidoDTO.Response> criar(
            @AuthenticationPrincipal AuthenticatedUser usuario,
            @Valid @RequestBody PedidoDTO.Request dto) {
        String usuarioId = usuario == null ? null : usuario.getUser().getId();
        PedidoDTO.Response criado = pedidoService.criar(dto, usuarioId);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.id())
                .toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    @Operation(summary = "Altera o status de um pedido")
    public ResponseEntity<PedidoDTO.Response> alterarStatus(
            @PathVariable String id,
            @Valid @RequestBody PedidoDTO.PatchStatus dto) {
        return ResponseEntity.ok(pedidoService.alterarStatus(id, dto.status(), dto.observacao()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um pedido")
    public ResponseEntity<Void> deletar(@PathVariable String id) {
        pedidoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
