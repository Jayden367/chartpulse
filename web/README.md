# ChartPulse Web

Standalone, zero-build static web version of **ChartPulse** (Analysis, Planner, and Journal), optimized for iPhone Safari and desktop browsers.

## Files
- `index.html` — Responsive mobile-first app shell and modal host
- `styles.css` — Dark Obsidian theme, tabular numeric typography, 48px touch targets, and iOS safe-area handling
- `app.js` — Chart presets, interactive price-action canvas, signal analyzer, bi-directional position/risk calculator, and `localStorage` trading journal

## Deploy as a Standalone Website
Download this project via AI Studio **Settings → Download ZIP**, extract it, and deploy the `web/` folder directly (no build step required):

- **Netlify Drop:** Drag and drop the `web/` folder onto [app.netlify.com/drop](https://app.netlify.com/drop).
- **Vercel:** Run `vercel web/` or point the project Root Directory to `web`.
- **GitHub Pages / Cloudflare Pages / S3:** Upload the contents of `web/` (`index.html`, `styles.css`, `app.js`) as the static site root.
