import re
from core.ast import ASTBlock, ASTAttribute, VesperAST

_BLOCK_RE = re.compile(r'^\[(?P<type>[A-Z_]+):\s*([A-Z0-9_\-]+)\]', re.MULTILINE)
_ATTR_RE = re.compile(r'([A-Z0-9_]+)\s*::=\s*"?(.*?)"?$', re.MULTILINE)

def parse_sys_file(path) -> VesperAST:
    with open(path) as f:
        text = f.read()

    ast = VesperAST()
    for block_match in _BLOCK_RE.finditer(text):
        block_type, block_name = block_match.group("type"), block_match.group(2)
        block_start = block_match.end()
        block_body = text[block_start:text.find('[', block_start)]  # naive: to next block or end
        attrs = []
        for attr_match in _ATTR_RE.finditer(block_body):
            k, v = attr_match.group(1), attr_match.group(2)
            # Heuristic: if an equation, mark type
            attr_type = "equation" if any(op in v for op in "=∑∫πφΦ√ΛΔ") else "constant"
            attrs.append(ASTAttribute(k, v, attr_type=attr_type))
        ast.add_block(ASTBlock(name=block_name, block_type=block_type, attributes=attrs))
    return ast
