# LearnWise design system

Phase 1 uses a light-only, warm ivory foundation with soft sage, lavender, powder-blue, and peach accents. The system is intentionally tactile: raised surfaces use a paired light/dark shadow and inset surfaces use a restrained inner shadow.

## Foundations

- **Typography:** DM Sans for interface text; Playfair Display italic for expressive emphasis.
- **Color tokens:** CSS custom properties live in [`src/styles.css`](../src/styles.css). Text uses deep green-black `--ink`; secondary text uses `--muted`.
- **Shape:** 10px controls, 12px small surfaces, and 18px cards. Pills are reserved for statuses and filters.
- **Spacing:** Components use multiples of 4px, with generous page-level whitespace.
- **Motion:** Hover and focus transitions are subtle. All motion is reduced under `prefers-reduced-motion`.

## Components

Reusable primitives are in [`src/components/ui.tsx`](../src/components/ui.tsx): `Button`, `Card`, `Badge`, `Progress`, `TextInput`, `Select`, `EmptyState`, and `PageHeader`. The icon system in [`src/components/Icon.tsx`](../src/components/Icon.tsx) wraps Lucide icons so controls have consistent, accessible SVG iconography.

## Layout

The authenticated application shell is in [`src/components/Layout.tsx`](../src/components/Layout.tsx). It provides a responsive sidebar, topbar, navigation states, mobile drawer, and a consistent content boundary. Public landing and authentication pages intentionally use distinct layouts.

## Data boundary

All displayed course, activity, and topic values are development-only fixtures in [`src/data/fixtures.ts`](../src/data/fixtures.ts). Screens explicitly show a fixture notice where progress could otherwise be mistaken for live account data. No backend or AI integration is represented as complete in Phase 1.
