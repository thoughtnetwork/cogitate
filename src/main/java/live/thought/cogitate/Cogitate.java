/*
 * cogitate - Blockchain browser for the Thought Network.
 *
 * Copyright (c) 2018 - 2019, Thought Network LLC
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, version 2, as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */
package live.thought.cogitate;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

public class Cogitate
{
  /** The Commons CLI command line parser. */
  protected static final CommandLineParser gnuParser           = new GnuParser();
  /** Options for the command line parser. */
  protected static final Options           options             = new Options();
  /** Default configuration options. **/
  private static final String              DEFAULT_HOST        = "localhost";
  private static final String              DEFAULT_PORT        = "10617";
  private static final String              DEFAULT_USER        = "user";
  private static final String              DEFAULT_PASS        = "pass";
  private static final String              DEFAULT_SERVER_HOST = "0.0.0.0";
  private static final String              DEFAULT_SERVER_PORT = "3141";
  /** Configuration options. **/
  private static final String              RPC_HOST            = "rpc.host";
  private static final String              RPC_PORT            = "rpc.port";
  private static final String              RPC_USER            = "rpc.user";
  private static final String              RPC_PASS            = "rpc.pass";
  private static final String              SERVER_HOST         = "server.host";
  private static final String              SERVER_PORT         = "server.port";

  /** Logging **/
  private static final Logger              LOG;
  private static final Logger              mainLogger;

  /** Server Properties **/
  protected String                         serverHost;
  protected int                            serverPort;
  protected String                         rpcHost;
  protected int                            rpcPort;
  protected String                         rpcUser;
  protected String                         rpcPass;

  /** Single instance */
  private static Cogitate                  instance;

  static
  {
    mainLogger = Logger.getLogger("live.thought");
    mainLogger.setUseParentHandlers(false);
    ConsoleHandler handler = new ConsoleHandler();
    handler.setFormatter(new SimpleFormatter()
    {
      private static final String format = "[%1$tF %1$tT] [%2$-7s] %3$s %n";

      @Override
      public synchronized String format(LogRecord lr)
      {
        return String.format(format, new Date(lr.getMillis()), lr.getLevel().getLocalizedName(), lr.getMessage());
      }
    });
    mainLogger.addHandler(handler);
    mainLogger.setLevel(Level.ALL);
    LOG = Logger.getLogger(Cogitate.class.getName());
    LOG.setLevel(Level.ALL);

    options.addOption("h", "help", true, "Display usage information.");
    options.addOption("f", "file", true, "Properties file to load.");
    options.addOption("H", "host", true, "Thought RPC server host (default: localhost)");
    options.addOption("P", "port", true, "Thought RPC server port (default: 10617)");
    options.addOption("u", "user", true, "Thought server RPC user");
    options.addOption("p", "password", true, "Thought server RPC password");
    options.addOption("b", "server-host", true, "Cogitate server bind address (default: all interfaces)");
    options.addOption("s", "server-port", true, "Cogitate server port (default: 3141)");
  }

  protected Cogitate(Properties props)
  {
    if (null == instance)
    {
      instance = this;

      rpcHost = props.getProperty(RPC_HOST, DEFAULT_HOST);
      rpcPort = Integer.parseInt(props.getProperty(RPC_PORT, DEFAULT_PORT));
      rpcUser = props.getProperty(RPC_USER, DEFAULT_USER);
      rpcPass = props.getProperty(RPC_PASS, DEFAULT_PASS);
      serverHost = props.getProperty(SERVER_HOST, DEFAULT_SERVER_HOST);
      serverPort = Integer.parseInt(props.getProperty(SERVER_PORT, DEFAULT_SERVER_PORT));

    }
  }

  public void run()
  {
    Server server = new Server(new InetSocketAddress(serverHost, serverPort));
    ServletHandler handler = new ServletHandler();
    server.setHandler(handler);
    handler.addServletWithMapping(BlockServlet.class, "/getblock");
    handler.addServletWithMapping(TransactionServlet.class, "/gettransaction");
    handler.addServletWithMapping(ResourceServlet.class, "/resources/*");
    handler.addServletWithMapping(BlockchainInfoServlet.class, "/");
    try
    {
      server.start();
    }
    catch (Exception e)
    {
      throw new RuntimeException("Exception starting Cogitate server: ", e);
    }
    try
    {
      server.join();
    }
    catch (InterruptedException e)
    {
      LOG.warning("Interrupted during server.join()");
    }
  }

  public String getServerHost()
  {
    return serverHost;
  }

  public void setServerHost(String serverHost)
  {
    this.serverHost = serverHost;
  }

  public int getServerPort()
  {
    return serverPort;
  }

  public void setServerPort(int serverPort)
  {
    this.serverPort = serverPort;
  }

  public String getRpcHost()
  {
    return rpcHost;
  }

  public void setRpcHost(String rpcHost)
  {
    this.rpcHost = rpcHost;
  }

  public int getRpcPort()
  {
    return rpcPort;
  }

  public void setRpcPort(int rpcPort)
  {
    this.rpcPort = rpcPort;
  }

  public String getRpcUser()
  {
    return rpcUser;
  }

  public void setRpcUser(String rpcUser)
  {
    this.rpcUser = rpcUser;
  }

  public String getRpcPass()
  {
    return rpcPass;
  }

  public void setRpcPass(String rpcPass)
  {
    this.rpcPass = rpcPass;
  }

  public URL getThoughtURL()
  {
    URL url = null;
    try
    {
      url = new URL("http://" + rpcUser + ':' + rpcPass + "@" + rpcHost + ":" + rpcPort + "/");
    }
    catch (MalformedURLException e)
    {
      throw new IllegalArgumentException("Invalid URL: " + url);
    }
    return url;
  }

  public static Cogitate instance()
  {
    return instance;
  }

  protected static void usage()
  {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("Cogitate", options);
  }

  public static void main(String[] args) throws Exception
  {
    CommandLine commandLine = null;
    Properties props = new Properties();
    try
    {
      commandLine = gnuParser.parse(options, args);
      if (commandLine.hasOption("file"))
      {
        File pfile = new File(commandLine.getOptionValue("file"));
        InputStream is = new FileInputStream(pfile);
        props.load(is);
        is.close();
      }
      if (commandLine.hasOption("host"))
      {
        props.setProperty(RPC_HOST, commandLine.getOptionValue("host"));
      }
      if (commandLine.hasOption("port"))
      {
        props.setProperty(RPC_PORT, commandLine.getOptionValue("port"));
      }
      if (commandLine.hasOption("user"))
      {
        props.setProperty(RPC_USER, commandLine.getOptionValue("user"));
      }
      if (commandLine.hasOption("password"))
      {
        props.setProperty(RPC_PASS, commandLine.getOptionValue("password"));
      }
      if (commandLine.hasOption("server-host"))
      {
        props.setProperty(SERVER_HOST, commandLine.getOptionValue("server-host"));
      }
      if (commandLine.hasOption("server-port"))
      {
        props.setProperty(SERVER_PORT, commandLine.getOptionValue("server-port"));
      }
      if (commandLine.hasOption("help"))
      {
        usage();
        System.exit(1);
      }

      Cogitate c = new Cogitate(props);
      c.run();
    }
    catch (ParseException pe)
    {
      System.err.println(pe.getLocalizedMessage());
      usage();
    }
    catch (Exception e)
    {
      e.printStackTrace(System.err);
    }

  }
}