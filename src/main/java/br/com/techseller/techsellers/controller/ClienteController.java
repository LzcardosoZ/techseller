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

import java.util.*;

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
        cliente.setEnderecoFaturamento(new Endereco());

        List<Endereco> enderecosEntrega = new ArrayList<>();
        enderecosEntrega.add(new Endereco());
        cliente.setEnderecosEntrega(enderecosEntrega);

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
        model.addAttribute("generos", Arrays.asList("Masculino", "Feminino", "Outro"));

        if (result.hasErrors()) {
            return "cadastroCliente";
        }

        if (!cliente.getSenha().equals(confirmarSenha)) {
            model.addAttribute("erroSenha", "As senhas não coincidem.");
            return "cadastroCliente";
        }

        try {
            if (clienteService.emailJaCadastrado(cliente.getEmail())) {
                model.addAttribute("erroEmail", "Este e-mail já está em uso");
                return "cadastroCliente";
            }

            clienteService.cadastrarCliente(cliente);

            return "redirect:/" + ("carrinho".equals(from) ? "carrinho" : "loja");

        } catch (IllegalArgumentException e) {
            model.addAttribute("erroCEP", e.getMessage());
            return "cadastroCliente";
        }
    }



    // ===== EXIBIR PÁGINA DE CONTA =====
    @GetMapping("/conta")
    public String mostrarConta(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Optional<Cliente> clienteOpt = clienteService.buscarPorEmail(userDetails.getUsername());
        if (clienteOpt.isPresent()) {
            model.addAttribute("cliente", clienteOpt.get());
            return "conta";
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

            novoEndereco.setCliente(cliente);
            cliente.getEnderecosEntrega().add(novoEndereco);

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

            if (index >= 0 && index < cliente.getEnderecosEntrega().size()) {
                Endereco endereco = cliente.getEnderecosEntrega().get(index);
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

            if (index >= 0 && index < cliente.getEnderecosEntrega().size()) {
                // Desmarcar todos os outros
                cliente.getEnderecosEntrega().forEach(e -> e.setPadrao(false));
                // Marcar o escolhido como padrão
                cliente.getEnderecosEntrega().get(index).setPadrao(true);

                clienteService.atualizarCliente(cliente);
            }
        }

        return "redirect:/clientes/conta";
    }


}
