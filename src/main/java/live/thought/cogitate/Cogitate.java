package live.thought.cogitate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

import live.thought.thought4j.ThoughtClientInterface.Block;
import live.thought.thought4j.ThoughtClientInterface.BlockChainInfo;
import live.thought.thought4j.ThoughtClientInterface.RawTransaction;
import live.thought.thought4j.ThoughtClientInterface.RawTransaction.In;
import live.thought.thought4j.ThoughtClientInterface.RawTransaction.Out;
import live.thought.thought4j.ThoughtClientInterface.RawTransaction.Out.ScriptPubKey;
import live.thought.thought4j.ThoughtRPCClient;

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
  private static final String              DEFAULT_SERVER_PORT = "3141";
  /** Configuration options. **/
  private static final String              RPC_HOST            = "rpc.host";
  private static final String              RPC_PORT            = "rpc.port";
  private static final String              RPC_USER            = "rpc.user";
  private static final String              RPC_PASS            = "rpc.pass";
  private static final String              SERVER_PORT         = "server.port";
  
  private static final String              RESOURCE_PATH       = "res";

  /** Logging **/
  private static final Logger              LOG;
  private static final Logger              mainLogger;

  /** Server Properties **/
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
      serverPort = Integer.parseInt(props.getProperty(SERVER_PORT, DEFAULT_SERVER_PORT));

    }
  }

  public void run()
  {
    Server server = new Server(serverPort);
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
  
  private static String baseUrl(HttpServletRequest request)
  {
    return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
  }

  @SuppressWarnings("serial")
  public static class BlockchainInfoServlet extends HttpServlet
  {
    /** Thoughtd Client **/
    private ThoughtRPCClient client;

    public BlockchainInfoServlet()
    {
      URL thoughtUrl = Cogitate.instance().getThoughtURL();
      client = new ThoughtRPCClient(thoughtUrl);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
      try
      {
        BlockChainInfo bci = client.getBlockChainInfo();
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println(CogitateStyler.LEADER);
        response.getWriter().println(CogitateStyler.HEADER.replace("@@BASE_URL@@", baseUrl(request)));
        response.getWriter().println("<section>");
        response.getWriter().println(CogitateStyler.NAV.replace("@@BASE_URL@@", baseUrl(request)));
        response.getWriter().println("<article>");
        response.getWriter().println("<h1>Thought Blockchain Info</h1>");
        response.getWriter().println("<table>");
        response.getWriter().println("<tr><td>Network</td><td>" + bci.chain() + "</td></tr>");
        response.getWriter().println("<tr><td>Number of Blocks</td><td>" + bci.blocks() + "</td></tr>");
        // Link to the top block
        response.getWriter().print("<tr><td>Best Block Hash</td><td>");
        response.getWriter().print("<a href='");
        response.getWriter().print(baseUrl(request));
        response.getWriter().println("/getblock?hash=" + bci.bestBlockHash() + "'>" + bci.bestBlockHash() + "</a></td></tr>");
        response.getWriter().println("<tr><td>Difficulty</td><td>" + bci.difficulty() + "</td></tr>");
        response.getWriter().println("<tr><td>Chainwork</td><td>" + bci.chainWork() + "</td></tr>");
        response.getWriter().println("<tr><td>Verification Progress</td><td>" + bci.verificationProgress() + "</td></tr>");
        response.getWriter().println("</table>");
        response.getWriter().println("</article>");
        response.getWriter().println("</section>");
        response.getWriter().println(CogitateStyler.FOOTER);
        response.getWriter().println(CogitateStyler.TRAILER);
      }
      catch (Exception e)
      {
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().println("<h1>Thought Daemon not responding.</h1>");
      }
    }
  }

  @SuppressWarnings("serial")
  public static class TransactionServlet extends HttpServlet
  {
    /** Thoughtd Client **/
    private ThoughtRPCClient client;

    public TransactionServlet()
    {
      URL thoughtUrl = Cogitate.instance().getThoughtURL();
      client = new ThoughtRPCClient(thoughtUrl);
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
      String hash = request.getParameter("hash");
      if (null == hash)
      {
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().println("<h1>No Transaction Hash Specified</h1>");
      }
      else
      {
        RawTransaction rt = client.getRawTransaction(hash);
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println(CogitateStyler.LEADER);
        response.getWriter().println(CogitateStyler.HEADER.replace("@@BASE_URL@@", baseUrl(request)));
        response.getWriter().println("<section>");
        response.getWriter().println(CogitateStyler.NAV.replace("@@BASE_URL@@", baseUrl(request)));
        response.getWriter().println("<article>");
        response.getWriter().println("<h1>Transaction " + rt.txId() + "</h1>");
        
        response.getWriter().println("<table>");
        response.getWriter().println("<tr><td>Version</td><td>" + rt.version() + "</td></tr>");
        response.getWriter().println("<tr><td>Time</td><td>" + rt.time() + "</td></tr>");
        response.getWriter().println("<tr><td>Block Time</td><td>" + rt.blocktime() + "</td></tr>");
        response.getWriter().print("<tr><td>Block Hash</td><td>");
        response.getWriter().print("<a href='");
        response.getWriter().print(baseUrl(request));
        response.getWriter().println("/getblock?hash=" + rt.blockHash() + "'>" + rt.blockHash() + "</a></td></tr>");        
        response.getWriter().println("<tr><td>Size</td><td>" + rt.size() + "</td></tr>");
        response.getWriter().println("<tr><td>Confirmations</td><td>" + rt.confirmations() + "</td></tr>");
        response.getWriter().println("<tr><td>Lock Time</td><td>" + rt.lockTime() + "</td></tr>");
        response.getWriter().println("</table>");
              

        response.getWriter().println("<h3>Inputs</h3>");      
        List<In> inputs = rt.vIn();
        response.getWriter().println("<table>");
        boolean first = true;
        for (In in : inputs)
        {
          if (in.coinbase())
          {
            response.getWriter().println("<tr><td>Coinbase</tr></td>");
          }
          else
          {
            if (first)
            {
              response.getWriter().println("<tr>");
              response.getWriter().println("<th>Sequence</th>");
              response.getWriter().println("<th>Transaction ID</th>");
              response.getWriter().println("<th>Value</th>");
              response.getWriter().println("</tr>");
              first = false;
              
            }
            response.getWriter().println("<tr>");
            response.getWriter().println("<td" + in.sequence() + "</td>");
            Out txout = in.getTransactionOutput();
            response.getWriter().println("<td>" + txout.transaction().hash() + "</td>");
            response.getWriter().println("<td>" + txout.value() + "</td>");
            response.getWriter().println("</tr>");
          }
        }
        response.getWriter().println("</table>");
        
        response.getWriter().println("<h3>Outputs</h3>");
        response.getWriter().println("<table>");
        List<Out> outputs = rt.vOut();
        response.getWriter().println("<tr><th>Address</th><th>Value</th></tr>");
        for (Out out : outputs)
        {
          response.getWriter().print("<td>");
          ScriptPubKey spk = out.scriptPubKey();
          List<String> addresses = spk.addresses();
          if (addresses != null && addresses.size() > 0)
          {
            boolean addBreak = false;
            for (String addr : addresses)
            {
              response.getWriter().print(addr);
              if (addBreak)
              {
                response.getWriter().print("<br>");
              }
              else
              {
                addBreak = true;
              }
            }      
          }
          else
          {
            response.getWriter().print(spk.hex());
          }
          response.getWriter().println("</td>");
          response.getWriter().println("<td>" + out.value() + "</td><tr>");
        }
     
        response.getWriter().println("</table>");
        response.getWriter().println("</article>");
        response.getWriter().println("</section>");
        response.getWriter().println(CogitateStyler.FOOTER);
        response.getWriter().println(CogitateStyler.TRAILER);
      }

    }
  }

  @SuppressWarnings("serial")
  public static class BlockServlet extends HttpServlet
  {
    /** Thoughtd Client **/
    private ThoughtRPCClient client;

    public BlockServlet()
    {
      URL thoughtUrl = Cogitate.instance().getThoughtURL();
      client = new ThoughtRPCClient(thoughtUrl);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
      String hash = request.getParameter("hash");
      if (null == hash)
      {
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().println("<h1>No Block Hash Specified</h1>");
      }
      else
      {
        Block b = client.getBlock(hash);
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println(CogitateStyler.LEADER);
        response.getWriter().println(CogitateStyler.HEADER.replace("@@BASE_URL@@", baseUrl(request)));
        response.getWriter().println("<section>");
        response.getWriter().println(CogitateStyler.NAV.replace("@@BASE_URL@@", baseUrl(request)));
        response.getWriter().println("<article>");
        response.getWriter().println("<h1>Block " + b.height() + " (" + b.hash() + ")</h1>");
        response.getWriter().println("<table>");
        response.getWriter().println("<tr><td>Confirmations</td><td>" + b.confirmations() + "</td></tr>");
        response.getWriter().println("<tr><td>Size</td><td>" + b.size() + "</td></tr>");
        response.getWriter().println("<tr><td>Time</td><td>" + b.time() + "</td></tr>");
        response.getWriter().println("<tr><td>Version</td><td>" + b.version() + "</td></tr>");
        String prev = b.previousHash();
        if (null != prev && prev.length() > 0)
        {
          response.getWriter().print("<tr><td>Previous Block</td><td>");
          response.getWriter().print("<a href='");
          response.getWriter().print(baseUrl(request));
          response.getWriter().println("/getblock?hash=" + prev + "'>" + prev + "</a></td></tr>");
        }

        // Link to the next block if there is one
        String next = b.nextHash();
        if (null != next && next.length() > 0)
        {
          response.getWriter().print("<tr><td>Next Block</td><td>");
          response.getWriter().print("<a href='");
          response.getWriter().print(baseUrl(request));
          response.getWriter().println("/getblock?hash=" + next + "'>" + next + "</a></td></tr>");
        }

        response.getWriter().println("</table>");
        
        response.getWriter().print("<h3>Transactions</h3>");
        response.getWriter().println("<table>");
        List<String> txs = b.tx();
        if (null != txs)
        {
          for (String s : txs)
          {
            response.getWriter().print("<a href='");
            response.getWriter().print(baseUrl(request));
            response.getWriter().print("/gettransaction?hash=" + s + "'>" + s + "</a><br/>");
          }
        }
        response.getWriter().println("</td></tr>");
        // Link to the previous block, if there is one
        response.getWriter().println("</table>");
        
        response.getWriter().println("</article>");
        response.getWriter().println("</section>");
        response.getWriter().println(CogitateStyler.FOOTER);
        response.getWriter().println(CogitateStyler.TRAILER);
      }
    }
  }
  
  @SuppressWarnings("serial")
  public static class ResourceServlet extends HttpServlet
  {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
      try
      {
        String resource = RESOURCE_PATH + request.getPathInfo();
        response.setContentType("image/png");
        response.setStatus(HttpServletResponse.SC_OK);
        InputStream instream = Cogitate.class.getClassLoader().getResourceAsStream(resource);
        OutputStream os = response.getOutputStream();
        int b = instream.read();
        while (b != -1)
        {
          os.write(b);
          b = instream.read();
        }
        instream.close();
      }
      catch (Exception e)
      {
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        e.printStackTrace(System.err);
      }
    }
  }
}