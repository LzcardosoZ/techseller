package br.com.techseller.techsellers.controller;

import br.com.techseller.techsellers.entity.Produto;
import br.com.techseller.techsellers.service.ArmazenamentoImagemService;
import br.com.techseller.techsellers.service.CarrinhoService;
import br.com.techseller.techsellers.service.ProdutoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;


import java.math.BigDecimal;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
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
        when(produtoService.salvarProduto(any(Produto.class), any(MultipartFile[].class)))
                .thenReturn(produto);

        MockMultipartFile imagemVazia = new MockMultipartFile("imagens", "", "image/png", new byte[0]);

        mockMvc.perform(multipart("/produtos/test-salvar")
                        .file(imagemVazia)
                        .param("nome", "Mouse Gamer")
                        .param("descricaoDetalhada", "Mouse com RGB")
                        .param("preco", "150.0"))
                .andExpect(status().isOk());

        verify(produtoService, times(1)).salvarProduto(any(Produto.class), any(MultipartFile[].class));
    }


    @TestConfiguration
    static class MockConfig {

        @Bean
        public ProdutoService produtoService() {
            return mock(ProdutoService.class);
        }

        @Bean
        public ArmazenamentoImagemService armazenamentoImagemService() {
            return mock(ArmazenamentoImagemService.class);
        }

        @Bean
        public CarrinhoService carrinhoService() {
            return mock(CarrinhoService.class);
        }
    }


}


