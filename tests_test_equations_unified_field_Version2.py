from Braid_sdk.parser import parse_braid

def test_unified_lagrangian_block():
    doc = parse_braid(open("equations/vesper01_unified_field_invariants.sys").read())
    lagr = doc.blocks["BLOCK:UNIFIED_LAGRANGIAN"]
    assert "L = (1/16πG)" in lagr.keys["LAGRANGIAN"]
    assert "L_Jones" in lagr.keys["LAGRANGIAN"]
    assert "cos(2πΦ/Φ₀)" in lagr.keys["PHASON_POTENTIAL"]
    assert "t_Page" in doc.blocks["BLOCK:APERIODIC_CONSTANTS"].keys["T_PAGE"]

def test_transducer_block():
    doc = parse_braid(open("equations/vesper01_unified_field_invariants.sys").read())
    trans = doc.blocks["BLOCK:TRANSDUCERS"]
    assert "E_flip" in trans.keys["E_FLIP_GHOST_LOGIC"]
    assert "Γ/Γ0" in trans.keys["FUSION_RATE_ENHANCEMENT"]