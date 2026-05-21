from Braid_sdk.parser import parse_braid

with open("examples/quantum_geometry_example.sys") as f:
    doc = parse_braid(f.read())

print(doc)
# Output:
# BraidDocument(keys={...}, blocks={'MODULE:QUANTUM_GEOMETRY_DEMO': ..., 'BLOCK:QUBIT_MANIFOLD': ...})