# Braid SDK Contributor Onboarding Guide

Welcome to the VESPER Braid ecosystem.  
This guide prepares new contributors to work with:

- Braid Syntax  
- The Python SDK  
- The Santos Lagrangian compiler  
- The VSCode extension  
- The intelligence engine  

---

# 1. Prerequisites

Contributors should be comfortable with:

- Python 3.10+  
- Deterministic systems  
- Basic manifold/geometry concepts  
- GitHub workflows  

---

# 2. Repository Structure

`
braid_sdk/        # Python SDK
vscode-braid/     # VSCode extension
docs/             # Documentation site
examples/         # Sample .sys modules

---

# 3. How to Build the SDK
pip install-e

Run tests:
pytest
---

# 4. How to Work With Braid Syntax

A `.sys` file contains:

- block headers  
- invariants  
- operators  

Example:

[OPERATOR: EXAMPLE]
PHASE_DELTA::= 0.17259029
L0::= LAMINAR.KINETIC(dq/dt)
`

---

5. How to Add New Operators

1. Add operator definition to intelligence.py  
2. Add grammar support if needed  
3. Add example usage in examples/  
4. Update docs  
6. How to Extend the Compiler

Compiler logic lives in:

`
braid_sdk/compiler.py
`

Add new Lagrangian terms by:

- defining a new key pattern  
- mapping it to a Lagrangian field  
- updating docs  

---

7. How to Update IntelliSense

The VSCode extension reads:

`
braidsdk/intelligenceregistry.json
`

Add:

- new block types  
- new keys  
- new operators  

---

8. Contribution Rules

- Maintain laminar clarity  
- Avoid probabilistic constructs  
- Keep syntax deterministic  
- Document all new operators  

---

9. Certification Checklist

A contributor is ready when they can:

- parse a .sys file  
- write a new operator block  
- extend the compiler  
- update IntelliSense  
- maintain laminar discipline  