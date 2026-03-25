package back.camarao.sistema.config;

import back.camarao.sistema.enums.Categoria;
import back.camarao.sistema.model.Produto;
import back.camarao.sistema.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataSeeder {

    @Bean
    @Profile("dev")
    public CommandLineRunner seed(ProdutoRepository repo) {
        return args -> {
            if (repo.count() > 0) {
                log.info("Banco já populado – seed ignorado.");
                return;
            }

            List<Produto> produtos = List.of(
                Produto.builder()
                    .nome("Moqueca de Camarão Tradicional")
                    .preco(new BigDecimal("32.00"))
                    .descricao("Camarões frescos selecionados, cozidos lentamente em leite de coco artesanal, azeite de dendê legítimo e um refogado secreto de ervas frescas.")
                    .isDisponivel(true)
                    .img("https://lh3.googleusercontent.com/aida-public/AB6AXuCgGjwrdXqubYerEPjH44JEqy8fWcDb2nmQ77CyXk67WR71ak6L5Sr-08zjTMsd6rXghQbPieBWeLw8oKnq3MOYjKgvCyXzGWRgnYEgjsVjSthLlUEbwt0ozPy3KSaHWffu-6G7_FF4BHBu5draimJzNkIm95HLw98UjKU6IAvWvG_33iM5VXsh_AOTKu_cll2mNZ7tN4r6z_h17L1U7eYOE9fna6T6-lwT97cQd8f7aMLAUqtMxOj-Z3iNrVho1UJtEsZ7-0QE9p8")
                    .categoria(Categoria.MOQUECAS)
                    .tags(List.of("frutos_do_mar", "coco", "dendê"))
                    .notaMedia(4.8)
                    .build(),
                Produto.builder()
                    .nome("Moqueca de Peixe")
                    .preco(new BigDecimal("38.00"))
                    .descricao("Filé de peixe fresco do dia, cozido em caldo de coco, tomate e pimentão. Acompanha arroz branco e pirão.")
                    .isDisponivel(true)
                    .img("https://images.unsplash.com/photo-1580822184713-fc5400e7fe10?w=800")
                    .categoria(Categoria.MOQUECAS)
                    .tags(List.of("frutos_do_mar", "coco"))
                    .notaMedia(4.6)
                    .build(),
                Produto.builder()
                    .nome("Casquinha de Siri")
                    .preco(new BigDecimal("18.00"))
                    .descricao("Casquinha gratinada com carne de siri temperada com ervas finas, azeite e um toque de limão.")
                    .isDisponivel(true)
                    .img("https://images.unsplash.com/photo-1565557623262-b51c2513a641?w=800")
                    .categoria(Categoria.ENTRADAS)
                    .tags(List.of("frutos_do_mar", "gratinado"))
                    .notaMedia(4.9)
                    .build(),
                Produto.builder()
                    .nome("Farofa de Dendê")
                    .preco(new BigDecimal("8.00"))
                    .descricao("Farofa artesanal preparada com azeite de dendê, cebola caramelizada e cheiro-verde.")
                    .isDisponivel(true)
                    .img("https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=800")
                    .categoria(Categoria.ACOMPANHAMENTOS)
                    .tags(List.of("vegano", "dendê"))
                    .notaMedia(4.5)
                    .build(),
                Produto.builder()
                    .nome("Água de Coco Natural")
                    .preco(new BigDecimal("9.00"))
                    .descricao("Servida direto no coco verde, gelada e refrescante.")
                    .isDisponivel(true)
                    .img("https://images.unsplash.com/photo-1548369937-47519962c11a?w=800")
                    .categoria(Categoria.BEBIDAS)
                    .tags(List.of("vegano", "sem_gluten", "natural"))
                    .notaMedia(5.0)
                    .build(),
                Produto.builder()
                    .nome("Pudim de Leite Condensado")
                    .preco(new BigDecimal("14.00"))
                    .descricao("Pudim cremoso com calda de caramelo feita na hora. Receita da casa há 10 anos.")
                    .isDisponivel(false)
                    .img("https://images.unsplash.com/photo-1551024506-0bccd828d307?w=800")
                    .categoria(Categoria.SOBREMESAS)
                    .tags(List.of("sobremesa", "classico"))
                    .notaMedia(4.7)
                    .build()
            );

            repo.saveAll(produtos);
            log.info("Seed concluído: {} produtos inseridos.", produtos.size());
        };
    }
}
