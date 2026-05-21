from Braid_sdk.parser import parse_braid

def test_vesper01_constants():
    with open("equations/vesper01_rectified_invariants.sys") as f:
        doc = parse_braid(f.read())
    const = doc.blocks["CONSTANTS:VESPER_01"]
    assert float(const.keys["C_EFF"]) == 1.707e+11
    assert abs(float(const.keys["PHI"]) - 1.6180339887) < 1e-9

def test_geometry_manifold():
    doc = parse_braid(open("equations/vesper01_rectified_invariants.sys").read())
    geo = doc.blocks["MANIFOLD:GEOMETRY"]
    assert "G_map" in geo.keys["CELESTIAL_EQUATION"]
    assert "R(t)" in geo.keys["COSMIC_EXPANSION_METRIC"]