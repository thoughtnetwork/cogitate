package live.thought.cogitate;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import live.thought.thought4j.ThoughtClientInterface.BlockChainInfo;
import live.thought.thought4j.ThoughtRPCClient;

@SuppressWarnings("serial")
public class CoinServlet extends HttpServlet
{
  /** Thoughtd Client **/
  private ThoughtRPCClient client;

  public CoinServlet()
  {
    URL thoughtUrl = Cogitate.instance().getThoughtURL();
    client = new ThoughtRPCClient(thoughtUrl);
  }
  
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    String query = request.getParameter("q");
    try
    {
      BlockChainInfo bci = client.getBlockChainInfo();
      
      double supply = ((bci.blocks() - 1) * 314) + Cogitate.instance().getCoinPremine() - Cogitate.instance().getCoinBurned();
      double circulating = supply - Cogitate.instance().getCoinLocked();
      
      String output;
      if (null != query && "totalcoins".equalsIgnoreCase(query))
      {
        output = String.format("%.8f", supply);
      }
      else if (null != query && "circulating".equalsIgnoreCase(query))
      {
        output = String.format("%.8f", circulating);
      }
      else
      {
        response.sendError(400, "Malformed query");
        return;
      }
      
      System.out.println(output);
      response.setContentType("text/plain");
      response.setCharacterEncoding("UTF-8");
      response.setStatus(HttpServletResponse.SC_OK);

      // create HTML response
      PrintWriter  responder = response.getWriter();
      responder.append(output);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      response.sendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Thought Daemon not responding");
    }
  }
}
