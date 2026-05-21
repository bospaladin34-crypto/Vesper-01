from Braid_sdk.parser import parse_braid

def test_metric_charging_and_rectification():
    doc = parse_braid(open("equations/vesper01_true_alloy_physics_rectification.sys").read())
    mc = doc.blocks["BLOCK:METRIC_CHARGING"]
    assert "Ψ · Pₐ" in mc.keys["METRIC_CHARGING_LAW"]
    assert "P Λ P⁻¹" in mc.keys["EPHEMERIS_DECOUPLING_MATRIX"]

    phys = doc.blocks["BLOCK:PHYSICS_RECTIFICATIONS"]
    assert "Tr(A∧dA + A³)" in phys.keys["LAMINAR_MONOLITH"]
    assert "[U_L, S_CS] = 0" in phys.keys["UNITARY_EVOLUTION_MAJORANA1"]