package com.vidaplus.config;

import com.vidaplus.entity.Polo;
import com.vidaplus.entity.Usuario;
import com.vidaplus.repository.PoloRepository;
import com.vidaplus.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class DataMigrationConfig {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PoloRepository poloRepository;

    @Bean
    public CommandLineRunner corrigirUsuariosOrfaos() {
        return args -> {
            System.out.println(">>> INICIANDO MIGRAÇÃO DE USUÁRIOS ANTIGOS...");
            
            List<Usuario> todosUsuarios = usuarioRepository.findAll();
            int corrigidos = 0;

            for (Usuario u : todosUsuarios) {
                // Se o usuário não tem nenhum polo vinculado ou a lista é nula
                if (u.getPolos() == null || u.getPolos().isEmpty()) {
                    
                    // Inicializa a lista se estiver nula
                    if (u.getPolos() == null) u.setPolos(new ArrayList<>());

                    // Tenta recuperar endereço. Se não tiver, define um padrão.
                    String cidade = (u.getCidade() != null && !u.getCidade().isEmpty()) ? u.getCidade() : "Itajubá";
                    String bairro = (u.getBairro() != null && !u.getBairro().isEmpty()) ? u.getBairro() : "Centro";
                    String cep = (u.getCep() != null) ? u.getCep() : "37500-000";

                    // --- REPLICA A LÓGICA DE AUTO-VINCULAÇÃO ---
                    
                    // 1. Busca/Cria Hospital (Pai)
                    Polo hospital = poloRepository.findByPoloPaiIsNull().stream()
                            .filter(p -> p.getCidade().equalsIgnoreCase(cidade) && "HOSPITAL".equalsIgnoreCase(p.getTipo()))
                            .findFirst()
                            .orElse(null);

                    if (hospital == null) {
                        hospital = new Polo();
                        hospital.setNome("Hospital VidaPlus " + cidade);
                        hospital.setCidade(cidade);
                        hospital.setTipo("HOSPITAL");
                        hospital.setCep(cep);
                        hospital.setAtivo(true);
                        hospital.setHorarioFuncionamento("24 Horas");
                        hospital.setDataInauguracao(LocalDate.now());
                        // Define um admin temporário se necessário, ou deixe null para editar depois
                        hospital = poloRepository.save(hospital);
                        System.out.println(">>> MIGRAÇÃO: Hospital criado para " + cidade);
                    }

                    // 2. Busca/Cria Clínica (Filho)
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
                        clinica.setPoloPai(finalHospital);
                        clinica.setCep(cep);
                        clinica.setAtivo(true);
                        clinica.setHorarioFuncionamento("08:00 às 18:00");
                        clinica.setDataInauguracao(LocalDate.now());
                        clinica.setLogradouro(bairro + ", " + cidade);
                        clinica = poloRepository.save(clinica);
                        System.out.println(">>> MIGRAÇÃO: Clínica criada para " + bairro);
                    }

                    // 3. Vincula o usuário
                    u.getPolos().add(clinica);
                    usuarioRepository.save(u);
                    corrigidos++;
                    System.out.println(">>> USUÁRIO RECUPERADO: " + u.getNome() + " -> " + clinica.getNome());
                }
            }
            
            System.out.println(">>> MIGRAÇÃO CONCLUÍDA: " + corrigidos + " usuários antigos foram organizados nos polos.");
        };
    }
}