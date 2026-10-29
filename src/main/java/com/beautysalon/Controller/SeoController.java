package com.beautysalon.Controller;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * Controller de infraestrutura de SEO.
 * Entrega robots.txt e sitemap.xml com status 200 OK, cabeçalhos de cache otimizados
 * e tipos MIME precisos, garantindo indexação imediata pelo Googlebot.
 */
@RestController
public class SeoController {

    private static final String ROBOTS_TXT = """
            User-agent: *
            Allow: /
            Allow: /login
            Allow: /register
            Allow: /termos
            Allow: /privacidade
            Allow: /*/agendar
            Allow: /p/*

            # Bloquear indexação de áreas restritas, autenticadas e administrativas
            Disallow: /*/home
            Disallow: /*/financeiro
            Disallow: /*/estoque
            Disallow: /*/clientes
            Disallow: /*/servicos
            Disallow: /*/usuarios
            Disallow: /*/configuracoes
            Disallow: /*/relatorios
            Disallow: /*/assinatura
            Disallow: /api/
            Disallow: /actuator/

            Sitemap: https://nexipro.com.br/sitemap.xml
            """;

    private static final String SITEMAP_XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
                <url>
                    <loc>https://nexipro.com.br/</loc>
                    <lastmod>2026-10-29</lastmod>
                    <changefreq>daily</changefreq>
                    <priority>1.0</priority>
                </url>
                <url>
                    <loc>https://nexipro.com.br/register</loc>
                    <lastmod>2026-10-29</lastmod>
                    <changefreq>weekly</changefreq>
                    <priority>0.9</priority>
                </url>
                <url>
                    <loc>https://nexipro.com.br/login</loc>
                    <lastmod>2026-10-29</lastmod>
                    <changefreq>monthly</changefreq>
                    <priority>0.8</priority>
                </url>
                <url>
                    <loc>https://nexipro.com.br/termos</loc>
                    <lastmod>2026-10-29</lastmod>
                    <changefreq>monthly</changefreq>
                    <priority>0.5</priority>
                </url>
                <url>
                    <loc>https://nexipro.com.br/privacidade</loc>
                    <lastmod>2026-10-29</lastmod>
                    <changefreq>monthly</changefreq>
                    <priority>0.5</priority>
                </url>
            </urlset>
            """;

    @GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8")
    public ResponseEntity<String> getRobotsTxt() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).cachePublic())
                .body(ROBOTS_TXT);
    }

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE + ";charset=UTF-8")
    public ResponseEntity<String> getSitemapXml() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).cachePublic())
                .body(SITEMAP_XML);
    }
}
