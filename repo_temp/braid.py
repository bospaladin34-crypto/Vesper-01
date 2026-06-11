import numpy as np
NU_P=0.17259029; PHI=1.6180339887; F0=15.965
class State:
    def __init__(self,s): self.seed=s%1.0
def witness(a): return State(a.seed)
def anchor(a): return State(round(a.seed/NU_P)*NU_P)
def braid(a,b,c):
    yz=(b.seed*c.seed*PHI)%1.0; xz=(a.seed*c.seed*PHI)%1.0
    final=(xz+yz*NU_P+0.01*np.sin(2*np.pi*F0*0.0625))%1.0
    return State(final)
