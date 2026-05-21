import unittest
from Braid_sdk.parser import parse_braid
from Braid_sdk.model import BraidDocument, BraidBlock

EXAMPLE_SYS = """
[MODULE: QUANTUM_TEST]
DESC ::= "Quantum parsing test"

[BLOCK: QSIM_PIPELINE]
GATES ::= ["H", "CNOT", "T"]
SEQUENCE ::= [
  "H q0",
  "CNOT q0 q1"
]
RESULT_TARGET ::= "QUANTUM_RESULT"

[BLOCK: E8_GEOMETRY]
MANIFOLD ::= "E8"
STATE_VECTOR ::= "|10111001>"

[BLOCK: LAGRANGIAN_EXPORT]
INCLUDE_TERMS ::= ["L0", "DIV", "RENORM"]
"""

class TestBraidParser(unittest.TestCase):
    def test_parse_blocks_and_keys(self):
        doc: BraidDocument = parse_braid(EXAMPLE_SYS)
        # Basic top-level check
        self.assertIn("MODULE:QUANTUM_TEST", doc.blocks)
        self.assertIn("BLOCK:QSIM_PIPELINE", doc.blocks)
        self.assertIn("BLOCK:E8_GEOMETRY", doc.blocks)
        self.assertIn("BLOCK:LAGRANGIAN_EXPORT", doc.blocks)
        
        # Check fields in quantum block
        qsim = doc.blocks["BLOCK:QSIM_PIPELINE"]
        self.assertIn("GATES", qsim.keys)
        self.assertIn("SEQUENCE", qsim.operators)  # Should be operator category (because it's a list)

        # Check E8 geometry and lagrangian
        geom = doc.blocks["BLOCK:E8_GEOMETRY"]
        self.assertEqual(geom.keys.get("MANIFOLD"), "E8")
        self.assertEqual(geom.keys.get("STATE_VECTOR"), "|10111001>")

        lagr = doc.blocks["BLOCK:LAGRANGIAN_EXPORT"]
        self.assertIn("INCLUDE_TERMS", lagr.keys)
        self.assertEqual(lagr.keys["INCLUDE_TERMS"], '["L0", "DIV", "RENORM"]')

    def test_parse_error_on_bad_header(self):
        # Malformed block header should throw
        BAD_SYS = "[BLOCK QSIM"  # Missing colon and closing bracket
        with self.assertRaises(SyntaxError):
            parse_braid(BAD_SYS)

if __name__ == "__main__":
    unittest.main()