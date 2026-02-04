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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ca.uqac.lif.fs.FileSystem;
import ca.uqac.lif.fs.FileSystemException;
import ca.uqac.lif.fs.JarFile;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;

@RunWith(Parameterized.class)
public class ConnectorTest
{
  protected static final FileSystem fs = new JarFile(ConnectorTest.class);

  static
  {
    try
    {
      fs.open();
      fs.chdir("scripts/");
    }
    catch (FileSystemException e)
    {
      throw new RuntimeException("Could not open file system", e);
    }
  }

  @Parameters(name = "{0}")
  public static Collection<Object[]> data() throws FileSystemException
  {
    List<Object[]> params = new ArrayList<>();
    for (String fn : fs.ls())
    {
      if (fs.isFile(fn) && fn.endsWith(".groovy"))
      {
        params.add(new Object[] { fn });
      }
    }
    return params;
  }

  private final String scriptName;

  public ConnectorTest(String scriptName)
  {
    this.scriptName = scriptName;
  }

  protected final ClassLoader loader = new GroovyClassLoader(getClass().getClassLoader());
  protected final GroovyShell shell = new GroovyShell(loader);

  @Test
  public void testEachScript() throws Exception
  {
    try
    {
      shell.evaluate(reader(scriptName), scriptName);
    }
    catch (Throwable t)
    {
      throw new AssertionError("Script failed: " + scriptName, t);
    }
  }


  protected static Reader reader(String resource) throws FileSystemException
  {
    InputStream is = fs.readFrom(resource);
    return new InputStreamReader(is);
  }
}
