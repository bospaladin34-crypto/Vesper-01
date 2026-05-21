from typing import Dict, Any

class BraidBlock:
    """
    Represents a single [BLOCK_TYPE: BLOCK_NAME] in a .sys file.
    Supports quantum, geometry, physics, and extensible user keys/operators.
    """
    def __init__(self, block_type: str, block_name: str):
        self.block_type = block_type
        self.block_name = block_name
        self.keys: Dict[str, Any] = {}       # Scalar configuration or metadata
        self.operators: Dict[str, Any] = {}  # Procedures, expressions, resolvables

    def __repr__(self):
        return (f"BraidBlock(type={self.block_type}, name={self.block_name}, "
                f"keys={self.keys}, operators={self.operators})")

class BraidDocument:
    """
    Top-level AST, holds all parsed blocks and top-level keys.
    """
    def __init__(self):
        self.blocks: Dict[str, BraidBlock] = {}
        self.keys: Dict[str, Any] = {}

    def __repr__(self):
        return f"BraidDocument(keys={self.keys}, blocks={self.blocks})"