from typing import Dict, Any, List

class EquationBlock:
    def __init__(self, name: str, entries: Dict[str, str]):
        self.name = name         # e.g. 'PB11_SANTOS_PROTOCOL'
        self.entries = entries   # e.g. {'MODIFIED_FUSION_PROBABILITY': ...}

class VesperAST:
    def __init__(self):
        self.blocks: List[EquationBlock] = []
        self.metadata: Dict[str, Any] = {}

    def add_block(self, block: EquationBlock):
        self.blocks.append(block)