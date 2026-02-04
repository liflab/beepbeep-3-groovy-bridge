/*
    Syntactical shortcuts for BeepBeep in Groovy
    Copyright (C) 2023-2026 Laboratoire d'informatique formelle

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package beepbeep;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Queue;

import ca.uqac.lif.cep.CallAfterConnect;
import ca.uqac.lif.cep.CallAfterConnect.StartAfterConnect;
import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.ProcessorException;
import ca.uqac.lif.cep.functions.BinaryFunction;
import ca.uqac.lif.cep.functions.Constant;
import ca.uqac.lif.cep.functions.Function;
import ca.uqac.lif.cep.functions.FunctionTree;
import ca.uqac.lif.cep.io.SpliceSource.SpliceByteSource;
import ca.uqac.lif.cep.io.SpliceSource.SpliceLineSource;
import ca.uqac.lif.cep.io.SpliceSource.SpliceTokenSource;
import ca.uqac.lif.cep.io.WriteOutputStream;
import ca.uqac.lif.cep.tmf.Pump;
import ca.uqac.lif.cep.tuples.SpliceTupleSource;
import ca.uqac.lif.cep.util.NthElement;

/**
 * A utility class defining static methods to instantiate processors and
 * functions from various BeepBeep packages.
 * <p>
 * The goal of this class is not to provide new functionalities, but only
 * to reduce the amount of boilerplate code that needs to be written,
 * especially in Groovy scripts that use BeepBeep objects. Thus, a "classical"
 * Java piece of code like:
 * <pre>
 * import static ca.uqac.lif.cep.Connector.connect;
 * import ca.uqac.lif.cep.functions.Cumulate;
 * import ca.uqac.lif.cep.json.JsonPath;
 * import ca.uqac.lif.cep.json.NumberValue;
 * import ca.uqac.lif.cep.util.Numbers;
 * 
 * ApplyFunction val = new ApplyFunction(new FunctionTree(NumberValue.instance, new JsonPath("foo")));
 * Cumulate sum = new Cumulate(Numbers.addition);
 * connect(val, new)</pre>
 * should become in Groovy:
 * <pre>
 * import static beepbeep.groovy.*
 * 
 * ApplyFunction(FunctionTree(NumberValue.instance, JsonPath("foo"))) | Cumulate(Numbers.addition)</pre>
 * <p>
 * One can see that:
 * <ul>
 * <li>static methods avoid the repeated use of <tt>new</tt> to instantiate
 * processors</li>
 * <li>all these methods are grouped under the same "umbrella" class,
 * requiring a single <tt>import</tt> statement</li>
 * <li>explicit calls to <tt>Connector.connect</tt> are replaced by the
 * pipe character</li>
 * </ul> 
 * 
 * @author Sylvain Hallé
 */
public class groovy
{
	/* ca.uqac.lif.cep */

	/**
	 * Creates a new instance of the {@link ca.uqac.lif.cep.GroupProcessor}
	 * processor.
	 * @param in_arity The input arity of the group
	 * @param out_arity The output arity of the group
	 * @return The processor
	 */
	public static class Group extends ca.uqac.lif.cep.GroupProcessor
	{
		public Group()
		{
			super();
		}

		public Group(int in_arity, int out_arity)
		{
			super(in_arity, out_arity);
		}

		public Processor i(Processor p)
		{
			return in(p);
		}

		public CallAfterConnect o(Processor p)
		{
			return out(p);
		}
	}

	public static final int BOTTOM = ca.uqac.lif.cep.Connector.BOTTOM;

	public static final int INPUT = ca.uqac.lif.cep.Connector.INPUT;

	public static final int OUTPUT = ca.uqac.lif.cep.Connector.OUTPUT;

	public static final int TOP = ca.uqac.lif.cep.Connector.TOP;

	/* ca.uqac.lif.cep.functions */

	/**
	 * Creates a new instance of the {@link ca.uqac.lif.cep.functions.ApplyFunction}
	 * processor.
	 * @param f The function to apply to each event
	 * @return The processor
	 */
	public static ca.uqac.lif.cep.functions.ApplyFunction ApplyFunction(Object f)
	{
		return new ca.uqac.lif.cep.functions.ApplyFunction(liftFunction(f));
	}

	/**
	 * A class extending {@ca.uqac.lif.cep.functions.StreamVariable} to provide
	 * direct access to its static fields and methods.
	 */
	public static class StreamVariable extends ca.uqac.lif.cep.functions.StreamVariable
	{
		// Nothing
	}

	/**
	 * Creates a new instance of the {@link ca.uqac.lif.cep.functions.Constant}
	 * function.
	 * @param c The value to return
	 * @return The function
	 */
	public static ca.uqac.lif.cep.functions.Constant Constant(Object c)
	{
		return new ca.uqac.lif.cep.functions.Constant(c);
	}

	/**
	 * Creates a new instance of the {@link ca.uqac.lif.cep.functions.Cumulate}
	 * processor.
	 * @param f The function to cumulate
	 * @return The processor
	 */
	public static ca.uqac.lif.cep.functions.Cumulate Cumulate(ca.uqac.lif.cep.functions.BinaryFunction<?,?,?> f)
	{
		return new ca.uqac.lif.cep.functions.Cumulate(f);
	}

	/**
	 * Creates a new instance of the {@link ca.uqac.lif.cep.functions.FunctionTree}
	 * function.
	 * @param arguments The arguments to create the function tree
	 * @return The function
	 */
	public static ca.uqac.lif.cep.functions.FunctionTree FunctionTree(Object ... arguments)
	{
		return new ca.uqac.lif.cep.functions.FunctionTree(liftFunction(arguments));
	}

	/**
	 * Creates a new instance of the {@link ca.uqac.lif.cep.functions.IdentityFunction}
	 * function.
	 * @return The function
	 */
	public static ca.uqac.lif.cep.functions.IdentityFunction IdentityFunction()
	{
		return new ca.uqac.lif.cep.functions.IdentityFunction();
	}

	/**
	 * Creates a new instance of the {@link ca.uqac.lif.cep.functions.IdentityFunction}
	 * function of given arity.
	 * @param arity The arity of the function
	 * @return The function
	 */
	public static ca.uqac.lif.cep.functions.IdentityFunction IdentityFunction(int arity)
	{
		return new ca.uqac.lif.cep.functions.IdentityFunction(arity);
	}

	/**
	 * Creates a new instance of the {@link ca.uqac.lif.cep.functions.IfThenElse}
	 * function.
	 * @return The function
	 */
	public static ca.uqac.lif.cep.functions.IfThenElse IfThenElse()
	{
		return ca.uqac.lif.cep.functions.IfThenElse.instance;
	}

	/**
	 * Creates a new instance of the {@link ca.uqac.lif.cep.functions.TurnInto}
	 * processor.
	 * @param o The object to return
	 * @return The processor
	 */
	public static ca.uqac.lif.cep.functions.TurnInto TurnInto(Object o)
	{
		return new ca.uqac.lif.cep.functions.TurnInto(o);
	}

	/* ca.uqac.lif.cep.io */

	/**
	 * An instance of the {@link ca.uqac.lif.cep.io.ToBase64} function.
	 */
	public static ca.uqac.lif.cep.io.ToBase64 ToBase64 = ca.uqac.lif.cep.io.ToBase64.instance;

	/**
	 * Creates an new instance of the {@link ca.uqac.lif.cep.io.Print}
	 * processor.
	 * @return The processor
	 */
	public static StartAfterConnect Print()
	{
		return Print(System.out);
	}

	/**
	 * Creates an new instance of the {@link ca.uqac.lif.cep.io.Print}
	 * processor.
	 * @param ps The print stream to write the events to
	 * @return The processor
	 */
	public static StartAfterConnect Print(PrintStream ps)
	{
		return new StartAfterConnect(new PullPrintln(ps));
	}

	/**
	 * Creates an new instance of the {@link ca.uqac.lif.cep.io.WriteOutputStream}
	 * processor.
	 * @param os The output stream to write to
	 * @return The processor
	 */
	public static StartAfterConnect Write(OutputStream os)
	{
		return new StartAfterConnect(new PullWrite(os));
	}

	/**
	 * Creates an new instance of the {@link ca.uqac.lif.cep.io.WriteOutputStream}
	 * processor, and redirects its output to the Zeppelin console if it is
	 * running. Otherwise, the processor falls back to {@code System.out}.
	 * @return The processor
	 */
	public static StartAfterConnect ZPrint()
	{
		return Write(ca.uqac.lif.cep.zeppelin.ZepBridge.out());
	}

	/**
	 * Creates an new instance of the {@link ca.uqac.lif.cep.io.WriteOutputStream}
	 * processor that writes images in the image format of the a
	 * <href="https://sw.kovidgoyal.net/kitty/graphics-protocol/">Kitty</a> terminal.
	 * @param os
	 * @return
	 */
	public static StartAfterConnect KPrint(OutputStream os)
	{
		return new StartAfterConnect(new PullWriteSuffix(os, "\u001b_Gf=100;".getBytes(), "\u001b\\".getBytes()));
	}

	/**
	 * Creates an new instance of the {@link ca.uqac.lif.cep.io.WriteOutputStream}
	 * processor that writes HTML content.
	 * @param os The output stream to write to
	 * @return The processor
	 */
	public static StartAfterConnect ZHtml()
	{
		OutputStream os = ca.uqac.lif.cep.zeppelin.ZepBridge.out();
		try
		{
			os.write("%html ".getBytes());
			os.flush();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new StartAfterConnect(new PullWrite(os));
	}	

	/**
	 * Creates an new instance of the {@link ca.uqac.lif.cep.io.WriteOutputStream}
	 * processor directing it to the standard output.
	 * @return The processor
	 */
	public static StartAfterConnect Stdout()
	{
		return Write(System.out);
	}

	/**
	 * Creates an new instance of the {@link ca.uqac.lif.cep.io.WriteOutputStream}
	 * processor directing it to the standard error.
	 * @return The processor
	 */
	public static StartAfterConnect Stderr()
	{
		return Write(System.err);
	}

	/**
	 * Creates an new instance of the {@link ca.uqac.lif.cep.io.Print.Println}
	 * processor.
	 * @return The processor
	 */
	public static ca.uqac.lif.cep.io.Print.Println Println()
	{
		return new ca.uqac.lif.cep.io.Print.Println();
	}

	/**
	 * Creates an new instance of the {@link ca.uqac.lif.cep.io.Print.Println}
	 * processor.
	 * @param ps The print stream to write the events to
	 * @return The processor
	 */
	public static

	ca.uqac.lif.cep.io.Print.Println Println(PrintStream ps)
	{
		return new ca.uqac.lif.cep.io.Print.Println(ps);
	}

	/**
	 * Creates an new instance of the {@link ca.uqac.lif.cep.io.SpliceSource.SpliceByteSource}
	 * processor.
	 * @param ps The file names
	 * @return The processor
	 */
	public static ca.uqac.lif.cep.io.SpliceSource.SpliceByteSource SpliceByteSource(String ... args)
	{
		return new ca.uqac.lif.cep.io.SpliceSource.SpliceByteSource(args);
	}

	/**
	 * Creates an new instance of the {@link ca.uqac.lif.cep.io.SpliceSource.SpliceLineSource}
	 * processor.
	 * @param ps The file names
	 * @return The processor
	 */
	public static ca.uqac.lif.cep.io.SpliceSource.SpliceLineSource SpliceLineSource(String ... args)
	{
		return new ca.uqac.lif.cep.io.SpliceSource.SpliceLineSource(args);
	}

	/* ca.uqac.lif.cep.tmf */

	/**
	 * Creates an new instance of the {@link ca.uqac.lif.cep.tmf.CountDecimate}
	 * processor.
	 * @param interval The decimation interval
	 * @return The processor
	 */
	public static ca.uqac.lif.cep.tmf.CountDecimate CountDecimate(int interval)
	{
		return new ca.uqac.lif.cep.tmf.CountDecimate(interval);
	}

	public static ca.uqac.lif.cep.tmf.FilterOn FilterOn(Object f)
	{
		return new ca.uqac.lif.cep.tmf.FilterOn(liftFunction(f));
	}

	/**
	 * Creates an new instance of the {@link ca.uqac.lif.cep.tmf.Fork}
	 * processor.
	 * @return The processor
	 */
	public static ca.uqac.lif.cep.tmf.Fork Fork()
	{
		return new ca.uqac.lif.cep.tmf.Fork();
	}

	/**
	 * Creates an new instance of the {@link ca.uqac.lif.cep.tmf.Fork}
	 * processor.
	 * @param out_arity The output arity of the work
	 * @return The processor
	 */
	public static ca.uqac.lif.cep.tmf.Fork Fork(int out_arity)
	{
		return new ca.uqac.lif.cep.tmf.Fork(out_arity);
	}

	/**
	 * Creates an new instance of the {@link ca.uqac.lif.cep.tmf.KeepLast}
	 * processor.
	 * @return The processor
	 */
	public static ca.uqac.lif.cep.tmf.KeepLast KeepLast()
	{
		return new ca.uqac.lif.cep.tmf.KeepLast();
	}

	/**
	 * Creates an new instance of the {@link ca.uqac.lif.cep.tmf.QueueSink}
	 * processor.
	 * @param arity The input arity of the sink
	 * @return The processor
	 */
	public static ca.uqac.lif.cep.tmf.QueueSink QueueSink(int arity)
	{
		return new ca.uqac.lif.cep.tmf.QueueSink(arity);
	}

	/**
	 * Creates an new instance of the {@link ca.uqac.lif.cep.tmf.QueueSink}
	 * processor with input arity 1.
	 * @return The processor
	 */
	public static ca.uqac.lif.cep.tmf.QueueSink QueueSink()
	{
		return new ca.uqac.lif.cep.tmf.QueueSink(1);
	}

	/**
	 * Creates an new instance of the {@link ca.uqac.lif.cep.tmf.Pump}
	 * processor.
	 * @return The processor
	 */
	public static ca.uqac.lif.cep.tmf.Pump Pump()
	{
		return new ca.uqac.lif.cep.tmf.Pump();
	}

	/**
	 * Creates an new instance of the {@link ca.uqac.lif.cep.tmf.Pump}
	 * processor.
	 * @param interval The frequency at which events are pulled (in ms)
	 * @return The processor
	 */
	public static ca.uqac.lif.cep.tmf.Pump Pump(int interval)
	{
		return Pump(interval);
	}

	/**
	 * Creates an new instance of the {@link ca.uqac.lif.cep.tmf.Slice}
	 * processor.
	 * @param f The slicing function
	 * @param p The processor to run on each slice
	 * @return The processor
	 */
	public static ca.uqac.lif.cep.tmf.Slice Slice(Object f, Object p)
	{
		return new ca.uqac.lif.cep.tmf.Slice(liftFunction(f), liftProcessor(p));
	}

	/**
	 * Creates an new instance of the {@link ca.uqac.lif.cep.tmf.Trim}
	 * processor.
	 * @param prefix The number of events to trim
	 * @return The processor
	 */
	public static ca.uqac.lif.cep.tmf.Trim Trim(int prefix)
	{
		return new ca.uqac.lif.cep.tmf.Trim(prefix);
	}

	/* ca.uqac.lif.cep.util */

	/**
	 * A class extending {@ca.uqac.lif.cep.util.Booleans} to provide direct access
	 * to its static fields and methods.
	 */
	public static class Booleans extends ca.uqac.lif.cep.util.Booleans
	{
		private Booleans()
		{
			super();
		}
	}

	/**
	 * Creates a new instance of the {@link ca.uqac.lif.cep.util.Booleans.And}
	 * function.
	 * @return The function
	 */
	public static ca.uqac.lif.cep.util.Booleans.And And()
	{
		return ca.uqac.lif.cep.util.Booleans.and;
	}

	/**
	 * Creates a function asserting the conjunction of two terms.
	 * @param f1 The first term
	 * @param f2 The second term
	 * @return The function
	 */
	public static ca.uqac.lif.cep.functions.FunctionTree And(Object f1, Object f2)
	{
		return new ca.uqac.lif.cep.functions.FunctionTree(And(), liftFunction(f1), liftFunction(f2));
	}

	/**
	 * Creates a new instance of the {@link ca.uqac.lif.cep.util.Booleans.Or}
	 * function.
	 * @return The function
	 */
	public static ca.uqac.lif.cep.util.Booleans.Or Or()
	{
		return ca.uqac.lif.cep.util.Booleans.or;
	}

	/**
	 * Creates a function asserting the disjunction of two terms.
	 * @param f1 The first term
	 * @param f2 The second term
	 * @return The function
	 */
	public static ca.uqac.lif.cep.functions.FunctionTree Or(Object f1, Object f2)
	{
		return new ca.uqac.lif.cep.functions.FunctionTree(Or(), liftFunction(f1), liftFunction(f2));
	}

	/**
	 * Creates a new instance of the {@link ca.uqac.lif.cep.util.Equals}
	 * function.
	 * @return The function
	 */
	public static ca.uqac.lif.cep.util.Equals Equals()
	{
		return ca.uqac.lif.cep.util.Equals.instance;
	}

	/**
	 * Creates a function asserting the equality of two terms.
	 * @param f1 The first term
	 * @param f2 The second term
	 * @return The function
	 */
	public static Function Equals(Object f1, Object f2)
	{
		return new ca.uqac.lif.cep.functions.FunctionTree(Equals(), liftFunction(f1), liftFunction(f2));
	}

	public static Function Size()
	{
		return ca.uqac.lif.cep.util.Size.instance;
	}

	public static Function Size(Object o)
	{
		return new FunctionTree(ca.uqac.lif.cep.util.Size.instance, liftFunction(o));
	}

	/**
	 * A class extending {@link ca.uqac.lif.cep.util.Bags} to provide direct
	 * access to its static fields and methods.
	 */
	public static class Bags extends ca.uqac.lif.cep.util.Bags
	{
		private Bags()
		{
			super();
		}

		public static Function ApplyToAll(Object f, Object x)
		{
			return new FunctionTree(new Bags.ApplyToAll(liftFunction(f)), liftFunction(x));
		}
	}

	/**
	 * A class extending {@link ca.uqac.lif.cep.util.Lists} to provide direct
	 * access to its static fields and methods.
	 */
	public static class Lists extends ca.uqac.lif.cep.util.Lists
	{
		private Lists()
		{
			super();
		}

		/**
		 * Produces an instance of the {@link ca.uqac.lif.cep.util.Lists.Pack}
		 * processor.
		 * @return The processor
		 */
		public static ca.uqac.lif.cep.util.Lists.Pack Pack()
		{
			return new ca.uqac.lif.cep.util.Lists.Pack();
		}

		/**
		 * Produces an instance of the {@link ca.uqac.lif.cep.util.Lists.Sort}
		 * function.
		 * @return The function
		 */
		@SuppressWarnings("rawtypes")
		public static ca.uqac.lif.cep.util.Lists.Sort Sort()
		{
			return new ca.uqac.lif.cep.util.Lists.Sort();
		}

		/**
		 * Produces an instance of the {@link ca.uqac.lif.cep.util.Lists.SortOn}
		 * function.
		 * @param f The function used to compare elements
		 * @return The function
		 */
		@SuppressWarnings("rawtypes")
		public static ca.uqac.lif.cep.util.Lists.SortOn SortOn(Object f)
		{
			return new ca.uqac.lif.cep.util.Lists.SortOn(liftFunction(f));
		}

		/**
		 * Produces an instance of the {@link ca.uqac.lif.cep.util.Lists.Unpack}
		 * processor.
		 * @return The processor
		 */
		public static ca.uqac.lif.cep.util.Lists.Unpack Unpack()
		{
			return new ca.uqac.lif.cep.util.Lists.Unpack();
		}
	}

	/**
	 * A class extending {@link ca.uqac.lif.cep.util.Maps} to provide direct
	 * access to its static fields and methods.
	 */
	public static class Maps extends ca.uqac.lif.cep.util.Maps
	{
		protected Maps()
		{
			super();
		}

		/**
		 * Creates an instance of the {@link Maps.Keys} function.
		 * @param o The condition to filter the map
		 * @return The function
		 */
		public static ca.uqac.lif.cep.util.Maps.FilterMap FilterMap(Object o)
		{
			return new ca.uqac.lif.cep.util.Maps.FilterMap(liftFunction(o));
		}

		/**
		 * Creates an instance of the {@link Maps.Keys} function.
		 * @return The function
		 */
		public static ca.uqac.lif.cep.util.Maps.Keys Keys()
		{
			return ca.uqac.lif.cep.util.Maps.Keys.instance;
		}

		/**
		 * Creates an instance of the {@link Maps.Values} function.
		 * @return The function
		 */
		public static ca.uqac.lif.cep.util.Maps.Values Values()
		{
			return ca.uqac.lif.cep.util.Maps.Values.instance;
		}
	}

	/**
	 * A class extending {@link ca.uqac.lif.cep.util.Numbers} to provide direct
	 * access to its static fields and methods.
	 */
	public static class Numbers extends ca.uqac.lif.cep.util.Numbers
	{
		private Numbers()
		{
			super();
		}
	}

	/**
	 * A class extending {@link ca.uqac.lif.cep.util.Sets} to provide direct
	 * access to its static fields and methods.
	 */
	public static class Sets extends ca.uqac.lif.cep.util.Sets
	{
		private Sets()
		{
			super();
		}

		public static Processor PutInto()
		{
			return new ca.uqac.lif.cep.util.Sets.PutInto();
		}
	}

	/**
	 * A class extending {@link ca.uqac.lif.cep.util.Sets} to provide direct
	 * access to its static fields and methods.
	 */
	public static class Multiset extends ca.uqac.lif.cep.util.Multiset
	{
		public static final Function getCardinalities = ca.uqac.lif.cep.util.Multiset.getCardinalities;

		public static final Function insert = ca.uqac.lif.cep.util.Multiset.Insert.instance;

		private Multiset()
		{
			super();
		}

		public static Processor PutInto()
		{
			return new ca.uqac.lif.cep.util.Multiset.PutInto();
		}
	}

	public static Function Element(int index)
	{
		return new NthElement(index);
	}

	public static Function ToList(int arity)
	{
		return new Bags.ToList(arity);
	}

	public static Function ToList(int arity, Object x)
	{
		return new FunctionTree(new Bags.ToList(arity), liftFunction(x));
	}

	public static Function MapUnion(BinaryFunction<?,?,?> f)
	{
		return new Maps.MapUnion(f);
	}

	public static Function LessThan(Object x, Object y)
	{
		return new FunctionTree(Numbers.isLessThan, liftFunction(x), liftFunction(y));
	}

	public static Function GreaterThan(Object x, Object y)
	{
		return new FunctionTree(Numbers.isGreaterThan, liftFunction(x), liftFunction(y));
	}

	public static Function Minus(Object x, Object y)
	{
		return new FunctionTree(Numbers.subtraction, liftFunction(x), liftFunction(y));
	}

	public static Function Plus(Object x, Object y)
	{
		return new FunctionTree(Numbers.addition, liftFunction(x), liftFunction(y));
	}

	/**
	 * A class extending {@link ca.uqac.lif.cep.util.Strings} to provide direct
	 * access to its static fields and methods.
	 */
	public static class Strings extends ca.uqac.lif.cep.util.Strings
	{
		// Nothing
	}

	/**
	 * Produces an instance of the {@link ca.uqac.lif.cep.util.Strings.FindRegex}
	 * function.
	 * @param regex The regular expression to look for
	 * @return The function
	 */
	public static ca.uqac.lif.cep.util.Strings.FindRegex FindRegex(String regex)
	{
		return new ca.uqac.lif.cep.util.Strings.FindRegex(regex);
	}

	/**
	 * Produces an instance of the {@link ca.uqac.lif.cep.util.Strings.FindRegexOnce}
	 * function.
	 * @param regex The regular expression to look for
	 * @return The function
	 */
	public static ca.uqac.lif.cep.util.Strings.FindRegexOnce FindRegexOnce(String regex)
	{
		return new ca.uqac.lif.cep.util.Strings.FindRegexOnce(regex);
	}

	/**
	 * Produces an instance of the {@link ca.uqac.lif.cep.util.Strings.ReplaceAll}
	 * function.
	 * @param from An array containing all the patterns to match
	 * @param to An array containing all the corresponding replacement patterns
	 * @return The function
	 */
	public static ca.uqac.lif.cep.util.Strings.ReplaceAll ReplaceAll(String[] from, String[] to)
	{
		return new ca.uqac.lif.cep.util.Strings.ReplaceAll(from, to);
	}

	/**
	 * Produces an instance of the {@link ca.uqac.lif.cep.util.Strings.ReplaceAll}
	 * function.
	 * @param from An array containing all the patterns to match
	 * @param to An array containing all the corresponding replacement patterns
	 * @return The function
	 */
	public static ca.uqac.lif.cep.util.Strings.ReplaceAll ReplaceAll(java.util.List<String> from, java.util.List<String> to)
	{
		return new ca.uqac.lif.cep.util.Strings.ReplaceAll(from, to);
	}

	/**
	 * Produces an instance of the {@link ca.uqac.lif.cep.util.Strings.ReplaceAll}
	 * function.
	 * @param from The patterns to match
	 * @param to The corresponding replacement pattern
	 * @return The function
	 */
	public static ca.uqac.lif.cep.util.Strings.ReplaceAll ReplaceAll(String from, String to)
	{
		return new ca.uqac.lif.cep.util.Strings.ReplaceAll(from, to);
	}

	/**
	 * Produces an instance of the {@link ca.uqac.lif.cep.util.Strings.SplitString}
	 * function.
	 * @param separator The separator to split the string
	 * @return The function
	 */
	public static ca.uqac.lif.cep.util.Strings.SplitString SplitString(String separator)
	{
		return new ca.uqac.lif.cep.util.Strings.SplitString(separator);
	}

	/**
	 * Produces an instance of the {@link ca.uqac.lif.cep.util.Strings.SubString}
	 * function.
	 * @param start The start index
	 * @param end The end index
	 * @return The function
	 */
	public static ca.uqac.lif.cep.util.Strings.Substring Substring(int start, int end)
	{
		return new ca.uqac.lif.cep.util.Strings.Substring(start, end);
	}

	/* ca.uqac.lif.cep.json */

	/**
	 * Produces an instance of the {@link ca.uqac.lif.cep.json.JPathFunction}
	 * function.
	 * @param path The path to extract
	 * @return The function
	 */
	public static ca.uqac.lif.cep.json.JPathFunction JPathFunction(String path)
	{
		return new ca.uqac.lif.cep.json.JPathFunction(path);
	}

	/**
	 * Produces an instance of the {@link ca.uqac.lif.cep.json.NumberValue}
	 * function.
	 * @return The function
	 */
	public static ca.uqac.lif.cep.json.NumberValue NumberValue()
	{
		return ca.uqac.lif.cep.json.NumberValue.instance;
	}

	/**
	 * Produces an instance of the {@link ca.uqac.lif.cep.json.NumberValue}
	 * function with an argument.
	 * @return The function
	 */
	public static Function NumberValue(Object o)
	{
		return new FunctionTree(ca.uqac.lif.cep.json.NumberValue.instance, liftFunction(o));
	}

	/**
	 * Produces an instance of the {@link ca.uqac.lif.cep.json.NumberValue}
	 * function.
	 * @return The function
	 */
	public static ca.uqac.lif.cep.json.StringValue StringValue()
	{
		return ca.uqac.lif.cep.json.StringValue.instance;
	}

	/**
	 * Produces an instance of the {@link ca.uqac.lif.cep.json.NumberValue}
	 * function with an argument.
	 * @return The function
	 */
	public static FunctionTree StringValue(Object o)
	{
		return new FunctionTree(ca.uqac.lif.cep.json.StringValue.instance, liftFunction(o));
	}

	/* ca.uqac.lif.cep.mtnp */

	/**
	 * Creates an new instance of the {@link ca.uqac.lif.cep.mtnp.DrawPlot}
	 * processor.
	 * @param plot The plot type to draw
	 * @param type The image type
	 * @return The processor
	 */
	public static ca.uqac.lif.cep.mtnp.DrawPlot DrawPlot(ca.uqac.lif.mtnp.plot.Plot plot, ca.uqac.lif.mtnp.plot.Plot.ImageType type)
	{
		return new ca.uqac.lif.cep.mtnp.DrawPlot(plot, type);
	}

	/**
	 * Creates an new instance of the {@link ca.uqac.lif.cep.mtnp.DrawPlot}
	 * processor, setting PNG as the default format.
	 * @param plot The plot type to draw
	 * @return The processor
	 */
	public static ca.uqac.lif.cep.mtnp.DrawPlot DrawPngPlot(ca.uqac.lif.mtnp.plot.Plot plot)
	{
		return DrawPlot(plot, ca.uqac.lif.mtnp.plot.Plot.ImageType.PNG);
	}

	/**
	 * Creates an new instance of the {@link ca.uqac.lif.cep.mtnp.DrawPlot}
	 * processor, setting text as the default format.
	 * @param plot The plot type to draw
	 * @return The processor
	 */
	public static ca.uqac.lif.cep.mtnp.DrawPlot DrawTextPlot(ca.uqac.lif.mtnp.plot.Plot plot)
	{
		return DrawPlot(plot, ca.uqac.lif.mtnp.plot.Plot.ImageType.DUMB);
	}

	/**
	 * Creates an new instance of the {@link ca.uqac.lif.cep.mtnp.UpdateTableArray}
	 * processor.
	 * @param prefix The number of events to trim
	 * @return The processor
	 */
	public static ca.uqac.lif.cep.mtnp.UpdateTableArray UpdateTableArray(String ... column_names)
	{
		return new ca.uqac.lif.cep.mtnp.UpdateTableArray(column_names);
	}

	/**
	 * Creates an new instance of the {@link ca.uqac.lif.cep.mtnp.UpdateTableMap}
	 * processor.
	 * @param prefix The number of events to trim
	 * @return The processor
	 */
	public static ca.uqac.lif.cep.mtnp.UpdateTableMap UpdateTableMap(String ... column_names)
	{
		return new ca.uqac.lif.cep.mtnp.UpdateTableMap(column_names);
	}

	/**
	 * Creates an new instance of the {@link ca.uqac.lif.cep.mtnp.UpdateTableStream}
	 * processor.
	 * @param prefix The number of events to trim
	 * @return The processor
	 */
	public static ca.uqac.lif.cep.mtnp.UpdateTableStream UpdateTableStream(String ... column_names)
	{
		return new ca.uqac.lif.cep.mtnp.UpdateTableStream(column_names);
	}

	/**
	 * Returns an instance of an empty Gnuplot scatterplot.
	 * @return The plot
	 */
	public static ca.uqac.lif.mtnp.plot.gnuplot.Scatterplot GnuplotScatterplot()
	{
		return new ca.uqac.lif.mtnp.plot.gnuplot.Scatterplot();
	}

	/* ca.uqac.lif.cep.tuples */

	/**
	 * Produces an instance of the {@link ca.uqac.lif.cep.tuples.FetchAttribute}
	 * function.
	 * @param name The name of the attribute to fetch
	 * @return The function
	 */
	public static ca.uqac.lif.cep.tuples.FetchAttribute FetchAttribute(String name)
	{
		return new ca.uqac.lif.cep.tuples.FetchAttribute(name);
	}

	/**
	 * Produces an instance of the {@link ca.uqac.lif.cep.tuples.MergeScalars}
	 * function.
	 * @param names The name of the keys in the tuple
	 * @return The function
	 */
	public static ca.uqac.lif.cep.tuples.MergeScalars MergeScalars(String ... names)
	{
		return new ca.uqac.lif.cep.tuples.MergeScalars(names);
	}

	/* I/O from the scripts */

	public static SpliceLineSource ReadLinesFrom(String ... filenames)
	{
		if (filenames.length == 0)
		{
			return new SpliceLineSource(false, "-");
		}
		return new SpliceLineSource(false, filenames);
	}

	public static SpliceTokenSource ReadTokensFrom(String separator, String ... filenames)
	{
		if (filenames.length == 0)
		{
			return new SpliceTokenSource(false, separator, "-");
		}
		return new SpliceTokenSource(false, separator, filenames);
	}

	public static SpliceByteSource ReadBytesFrom(String ... filenames)
	{
		if (filenames.length == 0)
		{
			return new SpliceByteSource(false, "-");
		}
		return new SpliceByteSource(false, filenames);
	}

	public static SpliceTupleSource ReadTuplesFrom(String ... filenames)
	{
		if (filenames.length == 0)
		{
			return new SpliceTupleSource(false, "-");
		}
		return new SpliceTupleSource(false, filenames);
	}

	/**
	 * Creates a new instance of the {@link StartAfterConnect} processor
	 * that writes to standard output using {@link PullPrintln}.
	 * @return The processor
	 */
	public static StartAfterConnect Write()
	{
		return new StartAfterConnect(new PullWrite(System.out));
	}

	/**
	 * Creates a new instance of the {@link StartAfterConnect} processor
	 * that writes to a byte buffer {@link PullWrite}.
	 * @return The processor
	 */
	public static StartAfterConnect Collect()
	{
		return new StartAfterConnect(new PullWriteBuffer());
	}

	/**
	 * Creates a new instance of the {@link StartAfterConnect} processor
	 * that writes to standard output using {@link PullWrite}.
	 * @return The processor
	 */
	public static StartAfterConnect WriteBinary()
	{
		return new StartAfterConnect(new PullWrite(System.out));
	}

	/**
	 * Lifts an arbitrary object into a BeepBeep {@link Function}. 
	 * @param o The object
	 * @return The function
	 */
	protected static Function liftFunction(Object o)
	{
		if (o instanceof Function)
		{
			return (Function) o;
		}
		return new Constant(o);
	}

	/**
	 * Lifts an arbitrary object into a BeepBeep {@link Processor}. 
	 * @param o The object
	 * @return The processor
	 */
	protected static Processor liftProcessor(Object o)
	{
		if (o instanceof Processor)
		{
			return (Processor) o;
		}
		return new ca.uqac.lif.cep.functions.ApplyFunction(liftFunction(o));	
	}

	/**
	 * A pump whose {@link #start()} method does not create a new thread.
	 */
	protected static final class SingleThreadPump extends Pump
	{
		@Override
		public void start()
		{
			run();
		}

		@Override
		public SingleThreadPump duplicate(boolean with_state)
		{
			return new SingleThreadPump();
		}
	}

	/**
	 * A {@link WriteOutputStream} processor that includes its own pump. Calling
	 * {@link Processor#run() run()} on this processor therefore automatically
	 * starts pulling events from upstream, until the end of the stream is
	 * reached. As such, this processor does not bring any additional
	 * functionality compared to chaining a {@link Pump} and a {@link WriteOutputStream},
	 * except that it spares the user writing a Groovy script of a few keystrokes.
	 * @author Sylvain Hallé
	 */
	protected static class PullWrite extends GroupProcessor
	{
		/**
		 * The internal pump.
		 */
		private final Pump m_pump;

		/**
		 * The output stream where data is written.
		 */
		protected final OutputStream m_os;

		/**
		 * Creates a new instance of the processor.
		 * @param os The output stream where data is written
		 */
		public PullWrite(OutputStream os)
		{
			super(1, 0);
			m_os = os;
			m_pump = new SingleThreadPump();
			WriteOutputStream pr = new WriteOutputStream(os);
			Connector.connect(m_pump, pr);
			associateInput(m_pump);
			addProcessors(m_pump, pr);
		}

		@Override
		public void start()
		{
			super.start();
			m_pump.run();
		}
	}
	
	protected static class PullWriteSuffix extends GroupProcessor
	{
		/**
		 * The internal pump.
		 */
		private final Pump m_pump;

		/**
		 * The output stream where data is written.
		 */
		protected final OutputStream m_os;
		
		/**
		 * The processor writing to the output stream.
		 */
		protected final WriteOutputStreamSuffix m_wos;

		/**
		 * Creates a new instance of the processor.
		 * @param os The output stream where data is written
		 */
		public PullWriteSuffix(OutputStream os, byte[] prefix, byte[] suffix)
		{
			super(1, 0);
			m_os = os;
			m_pump = new SingleThreadPump();
			m_wos = new WriteOutputStreamSuffix(os, prefix, suffix);
			Connector.connect(m_pump, m_wos);
			associateInput(m_pump);
			addProcessors(m_pump, m_wos);
		}

		@Override
		public void start()
		{
			m_wos.start();
			m_pump.run();
		}
	}

	protected static class WriteOutputStreamSuffix extends WriteOutputStream
	{
		protected final byte[] m_prefix;

		protected final byte[] m_suffix;

		public WriteOutputStreamSuffix(OutputStream os, byte[] prefix, byte[] suffix)
		{
			super(os);
			m_prefix = prefix;
			m_suffix = suffix;
		}

		@Override
		public void start()
		{
			super.start();
			try
			{
				m_outputStream.write(m_prefix);
			}
			catch (IOException e)
			{
				throw new ProcessorException(e);
			}
		}

		@Override
		public boolean onEndOfTrace(Queue<Object[]> outputs)
		{
			super.onEndOfTrace(outputs);
			try
			{
				m_outputStream.write(m_suffix);
				m_outputStream.flush();
			}
			catch (IOException e)
			{
				throw new ProcessorException(e);
			}
			return false;
		}
	}

	/**
	 * A {@link WriteOutputStream} processor that includes its own pump. Calling
	 * {@link Processor#run() run()} on this processor therefore automatically
	 * starts pulling events from upstream, until the end of the stream is
	 * reached. As such, this processor does not bring any additional
	 * functionality compared to chaining a {@link Pump} and a {@link WriteOutputStream},
	 * except that it spares the user writing a Groovy script of a few keystrokes.
	 * @author Sylvain Hallé
	 */
	protected static class PullWriteBuffer extends PullWrite
	{

		/**
		 * Creates a new instance of the processor.
		 */
		public PullWriteBuffer()
		{
			super(new ByteArrayOutputStream());
		}

		@Override
		public String toString()
		{
			return m_os.toString();
		}
	}

	/**
	 * A {@link Println} processor that includes its own pump. Calling
	 * {@link Processor#run() run()} on this processor therefore automatically
	 * starts pulling events from upstream, until the end of the stream is
	 * reached. As such, this processor does not bring any additional
	 * functionality compared to chaining a {@link Pump} and a {@link Println},
	 * except that it spares the user writing a Groovy script of a few keystrokes.
	 * @author Sylvain Hallé
	 */
	protected static class PullPrintln extends GroupProcessor
	{
		/**
		 * The internal pump.
		 */
		private final ca.uqac.lif.cep.tmf.Pump m_pump;

		/**
		 * Creates a new instance of the processor.
		 */
		public PullPrintln(PrintStream ps)
		{
			super(1, 0);
			m_pump = new ca.uqac.lif.cep.tmf.Pump();
			ca.uqac.lif.cep.io.Print.Println pr = new ca.uqac.lif.cep.io.Print.Println(ps);
			Connector.connect(m_pump, pr);
			associateInput(m_pump);
		}

		@Override
		public void start()
		{
			m_pump.run();
		}
	}
}

