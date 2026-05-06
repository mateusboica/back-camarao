package back.camarao.sistema.service;

import back.camarao.sistema.dto.LojaDTO;
import back.camarao.sistema.exception.ResourceAlreadyExistsException;
import back.camarao.sistema.exception.ResourceNotFoundException;
import back.camarao.sistema.model.Loja;
import back.camarao.sistema.repository.LojaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LojaService {

    private final LojaRepository lojaRepository;

    public Page<LojaDTO.LojaResponse> listarTodos(Pageable pageable) {
        return lojaRepository.findAll(pageable).map(LojaDTO.LojaResponse::from);
    }

    public LojaDTO.LojaResponse buscarPorId(String id) {
        return LojaDTO.LojaResponse.from(encontrarOuLancar(id));
    }

    public LojaDTO.LojaResponse buscarPorNome(String nome) {
        Loja loja = lojaRepository.findByNomeIgnoreCase(nome)
                .orElseThrow(() -> new ResourceNotFoundException("Loja", nome));
        return LojaDTO.LojaResponse.from(loja);
    }

    public LojaDTO.LojaResponse criar(LojaDTO.Request dto) {
        if (lojaRepository.existsByNomeIgnoreCase(dto.nome())) {
            throw new ResourceAlreadyExistsException(
                    "Ja existe uma loja com o nome '%s'".formatted(dto.nome()));
        }

        Loja loja = Loja.builder()
                .nome(dto.nome().trim())
                .descricao(dto.descricao().trim())
                .endereco(dto.endereco().trim())
                .telefone(dto.telefone().trim())
                .aberto(dto.aberto())
                .logoUrl(dto.logoUrl().trim())
                .taxaServico(dto.taxaServico().trim())
                .taxaEntrega(dto.taxaEntrega().trim())
                .horarioFuncionamento(dto.horarioFuncionamento().trim())
                .build();

        Loja salva = lojaRepository.save(loja);
        log.info("Loja criada: id={}, nome={}", salva.getId(), salva.getNome());
        return LojaDTO.LojaResponse.from(salva);
    }

    public LojaDTO.LojaResponse atualizar(String id, LojaDTO.Request dto) {
        Loja existente = encontrarOuLancar(id);
        boolean nomeMudou = !existente.getNome().equalsIgnoreCase(dto.nome().trim());
        if (nomeMudou && lojaRepository.existsByNomeIgnoreCase(dto.nome())) {
            throw new ResourceAlreadyExistsException(
                    "Ja existe outra loja com o nome '%s'".formatted(dto.nome()));
        }

        existente.setNome(dto.nome().trim());
        existente.setDescricao(dto.descricao().trim());
        existente.setEndereco(dto.endereco().trim());
        existente.setTelefone(dto.telefone().trim());
        existente.setAberto(dto.aberto());
        existente.setLogoUrl(dto.logoUrl().trim());
        existente.setTaxaServico(dto.taxaServico().trim());
        existente.setTaxaEntrega(dto.taxaEntrega().trim());
        existente.setHorarioFuncionamento(dto.horarioFuncionamento().trim());

        Loja atualizada = lojaRepository.save(existente);
        log.info("Loja atualizada: id={}", id);
        return LojaDTO.LojaResponse.from(atualizada);
    }

    public LojaDTO.LojaResponse alterarStatus(String id, Boolean aberto) {
        Loja loja = encontrarOuLancar(id);
        loja.setAberto(aberto);
        return LojaDTO.LojaResponse.from(lojaRepository.save(loja));
    }

    public void deletar(String id) {
        encontrarOuLancar(id);
        lojaRepository.deleteById(id);
        log.info("Loja deletada: id={}", id);
    }

    public Loja encontrarOuLancar(String id) {
        return lojaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loja", id));
    }
}
