package com.beautysalon.Controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object statusObj = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object requestUriObj = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

        if (statusObj != null) {
            int statusCode = Integer.parseInt(statusObj.toString());
            model.addAttribute("errorCode", statusCode);

            // Se por algum motivo cair em 403 na rota de login, redireciona limpando para tela de login
            if (statusCode == 403 && requestUriObj != null && requestUriObj.toString().endsWith("/login")) {
                return "redirect:/login?expired=true";
            }

            if (statusCode == 403) {
                model.addAttribute("errorMessage", "Você não tem permissão para acessar este recurso ou sua sessão expirou.");
            } else if (statusCode == 404) {
                model.addAttribute("errorMessage", "A página ou estabelecimento solicitado não foi encontrado.");
            }
        }

        return "error/customError";
    }
}
