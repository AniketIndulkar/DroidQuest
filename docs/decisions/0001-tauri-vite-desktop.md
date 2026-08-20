# ADR 0001: Tauri with a static Vite frontend

- Status: accepted
- Date: 2026-08-16

## Decision

Build the macOS and Windows application with Tauri 2 and a static Vite/React
frontend. Share domain and React presentation packages with the existing web
client while leaving the web client's Cloudflare/Vinext deployment unchanged.

## Consequences

The desktop bundle remains small and offline-capable. The project maintains a
minimal Rust shell and gains separate platform adapters. Web and desktop keep
independent lockfiles so hosted web builds are not coupled to desktop tooling.
