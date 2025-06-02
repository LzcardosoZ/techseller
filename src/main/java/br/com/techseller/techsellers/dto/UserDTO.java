package br.com.techseller.techsellers.dto;

public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String cpf;
    private boolean status;
    private String nomeGrupo;

    public UserDTO(Long id, String username, String email, String cpf, boolean status, String nomeGrupo) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.cpf = cpf;
        this.status = status;
        this.nomeGrupo = nomeGrupo;
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public boolean isStatus() { return status; }
    public void setStatus(boolean status) { this.status = status; }

    public String getNomeGrupo() { return nomeGrupo; }
    public void setNomeGrupo(String nomeGrupo) { this.nomeGrupo = nomeGrupo; }
}
