from Braid_sdk.parser import parse_braid

def test_lagrangian_block():
    doc = parse_braid(open("equations/vesper01_lagrangian_kinetics.sys").read())
    lagr = doc.blocks["BLOCK:VESPER_LAGRANGIAN"]
    assert "J_MHD" in lagr.keys["COUPLED_LAGRANGIAN"]
    assert "cos((f₀/φ) t" in lagr.keys["B_TOR_APERIODIC"]
    assert "exp[i(k_A z" in lagr.keys["PLASMA_VELOCITY_FIELD"]

def test_topological_manifold():
    doc = parse_braid(open("equations/vesper01_lagrangian_kinetics.sys").read())
    topo = doc.blocks["BLOCK:TOPOLOGICAL_MANIFOLD_INVARIANTS"]
    assert "H_top" in topo.keys["HELICITY_INVARIANT"]
    assert "exp[i2π(f₀/φ)t]" in topo.keys["VESPER_APERIODIC_MODULATOR"]
    assert "χ(M)" in topo.keys["EULER_CHARACTERISTIC"]

def test_pb11_kinetics():
    doc = parse_braid(open("equations/vesper01_lagrangian_kinetics.sys").read())
    kinetics = doc.blocks["BLOCK:PB11_TRANSDUCER_KINETICS"]
    assert "ΔV_eff" in kinetics.keys["DELTA_V_EFFECTIVE"]
    assert "E_flip" in kinetics.keys["E_FLIP_APERIODIC"]
    assert "< 0.0113" in kinetics.keys["DECOHERENCE_THRESHOLD"]