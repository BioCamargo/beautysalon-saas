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
    private static final ThreadLocal<com.beautysalon.model.PlanoSaaS> CURRENT_EMPRESA_PLANO = new ThreadLocal<>();
    private static final ThreadLocal<com.beautysalon.model.StatusAssinatura> CURRENT_EMPRESA_STATUS_ASSINATURA = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_EMPRESA_LOGO_URL = new ThreadLocal<>();
    private static final ThreadLocal<Long> CURRENT_EMPRESA_DIAS_RESTANTES_TRIAL = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> CURRENT_EMPRESA_TRIAL_ATIVO = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_EMPRESA_META_PIXEL_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_EMPRESA_GOOGLE_ADS_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_EMPRESA_GOOGLE_ANALYTICS_ID = new ThreadLocal<>();

    private TenantContext() {}

    public static void setLogoUrl(String logoUrl) {
        CURRENT_EMPRESA_LOGO_URL.set(logoUrl);
    }

    public static String getLogoUrl() {
        return CURRENT_EMPRESA_LOGO_URL.get();
    }

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

    public static void setPlano(com.beautysalon.model.PlanoSaaS plano) {
        CURRENT_EMPRESA_PLANO.set(plano);
    }

    public static com.beautysalon.model.PlanoSaaS getPlano() {
        com.beautysalon.model.PlanoSaaS p = CURRENT_EMPRESA_PLANO.get();
        return p != null ? p : com.beautysalon.model.PlanoSaaS.STUDIO;
    }

    public static void setStatusAssinatura(com.beautysalon.model.StatusAssinatura status) {
        CURRENT_EMPRESA_STATUS_ASSINATURA.set(status);
    }

    public static com.beautysalon.model.StatusAssinatura getStatusAssinatura() {
        com.beautysalon.model.StatusAssinatura s = CURRENT_EMPRESA_STATUS_ASSINATURA.get();
        return s != null ? s : com.beautysalon.model.StatusAssinatura.TRIAL;
    }

    public static void setDiasRestantesTrial(Long dias) {
        CURRENT_EMPRESA_DIAS_RESTANTES_TRIAL.set(dias);
    }

    public static Long getDiasRestantesTrial() {
        Long d = CURRENT_EMPRESA_DIAS_RESTANTES_TRIAL.get();
        return d != null ? d : 0L;
    }

    public static void setTrialAtivo(Boolean ativo) {
        CURRENT_EMPRESA_TRIAL_ATIVO.set(ativo);
    }

    public static Boolean isTrialAtivo() {
        Boolean b = CURRENT_EMPRESA_TRIAL_ATIVO.get();
        return b != null ? b : false;
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

    public static void setMetaPixelId(String metaPixelId) {
        CURRENT_EMPRESA_META_PIXEL_ID.set(metaPixelId);
    }

    public static String getMetaPixelId() {
        return CURRENT_EMPRESA_META_PIXEL_ID.get();
    }

    public static void setGoogleAdsId(String googleAdsId) {
        CURRENT_EMPRESA_GOOGLE_ADS_ID.set(googleAdsId);
    }

    public static String getGoogleAdsId() {
        return CURRENT_EMPRESA_GOOGLE_ADS_ID.get();
    }

    public static void setGoogleAnalyticsId(String googleAnalyticsId) {
        CURRENT_EMPRESA_GOOGLE_ANALYTICS_ID.set(googleAnalyticsId);
    }

    public static String getGoogleAnalyticsId() {
        return CURRENT_EMPRESA_GOOGLE_ANALYTICS_ID.get();
    }

    /**
     * SEMPRE deve ser chamado ao fim de cada request para evitar memory leaks.
     */
    public static void clear() {
        CURRENT_EMPRESA_ID.remove();
        CURRENT_EMPRESA_SLUG.remove();
        CURRENT_EMPRESA_NOME.remove();
        CURRENT_EMPRESA_LOGO_URL.remove();
        CURRENT_EMPRESA_SEGMENTO.remove();
        CURRENT_EMPRESA_PLANO.remove();
        CURRENT_EMPRESA_STATUS_ASSINATURA.remove();
        CURRENT_EMPRESA_DIAS_RESTANTES_TRIAL.remove();
        CURRENT_EMPRESA_TRIAL_ATIVO.remove();
        CURRENT_EMPRESA_META_PIXEL_ID.remove();
        CURRENT_EMPRESA_GOOGLE_ADS_ID.remove();
        CURRENT_EMPRESA_GOOGLE_ANALYTICS_ID.remove();
    }
}
