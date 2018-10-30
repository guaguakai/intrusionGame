package utilities;

import java.io.PrintStream;

public class NullPrintStream extends PrintStream {
	public NullPrintStream() {
		super(new NullOutputStream());
		// TODO Auto-generated constructor stub
	}
}
