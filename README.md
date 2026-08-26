# FiguTroca ⚽

App Android nativo e **100% offline** para gerenciar sua coleção de figurinhas
da Copa do Mundo: controle o que você já tem, o que está repetido para trocar e
o que ainda falta — e gere listas prontas para colar no WhatsApp.

## Funcionalidades

- **Álbum**: grade visual de todas as figurinhas, com estados por cor
  - 🟢 verde = você tem
  - 🟡 âmbar (com selo `+N`) = você tem repetidas
  - ⚪ cinza = falta
  - Toque para adicionar uma figurinha, **segure** para remover.
  - Barra de progresso e contadores de *Tenho / Faltam / Repetidas*.
  - Busca por número/seleção e filtros (Todas · Tenho · Faltam · Repetidas).
- **Repetidas**: lista de troca com botões `+`/`−` por figurinha e um texto
  pronto para **Copiar** ou **Compartilhar** (ex.: `Repetidas (4): 7, 7, 12, 30`).
- **Faltam**: mesma ideia para o que você precisa (ex.: `Faltam (3): 5, 18, 220`).
- **Coleções**:
  - Criar nova coleção (com um total numerado, ex.: 1 a 670).
  - **Arquivar** a coleção atual e **iniciar uma nova** para a próxima Copa.
  - Reabrir ou excluir coleções arquivadas.
- **Adicionar figurinhas** (botão `+` na aba Álbum):
  - Por **intervalo** numérico (ex.: `1` a `670`), ou
  - Por **códigos avulsos** (ex.: `ARG1, ARG2, FWC, 101`),
  - Com um grupo/seleção opcional (ex.: `Brasil`).

Como o álbum oficial da Copa 2026 ainda pode mudar de tamanho, o total de
figurinhas é configurável — o padrão sugerido é 670.

## Como o modelo de dados funciona

Cada figurinha guarda quantas cópias você possui (`count`):

| count | significado |
|------:|-------------|
| 0     | falta       |
| 1     | você tem    |
| 2+    | você tem + `count − 1` repetidas para troca |

## Stack técnica

- **Kotlin** + **Jetpack Compose** (Material 3, tema claro/escuro + cores dinâmicas)
- **Room** para persistência local (nenhum servidor, nenhum login)
- Arquitetura simples: `Repository` → `AppViewModel` → telas Compose
- `minSdk 26`, `targetSdk 35`

## Como compilar

Requer o Android SDK instalado (via Android Studio ou command-line tools).

```bash
# aponte para o seu SDK
echo "sdk.dir=/caminho/para/Android/Sdk" > local.properties

# gere o APK de debug
./gradlew assembleDebug
# APK em: app/build/outputs/apk/debug/app-debug.apk

# ou instale direto num dispositivo/emulador conectado
./gradlew installDebug
```

Ou simplesmente abra a pasta no **Android Studio** e clique em *Run*.

## Estrutura

```
app/src/main/java/com/figutroca/app/
├── MainActivity.kt
├── data/            # Room: entidades, DAOs, banco, repositório
│   ├── Entities.kt
│   ├── Daos.kt
│   ├── AppDatabase.kt
│   └── Repository.kt
├── ui/
│   ├── AppViewModel.kt
│   ├── FiguTrocaApp.kt        # Scaffold + navegação por abas
│   ├── screens/               # Álbum, Listas (Repetidas/Faltam), Coleções
│   ├── components/            # célula de figurinha, sheet de adicionar, etc.
│   └── theme/
└── util/ShareLists.kt         # geração das listas de troca
```
