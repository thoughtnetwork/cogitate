package live.thought.cogitate;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import live.thought.thought4j.ThoughtClientInterface.BlockChainInfo;
import live.thought.thought4j.ThoughtClientInterface.BlockTemplate;
import live.thought.thought4j.ThoughtClientInterface.Masternode;
import live.thought.thought4j.ThoughtClientInterface.MasternodeInfo;
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
      else if (null != query && "apy".equalsIgnoreCase(query))
      {
        doApy(request,response);
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
      Map<String, MasternodeInfo> masternodes = client.masternodeList();
      
      double locked = Cogitate.instance().getCoinLocked() + (314000 * masternodes.size());

      double         supply      = ((bci.blocks() - 1) * 314) + Cogitate.instance().getCoinPremine()
          - Cogitate.instance().getCoinBurned();
      double         circulating = supply - locked;
      double total = 1618033988;
      int running = 100;
      
      
      // Locked 
      int lockedPercent = (int) Math.ceil(locked/total * 100.0);
      double millionLocked = locked / 1000000;
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
      Map<String, MasternodeInfo> masternodes = client.masternodeList();
      
      double locked = Cogitate.instance().getCoinLocked() + (314000 * masternodes.size());


      double         supply      = ((bci.blocks() - 1) * 314) + Cogitate.instance().getCoinPremine()
          - Cogitate.instance().getCoinBurned();
      double         circulating = supply - locked;
      double total = 1618033988;
      int running = 100;
      
      
      // Locked 
      int lockedPercent = (int) Math.ceil(locked/total * 100.0);
      double millionLocked = locked / 1000000;
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
      Map<String, MasternodeInfo> masternodes = client.masternodeList();
      
      double locked = Cogitate.instance().getCoinLocked() + (314000 * masternodes.size());

      double         supply = ((bci.blocks() - 1) * 314) + Cogitate.instance().getCoinPremine()
          - Cogitate.instance().getCoinBurned();
      double         circulating = supply - locked;
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
  
  protected void doApy(HttpServletRequest request, HttpServletResponse response) throws IOException
  {
    try
    {
      int mnstake = 314000;
      double blockTime = 1.67;  // minutes
      double blocksPerDay = 60 * 24 / blockTime;
      
      BlockTemplate bt = client.getBlockTemplate();
      List<Masternode> mnlist = bt.masternode();
      
      double mnReward = 0.0;
      for (Masternode m : mnlist)
      {
        mnReward += m.amount();
      }
      mnReward /= 100000000; // It's in notions
      
      Map<String,MasternodeInfo> mns = client.masternodeList();
      int mncount = mns.size();
      
      // Daily reward per masternode
      double mndaily = blocksPerDay * mnReward / mncount;
      
      double apy = ((mndaily * 365) / mnstake) * 100;
      
      
      response.setContentType("text/plain");
      response.setCharacterEncoding("UTF-8");
      response.setStatus(HttpServletResponse.SC_OK);
      
      String         output = String.format("%.4f%%", apy);

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
