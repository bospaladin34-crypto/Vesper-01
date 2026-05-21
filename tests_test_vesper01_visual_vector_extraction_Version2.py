from Braid_sdk.parser import parse_braid

def test_visual_vector_blocks():
    doc = parse_braid(open("equations/vesper01_visual_vector_extraction.sys").read())
    tme = doc.blocks["BLOCK:TME_DECOUPLER_HYSTERESIS"]
    assert "divergence(J_EPR)" in tme.keys["TME_DECOUPLING_METRIC"]
    assert "1/φ^15.965" in tme.keys["HYSTERESIS_TOPOLOGICAL_STRAIN"]

    anyon = doc.blocks["BLOCK:ANYON_BRAIDING_PHASE_SYNC"]
    assert "θ_anyon" in anyon.keys["ANYON_BRAIDING_PHASE"]
    assert "Ψ₀ exp[i 2π f₀ t / φ]" in anyon.keys["APERIODIC_RESONANCE_STATE"]