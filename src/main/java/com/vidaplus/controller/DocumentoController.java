package com.vidaplus.controller;

import com.vidaplus.entity.Documento;
import com.vidaplus.entity.Usuario;
import com.vidaplus.repository.DocumentoRepository;
import com.vidaplus.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class DocumentoController {

    @Autowired
    private DocumentoRepository documentoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/documentos")
    public String listarDocumentos(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        String loginUsuario = principal.getName();
        Usuario usuario = usuarioRepository.findByUsernameOrCpf(loginUsuario);
        
        model.addAttribute("usuario", usuario);

        List<Documento> documentos = documentoRepository.findByUsuario(usuario);
        
        // Mantendo seu código de exemplo original (opcional, mas mantido para não quebrar lógica existente)
        if (documentos.isEmpty()) {
            Documento docExemplo = new Documento();
            docExemplo.setNomeArquivo("Exame_Sangue_Exemplo.pdf");
            docExemplo.setTipo("pdf");
            docExemplo.setDataUpload(LocalDateTime.now());
            docExemplo.setUsuario(usuario);
            docExemplo.setDescricao("Cardiologista - Dr. House");
            // Nota: docExemplo não tem .setDados(), então o download dele falhará (retornará 404), o que é correto.
            documentos.add(docExemplo);
        }

        model.addAttribute("documentos", documentos);
        return "documento/documentos";
    }

    // --- NOVA ROTA DE DOWNLOAD ---
    @GetMapping("/documentos/download/{id}")
    public ResponseEntity<byte[]> downloadDocumento(@PathVariable Long id) {
        
        // 1. Busca o documento no banco
        Documento doc = documentoRepository.findById(id).orElse(null);

        // 2. Verifica se existe e se tem conteúdo salvo
        if (doc == null || doc.getDados() == null) {
            return ResponseEntity.notFound().build();
        }

        // 3. Retorna o arquivo com cabeçalhos para download
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getNomeArquivo() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(doc.getDados());
    }
}