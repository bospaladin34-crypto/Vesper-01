from typing import Dict, List, Optional, Any

class ASTAttribute:
    def __init__(self, key: str, value: Any, attr_type: str = "constant"):
        self.key = key
        self.value = value
        self.attr_type = attr_type    # e.g., 'constant', 'equation', 'operator', etc.

class ASTBlock:
    def __init__(
        self, 
        name: str, 
        block_type: Optional[str] = None,   # e.g., "BRAIN", "OPERATOR", "SUBSYSTEM"
        attributes: Optional[List[ASTAttribute]] = None,
        children: Optional[List['ASTBlock']] = None,
        metadata: Optional[Dict[str, Any]] = None
    ):
        self.name = name
        self.block_type = block_type or "GENERIC"
        self.attributes = attributes or []
        self.children = children or []
        self.metadata = metadata or {}

    def add_attribute(self, attr: ASTAttribute):
        self.attributes.append(attr)
        
    def add_child(self, block: 'ASTBlock'):
        self.children.append(block)
        
    def __repr__(self):
        return f"ASTBlock({self.block_type}:{self.name}, attrs={self.attributes}, children={self.children})"

class VesperAST:
    def __init__(self):
        self.blocks: List[ASTBlock] = []

    def add_block(self, block: ASTBlock):
        self.blocks.append(block)
        
    def find_blocks(self, block_type: str) -> List[ASTBlock]:
        return [b for b in self.blocks if b.block_type == block_type]
