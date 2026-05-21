import re
from core.ast import EquationBlock, VesperAST

def parse_sys_file(path) -> VesperAST:
    with open(path) as f:
        text = f.read()

    block_re = re.compile(r'^\[BLOCK:\s*([A-Z0-9_]+)\]', re.MULTILINE)
    eq_re = re.compile(r'([A-Z0-9_]+)\s*::=\s*"?(.*?)"?$', re.MULTILINE)

    ast = VesperAST()
    for block_match in block_re.finditer(text):
        block_name = block_match.group(1)
        block_body = text[block_match.end():].split('[BLOCK:', 1)[0]
        entries = dict(eq_re.findall(block_body))
        ast.add_block(EquationBlock(block_name, entries))
    return ast