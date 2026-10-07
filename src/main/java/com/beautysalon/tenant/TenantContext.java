package com.beautysalon.tenant;

/**
 * Armazena o contexto do tenant (empresa) para o request HTTP atual.
 * Usa ThreadLocal para garantir isolamento entre threads.
 *
 * Uso:
 *   TenantContext.setEmpresaId(1L);   // definido pelo TenantInterceptor
 *   Long id = TenantContext.getEmpresaId(); // usado nos Services
 *   TenantContext.clear();              // limpo após o request
 */
public final class TenantContext {

    private static final ThreadLocal<Long> CURRENT_EMPRESA_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_EMPRESA_SLUG = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_EMPRESA_NOME = new ThreadLocal<>();
    private static final ThreadLocal<com.beautysalon.model.SegmentoEmpresa> CURRENT_EMPRESA_SEGMENTO = new ThreadLocal<>();

    private TenantContext() {}

    public static void setEmpresaId(Long id) {
        CURRENT_EMPRESA_ID.set(id);
    }

    public static Long getEmpresaId() {
        return CURRENT_EMPRESA_ID.get();
    }

    public static void setSlug(String slug) {
        CURRENT_EMPRESA_SLUG.set(slug);
    }

    public static String getSlug() {
        return CURRENT_EMPRESA_SLUG.get();
    }

    public static void setNome(String nome) {
        CURRENT_EMPRESA_NOME.set(nome);
    }

    public static String getNome() {
        return CURRENT_EMPRESA_NOME.get();
    }

    public static void setSegmento(com.beautysalon.model.SegmentoEmpresa segmento) {
        CURRENT_EMPRESA_SEGMENTO.set(segmento);
    }

    public static com.beautysalon.model.SegmentoEmpresa getSegmento() {
        com.beautysalon.model.SegmentoEmpresa seg = CURRENT_EMPRESA_SEGMENTO.get();
        return seg != null ? seg : com.beautysalon.model.SegmentoEmpresa.SALAO_BELEZA;
    }

    public static void setTenant(String slug, Long id) {
        CURRENT_EMPRESA_SLUG.set(slug);
        CURRENT_EMPRESA_ID.set(id);
    }

    public static void setTenant(String slug, Long id, String nome) {
        CURRENT_EMPRESA_SLUG.set(slug);
        CURRENT_EMPRESA_ID.set(id);
        CURRENT_EMPRESA_NOME.set(nome);
    }

    public static void setTenant(String slug, Long id, String nome, com.beautysalon.model.SegmentoEmpresa segmento) {
        CURRENT_EMPRESA_SLUG.set(slug);
        CURRENT_EMPRESA_ID.set(id);
        CURRENT_EMPRESA_NOME.set(nome);
        CURRENT_EMPRESA_SEGMENTO.set(segmento);
    }

    /**
     * SEMPRE deve ser chamado ao fim de cada request para evitar memory leaks.
     */
    public static void clear() {
        CURRENT_EMPRESA_ID.remove();
        CURRENT_EMPRESA_SLUG.remove();
        CURRENT_EMPRESA_NOME.remove();
        CURRENT_EMPRESA_SEGMENTO.remove();
    }
}
