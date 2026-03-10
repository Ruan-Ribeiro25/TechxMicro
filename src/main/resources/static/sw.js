const CACHE_NAME = 'Helpdesk-v2';

// Evento de Instalação: O navegador reconhece que é um PWA
self.addEventListener('install', (event) => {
  self.skipWaiting(); // Força a ativação imediata
  console.log('Service Worker: Instalado');
});

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

// Evento de Busca (Fetch): ESTRATÉGIA SEGURA (Network Only)
// Isso garante que o site só carregue se tiver internet e evita o erro ERR_FAILED
self.addEventListener('fetch', (event) => {
  // Apenas deixa o navegador buscar a página normalmente na internet
  event.respondWith(fetch(event.request));
});