const CACHE_NAME = 'nexipro-cache-v1';
const STATIC_ASSETS = [
    '/css/style.css',
    '/images/nexipro-icon-official.png',
    '/images/nexipro-official-logo.png',
    '/images/nexipro-icon.svg',
    '/images/nexipro-logo.svg',
    '/manifest.json'
];

// Instalação do Service Worker & Cache de Assets Estáticos
self.addEventListener('install', (event) => {
    event.waitUntil(
        caches.open(CACHE_NAME).then((cache) => {
            return cache.addAll(STATIC_ASSETS);
        })
    );
    self.skipWaiting();
});

// Ativação e Limpeza de Caches Antigos
self.addEventListener('activate', (event) => {
    event.waitUntil(
        caches.keys().then((cacheNames) => {
            return Promise.all(
                cacheNames.map((name) => {
                    if (name !== CACHE_NAME) {
                        return caches.delete(name);
                    }
                })
            );
        })
    );
    self.clients.claim();
});

// Interceptação de Requisições: Network-First com fallback de Cache para assets estáticos
self.addEventListener('fetch', (event) => {
    const request = event.request;

    // Apenas GET requests
    if (request.method !== 'GET') return;

    // Para páginas HTML dinâmicas (multi-tenant): busca sempre da rede
    if (request.mode === 'navigate') {
        event.respondWith(
            fetch(request).catch(() => {
                return caches.match(request);
            })
        );
        return;
    }

    // Para assets estáticos: Cache First ou Network First
    const url = new URL(request.url);
    if (url.pathname.startsWith('/css/') || url.pathname.startsWith('/images/') || url.pathname.startsWith('/js/')) {
        event.respondWith(
            caches.match(request).then((cachedResponse) => {
                if (cachedResponse) {
                    // Retorna cache e atualiza em segundo plano
                    fetch(request).then((networkResponse) => {
                        if (networkResponse && networkResponse.status === 200) {
                            caches.open(CACHE_NAME).then((cache) => cache.put(request, networkResponse));
                        }
                    }).catch(() => {});
                    return cachedResponse;
                }
                return fetch(request).then((networkResponse) => {
                    if (networkResponse && networkResponse.status === 200) {
                        const responseClone = networkResponse.clone();
                        caches.open(CACHE_NAME).then((cache) => cache.put(request, responseClone));
                    }
                    return networkResponse;
                });
            })
        );
    }
});
