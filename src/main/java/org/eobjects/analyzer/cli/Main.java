/**
 * eobjects.org AnalyzerBeans
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.analyzer.cli;

import java.io.PrintWriter;

import org.kohsuke.args4j.CmdLineException;

/**
 * Main class for the AnalyzerBeans Command-line interface (CLI).
 * 
 * @author Kasper Sørensen
 */
public final class Main {
	
	private static PrintWriter out = new PrintWriter(System.out);

	/**
	 * Main method of the Command-line interface (CLI)
	 * 
	 * @param args
	 *            command-line arguments
	 */
	public static void main(String[] args) {
		try {
			CliArguments arguments = CliArguments.parse(args);
			CliRunner runner = new CliRunner(arguments, out);
			runner.run();
		} catch (CmdLineException e) {
			CliArguments.printUsage(out);
		}
		out.flush();
	}
	
	public static void setOut(PrintWriter out) {
		Main.out = out;
	}
}
