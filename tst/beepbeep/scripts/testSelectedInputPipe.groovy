import static beepbeep.groovy.*
import static org.junit.Assert.*

def f = TurnInto(2)
def a = ApplyFunction(Numbers.addition)
def b = TurnInto(0)

//Connect output 0 of b to input 1 of a
def c1 = b | a.in[1]
//Connect output 0 of f to input 0 of a
def c2 = f | a.in[0]

// Check that the connections are correct
assertSame(f.getPushableOutput(0).getProcessor(), a)
assertSame(b.getPushableOutput(0).getProcessor(), a)

assertSame(a.getPullableInput(0).getProcessor(), f)
assertSame(a.getPullableInput(1).getProcessor(), b)

// Check that the pipeline returns the last processor in the chain
assertSame(c1, a)
assertSame(c2, a)