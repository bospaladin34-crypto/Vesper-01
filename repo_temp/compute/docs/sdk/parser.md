# Parser

The parser converts `.sys` files into a structured AST.

from braidsdk.parser import parsebraid
doc = parse_braid(open("module.sys").read())

The AST contains:

- Blocks
- Keys
- Operators