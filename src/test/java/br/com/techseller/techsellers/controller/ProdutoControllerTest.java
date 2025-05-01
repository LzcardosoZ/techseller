/*package br.com.techseller.techsellers.controller;

import br.com.techseller.techsellers.entity.Produto;
import br.com.techseller.techsellers.service.ProdutoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProdutoController.class)
@Import(ProdutoControllerTest.MockConfig.class)
public class ProdutoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProdutoService produtoService;

    private Produto produto;

    @BeforeEach
    void setup() {
        produto = new Produto();
        produto.setNome("Mouse Gamer");
        produto.setDescricaoDetalhada("Mouse com RGB");
        produto.setPreco(new BigDecimal("150.0"));
    }

    @Test
    void deveCadastrarProdutoComDadosValidos() throws Exception {
        when(produtoService.salvarProduto(any(Produto.class))).thenReturn(produto);

        mockMvc.perform(post("/produtos/salvar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(produto)))
                .andExpect(status().isOk());

        verify(produtoService, times(1)).salvarProduto(any(Produto.class));
    }

    @TestConfiguration
    static class MockConfig {
        @Bean
        public ProdutoService produtoService() {
            return mock(ProdutoService.class);
        }
    }
}
*/