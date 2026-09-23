package com.beautysalon.Controller;

import com.beautysalon.DTO.ServicoDTO;
import com.beautysalon.Inteface.ServicoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
@RequestMapping("/{slug}/servicos")
public class ServicoController {

    private final ServicoService servicoService;
    private final String uploadDir = "C:/beautysalon/uploads/";

    public ServicoController(ServicoService servicoService) {
        this.servicoService = servicoService;
    }

    @GetMapping
    public String listar(@PathVariable String slug, Model model) {
        List<ServicoDTO> servicos = servicoService.listarTodos();
        model.addAttribute("servicos", servicos);
        model.addAttribute("empresaSlug", slug);
        model.addAttribute("pageTitle", "Lista de Serviços");
        return "servicos/list";
    }

    @GetMapping("/novo")
    public String novo(@PathVariable String slug, Model model) {
        model.addAttribute("servico", new ServicoDTO());
        model.addAttribute("empresaSlug", slug);
        model.addAttribute("pageTitle", "Novo Serviço");
        model.addAttribute("formAction", "/" + slug + "/servicos");
        return "servicos/form";
    }

    @PostMapping
    public String salvar(@PathVariable String slug,
                         @Valid @ModelAttribute("servico") ServicoDTO dto,
                         BindingResult result,
                         Model model) throws IOException {
        if (result.hasErrors()) {
            model.addAttribute("empresaSlug", slug);
            model.addAttribute("pageTitle", dto.getId() == null ? "Novo Serviço" : "Editar Serviço");
            model.addAttribute("formAction", "/" + slug + "/servicos");
            return "servicos/form";
        }

        if (dto.getImagemFile() != null && !dto.getImagemFile().isEmpty()) {
            Files.createDirectories(Paths.get(uploadDir));
            String nomeArquivo = System.currentTimeMillis() + "_" + dto.getImagemFile().getOriginalFilename();
            Path caminhoCompleto = Paths.get(uploadDir + nomeArquivo);
            Files.createDirectories(caminhoCompleto.getParent());
            dto.getImagemFile().transferTo(caminhoCompleto.toFile());
            dto.setImagem(nomeArquivo);
        }

        servicoService.salvar(dto);
        return "redirect:/" + slug + "/servicos";
    }

    @GetMapping("/edit/{id}")
    public String editar(@PathVariable String slug, @PathVariable Long id, Model model) {
        ServicoDTO servico = servicoService.buscarPorId(id);
        model.addAttribute("servico", servico);
        model.addAttribute("empresaSlug", slug);
        model.addAttribute("pageTitle", "Editar Serviço");
        model.addAttribute("formAction", "/" + slug + "/servicos/" + id + "/atualizar");
        return "servicos/form";
    }

    @PostMapping("/{id}/atualizar")
    public String atualizar(@PathVariable String slug,
                            @PathVariable Long id,
                            @Valid @ModelAttribute("servico") ServicoDTO dto,
                            BindingResult result,
                            Model model) throws IOException {
        if (result.hasErrors()) {
            model.addAttribute("empresaSlug", slug);
            model.addAttribute("pageTitle", "Editar Serviço");
            model.addAttribute("formAction", "/" + slug + "/servicos/" + id + "/atualizar");
            return "servicos/form";
        }

        if (dto.getImagemFile() != null && !dto.getImagemFile().isEmpty()) {
            Files.createDirectories(Paths.get(uploadDir));
            String nomeArquivo = System.currentTimeMillis() + "_" + dto.getImagemFile().getOriginalFilename();
            Path caminhoCompleto = Paths.get(uploadDir + nomeArquivo);
            dto.getImagemFile().transferTo(caminhoCompleto.toFile());
            dto.setImagem(nomeArquivo);
        }

        servicoService.atualizar(id, dto);
        return "redirect:/" + slug + "/servicos";
    }

    @GetMapping("/delete/{id}")
    public String deletar(@PathVariable String slug, @PathVariable Long id) {
        servicoService.excluir(id);
        return "redirect:/" + slug + "/servicos";
    }
}
