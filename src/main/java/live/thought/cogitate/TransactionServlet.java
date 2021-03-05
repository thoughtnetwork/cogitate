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
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import live.thought.thought4j.ThoughtClientInterface.RawTransaction;
import live.thought.thought4j.ThoughtClientInterface.RawTransaction.In;
import live.thought.thought4j.ThoughtRPCClient;
import live.thought.thought4j.ThoughtRPCException;

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
      if (hash.equals(request.getHeader("If-None-Match")))
      {
        response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        return;
      }

      Map<String, Object> ctx = TemplateRenderer.getBaseContext(request);
      try
      {
        RawTransaction rtx = client.getRawTransaction(hash);
        ctx.put("tx", rtx);
        List<In> inputs = rtx.vIn();
        String cbd = "";
        for (In in : inputs)
        {
          if (in.isCoinbase())
          {
            String coinbase = in.coinbase();
            if (coinbase.length() > 8)
            {
              cbd = new String(Util.hexStringToByteArray(coinbase.substring(8)));
            }
            break;
          }
        }
        ctx.put("cbd", cbd);
        response.setHeader("ETag", hash);
        renderer.renderTemplate(response, ctx);
      }
      catch (ThoughtRPCException e)
      {
        TemplateRenderer.error(request, response, "No such transaction " + hash, HttpServletResponse.SC_NOT_FOUND);
      }
    }
  }
}
