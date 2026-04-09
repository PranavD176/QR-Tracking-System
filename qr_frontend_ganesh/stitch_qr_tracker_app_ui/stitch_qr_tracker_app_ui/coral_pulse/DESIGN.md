# Design System Specification: High-End Editorial Logistics

## 1. Overview & Creative North Star: "The Kinetic Pulse"
This design system moves beyond the utility of a standard tracking tool into a "High-End Editorial" experience. The Creative North Star is **The Kinetic Pulse**. Logistics is the art of movement; therefore, the UI must feel alive, fluid, and premium. 

We reject the "boxed-in" layout of traditional Android apps. Instead, we use intentional asymmetry, overlapping layers, and high-contrast typography to guide the eye. This system treats package tracking not as a chore, but as a concierge service—utilizing generous white space, "glass" surfaces, and tonal depth to create a sense of calm and reliability.

---

## 2. Colors & Surface Philosophy
The palette is rooted in a high-energy "Coral-to-Peach" spectrum, balanced by a sophisticated grayscale that favors warmth over sterile slate.

### Primary Brand Gradient
*   **The Signature Pulse:** `#F05A28` (Primary) to `#F5A623` (Secondary). 
*   **Usage:** Reserved for primary actions, progress indicators, and "Hero" moments. This gradient represents the "soul" of the movement.

### Surface Hierarchy (The "No-Line" Rule)
**Crucial:** Do not use 1px solid borders for sectioning. Contrast must be achieved through background shifts.
*   **Surface (Base):** `#F6F6F6` - The canvas.
*   **Surface-Container-Lowest:** `#FFFFFF` - Elevated cards and floating elements.
*   **Surface-Container-Low:** `#F0F1F1` - Secondary content areas or "wells" within a card.
*   **Surface-Container-Highest:** `#DBDDDD` - Navigation bars or interactive backgrounds.

### Glass & Texture
*   **The Glass Rule:** For overlays or sticky headers, use `surface_container_lowest` at 80% opacity with a `24px` backdrop-blur.
*   **Signature Texture:** Use a subtle linear gradient from `primary` to `primary_container` on large CTAs to provide a three-dimensional "glow" that flat colors lack.

---

## 3. Typography: Editorial Authority
We pair **Plus Jakarta Sans** (Headlines) for modern geometric authority with **Be Vietnam Pro** (Body) for high legibility and a contemporary tech feel.

| Level | Token | Font | Size | Weight | Color |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Display** | `display-md` | Plus Jakarta Sans | 2.75rem | Bold | `on_surface` |
| **Headline** | `headline-sm` | Plus Jakarta Sans | 1.5rem | Bold | `on_surface` |
| **Title** | `title-md` | Be Vietnam Pro | 1.125rem | SemiBold | `on_surface` |
| **Body** | `body-md` | Be Vietnam Pro | 0.875rem | Regular | `on_surface_variant` |
| **Label** | `label-md` | Be Vietnam Pro | 0.75rem | SemiBold | `on_surface` |

**Editorial Note:** Use `display-md` for package counts or "Out for Delivery" statuses. Large type acts as a visual anchor, reducing the need for heavy containers.

---

## 4. Elevation & Depth: Tonal Layering
We move away from standard Material Design drop shadows. Depth is an environmental effect, not a structural necessity.

*   **The Layering Principle:** Stack `surface-container-lowest` cards on `surface` backgrounds. The natural contrast between the pure white and the off-white base provides 90% of the required separation.
*   **Ambient Shadows:** Use only when an element is truly "floating" (e.g., a Bottom Sheet).
    *   *Shadow:* `0px 12px 32px rgba(26, 26, 26, 0.06)` (A tinted, highly diffused shadow).
*   **The "Ghost Border":** For input fields, use `outline_variant` at 20% opacity. Never use 100% opaque borders.
*   **Asymmetric Radii:** While cards use `lg` (2rem) or `md` (1.5rem) roundedness, consider using "corner-smoothing" (iOS style) to ensure the curves feel organic rather than mechanical.

---

## 5. Signature Components

### Buttons: The Radiant Action
*   **Primary:** Fully rounded (`full`), Gradient (`primary` to `secondary`). No shadow, but a slight inner-glow (top-down white gradient at 10% opacity).
*   **Secondary:** `surface_container_low` background with `primary` text. No border.

### The Tracking Card (Specialized)
*   **Rule:** Forbid divider lines. 
*   **Layout:** Use `xl` (3rem) spacing between the package name and the status. 
*   **The Progress Track:** A 4dp thick track using `surface_container_highest` with a gradient-filled "pulse" line indicating the current location.

### Input Fields: Soft Focus
*   **Style:** `md` (1.5rem) rounded corners.
*   **States:** On focus, the `outline` transitions from 20% opacity to a soft `primary` glow. The label should float with an editorial "all-caps" style in `label-sm`.

### Status Chips (Pill-Shaped)
*   Use `surface_container_lowest` for the chip body.
*   Use `primary` or `secondary` for the text and a small 4dp leading dot to indicate "Live" movement.

---

## 6. Do’s and Don'ts

### Do
*   **Do** use overlapping elements. Let a package image "bleed" out of its card container to create depth.
*   **Do** use high vertical white space (32dp+) to separate major sections.
*   **Do** use color to denote momentum. Use the coral-to-peach gradient for "moving" objects and neutral tones for "stationary" history.

### Don't
*   **Don't** use 1px solid black or gray dividers. Use a 16dp gap or a tonal background shift instead.
*   **Don't** use pure black `#000000` for text. Use `on_surface` (`#2D2F2F`) for a softer, more premium contrast.
*   **Don't** use standard "Material" elevation shadows. They look "off-the-shelf." Stick to Tonal Layering and Ambient Shadows.