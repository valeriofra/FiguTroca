# FiguTroca ⚽🏆

App Android para **organizar coleções de figurinhas** e, no futuro, **colecionar
figurinhas digitais originais** — com packs, raridades, missões e um mercado de
troca. Funciona hoje **100% offline** (organizador); as partes colecionáveis são
projetadas para rodar online numa fase seguinte.

---

## 1) O que o app é hoje (nível grátis — organizador)

Um organizador da sua **coleção física** (ex.: Copa do Mundo), **sem internet,
sem cadastro**, com seus dados guardados só no aparelho.

- **Tela inicial:** cada seleção é um **card** com **bandeira** em destaque,
  **sigla**, nome em **inglês e português** e **cor da seleção**, na **ordem do
  álbum**, com barra de progresso. **Busca discreta** por sigla/nome.
- **3 seções** (botões no topo):
  - **Tenho (Álbum):** mostra tudo; um toque marca/desmarca.
  - **Faltam:** só o que falta; **ao tocar, a figurinha entra no álbum e sai da
    lista** de faltantes.
  - **Repetidas:** só as repetidas, com **bolinha vermelha** = quantas de sobra;
    **cada toque soma +1**.
- **Janela da seleção:** figurinhas como **retângulos** (cor da seleção quando
  você tem; vazio quando falta). Abre **travada** (cadeado 🔒) para não mudar
  nada por acidente; botão **+** para adicionar por **intervalo**.
- **Listas de troca:** **Copiar / Enviar** em Faltam e Repetidas, uma seleção
  por linha, na ordem do álbum, pronta para o WhatsApp.
- **Importar** lista de texto (ícone de colar, no topo).
- **Seções especiais:** FIFA World Cup, Coca-Cola, Legends (⭐).
- **Coleções:** criar, renomear, **arquivar a Copa atual e começar uma nova**,
  reabrir arquivadas.
- **Backup:** exportar para **onde quiser** e restaurar.
- **Extras:** atualiza **por cima sem perder dados** (assinatura fixa); tema
  claro/escuro; identidade visual da Copa 2026.

> Quando o app tiver marca própria, o organizador de Copas passa a ser **um item
> de menu** ("Organizador de álbuns físicos"), e o foco vira as coleções
> exclusivas.

---

## 2) Visão do produto (nível colecionável — futuro)

Um jogo de **figurinhas digitais originais** (desenhadas pelo autor), com temas
de **domínio público / arte própria** — sem infringir direitos autorais.
Ex. de temas para o público infantil/pré-adolescente: **Reis Lendários,
Animais Poderosos, Mitologias, Heróis Bíblicos, Folclore, Dinossauros, Dragões**,
e o mais valioso: **um universo original próprio**.

### Repositório de coleções (curado)
- **"Nova coleção" vira um catálogo:** o usuário só **baixa** coleções que o
  **autor publica** — ninguém cria coleção por conta própria.
- As **Copas do Mundo** são só **listagens** (texto), grátis, servindo de
  **isca** para as coleções exclusivas.

### Economia (packs, raridades, completável)
- **Packs de 7 figurinhas** (comprar ou ganhar).
- **Raridades:** Comum · Incomum · Rara · Épica · Lendária · **Especiais Extra**.
- **Todas** as figurinhas existem no **plano grátis** (dá para completar) — o que
  muda é a **frequência**: raras/lendárias mais difíceis no grátis.
- **Especiais Extra:** grupo ultra-raro, **fonte principal no pago**, com
  **pouquíssimas unidades soltas no grátis** (cria a sensação de que dá para
  completar sem pagar, embora difícil).
- **Sem frustração:** sistema **"pity"** (garantia de rara a cada X packs) e
  **repetidas viram fragmentos** para **forjar** uma figurinha escolhida.
- **Fontes grátis:** pack diário, missões/quiz, streak, (opcional) anúncio.

### Mercado de troca
- Cada figurinha exclusiva tem **número de série único** → **trocar = transferir
  a posse** entre contas.
- Fluxo: **"Ofereço X · Quero Y"** → quem tem Y e aceita fecha a troca; o
  **servidor troca de forma atômica** (ou os dois, ou nada).
- **Sem dinheiro entre usuários** e **sem chat livre** (proteção ao menor).

### Autenticidade (anti-falsificação) — camadas
1. **Assinatura digital:** todo arquivo é assinado com a **chave secreta do
   autor**; o app verifica com a **chave pública embutida**. Ninguém cria
   conteúdo "verdadeiro" sem a chave. Funciona offline.
2. **Marca d'água invisível** na arte (esteganografia) com série/assinatura.
3. **Número de série único ("cunhagem")** por cópia, emitido pelo servidor.
4. **Permissão (entitlement)** assinada para desbloqueios (grátis/pago).

### Monetização (com responsabilidade)
- Grátis: organizadores + algumas coleções + missões fáceis.
- Pago (Google Play Billing): coleções exclusivas, **packs**, missões pagas,
  passe premium.
- ⚠️ **Público infantil exige cuidado:** a Play Store pede **divulgar as
  probabilidades** de itens aleatórios; **loot box paga** é restrita/malvista
  para menores; política **"Famílias"** exige **trava parental**, anúncios
  adequados e coleta mínima de dados (LGPD/COPPA). Caminho seguro: **pagos
  determinísticos** (comprar a figurinha específica / moeda para forjar / packs
  com conteúdo garantido), deixando o aleatório mais no grátis.

---

## 3) Como as coleções são geradas (arquivos)

Uma coleção é um **arquivo JSON**. O app lê e monta o álbum. Formato:

```json
{
  "schema": 1,
  "id": "copa-2026",
  "name": "Copa do Mundo 2026",
  "cover": "🏆",
  "type": "listing",
  "version": 1,
  "sections": [
    { "code": "BRA", "name": "Brasil", "nameEn": "Brazil", "iso2": "BR", "color": "#009C3B", "from": 1, "to": 20 },
    { "code": "FWC", "name": "FIFA World Cup", "color": "#0067B1", "icon": "🏆", "labels": ["00", "1", "2"] }
  ]
}
```

- `type`: `listing` (organizador grátis) ou `collectible` (economia).
- Seção por **intervalo** (`from`/`to`) ou **labels** explícitas.
- Coleções `collectible` devem vir **assinadas** (ver autenticidade).

O **catálogo** é um índice publicado num lugar público (ex.: GitHub Pages):

```json
{ "collections": [
  { "id": "copa-2026", "name": "Copa do Mundo 2026", "cover": "🏆", "url": "…/copa-2026.json", "free": true }
]}
```

---

## 4) Roadmap por fases

1. **Catálogo + baixar coleção** (offline) e **verificação por assinatura**.
2. **Contas + backend** (Firebase/Supabase): inventário de séries, abertura de
   pack no servidor (anti-trapaça), entitlements.
3. **Economia:** packs de 7, raridades, pity, forjar com fragmentos.
4. **Mercado de troca** (transferência de série, atômica).
5. **Compras no app** e coleções premium + checklist legal "Famílias".

---

## 5) Estrutura do código (base preparada)

```
app/src/main/java/com/figutroca/app/
├── data/        # Room: entidades, DAOs, banco, repositório (organizador atual)
├── ui/          # Telas Compose (álbum, coleções, componentes)
├── util/        # geração de listas de troca
├── catalog/     # NOVO — formato de arquivo de coleção + catálogo (baixar)
├── economy/     # NOVO — raridades e modelos da economia (futuro)
└── security/    # NOVO — verificação de assinatura (anti-falsificação)
```

As pastas `catalog/`, `economy/` e `security/` são a **base** das novas ideias:
formatos e utilitários já organizados, sem alterar o banco atual.

---

## 6) Como compilar

Requer Android SDK. `./gradlew assembleDebug` (ou abra no Android Studio).
Todo push gera o APK via **GitHub Actions** (aba *Actions* → artefato
`FiguTroca-debug-apk`).
