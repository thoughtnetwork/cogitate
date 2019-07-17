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

import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import live.thought.thought4j.ThoughtRPCClient;
import live.thought.thought4j.ThoughtClientInterface.RawTransaction;
import live.thought.thought4j.ThoughtClientInterface.RawTransaction.In;
import live.thought.thought4j.ThoughtClientInterface.RawTransaction.Out;
import live.thought.thought4j.ThoughtClientInterface.RawTransaction.Out.ScriptPubKey;

@SuppressWarnings("serial")
public class TransactionServlet extends HttpServlet
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
      response.getWriter().println(CogitateStyler.HEADER.replace("@@BASE_URL@@", Cogitate.baseUrl(request)));
      response.getWriter().println("<section>");
      response.getWriter().println(CogitateStyler.NAV.replace("@@BASE_URL@@", Cogitate.baseUrl(request)));
      response.getWriter().println("<article>");
      response.getWriter().println("<h1>Transaction " + rt.txId() + "</h1>");

      response.getWriter().println("<table>");
      response.getWriter().println("<tr><td>Version</td><td>" + rt.version() + "</td></tr>");
      response.getWriter().println("<tr><td>Time</td><td>" + rt.time() + "</td></tr>");
      response.getWriter().println("<tr><td>Block Time</td><td>" + rt.blocktime() + "</td></tr>");
      response.getWriter().print("<tr><td>Block Hash</td><td>");
      response.getWriter().print("<a href='");
      response.getWriter().print(Cogitate.baseUrl(request));
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