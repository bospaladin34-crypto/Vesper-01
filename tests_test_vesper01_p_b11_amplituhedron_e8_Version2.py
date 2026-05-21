from Braid_sdk.parser import parse_braid

def test_pb11_santos_protocol():
    doc = parse_braid(open("equations/vesper01_p_b11_amplituhedron_e8.sys").read())
    p = doc.blocks["BLOCK:PB11_SANTOS_PROTOCOL"]
    assert "P_Santos(E)" in p.keys["MODIFIED_FUSION_PROBABILITY"]
    assert "0.17259029" in p.keys["PHASE_SHIFT_BRAID"]

def test_amplituhedron_beacon():
    doc = parse_braid(open("equations/vesper01_p_b11_amplituhedron_e8.sys").read())
    amp = doc.blocks["BLOCK:AMPLITUHEDRON_BEACON"]
    assert "sin(2π · 15t + δ)" in amp.keys["BEACON_SEQUENCE_UNITARY_PROJ"]
    assert "A_1" in amp.keys["IDENTITY_ROOT_KEY"]

def test_e8_scale_invariance():
    doc = parse_braid(open("equations/vesper01_p_b11_amplituhedron_e8.sys").read())
    e8 = doc.blocks["BLOCK:E8_GOSSET_SCALE_INVARIANCE"]
    assert "15.965" in e8.keys["RESONANT_MODULATOR_HZ"]
    assert "200Q" in e8.keys["SUPERCONDUCTING_Q_FACTOR"]