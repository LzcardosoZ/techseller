package br.com.techseller.techsellers.controller;

import br.com.techseller.techsellers.entity.Cliente;
import br.com.techseller.techsellers.entity.Endereco;
import br.com.techseller.techsellers.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;


import java.util.*;

@Slf4j
@Controller
@RequestMapping("/clientes")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ===== CADASTRO DE NOVO CLIENTE =====
    @GetMapping("/cadastro")
    public String mostrarFormCadastro(@RequestParam(name = "from", defaultValue = "loja") String from,
                                      Model model) {
        Cliente cliente = new Cliente();

        // Endereço de faturamento (padrao = true)
        Endereco faturamento = new Endereco();
        faturamento.setPadrao(true);

        // Endereço de entrega (padrao = false)
        Endereco entrega = new Endereco();
        entrega.setPadrao(false);

        // Define os dois endereços na lista única
        cliente.setEnderecos(List.of(faturamento, entrega));

        model.addAttribute("cliente", cliente);
        model.addAttribute("generos", Arrays.asList("Masculino", "Feminino", "Outro"));
        model.addAttribute("from", from);

        return "cadastroCliente";
    }



    @PostMapping("/cadastrar")
    public String cadastrarCliente(@Valid @ModelAttribute Cliente cliente,
                                   BindingResult result,
                                   @RequestParam String confirmarSenha,
                                   @RequestParam(name = "from", defaultValue = "loja") String from,
                                   Model model) {

        log.info("📥 Requisição recebida para cadastro de cliente: {}", cliente.getEmail());

        model.addAttribute("generos", Arrays.asList("Masculino", "Feminino", "Outro"));

        if (result.hasErrors()) {
            log.warn("❌ Erros de validação no formulário: {}", result.getAllErrors());

            preencherEnderecosMinimos(cliente);
            model.addAttribute("cliente", cliente);
            return "cadastroCliente";
        }

        if (!cliente.getSenha().equals(confirmarSenha)) {
            log.warn("❌ Senhas não coincidem para: {}", cliente.getEmail());
            model.addAttribute("erroSenha", "As senhas não coincidem.");

            preencherEnderecosMinimos(cliente);
            model.addAttribute("cliente", cliente);
            return "cadastroCliente";
        }

        try {
            if (clienteService.emailJaCadastrado(cliente.getEmail())) {
                log.warn("⚠️ E-mail já cadastrado: {}", cliente.getEmail());
                model.addAttribute("erroEmail", "Este e-mail já está em uso");

                preencherEnderecosMinimos(cliente);
                model.addAttribute("cliente", cliente);
                return "cadastroCliente";
            }

            clienteService.cadastrarCliente(cliente);
            log.info("✅ Cadastro finalizado com sucesso: {}", cliente.getEmail());

            // ✅ redireciona para login com parâmetro de sucesso
            return "redirect:/login_cliente?cadastro=sucesso";

        } catch (IllegalArgumentException e) {
            log.error("🚫 Erro ao cadastrar cliente: {}", e.getMessage());
            model.addAttribute("erroCadastro", e.getMessage());

            preencherEnderecosMinimos(cliente);
            model.addAttribute("cliente", cliente);
            return "cadastroCliente";
        }
    }





    // ===== EXIBIR PÁGINA DE CONTA =====
    @GetMapping("/conta")
    public String mostrarConta(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Optional<Cliente> clienteOpt = clienteService.buscarPorEmail(userDetails.getUsername());
        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();

            model.addAttribute("cliente", cliente);
            model.addAttribute("enderecos", cliente.getEnderecosEntrega());       // os não-padrão
            model.addAttribute("enderecoPadrao", cliente.getEnderecoPadrao());   // o atual padrão (visual)

            return "conta"; // seu template HTML
        }
        return "redirect:/login_cliente";
    }


    // ===== ATUALIZAR DADOS PESSOAIS =====
    @PostMapping("/conta/atualizarInfo")
    public String atualizarInfoPessoal(@ModelAttribute Cliente clienteAtualizado,
                                       @AuthenticationPrincipal UserDetails userDetails,
                                       Model model) {
        Optional<Cliente> clienteOpt = clienteService.buscarPorEmail(userDetails.getUsername());

        if (clienteOpt.isPresent()) {
            Cliente clienteExistente = clienteOpt.get();

            clienteExistente.setNomeCompleto(clienteAtualizado.getNomeCompleto());
            clienteExistente.setDataNascimento(clienteAtualizado.getDataNascimento());
            clienteExistente.setGenero(clienteAtualizado.getGenero());

            clienteService.atualizarCliente(clienteExistente);
        }

        return "redirect:/clientes/conta";
    }

    // ===== ALTERAR SENHA =====
    @PostMapping("/conta/alterarSenha")
    public String alterarSenha(@RequestParam String senhaAtual,
                               @RequestParam String novaSenha,
                               @RequestParam String confirmarSenha,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model) {
        Optional<Cliente> clienteOpt = clienteService.buscarPorEmail(userDetails.getUsername());

        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();

            if (!passwordEncoder.matches(senhaAtual, cliente.getSenha())) {
                model.addAttribute("erroSenha", "Senha atual incorreta");
                model.addAttribute("cliente", cliente);
                return "conta";
            }

            if (!novaSenha.equals(confirmarSenha)) {
                model.addAttribute("erroSenha", "As senhas não coincidem");
                model.addAttribute("cliente", cliente);
                return "conta";
            }

            clienteService.alterarSenha(cliente, novaSenha);
        }

        return "redirect:/clientes/conta";
    }

    // ===== ADICIONAR NOVO ENDEREÇO =====
    @PostMapping("/conta/adicionarEndereco")
    public String adicionarEndereco(@RequestParam Map<String, String> enderecoParams,
                                    @RequestParam(name = "from", defaultValue = "conta") String from,
                                    @AuthenticationPrincipal UserDetails userDetails) {

        Optional<Cliente> clienteOpt = clienteService.buscarPorEmail(userDetails.getUsername());
        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();

            Endereco novoEndereco = new Endereco();
            novoEndereco.setCep(enderecoParams.get("cep"));
            novoEndereco.setLogradouro(enderecoParams.get("logradouro"));
            novoEndereco.setNumero(enderecoParams.get("numero"));
            novoEndereco.setComplemento(enderecoParams.get("complemento"));
            novoEndereco.setBairro(enderecoParams.get("bairro"));
            novoEndereco.setCidade(enderecoParams.get("cidade"));
            novoEndereco.setUf(enderecoParams.get("uf"));

            novoEndereco.setPadrao(false);
            novoEndereco.setCliente(cliente);

            cliente.getEnderecos().add(novoEndereco);

            clienteService.atualizarCliente(cliente);
        }

        return "redirect:/" + ("checkout".equals(from) ? "carrinho/finalizar" : "clientes/conta");
    }


    @PostMapping("/conta/editarEndereco/{index}")
    public String editarEnderecoSalvo(@PathVariable int index,
                                      @RequestParam Map<String, String> form,
                                      @AuthenticationPrincipal UserDetails userDetails) {

        Optional<Cliente> clienteOpt = clienteService.buscarPorEmail(userDetails.getUsername());

        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();

            // Filtra apenas os endereços de entrega (padrao = false)
            List<Endereco> entregas = cliente.getEnderecos().stream()
                    .filter(e -> !Boolean.TRUE.equals(e.getPadrao()))
                    .toList();

            if (index >= 0 && index < entregas.size()) {
                Endereco endereco = entregas.get(index);

                endereco.setCep(form.get("cep"));
                endereco.setLogradouro(form.get("logradouro"));
                endereco.setNumero(form.get("numero"));
                endereco.setComplemento(form.get("complemento"));
                endereco.setBairro(form.get("bairro"));
                endereco.setCidade(form.get("cidade"));
                endereco.setUf(form.get("uf"));

                clienteService.atualizarCliente(cliente);
            }
        }

        return "redirect:/clientes/conta";
    }

    @PostMapping("/conta/setarEnderecoPadrao/{index}")
    public String setarEnderecoPadrao(@PathVariable int index,
                                      @AuthenticationPrincipal UserDetails userDetails) {
        Optional<Cliente> clienteOpt = clienteService.buscarPorEmail(userDetails.getUsername());

        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();

            List<Endereco> entregas = cliente.getEnderecos().stream()
                    .filter(e -> !Boolean.TRUE.equals(e.getPadrao()))
                    .toList();

            if (index >= 0 && index < entregas.size()) {
                for (Endereco e : entregas) {
                    e.setPadrao(false);
                }
                entregas.get(index).setPadrao(true);
                clienteService.atualizarCliente(cliente);
            }
        }

        return "redirect:/clientes/conta";
    }

    private void preencherEnderecosMinimos(Cliente cliente) {
        if (cliente.getEnderecos() == null) {
            cliente.setEnderecos(new ArrayList<>());
        }

        while (cliente.getEnderecos().size() < 2) {
            Endereco novo = new Endereco();
            novo.setPadrao(cliente.getEnderecos().isEmpty()); // o primeiro é faturamento
            cliente.getEnderecos().add(novo);
        }
    }


}
