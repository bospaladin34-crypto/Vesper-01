import unittest
from Braid_sdk.parser import parse_braid
from Braid_sdk.model import BraidDocument

GEOMETRY_TENSOR_SYS = """
[BLOCK: MANIFOLD_DEF]
LABEL ::= "Manifold48D"
BASE_SPACE ::= "M4"
INTERNAL_SPACE ::= "A44"
TENSOR_BASIS ::= ["dx", "dy", "dz", "dt"]
LATTICE_GEOMETRY ::= "E8 / Lorentzian"
EXPORTS ::= ["Manifold48D"]

[BLOCK: SU5_INTERNALS]
SYMMETRY_GROUP ::= "SU(5)"
STRUCTURE ::= "aperiodic"
QUANTUM_ROLE ::= "internal_phason_space"

[BLOCK: TENSOR_OPS]
METRIC_TENSOR ::= "g_ij"
OPERATOR ::= "Jacobian"
CONTRACTION_EQ ::= "U = S_i T^i"
INTEGRAL_FORMULA ::= "V = ∫∫∫ dx dy dz"
INVARIANT_CHECK ::= "Tr(U_res) = 1.0"
"""

class TestBraidParserGeometryTensor(unittest.TestCase):
    def setUp(self):
        self.doc: BraidDocument = parse_braid(GEOMETRY_TENSOR_SYS)

    def test_manifold_and_geometry_blocks(self):
        md = self.doc.blocks["BLOCK:MANIFOLD_DEF"]
        self.assertEqual(md.keys["LABEL"], "Manifold48D")
        self.assertEqual(md.keys["LATTICE_GEOMETRY"], "E8 / Lorentzian")
        # Arrays and export
        self.assertIn('["dx", "dy", "dz", "dt"]', md.keys["TENSOR_BASIS"] + md.keys["EXPORTS"])

    def test_su5_block(self):
        su5 = self.doc.blocks["BLOCK:SU5_INTERNALS"]
        self.assertEqual(su5.keys["SYMMETRY_GROUP"], "SU(5)")
        self.assertEqual(su5.keys["STRUCTURE"], "aperiodic")
        self.assertEqual(su5.keys["QUANTUM_ROLE"], "internal_phason_space")

    def test_tensor_ops(self):
        ops = self.doc.blocks["BLOCK:TENSOR_OPS"]
        self.assertIn("Tr(U_res) = 1.0", ops.keys["INVARIANT_CHECK"])
        self.assertIn("Jacobian", ops.keys["OPERATOR"])
        self.assertIn("U = S_i T^i", ops.keys["CONTRACTION_EQ"])

    def test_array_and_formula_storage(self):
        md = self.doc.blocks["BLOCK:MANIFOLD_DEF"]
        self.assertIn("[", md.keys["TENSOR_BASIS"])
        ops = self.doc.blocks["BLOCK:TENSOR_OPS"]
        self.assertIn("∫", ops.keys["INTEGRAL_FORMULA"])

if __name__ == "__main__":
    unittest.main()