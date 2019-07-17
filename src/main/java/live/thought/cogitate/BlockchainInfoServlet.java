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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import live.thought.thought4j.ThoughtRPCClient;
import live.thought.thought4j.ThoughtClientInterface.BlockChainInfo;

@SuppressWarnings("serial")
public class BlockchainInfoServlet extends HttpServlet
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
      response.getWriter().println(CogitateStyler.HEADER.replace("@@BASE_URL@@", Cogitate.baseUrl(request)));
      response.getWriter().println("<section>");
      response.getWriter().println(CogitateStyler.NAV.replace("@@BASE_URL@@", Cogitate.baseUrl(request)));
      response.getWriter().println("<article>");
      response.getWriter().println("<h1>Thought Blockchain Info</h1>");
      response.getWriter().println("<table>");
      response.getWriter().println("<tr><td>Network</td><td>" + bci.chain() + "</td></tr>");
      response.getWriter().println("<tr><td>Number of Blocks</td><td>" + bci.blocks() + "</td></tr>");
      // Link to the top block
      response.getWriter().print("<tr><td>Best Block Hash</td><td>");
      response.getWriter().print("<a href='");
      response.getWriter().print(Cogitate.baseUrl(request));
      response.getWriter()
          .println("/getblock?hash=" + bci.bestBlockHash() + "'>" + bci.bestBlockHash() + "</a></td></tr>");
      response.getWriter().println("<tr><td>Difficulty</td><td>" + bci.difficulty() + "</td></tr>");
      response.getWriter().println("<tr><td>Chainwork</td><td>" + bci.chainWork() + "</td></tr>");
      response.getWriter().println(
          "<tr><td>Verification Progress</td><td>" + Math.round(bci.verificationProgress() * 100) + "%</td></tr>");
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