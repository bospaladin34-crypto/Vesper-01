import sys
from core.parser import parse_sys_file
from core.backend import QuantumBackend, GeometryBackend

def main():
    source = sys.argv[1]
    ast = parse_sys_file(source)
    print("Parsed blocks:", [b.name for b in ast.blocks])

    # Example: run quantum backend if chosen
    qb = QuantumBackend(ast)
    qb.run()
    # Add user loop/menu/help etc.

if __name__ == "__main__":
    main()