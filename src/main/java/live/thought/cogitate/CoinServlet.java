package live.thought.cogitate;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Map;

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
  private static final TemplateRenderer chartRenderer = new TemplateRenderer("chart.html");
  private static final TemplateRenderer svgRenderer = new TemplateRenderer("svg.html");

  public CoinServlet()
  {
    URL thoughtUrl = Cogitate.instance().getThoughtURL();
    client = new ThoughtRPCClient(thoughtUrl);
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    String query = request.getParameter("q");
    
      if (null != query && "totalcoins".equalsIgnoreCase(query))
      {
        doTotal(request, response);
      }
      else if (null != query && "circulating".equalsIgnoreCase(query))
      {
        doCirculating(request,response);
      }
      else if (null != query && "chart".equalsIgnoreCase(query))
      {
        doChart(request,response);
      }
      else if (null != query && "svg".equalsIgnoreCase(query))
      {
        doSvg(request,response);
      }
      else
      {
        response.sendError(400, "Malformed query");
        return;
      }    
  }
  
  protected void doSvg(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    try
    {
      BlockChainInfo bci         = client.getBlockChainInfo();

      double         supply      = ((bci.blocks() - 1) * 314) + Cogitate.instance().getCoinPremine()
          - Cogitate.instance().getCoinBurned();
      double         circulating = supply - Cogitate.instance().getCoinLocked();
      double total = 1618000000.0;
      int running = 100;
      
      
      // Locked 
      int lockedPercent = (int) Math.ceil(Cogitate.instance().getCoinLocked()/total * 100.0);
      double millionLocked = Cogitate.instance().getCoinLocked() / 1000000;
      running -= lockedPercent;
      String lockedString = String.format("{ name: \"Locked\", millions: %.2f, percent: %d}", millionLocked, lockedPercent);
      
      // Circulating
      int circulatingPercent = (int) Math.ceil(circulating/total * 100.0);
      double millionCirculating = circulating / 1000000;
      running -= circulatingPercent;
      String circulatingString = String.format("{ name: \"Circulating\", millions: %.2f, percent: %d}", millionCirculating, circulatingPercent);
      
      // Available   
      double millionUnmined = (total - supply) / 1000000;
      String availableString = String.format("{ name: \"Unmined\", millions: %.2f, percent: %d}", millionUnmined, running);
      
      Map<String, Object> ctx = TemplateRenderer.getBaseContext(request);
      ctx.put("dataset", String.format("[%s,%s,%s]",  circulatingString, lockedString, availableString));
      ctx.put("max", total);
      ctx.put("supply", supply);
      ctx.put("circulating", circulating);
      
      svgRenderer.renderTemplate(response, ctx);
      
    }
    catch (Exception e)
    {
      e.printStackTrace();
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Thought Daemon not responding");
    }
  }

  protected void doChart(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    try
    {
      BlockChainInfo bci         = client.getBlockChainInfo();

      double         supply      = ((bci.blocks() - 1) * 314) + Cogitate.instance().getCoinPremine()
          - Cogitate.instance().getCoinBurned();
      double         circulating = supply - Cogitate.instance().getCoinLocked();
      double total = 1617000000.0;
      int running = 100;
      
      
      // Locked 
      int lockedPercent = (int) Math.ceil(Cogitate.instance().getCoinLocked()/total * 100.0);
      double millionLocked = Cogitate.instance().getCoinLocked() / 1000000;
      running -= lockedPercent;
      String lockedString = String.format("{ name: \"Locked\", millions: %.2f, percent: %d}", millionLocked, lockedPercent);
      
      // Circulating
      int circulatingPercent = (int) Math.ceil(circulating/total * 100.0);
      double millionCirculating = circulating / 1000000;
      running -= circulatingPercent;
      String circulatingString = String.format("{ name: \"Circulating\", millions: %.2f, percent: %d}", millionCirculating, circulatingPercent);
      
      // Available   
      double millionUnmined = (total - supply) / 1000000;
      String availableString = String.format("{ name: \"Unmined\", millions: %.2f, percent: %d}", millionUnmined, running);
      
      Map<String, Object> ctx = TemplateRenderer.getBaseContext(request);
      ctx.put("dataset", String.format("[%s,%s,%s]", circulatingString, lockedString, availableString));

      chartRenderer.renderTemplate(response, ctx);
      
    }
    catch (Exception e)
    {
      e.printStackTrace();
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Thought Daemon not responding");
    }
  }

  protected void doTotal(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    try
    {
      BlockChainInfo bci    = client.getBlockChainInfo();

      double         supply = ((bci.blocks() - 1) * 314) + Cogitate.instance().getCoinPremine()
          - Cogitate.instance().getCoinBurned();
      String         output = String.format("%.8f", supply);
      
      response.setContentType("text/plain");
      response.setCharacterEncoding("UTF-8");
      response.setStatus(HttpServletResponse.SC_OK);

      // create HTML response
      PrintWriter responder = response.getWriter();
      responder.append(output);    
    }
    catch (Exception e)
    {
      e.printStackTrace();
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Thought Daemon not responding");
    }
  }

  protected void doCirculating(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
  {
    try
    {
      BlockChainInfo bci    = client.getBlockChainInfo();

      double         supply = ((bci.blocks() - 1) * 314) + Cogitate.instance().getCoinPremine()
          - Cogitate.instance().getCoinBurned();
      double         circulating = supply - Cogitate.instance().getCoinLocked();
      String         output = String.format("%.8f", circulating);
      
      response.setContentType("text/plain");
      response.setCharacterEncoding("UTF-8");
      response.setStatus(HttpServletResponse.SC_OK);

      // create HTML response
      PrintWriter responder = response.getWriter();
      responder.append(output);    
    }
    catch (Exception e)
    {
      e.printStackTrace();
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Thought Daemon not responding");
    }
  }
}
