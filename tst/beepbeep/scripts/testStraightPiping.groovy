import static beepbeep.groovy.*
import static org.junit.Assert.*

def t = TurnInto(1)
def p = new ca.uqac.lif.cep.io.Print()
t | p
assertSame(t.getPushableOutput(0).getProcessor(), p)
assertSame(p.getPullableInput(0).getProcessor(), t)