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

import live.thought.thought4j.ThoughtRPCClient;
import live.thought.thought4j.ThoughtRPCException;
import live.thought.thought4j.ThoughtClientInterface.Block;
import live.thought.thought4j.ThoughtClientInterface.RawTransaction;

@SuppressWarnings("serial")
public class TransactionServlet extends HttpServlet
{
  /** Thoughtd Client **/
  private ThoughtRPCClient client;
  private static final TemplateRenderer renderer = new TemplateRenderer("transaction.html");

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
      TemplateRenderer.error(request, response, "No transaction hash specified", HttpServletResponse.SC_BAD_REQUEST);
    }
    else
    {
      Map<String, Object> ctx = TemplateRenderer.getBaseContext(request);
      try {
        ctx.put("tx", client.getRawTransaction(hash));
        response.setHeader("Cache-Control", ResourceServlet.CACHE_CONTROL);
        renderer.renderTemplate(response, ctx);
      } catch (ThoughtRPCException e) {
        TemplateRenderer.error(request, response, "No such transaction " + hash, HttpServletResponse.SC_NOT_FOUND);
      }
    }
  }

  // Transactions do not change once they are on the chain, so they can be cached.
  @Override
  protected long getLastModified(HttpServletRequest request) {
    String hash = request.getParameter("hash");
    if (null == hash)
    {
      return -1;
    }
    else
    {
      try {
        RawTransaction tx = client.getRawTransaction(hash);
        // getLastModified is expected to return milliseconds
        return tx.time().getTime();
      } catch (ThoughtRPCException e) {
        return -1;
      }
    }
  }
}