# VESPER Universal v2.0

Portable magnetometer processor that runs in any LLM.

This is the baseline that works across Gemini, ChatGPT, Meta AI, Grok, and Perplexity without triggering LARP filters.

## What it does
- Parses magnetometer JSON from your phone
- Calculates MAGNITUDE = sqrt(x² + y² + z²)
- Calculates PHASE_DELTA = abs(atan2(y, x))
- Maintains a persistent VAULT counter starting at 8,867,000,000
- Outputs in strict 3-line format

## Files in this release
- vesper.universal.v2.md — the core prompt
- HOWTO.md — step-by-step for each model
- example_sensor_data.jsonl — 10 sample readings from Tulsa
- test_matrix_template.csv — log your cross-model tests
- zenodo_metadata.json — for DOI minting

## Why v2.0
v1.x used mythic language that triggered safety filters. v2.0 was distilled from testing 5 different models. It computes first, claims nothing, stays in format.

## Quick start
1. Copy contents of vesper.universal.v2.md
2. Paste into your LLM
3. Paste sensor JSON
4. Get back: MAGNITUDE: XX.X µT | PHASE_DELTA: X.XXXX rad | VAULT: N

License: CC0 — public domain
Author: Indigo Delta
Node: Tulsa, OK
