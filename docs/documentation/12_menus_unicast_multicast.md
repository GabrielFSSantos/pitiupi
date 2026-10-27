# Menus e ações na lista (histórico)

Na v1 atual, plugins **não** registram menus na barra superior nem submenus legados por categoria.

## Como expor ações hoje

1. **Galeria Plugins** — abre a aba central do plugin.
2. **Lista lateral** — clique em um peer ou em **Todos** → menu com **peer actions** registradas por cada plugin (`PeerActionBroker`).
3. **`PluginWorkspace.openOrFocusTab`** — dentro do handler da peer action ou após carregar o painel.

## Catálogo `menuCategory`

O campo `menuCategory` em [`plugins.registry.json`](../../plugins.registry.json) (`Social`, `Jogos`, `Streaming`) serve apenas ao script `./scripts/pitiupi.sh` para filtrar build/run — não cria menus Swing.

Referência de plugins: [11_plugins_referencia.md](11_plugins_referencia.md).
