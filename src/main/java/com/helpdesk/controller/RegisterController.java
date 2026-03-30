package com.helpdesk.controller;

import com.helpdesk.entity.Administrador;
import com.helpdesk.entity.Polo;
import com.helpdesk.entity.Usuario;
import com.helpdesk.repository.AdministradorRepository;
import com.helpdesk.repository.PoloRepository;
import com.helpdesk.repository.UsuarioRepository;
import com.helpdesk.service.UsuarioService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
public class RegisterController {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AdministradorRepository administradorRepository;
    
    // --- INJEÇÃO DO SERVICE (CORREÇÃO FUNDAMENTAL) ---
    @Autowired private UsuarioService usuarioService;
    
    // --- INJEÇÕES PARA A LÓGICA DE POLOS E SEGURANÇA ---
    @Autowired private PoloRepository poloRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    // =========================================================================
    // 1. REGISTRO PÚBLICO (PACIENTES / PROFISSIONAIS)
    // =========================================================================

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "register"; 
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute Usuario usuario, Model model, RedirectAttributes redirectAttributes) {
        // 1. Validação de Duplicidade
        if (usuarioRepository.findByUsernameOrCpf(usuario.getUsername()) != null ||
            usuarioRepository.findByUsernameOrCpf(usuario.getCpf()) != null) {
            model.addAttribute("error", "Usuário ou CPF já cadastrado!");
            return "register";
        }

        try {
            // NOTA: A criptografia de senha foi movida para o Service para centralizar a regra.
            
            // 2. Definições Padrão
            // REMOVIDO: usuario.setAtivo(true); -> O Service agora define como FALSE.
            
            if (usuario.getPerfil() == null || usuario.getPerfil().isEmpty()) {
                usuario.setPerfil("PACIENTE"); // Perfil padrão se não especificado
            }

            // 3. LÓGICA INTELIGENTE: VÍNCULO AUTOMÁTICO DE POLO
            // Cria ou recupera a estrutura Hospital (Cidade) > Clínica (Bairro)
            Polo clinicaVinculada = vincularPoloAutomatico(usuario);
            
            if (usuario.getPolos() == null) {
                usuario.setPolos(new ArrayList<>());
            }
            
            // Adiciona o usuário à clínica do seu bairro se ela foi criada/encontrada
            if (clinicaVinculada != null) {
                usuario.getPolos().add(clinicaVinculada);
            }

            // 4. CHAMADA AO SERVICE (CORREÇÃO PRINCIPAL)
            // Delega o salvamento, criptografia e envio de e-mail para o Service
            usuarioService.cadastrar(usuario);

            // 5. REDIRECIONAMENTO PARA TELA DE CÓDIGO
            // Passamos o e-mail como parâmetro ou FlashAttribute para a tela de verificação saber quem validar
            redirectAttributes.addFlashAttribute("mensagem", "Cadastro realizado! Verifique seu e-mail.");
            return "redirect:/verificar-conta?email=" + usuario.getEmail();
            
        } catch (Exception e) {
            System.err.println("Erro no cadastro: " + e.getMessage()); // O erro gigante fica escondido no terminal
            model.addAttribute("error", "Ocorreu um erro interno ao processar seu cadastro. Verifique os dados ou contate o suporte.");
            return "register";
        }
    }

    /**
     * MÉTODO MÁGICO: Garante a hierarquia de Polos e preenche TODOS os dados
     */
    private Polo vincularPoloAutomatico(Usuario usuario) {
        String cidade = usuario.getCidade();
        String bairro = usuario.getBairro();
        String cep = usuario.getCep();

        // Se não tiver endereço completo, não cria vínculo
        if (cidade == null || bairro == null) return null;

        // --- BUSCA O RESPONSÁVEL PADRÃO (ADMIN) ---
        Usuario responsavelPadrao = usuarioRepository.findByUsernameOrCpf("admin");
        if (responsavelPadrao == null) {
            // Fallback: pega o primeiro usuário (geralmente ID 1) se não achar pelo login 'admin'
            List<Usuario> users = usuarioRepository.findAll();
            if (!users.isEmpty()) responsavelPadrao = users.get(0);
        }

        // A. Busca/Cria o HOSPITAL (Matriz da Cidade)
        Polo hospital = poloRepository.findByPoloPaiIsNull().stream()
                .filter(p -> p.getCidade().equalsIgnoreCase(cidade) && "HOSPITAL".equalsIgnoreCase(p.getTipo()))
                .findFirst()
                .orElse(null);

        if (hospital == null) {
            hospital = new Polo();
            hospital.setNome("Hospital VidaPlus " + cidade);
            hospital.setCidade(cidade);
            hospital.setTipo("HOSPITAL");
            
            // PREENCHIMENTO AUTOMÁTICO DE DADOS OBRIGATÓRIOS
            hospital.setCep(cep); 
            hospital.setAtivo(true);
            hospital.setHorarioFuncionamento("24 Horas");
            hospital.setDataInauguracao(LocalDate.now()); // Data de hoje
            hospital.setResponsavel(responsavelPadrao);   // Admin Master
            
            hospital = poloRepository.save(hospital);
            System.out.println(">>> AUTO-POLO: Novo Hospital criado para a cidade de " + cidade);
        }

        // B. Busca/Cria a CLÍNICA (Filial do Bairro)
        Polo finalHospital = hospital;
        Polo clinica = poloRepository.findByPoloPai_Id(hospital.getId()).stream()
                .filter(p -> p.getBairro() != null && p.getBairro().equalsIgnoreCase(bairro))
                .findFirst()
                .orElse(null);

        if (clinica == null) {
            clinica = new Polo();
            clinica.setNome("Clínica " + bairro);
            clinica.setCidade(cidade);
            clinica.setBairro(bairro);
            clinica.setTipo("CLINICA");
            clinica.setPoloPai(finalHospital); // VÍNCULO HIERÁRQUICO (Pai = Hospital)
            
            // PREENCHIMENTO AUTOMÁTICO DE DADOS OBRIGATÓRIOS
            clinica.setCep(cep);
            clinica.setAtivo(true);
            clinica.setHorarioFuncionamento("08:00 às 18:00");
            clinica.setDataInauguracao(LocalDate.now()); // Data de hoje
            clinica.setResponsavel(responsavelPadrao);   // Admin Master
            clinica.setLogradouro(bairro + ", " + cidade); // Logradouro genérico baseado no bairro
            
            clinica = poloRepository.save(clinica);
            System.out.println(">>> AUTO-POLO: Nova Clínica criada para o bairro " + bairro);
        }

        return clinica; // Retorna a clínica para vincular o usuário
    }


    // =========================================================================
    // 2. REGISTRO ADMINISTRATIVO (SEU CÓDIGO ORIGINAL MANTIDO)
    // =========================================================================

    @GetMapping("/register-admin")
    public String showAdminRegisterForm(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        Usuario usuario = usuarioRepository.findByUsernameOrCpf(principal.getName());
        model.addAttribute("usuario", usuario);
        
        return "admin/register-admin"; 
    }

    @PostMapping("/register-admin")
    public String processAdminRegister(@RequestParam String matricula,
                                       @RequestParam LocalDate dataMatricula, 
                                       @RequestParam String cargo, 
                                       Principal principal) {
        
        if (principal == null) return "redirect:/login";

        Usuario usuario = usuarioRepository.findByUsernameOrCpf(principal.getName());
        
        if (usuario != null) {
            usuario.setPerfil("ADMIN");
            usuario.setAtivo(false); // Pendente de aprovação
            usuarioRepository.save(usuario);

            Administrador admin = new Administrador();
            admin.setUsuario(usuario);
            admin.setMatricula(matricula);
            admin.setDataMatricula(dataMatricula);
            admin.setNivelAcesso(cargo);
            
            administradorRepository.save(admin);
        }

        return "redirect:/login?pending=true";
    }
}