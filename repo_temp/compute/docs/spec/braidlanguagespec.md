# Braid Syntax Language Specification
Version 1.0

## 1. Overview
Braid Syntax is a deterministic, laminar, operator‑driven DSL used for:
- manifold configuration
- tensor ingestion
- Santos‑Protocol physics modeling
- semantic‑mass and thermodynamic invariants

It is designed to be:
- whitespace‑agnostic
- block‑structured
- reversible
- unambiguous
- geometry‑aligned

---

## 2. File Structure

A Braid file consists of:
- Block headers
- Key/value invariants
- Operator expressions
- Optional comments

Example:

[OPERATOR: SANTOS_LAGRANGIAN]
PHASE_DELTA::= 0.17259029
L0::= LAMINAR.KINETIC(dq/dt)
`

---

3. Block Headers

Syntax:
`
[BLOCKTYPE: BLOCKNAME]
`

Allowed block types:
- OPERATOR
- TENSOR
- INGEST
- MANIFOLD

---

4. Invariants (Key/Value Pairs)

Syntax:
`
KEY::= VALUE
`

Keys must be uppercase with underscores.  
Values may be:
- numbers
- identifiers
- operator expressions
- symbolic constants

---

5. Operator Expressions

Operators define functional behavior.

General form:
`
NAMESPACE.OPERATOR(args)
`

Examples:
- LAMINAR.KINETIC(dq/dt)
- TURBULENT.POTENTIAL(q)
- PULLBACK(Φ(q) -> NODE[0,0,0,0])

---

6. Comments

`

This is a comment
`

---

7. Determinism

Braid Syntax is fully deterministic:
- No randomness
- No entropy pools
- No probabilistic branching

This is why it integrates cleanly with physics‑based architectures.

---

8. Reserved Words

- LAMINAR
- TURBULENT
- MAJORANA‑1
- PHASE_DELTA
- RENORM
- CHANDRA
- FGR

---

9. Grammar (EBNF)

`
document      = { block | assignment } ;
block         = "[" type ":" name "]" { assignment } ;
assignment    = key "::=" value ;
value         = number | ident | operator ;
operator      = ident "." ident "(" args ")" ;
args          = { ident | number | operator | symbol } ;
`

---

10. Compliance

A valid Braid file must:
- contain at least one block
- contain at least one invariant
- contain no ambiguous operators