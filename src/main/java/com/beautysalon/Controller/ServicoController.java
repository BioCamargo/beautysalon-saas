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
    private final com.beautysalon.service.EstoqueService estoqueService;
    private final com.beautysalon.repository.ServicoInsumoRepository servicoInsumoRepository;
    private final com.beautysalon.repository.ServicoRepository servicoRepository;
    private final com.beautysalon.repository.EmpresaRepository empresaRepository;
    private final String uploadDir = "C:/beautysalon/uploads/";

    public ServicoController(ServicoService servicoService,
                             com.beautysalon.service.EstoqueService estoqueService,
                             com.beautysalon.repository.ServicoInsumoRepository servicoInsumoRepository,
                             com.beautysalon.repository.ServicoRepository servicoRepository,
                             com.beautysalon.repository.EmpresaRepository empresaRepository) {
        this.servicoService = servicoService;
        this.estoqueService = estoqueService;
        this.servicoInsumoRepository = servicoInsumoRepository;
        this.servicoRepository = servicoRepository;
        this.empresaRepository = empresaRepository;
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
        
        // Ficha técnica (Insumos)
        Long empresaId = com.beautysalon.tenant.TenantContext.getEmpresaId();
        model.addAttribute("insumos", servicoInsumoRepository.findByServicoIdAndEmpresaId(id, empresaId));
        model.addAttribute("produtosInsumo", estoqueService.listarTodos());
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
            Long empresaId = com.beautysalon.tenant.TenantContext.getEmpresaId();
            model.addAttribute("insumos", servicoInsumoRepository.findByServicoIdAndEmpresaId(id, empresaId));
            model.addAttribute("produtosInsumo", estoqueService.listarTodos());
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
        return "redirect:/" + slug + "/servicos/edit/" + id + "?salvo=true";
    }

    @PostMapping("/{id}/insumos/adicionar")
    public String adicionarInsumo(@PathVariable String slug,
                                  @PathVariable Long id,
                                  @RequestParam Long produtoId,
                                  @RequestParam(defaultValue = "1") Integer quantidadeGasta,
                                  org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        Long empresaId = com.beautysalon.tenant.TenantContext.getEmpresaId();
        var servicoOpt = servicoRepository.findByIdAndEmpresaId(id, empresaId);
        var produto = estoqueService.buscarPorId(produtoId);
        var empresa = empresaRepository.findById(empresaId).orElse(null);

        if (servicoOpt.isPresent() && produto != null && empresa != null) {
            com.beautysalon.model.ServicoInsumo insumo = com.beautysalon.model.ServicoInsumo.builder()
                    .servico(servicoOpt.get())
                    .produto(produto)
                    .quantidadeGasta(quantidadeGasta > 0 ? quantidadeGasta : 1)
                    .empresa(empresa)
                    .build();
            servicoInsumoRepository.save(insumo);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Insumo adicionado à ficha técnica!");
        }
        return "redirect:/" + slug + "/servicos/edit/" + id;
    }

    @PostMapping("/{id}/insumos/{insumoId}/remover")
    public String removerInsumo(@PathVariable String slug,
                                @PathVariable Long id,
                                @PathVariable Long insumoId,
                                org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        servicoInsumoRepository.deleteById(insumoId);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Insumo removido da ficha técnica!");
        return "redirect:/" + slug + "/servicos/edit/" + id;
    }

    @GetMapping("/delete/{id}")
    public String deletar(@PathVariable String slug, @PathVariable Long id) {
        servicoService.excluir(id);
        return "redirect:/" + slug + "/servicos";
    }
}
