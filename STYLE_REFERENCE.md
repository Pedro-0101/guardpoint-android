# GuardPoint Style Reference

> Documento oficial de referência visual para agentes de IA e desenvolvedores.
> Este documento traduz o design system do **guardpoint-manager** (web) para o ecossistema Android nativo (XML + Material Design 3).
>
> **Sempre consulte este documento antes de criar ou modificar qualquer componente visual.**

---

## 1. Identidade Visual

| Atributo | Valor |
|---|---|
| **Estilo** | Moderno, minimalista, neutro-tonalizado |
| **Framework Android** | Material Design 3 (Material You) via `com.google.android.material` |
| **Inspiração primária** | shadcn/ui + Tailwind v4 + Zard UI (do guardpoint-manager) |
| **Sistema de cores** | OKLCH (adaptado para MD3 color roles) |
| **Renderização** | Android Views (XML) — Sem Jetpack Compose |

---

## 2. Paleta de Cores

### 2.1 Cores Base (Material Design 3)

As cores seguem o sistema de roles do Material 3, mapeadas do design system OKLCH do guardpoint-manager.

| MD3 Role | HEX | Uso |
|---|---|---|
| `primary` | `#1A1A2E` | Neutro escuro (botões, links, acentos primários) |
| `onPrimary` | `#FFFFFF` | Texto sobre primary |
| `primaryContainer` | `#F0F0F5` | Superfície clara do primary |
| `onPrimaryContainer` | `#1A1A2E` | Texto sobre primaryContainer |
| `secondary` | `#E8E8EE` | Superfície secundária sutil |
| `onSecondary` | `#1A1A2E` | Texto sobre secondary |
| `secondaryContainer` | `#F5F5F8` | Container secundário |
| `onSecondaryContainer` | `#1A1A2E` | Texto sobre secondaryContainer |
| `tertiary` | `#E0E0E8` | Terciário (sidebar, superfícies) |
| `onTertiary` | `#1A1A2E` | Texto sobre tertiary |
| `background` | `#FAFAFA` | Fundo da página |
| `onBackground` | `#1A1A2E` | Texto sobre background |
| `surface` | `#FFFFFF` | Superfície de cards, modais |
| `onSurface` | `#1A1A2E` | Texto sobre surface |
| `surfaceVariant` | `#F5F5F8` | Variante de superfície |
| `onSurfaceVariant` | `#6B7280` | Texto secundário/muted |
| `outline` | `#D4D4D8` | Bordas e divisores |
| `outlineVariant` | `#E5E5E8` | Borda mais clara |
| `error` | `#D32F2F` | Erro, perigo, deletar |
| `onError` | `#FFFFFF` | Texto sobre error |
| `errorContainer` | `#FEE2E2` | Container de erro |
| `onErrorContainer` | `#991B1B` | Texto no container de erro |
| `inverseSurface` | `#1A1A2E` | Superfície inversa (dark mode) |

### 2.2 Cores Semânticas (Status)

| Token | HEX | Uso |
|---|---|---|
| `success` | `#16A34A` | Check-in OK, sucesso, verde |
| `onSuccess` | `#FFFFFF` | Texto sobre success |
| `successContainer` | `#DCFCE7` | Fundo de badge sucesso |
| `warning` | `#D97706` | Atenção, warning, âmbar |
| `onWarning` | `#FFFFFF` | Texto sobre warning |
| `warningContainer` | `#FEF3C7` | Fundo de badge warning |
| `info` | `#2563EB` | Informativo, azul |
| `onInfo` | `#FFFFFF` | Texto sobre info |
| `infoContainer` | `#DBEAFE` | Fundo de badge info |

### 2.3 Cores do Sidebar (Navegação)

| Token | HEX | Uso |
|---|---|---|
| `sidebarBackground` | `#FFFFFF` | Fundo da sidebar |
| `sidebarForeground` | `#1A1A2E` | Texto da sidebar |
| `sidebarItemActive` | `#F0F0F5` | Item ativo na sidebar |
| `sidebarItemHover` | `#F5F5F8` | Hover do item |
| `sidebarIconActive` | `#1A1A2E` | Ícone ativo |
| `sidebarIconInactive` | `#9CA3AF` | Ícone inativo |
| `sidebarBorder` | `#E5E5E8` | Borda da sidebar |
| `sidebarFooterBg` | `#FAFAFA` | Fundo do rodapé da sidebar |

### 2.4 Cores de Alerta por Gravidade

| Gravidade | Badge BG | Badge Text | Card Border | Uso |
|---|---|---|---|---|
| `baixa` | `#DCFCE7` | `#16A34A` | `#16A34A33` | Verde claro |
| `media` | `#FEF3C7` | `#D97706` | `#D9770633` | Âmbar claro |
| `alta` | `#FEE2E2` | `#DC2626` | `#DC262633` | Vermelho claro |
| `critica` | `#FEE2E2` | `#B91C1C` | `#B91C1C66` | Vermelho intenso + borda mais forte + pulse |

### 2.5 Dark Mode

Aplicar classe `.dark` no `<html>` (web) → `values-night/themes.xml` (Android).

| MD3 Role | Dark HEX | Equivalente OKLCH Web |
|---|---|---|
| `background` | `#121212` | oklch(0.141 0 0) |
| `surface` | `#1E1E2E` | oklch(0.21 0.006 285.885) |
| `primary` | `#E8E8EE` | oklch(0.92 0.004 286.32) |
| `onPrimary` | `#1A1A2E` | oklch(0.21 0.006 285.885) |
| `surfaceVariant` | `#2A2A3E` | oklch(0.274 0.006 286.033) |
| `onSurfaceVariant` | `#9CA3AF` | oklch(0.705 0.015 286.067) |
| `outline` | `#3A3A4E` | oklch(0.35 0.006 286) |

---

## 3. Tipografia

### 3.1 Font Stack

Android Material Design 3 usa **Roboto** como padrão. Para alinhar com o guardpoint-manager (que usa **Geist**), utilizar:

| Uso | Font Family | Fallback |
|---|---|---|
| **Títulos / UI** | `sans-serif` (Roboto) | Android padrão |
| **Código / Monospace** | `monospace` | Android padrão |
| **Valores de KPI / Números** | `sans-serif` com `font-variant-numeric: tabular-nums` | — |

> **Nota:** Como o Android não suporta Geist nativamente, mantemos Roboto (padrão MD3).
> Caso deseje Geist, é necessário baixar a fonte e adicionar em `res/font/`.

### 3.2 Type Scale (Material Design 3)

| Style | Size (sp) | Weight | Line Height | Uso |
|---|---|---|---|---|
| `displayLarge` | 57 | 400 | 64px | — |
| `displayMedium` | 45 | 400 | 52px | — |
| `displaySmall` | 36 | 400 | 44px | Telas de boas-vindas |
| `headlineLarge` | 32 | 600 | 40px | Título de página |
| `headlineMedium` | 28 | 600 | 36px | Valor de KPI / timer |
| `headlineSmall` | 24 | 600 | 32px | Título de card / saudação |
| `titleLarge` | 22 | 600 | 28px | Nome do posto / seção |
| `titleMedium` | 16 | 600 | 24px | Subtítulo / label de seção |
| `titleSmall` | 14 | 600 | 20px | Título de item compacto |
| `labelLarge` | 14 | 500 | 20px | Botões, labels de status |
| `labelMedium` | 12 | 500 | 16px | Badges, timestamps, meta |
| `labelSmall` | 11 | 500 | 16px | Gravidade, tags menores |
| `bodyLarge` | 16 | 400 | 24px | Corpo de texto |
| `bodyMedium` | 14 | 400 | 20px | Texto padrão |
| `bodySmall` | 12 | 400 | 16px | Texto auxiliar |

> **Base:** 14sp (bodyMedium) — menor que o padrão 16sp para densidade informacional.

---

## 4. Cantos (Border Radius)

| Token | dp | Uso |
|---|---|---|
| `--radius-none` | 0 | — |
| `--radius-sm` | 4 | Botões, inputs, links da sidebar |
| `--radius-md` | 6 | Containers de card, tabelas, modais |
| `--radius-lg` | 8 | Cards padrão (MD3 `cornerSize`) |
| `--radius-xl` | 12 | Containers grandes, cards de KPI |
| `--radius-full` | 9999 | Avatares, botões de ícone, pills |

Mapeamento Material 3:
- **ShapeAppearance.SmallComponent**: `--radius-sm` (4dp)
- **ShapeAppearance.MediumComponent**: `--radius-lg` (8dp)
- **ShapeAppearance.LargeComponent**: `--radius-xl` (12dp)

---

## 5. Elevação e Sombras

| Token | dp (elevation) | Uso |
|---|---|---|
| `--shadow-none` | 0 | Superfícies planas |
| `--shadow-sm` | 1–2 | Cards, tabelas |
| `--shadow-md` | 4 | Cards em hover, modais, bottom sheets |
| `--shadow-lg` | 8 | Diálogos, floating panels |
| `--shadow-xl` | 12 | Nav drawer elevado |

> MD3 usa `android:elevation` + `outlineAmbientShadowColor`/`outlineSpotShadowColor`.
> Sombras devem ser sutis (baixa opacidade).

---

## 6. Espaçamentos

| Token | dp | Uso |
|---|---|---|
| `--space-1` | 4 | Íntimo (gap entre ícone e texto) |
| `--space-2` | 8 | Compacto (padding de chip, badge) |
| `--space-3` | 12 | Itens de lista, nav link |
| `--space-4` | 16 | Padding padrão de layout |
| `--space-5` | 20 | Header horizontal padding |
| `--space-6` | 24 | Padding de card, margem de página |
| `--space-8` | 32 | Dashboard padding, grid gap |
| `--space-10` | 40 | Seções grandes |
| `--space-12` | 48 | Top section spacing |

---

## 7. Componentes (Zard UI → Android)

Cada componente do guardpoint-manager (web) tem um correspondente Android:

### 7.1 Botão (`z-button` → `MaterialButton`)

| Variant | MD3 Style | Uso |
|---|---|---|
| `default` | `style="@style/Widget.Material3.Button"` | Ação primária |
| `secondary` | `style="@style/Widget.Material3.Button.OutlinedButton"` | Ação secundária |
| `outline` | `style="@style/Widget.Material3.Button.OutlinedButton"` | Terciário |
| `ghost` | `style="@style/Widget.Material3.Button.TextButton"` | Ação sutil |
| `destructive` | `style="@style/Widget.Material3.Button"` com cor error | Deletar, perigo |
| `link` | `style="@style/Widget.Material3.Button.TextButton"` | Link |

Tamanhos:
| Size | Height | Corner |
|---|---|---|
| `sm` | 32dp | 4dp |
| `default` | 40dp | 6dp |
| `lg` | 48dp | 8dp |
| `icon` | 40×40dp | `--radius-full` |

### 7.2 Card (`z-card` → `MaterialCardView`)

| Variant | Elevation | Corner | Padding |
|---|---|---|---|
| `default` | 1dp | 8dp | 24dp |
| `interactive` | 4dp (hover: 6dp) | 8dp | 24dp |
| `compact` | 1dp | 6dp | 16dp |

### 7.3 Badge (`z-badge` → `com.google.android.material.chip.Chip` ou TextView stylizado)

| Variant | Corner | Padding | Fundo |
|---|---|---|---|
| `default` | 4dp | 8dp h / 4dp v | `@color/primaryContainer` |
| `secondary` | 4dp | 8dp h / 4dp v | `@color/surfaceVariant` |
| `destructive` | 4dp | 8dp h / 4dp v | `@color/errorContainer` |
| `outline` | 4dp | 8dp h / 4dp v | Transparente com borda |
| `pill` | 9999dp | 12dp h / 4dp v | Variável |

### 7.4 Input (`z-input` → `TextInputLayout` + `TextInputEditText`)

| Variant | Corner | Stroke |
|---|---|---|
| `default` | 4dp (box) | `@color/outline` → `@color/primary` (focus) |
| `outline` | 4dp (box) | `@color/outline` |
| `ghost` | 4dp | Sem borda |

### 7.5 Tabela (`z-table` → `TableLayout` ou `RecyclerView`)

| Variant | Cell Padding |
|---|---|
| `compact` | 8dp h / 4dp v |
| `default` | 16dp h / 8dp v |
| `comfortable` | 24dp h / 16dp v |

### 7.6 Sidebar / Navigation Drawer

| Propriedade | Valor |
|---|---|
| Largura expandida | 260dp |
| Largura colapsada | 64dp |
| Padding brand | 16dp h / 20dp v |
| Padding nav link | 12dp h / 10dp v |
| Gap ícone-texto | 12dp |
| Fundo | `@color/sidebarBackground` |
| Item ativo | `@color/sidebarItemActive` |

### 7.7 Header / TopAppBar

| Propriedade | Valor |
|---|---|
| Altura | 64dp |
| Padding horizontal | 20dp |
| Ícone avatar | 40×40dp, `--radius-full` |

---

## 8. Transições e Animações

| Animação | Duração | Interpolação | Uso |
|---|---|---|---|
| `--transition-fast` | 150ms | ease | Hover, cor, estado |
| `--transition-normal` | 250ms | ease | Sidebar expandir/recolher |
| `--transition-slow` | 350ms | ease | Transições de tela |
| **Pulse (coação)** | 1–1.5s | ease-in-out infinite | Alerta de coerção/emergência |
| **Shake (erro)** | 400ms | ease-in-out | Validação de input |
| **Fade in** | 200ms | ease | Aparecimento de card/floating UI |

### 8.1 Animações Android

| Animação | Recurso | Uso |
|---|---|---|
| Transição de tela | `res/anim/` (slide_in_right, slide_out_left) | Navegação entre telas |
| Fade in | `View.animate().alpha(1).setDuration(200)` | Cards, conteúdo |
| Shake | `ObjectAnimator` de translação X | Erro em input |
| Pulse (coação) | `ValueAnimator` de alpha + scale | Alerta crítico |
| Loading spinner | `ProgressBar` com interpolação linear | Carregamento |

---

## 9. Padrões de Layout

### 9.1 Página de Lista
```
[Header: Título + Ações]
[Filtros / Busca]
[Tabela ou Grid de Cards]
[Paginação (se houver)]
```

### 9.2 Página de Dashboard
```
[Header + KPI Cards (grid 2-4 colunas)]
[Gráficos (grid 2 colunas)]
[Tabela resumo]
```

### 9.3 Página de Detalhe
```
[Header: Título + Status + Ações]
[Card de Informações]
[Timeline / Histórico]
[Ações secundárias]
```

### 9.4 Formulário
```
[Card centralizado / full-width]
[Grupo de campos com labels]
[Botões de ação (Cancelar + Salvar)]
```

### 9.5 Login
```
[Card centralizado verticalmente]
[Logo + Título]
[Chip de modo (Admin / Vigia)]
[Campos condicionais]
[Botão primário cheio]
[Indicador de erro/loading]
```

---

## 10. Convenções de Nomenclatura (Android)

### Resources
- **Colors:** `snake_case` descritivo (`primary_container`, `on_surface_variant`, `success_container`)
- **Dimens:** `snake_case` com prefixo de contexto (`card_corner_radius`, `sidebar_width`, `space_4`)
- **Strings:** `snake_case` com prefixo de tela (`login_title`, `home_welcome`, `turno_status`)
- **Layouts:** `activity_<name>.xml`, `fragment_<name>.xml`, `dialog_<name>.xml`, `item_<name>.xml`
- **Drawables:** `bg_<desc>.xml`, `ic_<desc>.xml`, `selector_<desc>.xml`

### Código
- **IDs XML:** `snake_case` (`tv_welcome`, `btn_login`, `til_email`, `et_password`)
- **ViewModels:** `LoginViewModel.java`, `HomeViewModel.java`
- **Activities/Fragments:** `LoginActivity.java`, `HomeFragment.java`

---

## 11. Recursos Obrigatórios

Para implementar o design system completo, o projeto Android deve ter:

```
res/
├── font/                     # (opcional) Fonte Geist baixada
├── drawable/
│   ├── bg_<component>.xml    # Shapes, backgrounds
│   ├── ic_<icon>.xml         # Vector icons
│   └── selector_<comp>.xml   # State list drawables
├── layout/
│   ├── activity_*.xml
│   ├── fragment_*.xml
│   ├── dialog_*.xml
│   └── item_*.xml
├── values/
│   ├── colors.xml            # Paleta completa (light)
│   ├── dimens.xml            # Espaçamentos, radii, tamanhos
│   ├── strings.xml           # Strings (PT + EN)
│   ├── styles.xml            # Estilos globais
│   └── themes.xml            # Tema base
├── values-night/
│   ├── colors.xml            # Paleta dark mode
│   └── themes.xml            # Tema dark
└── anim/                     # (opcional) Transições
```

---

## 12. Verificação de Consistência

Antes de finalizar qualquer alteração visual, verificar:

- [ ] Cores seguem a paleta definida neste documento
- [ ] Tipografia usa os estilos MD3 corretos
- [ ] Bordas seguem os tokens de radius
- [ ] Espaçamentos seguem os tokens definidos
- [ ] Dark mode tem correspondente em `values-night/`
- [ ] Strings estão em `strings.xml` (não hardcoded)
- [ ] Ícones são vetoriais (XML vector drawable)
- [ ] Elevações seguem os tokens de shadow
- [ ] Animações respeitam as durações definidas
- [ ] Nomenclatura segue as convenções da seção 10

---

> **Última atualização:** Julho 2026
> **Baseado em:** guardpoint-manager (web) — Zard UI + Tailwind v4 + OKLCH design system
