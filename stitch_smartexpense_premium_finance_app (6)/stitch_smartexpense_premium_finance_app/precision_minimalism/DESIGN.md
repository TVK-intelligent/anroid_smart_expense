---
name: Precision Minimalism
colors:
  surface: '#f8f9ff'
  surface-dim: '#cbdbf5'
  surface-bright: '#f8f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#eff4ff'
  surface-container: '#e5eeff'
  surface-container-high: '#dce9ff'
  surface-container-highest: '#d3e4fe'
  on-surface: '#0b1c30'
  on-surface-variant: '#444651'
  inverse-surface: '#213145'
  inverse-on-surface: '#eaf1ff'
  outline: '#757682'
  outline-variant: '#c5c5d3'
  surface-tint: '#4059aa'
  primary: '#00236f'
  on-primary: '#ffffff'
  primary-container: '#1e3a8a'
  on-primary-container: '#90a8ff'
  inverse-primary: '#b6c4ff'
  secondary: '#0058be'
  on-secondary: '#ffffff'
  secondary-container: '#2170e4'
  on-secondary-container: '#fefcff'
  tertiary: '#00311f'
  on-tertiary: '#ffffff'
  tertiary-container: '#004a31'
  on-tertiary-container: '#27c38a'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#dce1ff'
  primary-fixed-dim: '#b6c4ff'
  on-primary-fixed: '#00164e'
  on-primary-fixed-variant: '#264191'
  secondary-fixed: '#d8e2ff'
  secondary-fixed-dim: '#adc6ff'
  on-secondary-fixed: '#001a42'
  on-secondary-fixed-variant: '#004395'
  tertiary-fixed: '#6ffbbe'
  tertiary-fixed-dim: '#4edea3'
  on-tertiary-fixed: '#002113'
  on-tertiary-fixed-variant: '#005236'
  background: '#f8f9ff'
  on-background: '#0b1c30'
  surface-variant: '#d3e4fe'
typography:
  display-lg:
    fontFamily: Inter
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 36px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Inter
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  title-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '500'
    lineHeight: 24px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  label-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.05em
  display-lg-mobile:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '700'
    lineHeight: 32px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base: 8px
  margin-horizontal: 16px
  gutter: 16px
  container-padding: 16px
  stack-space-sm: 4px
  stack-space-md: 12px
  stack-space-lg: 24px
---

## Brand & Style
This design system is built for a high-end fintech experience on Android, prioritizing clarity, speed, and institutional trust. The aesthetic follows a **Minimalist** approach infused with **Material Design 3** principles. It avoids unnecessary decoration in favor of structural hierarchy and purposeful whitespace.

The target audience consists of professionals and financially conscious users who require a tool that feels both powerful and easy to navigate. The UI evokes a sense of "Calm Control"—reducing the cognitive load associated with financial management through a systematic, organized, and premium interface.

## Colors
The palette is rooted in "Deep Navy" and "Action Blue" to establish a foundation of stability and modern tech-forwardness. 

- **Primary & Secondary:** Used for brand representation and primary actions (e.g., FABs, primary buttons).
- **Surface & Background:** A subtle Slate-white background distinguishes the app from generic white interfaces, while pure white surfaces are reserved for elevated cards and containers.
- **Semantic Colors:** Emerald and Red are used strictly for financial status—income and expenses respectively—ensuring immediate data recognition.

## Typography
**Inter** is utilized for its exceptional legibility and systematic feel, which is critical for displaying dense financial data.

- **Scale:** The hierarchy is tight. Large titles are used only for account balances or primary headers.
- **Weight:** SemiBold is the primary "anchor" for section headers to ensure they stand out against white surfaces without needing heavy borders.
- **Data Display:** For numerical values and currency, use `title-md` or `display-lg` with `tabular-nums` OpenType features enabled to ensure decimal alignment in lists.

## Layout & Spacing
The layout follows an **8dp grid system** consistent with Android's Material standards. 

- **Margins:** A standard 16dp horizontal margin is applied to all screens.
- **Vertical Rhythm:** Content blocks (cards) are separated by 12dp or 16dp to maintain a breathable, premium feel.
- **Alignment:** All text elements must align to the 4dp baseline grid. Complex data tables should use a fluid layout with fixed 16dp gutters.

## Elevation & Depth
In line with Material Design 3, this design system uses **Tonal Layers** combined with very **Soft Ambient Shadows**. 

- **Level 0 (Background):** #F8FAFC. No shadow.
- **Level 1 (Cards/Surface):** Pure #FFFFFF. Use a subtle shadow: `0px 2px 8px rgba(15, 23, 42, 0.05)`.
- **Level 2 (Floating Action Buttons/Modals):** Pure #FFFFFF. Use a more pronounced shadow: `0px 8px 24px rgba(15, 23, 42, 0.08)`.

Depth is also communicated through "Tonal Elevation," where an overlay of the primary color (at 5-8% opacity) can be applied to surfaces to indicate they are "above" the background.

## Shapes
The shape language is modern and friendly but retains professional "corners."

- **Cards:** Use `rounded-lg` (16px) to create the distinct high-end look requested.
- **Buttons:** Use `rounded-lg` (16px) for a consistent container language, or full pill-shape for chips and tags.
- **Inputs:** Use `rounded-md` (8px) to differentiate actionable form fields from structural cards.

## Components
Consistent implementation of components ensures a cohesive financial experience.

- **Primary Buttons:** High-emphasis, filled with `#1E3A8A`. Height of 48dp or 56dp for touch accessibility.
- **Cards:** 16dp corner radius. Use for transaction summaries and account balances. No borders; use Level 1 shadows for separation.
- **Transaction List Items:** 72dp height. Left-aligned icon (in a soft-tinted circle), followed by category name (Title-md) and timestamp (Label-sm). Amounts are right-aligned.
- **Input Fields:** Outlined style with a 1px border (#CBD5E1). On focus, the border thickens to 2px using `#3B82F6`.
- **Chips:** Used for filtering categories (e.g., "Food", "Travel"). 32dp height, 16dp (pill) radius, subtle Slate-100 background.
- **Progress Bars:** Thin 4dp tracks for budget monitoring. Background track uses #E2E8F0, with progress filled in Primary or Expense Red if over budget.