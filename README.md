# GAPSI Store — Examen práctico Android

Aplicación nativa de eCommerce (Kotlin + Jetpack Compose) para buscar productos del catálogo de Walmart a través de la API **Axesso – Walmart Data Service** (RapidAPI).

## Funcionalidad

| Requisito del examen | Implementación |
|---|---|
| Buscar productos por texto | Campo de búsqueda con acción *Buscar* del teclado. La consulta se normaliza (espacios) antes de enviarse. |
| Listado con título, precio e imagen | `LazyColumn` de tarjetas con thumbnail (Coil), título y precio en USD. |
| Paginación con scroll vertical | **Paging 3**: se piden más páginas (`page=2,3…`) conforme el usuario hace scroll; termina al llegar a `maxPage`, con una página vacía o con un 404. |
| Historial de búsquedas persistente | **DataStore** tipado (JSON). Sobrevive reinicios; sin duplicados (ignora mayúsculas), lo más reciente primero, máximo 20; se puede eliminar una búsqueda o todo. |
| UI que no se bloquee | Corrutinas + Flow; red y disco fuera del hilo principal. Estados de carga, vacío, error con *Reintentar* y fin de lista. |
| Android 12 (API 31)+ | `minSdk = 31`. |

## Arquitectura

Clean Architecture ligera + MVVM con flujo unidireccional de datos:

```
ui/          Compose (SearchScreen, componentes) + SearchViewModel (StateFlow + PagingData)
domain/      Modelos, errores tipados (SearchException) y contratos de repositorio
data/
  remote/    Retrofit API, interceptor de RapidAPI, DTOs tolerantes, mapper, mapeo de errores
  paging/    ProductPagingSource (paginación por número de página, dedupe entre páginas)
  local/     DataStore del historial + reglas puras (SearchHistoryPolicy)
  repository Implementaciones de los repositorios
di/          Módulos de Koin
```

Decisiones relevantes:

- **Parseo robusto:** la respuesta de Axesso es el payload de Walmart (grande y sin versión). Sólo se modelan los campos necesarios, todos opcionales, y cada producto se decodifica por separado: un elemento con formato inesperado (anuncios, placeholders) se descarta sin perder el resto de la página. El precio usa varios *fallbacks* (`price` → `priceInfo.currentPrice` → `linePrice`).
- **Errores tipados:** la capa de datos traduce `IOException`, `HttpException` y `SerializationException` a `SearchException` (sin conexión, timeout, key inválida, límite de cuota 429, error de servidor, respuesta inesperada). La UI muestra un mensaje accionable para cada caso.
- **Estado que sobrevive:** el texto y la última búsqueda viven en `SavedStateHandle`; el `PagingData` se cachea con `cachedIn(viewModelScope)` para rotaciones.
- **Koin sin generación de código:** el proyecto usa AGP 9 con Kotlin integrado; Koin evita depender de kapt/KSP y mantiene compilaciones rápidas.
- **Secretos fuera del repositorio:** la API key se inyecta en `BuildConfig` desde `local.properties` (ignorado por git).

## Cómo compilar y probar

Requisitos: Android Studio reciente (compatible con AGP 9.4), JDK 17+ y un emulador o dispositivo con Android 12 o superior.

1. Clona el repositorio y ábrelo en Android Studio.
2. Agrega la API key en `local.properties` (en la raíz del proyecto):
   ```properties
   RAPIDAPI_KEY=tu_api_key
   ```
   También se acepta como propiedad de Gradle (`-PRAPIDAPI_KEY=...`) o variable de entorno `RAPIDAPI_KEY`. Si falta, la app compila y muestra un mensaje indicando cómo configurarla.
3. Ejecuta la configuración `app` en un emulador o dispositivo.
4. Prueba búsquedas en inglés o por marca: `nintendo`, `sony`, `computer`, `lego`.

Pruebas unitarias:

```bash
./gradlew testDebugUnitTest
```

Cubren el mapeo de la respuesta (con un JSON de ejemplo), el `PagingSource` (siguiente página, fin de lista, duplicados, 404 y errores), el mapeo de errores, las reglas del historial y el `SearchViewModel`.

## Stack

Kotlin · Jetpack Compose (Material 3) · Coroutines/Flow · Paging 3 · Retrofit + OkHttp · kotlinx.serialization · DataStore · Coil 3 · Koin · JUnit 4 + kotlinx-coroutines-test
