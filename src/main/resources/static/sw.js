const CACHE_NAME = 'Helpdesk-v2';

// Evento de Ativação: Limpa caches antigos
self.addEventListener('activate', (event) => {
    event.waitUntil(
        caches.keys().then((cacheNames) => {
            return Promise.all(
                cacheNames.map((cacheName) => {
                    if (cacheName !== CACHE_NAME) {
                        return caches.delete(cacheName);
                    }
                })
            );
        })
    );
    console.log('Service Worker: Ativado');
});

// Evento de Busca (Fetch): ÚNICO NO ARQUIVO
self.addEventListener('fetch', function(event) {
    // 🚨 A REGRA DE OURO: Ignorar tudo que não for GET (POST, PUT, DELETE passam direto pro Java)
    if (event.request.method !== 'GET') {
        return; 
    }

    // Estratégia de cache para o resto do site
    event.respondWith(
        caches.match(event.request).then(function(response) {
            return response || fetch(event.request);
        })
    );
});