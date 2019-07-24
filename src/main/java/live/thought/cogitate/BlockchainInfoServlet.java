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
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import live.thought.thought4j.ThoughtClientInterface.BlockChainInfo;
import live.thought.thought4j.ThoughtRPCClient;

@SuppressWarnings("serial")
public class BlockchainInfoServlet extends HttpServlet
{
  /** Thoughtd Client **/
  private ThoughtRPCClient client;
  /** Template renderer **/
  private static final TemplateRenderer renderer = new TemplateRenderer("blockchaininfo.html");

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
      Map<String, Object> ctx = TemplateRenderer.getBaseContext(request);
      ctx.put("bci", bci);
      ctx.put("verificationProgress", Math.round(bci.verificationProgress() * 100));
      renderer.renderTemplate(response, ctx);
    }
    catch (Exception e)
    {
      TemplateRenderer.error(request, response, "Thought Daemon not responding", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }
}