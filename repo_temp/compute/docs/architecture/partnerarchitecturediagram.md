# Partner Architecture Overview (Specification)

## Purpose

This document describes the system architecture in terms suitable for partners,
integrators, and non‑cryptographic security reviewers.

## High‑Level Components

1. **Braid Syntax Layer**
   - Domain‑specific language for defining operators, tensors, and manifolds.
   - Files: `.sys`, `.braid`, `.vesper`.

2. **Braid SDK (Python)**
   - Parser: converts Braid files into structured models.
   - Compiler: assembles Santos Lagrangian objects.
   - Intelligence: registry for operators, keys, and completions.

3. **Santos Protocol Core**
   - Applies the Santos Lagrangian to model system behavior.
   - Enforces deterministic, geometry‑based dynamics.

4. **VSCode Extension**
   - Syntax highlighting and IntelliSense for Braid Syntax.
   - Developer‑facing tooling only (no runtime logic).

## Data Flow (Conceptual)

1. Operator defines a `.sys` / `.braid` file using Braid Syntax.  
2. Braid SDK parses the file into a structured document model.  
3. Compiler extracts Lagrangian terms and constructs a Santos Lagrangian.  
4. The Santos Protocol core uses this Lagrangian to drive deterministic behavior.  

## Key Properties

- No secrets or keys are stored or processed.
- No encryption or decryption occurs at any stage.
- All transformations are deterministic and reversible.
- All behavior is governed by geometric and thermodynamic constraints.

## Integration Points

Partners typically integrate at:

- **Braid SDK API** (Python)  
- **Generated Lagrangian objects**  
- **Configuration files in Braid Syntax**  

There is no cryptographic boundary or key boundary to manage.