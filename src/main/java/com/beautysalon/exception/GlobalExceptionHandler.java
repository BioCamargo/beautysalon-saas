package com.beautysalon.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger= LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Erro 404 - Página não encontrada
    @ExceptionHandler(NoHandlerFoundException.class)
    public String handleNotFound(NoHandlerFoundException ex, Model model) {
        logger.error("unexpected error  ", ex);
        model.addAttribute("errorCode", "404");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/customError";
    }

    // Erro geral - Exceções não tratadas
    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex, Model model) {
        logger.error("unexpected error  ", ex);
        model.addAttribute("errorCode", "500");
        model.addAttribute("errorMessage",ex.getMessage()); //"Ocorreu um erro inesperado.");
        return "error/customError";
    }
}
