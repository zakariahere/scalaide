# Zakaria identity

Source: [zakaria.lu](https://zakaria.lu), inspected live on 2026-09-17, and its
existing `cvnext` brand kit. The live DOM tokens match the checked-in brand kit.
This adaptation is the **Zakaria Graphite** IntelliJ theme and a branded Scala
Workbench; the development host still identifies itself as IntelliJ IDEA.

## Palette

| Role | Value |
| --- | --- |
| Graphite / editor | `#0B1018` |
| Raised panels | `#121B29` |
| Outer frame / subtle surface | `#182338` |
| Cloud / primary text | `#F5F7FB` |
| Secondary / muted text | `#B3C0D4` / `#93A4BF` |
| Electric / ice accent | `#315CFD` / `#A8C7FF` |
| Border | `#28354B` |
| Success / warning / error | `#74DFB1` / `#F4C56A` / `#FF95A5` |

The theme inherits native Islands layout and changes UI colors plus its paired
editor scheme. Diagnostic colors retain their distinct meaning. Code gets a
quiet solid background; the mascot appears in the Workbench header. Existing
editor font preferences and normal IntelliJ keymaps are not forcibly remapped.

Select **Settings → Appearance & Behavior → Appearance → Theme → Zakaria
Graphite**, or invoke **Apply Zakaria Graphite Theme** through Find Action or
the Scala menu. The Workbench has the same action. Switching is explicit; startup
does not override a theme the user chose later. Select another theme to revert.

## Assets and provenance

- `branding/zak-hoodie.webp`: unmodified original from
  `cvnext/frontend/public/assets/zak-hoodie.webp`, also served at
  [the live mascot URL](https://zakaria.lu/assets/zak-hoodie.webp).
- `branding/zak-hoodie.png`: lossless format conversion for JVM ImageIO support.
  Decoded RGBA bytes exactly match the 368 × 560 original; transparency is
  preserved. No regeneration, crop, background replacement or identity change.
- `branding/zb.svg`, dark variant, Workbench icon and plugin icon: the connected
  Z/B paths from `cvnext/frontend/src/components/BrandGlyph.tsx`. The original
  blue diagonal remains `#0875FF`.
- Space Grotesk and Hanken Grotesk: bundled, unmodified font files from the
  [Google Fonts repository](https://github.com/google/fonts/tree/main/ofl).
  Their SIL Open Font License notices are included beside each font. Used only
  for the Workbench identity/content; fonts are not installed into Windows.
- Header wording reuses the site's “Stay curious. Keep building.” identity.

All assets load locally. The IDE does not fetch the website, fonts or mascot
during normal use. This repository does not grant third parties new rights to
Zakaria's personal identity or artwork; the adaptation was requested by its owner.

## Implementation

- `themes/zakaria-graphite.theme.json`: registered native theme and UI palette.
- `themes/zakaria-graphite.xml`: editor/console syntax and diagnostic colors.
- `BrandHeader` / `BrandIdentity`: accessible Swing header, fonts and original mascot.
- `ApplyBrandThemeAction`: explicit native theme selection.
- Theme docs: [UI customization](https://plugins.jetbrains.com/docs/intellij/themes-customize.html),
  [editor schemes](https://plugins.jetbrains.com/docs/intellij/themes-extras.html),
  [Islands inheritance](https://plugins.jetbrains.com/docs/intellij/supporting-islands-theme.html).

Validated on Windows on 2026-09-17: all 20 existing tests pass, and the plugin ZIP
builds successfully. In the actual development IDE, the theme action applies both
the UI palette and editor scheme; the original mascot and bundled fonts render
correctly. The header remains readable in a roughly 345-pixel-wide tool window.
A full IDE restart preserves the theme, editor scheme and Workbench layout.
The saved native settings identify `dev.scalaide.zakaria.graphite` and the
`Zakaria Graphite` editor scheme.
