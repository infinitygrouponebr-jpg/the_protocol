# The Protocol / Survival Professions

## Plataforma e arquitetura

- Minecraft `1.21.1`, NeoForge `21.1.x`, Java `21`; nunca usar APIs, eventos, assets ou estruturas de 1.21.4+.
- Mod ID: `the_protocol`. Nome: `Survival Professions`.
- O foco é profissões, sobrevivência, progressão, perks e compatibilidade segura com outros mods.
- Preserve a arquitetura existente. Faça mudanças pequenas, focadas e testáveis; não reescreva ou refatore código fora do escopo.
- Antes de editar, inspecione os arquivos relevantes e confirme nomes reais de classes, métodos, eventos e packages. Para dependências externas, confira o JAR/versão real antes de usar APIs.
- Não esconda falhas de build. Corrija a causa raiz e mantenha logs claros, apenas quando necessários.

## Economia de tokens e contexto

- Leia apenas os arquivos necessários; não imprima arquivos inteiros quando um trecho basta.
- Responda objetivamente e não explique conceitos básicos sem necessidade.
- Não repita planos, não faça relatórios grandes para mudanças pequenas e não refatore código fora da tarefa.
- Não abra dependências externas sem necessidade. Ao pedir contexto, peça somente o mínimo.
- Ao encontrar um erro, investigue primeiro sua causa raiz. Priorize correção funcional e build limpo.

## Código NeoForge

- Use eventos, attachments, attributes e configs quando forem a solução adequada; não execute lógica pesada a cada tick.
- Mantenha o servidor dedicado compatível. Código comum/server não pode importar `Minecraft`, `LocalPlayer`, `KeyMapping`, `GuiGraphics` ou outras classes client.
- Isole código client e registre-o somente no lado client.
- Use `ResourceLocation.fromNamespaceAndPath(...)` quando aplicável. Confirme construtores de `AttributeModifier` e nomes de eventos na API 1.21.1 antes de usar.
- Evite reflection e mixins; use-os apenas quando não existir evento ou API pública limpa.
- Registre corretamente eventos, itens, configs e attachments.

## Profissões e persistência

Profissões atuais: `NONE`, `SHOOTER`, `PARKOUR`, `MECHANIC`, `MEDIC`, `ENGINEER`, `DRIVER` e `SURVIVOR`.

- Consulte dados com `ProfessionManager.get(player)`.
- Altere profissão com `ProfessionManager.setProfession(player, profession)` ou `ProfessionManager.reset(player)`.
- Comandos devem chamar managers/services; não devem conter regras de gameplay.
- Os dados persistem em `PlayerProfessionProvider.PROFESSION_DATA`: profissão, `level`, `experience` e `perkPoints`. Nunca apague progresso sem pedido explícito.

## Configuração e comandos

- Configs ficam em `CommonConfig.java`. Recursos opcionais devem ter config em vez de valores importantes hardcoded.
- Preserve as configs atuais: `ENABLE_PROFESSION_SYSTEM`, `ENABLE_DEBUG_COMMANDS`, `ENABLE_TACZ_COMPAT`, `ENABLE_VEHICLE_COMPAT`, `ENABLE_MOVEMENT_PERKS`, `STARTING_PROFESSION`, `MAX_PROFESSION_LEVEL` e `XP_PER_LEVEL_BASE`.
- Comandos debug respeitam `ENABLE_DEBUG_COMMANDS`; comandos administrativos exigem permissão adequada.
- Mensagens para jogadores devem ser claras e, quando possível, traduzíveis.

## Compatibilidade externa

- Coloque integrações em `Infinitygroup.the_protocol.compat` e isole cada mod.
- Para dependências opcionais, confira presença do mod antes de acessar sua API. Não importe classes opcionais em código sempre carregado.
- Não copie código de mods externos; respeite licenças. Prefira APIs públicas, registries, attributes e eventos.

### Combat Roll

- Mod ID: `combat_roll`; use uma versão NeoForge compatível com Minecraft 1.21.1.
- Combat Roll mantém tecla, movimento, animação, partículas, sons, cooldown, HUD e sincronização. The Protocol controla somente a permissão.
- `PARKOUR` é a profissão de roll; outras profissões não podem recebê-lo por acidente.
- Prefira a API/attribute público de quantidade de rolls. Use modificador transitório, nunca altere permanentemente o valor base quando isso for evitável.
- Não crie outro `KeyMapping`, nem simule roll com `player.push`, `setDeltaMovement` ou teleport.

## Assets e resources

- Em 1.21.1, não use estrutura de item model de versões 1.21.4+.
- Assets ficam em `src/main/resources/assets/the_protocol`.
- Ao criar texto visível, atualize `en_us.json` e `pt_br.json`. Evite texto hardcoded exceto debug temporário.

## Build e validação

- Sempre tente `gradlew.bat clean build` após mudanças.
- Se afetar client, tente `gradlew.bat runClient`; se afetar código comum/server, tente `gradlew.bat runServer`.
- Se algum teste não puder ser concluído, informe exatamente a causa.
- Teste `/profession` quando a alteração puder afetar profissão, comando, persistência ou multiplayer.

## Prevenção de erros recorrentes

- Não misture Fabric com NeoForge nem use APIs da versão errada do Minecraft.
- Não quebre servidor dedicado com imports client, nem multiplayer com estado local ou referências estáticas a jogadores.
- Não duplique sistemas existentes, não apague attachments/progresso, não rode trabalho pesado por tick e não crie mixin se evento/API resolver.
- Não esqueça registros, traduções, configs e validação de build.

## Relatório final curto

Use, quando aplicável:

```text
Resumo:
Arquivos alterados:
Build/testes:
Riscos:
O que testar no jogo:
```
