# ADR-002: Frontend — Vite + React single-page application

- **Status:** Accepted
- **Date:** 05.10.2026
- **Deciders:** @merttemiz, @hasan4adnan

## Context

DOP is self-hosted software that a user starts with one command on a laptop with 8 GB of RAM.
Every container in the bundle costs memory, pull time and a thing that can fail against the
ten-minute rule. The interface is fed entirely from the API; the product has no need for
server-side rendering, and no page of it benefits from one.

## Decision

The web interface is a **static single-page application built with Vite**, in React and
TypeScript, served by **Nginx**. No Node runtime ships in the image.

The library set does not change from the master roadmap: React, TypeScript, TanStack Query,
ECharts, Cytoscape.js, Tailwind.

## Alternatives considered

| Alternative | Why not |
|:------------|:--------|
| Next.js | In a self-hosted product it means a separate Node runtime container for server rendering nobody needs. One more image to build, pull, patch and operate. |
| A third-party UI component kit | A dependency that dictates the look and the bundle size for what is, at v0.1, two pages. |
| Server-rendered templates from core | Couples the interface to the Java service and discards the API-first boundary that the SPA keeps honest. |

## Consequences

- The Compose bundle is one container smaller, and the web image is a static file server.
- The API is the only way the interface gets data, so an API gap is visible immediately rather
  than being papered over on the server.
- No server-side rendering means no SEO story and a first paint that waits for the bundle.
  Neither matters for an authenticated-by-v0.5 internal tool.
- The frontend table of the architecture reference is written against this decision.
