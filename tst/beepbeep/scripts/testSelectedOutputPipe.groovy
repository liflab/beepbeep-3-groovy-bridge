import static beepbeep.groovy.*
import static org.junit.Assert.*

def f = Fork(2)
def a = ApplyFunction(Numbers.addition)
def b = TurnInto(0)

//Connect output 0 of f to input 0 of a
def c1 = f.out[0] | a
//Connect output 1 of f to input 0 of b
def c2 = f.out[1] | b

// Check that the connections are correct
assertSame(f.getPushableOutput(0).getProcessor(), a)
assertSame(f.getPushableOutput(1).getProcessor(), b)
assertSame(b.getPullableInput(0).getProcessor(), f)
assertSame(a.getPullableInput(0).getProcessor(), f)
assertNull(a.getPullableInput(1))

// Check that the pipeline returns the last processor in the chain
assertSame(c1, a)
assertSame(c2, b)