package live.thought.cogitate;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import live.thought.thought4j.ThoughtRPCClient;
import live.thought.thought4j.ThoughtClientInterface.Block;

@SuppressWarnings("serial")
public class BlockServlet extends HttpServlet
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
      response.getWriter().println(CogitateStyler.HEADER.replace("@@BASE_URL@@", Cogitate.baseUrl(request)));
      response.getWriter().println("<section>");
      response.getWriter().println(CogitateStyler.NAV.replace("@@BASE_URL@@", Cogitate.baseUrl(request)));
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
        response.getWriter().print(Cogitate.baseUrl(request));
        response.getWriter().println("/getblock?hash=" + prev + "'>" + prev + "</a></td></tr>");
      }

      // Link to the next block if there is one
      String next = b.nextHash();
      if (null != next && next.length() > 0)
      {
        response.getWriter().print("<tr><td>Next Block</td><td>");
        response.getWriter().print("<a href='");
        response.getWriter().print(Cogitate.baseUrl(request));
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
          response.getWriter().print(Cogitate.baseUrl(request));
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