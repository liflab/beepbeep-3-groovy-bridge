import static beepbeep.groovy.*
import static org.junit.Assert.*

def s = QueueSink(2)
s << "foo"
s.in[1] << "bar"
assertEquals("foo", s.getQueue().poll())
assertEquals("bar", s.getQueue(1).poll())