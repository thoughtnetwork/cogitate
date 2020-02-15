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

@SuppressWarnings("serial")
public class BlockServlet extends HttpServlet
{
  /** Thoughtd Client **/
  private ThoughtRPCClient client;
  private static final TemplateRenderer renderer = new TemplateRenderer("block.html");

  public BlockServlet()
  {
    URL thoughtUrl = Cogitate.instance().getThoughtURL();
    client = new ThoughtRPCClient(thoughtUrl);
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    String hash = request.getParameter("hash");
    String height = request.getParameter("height");
    if (null == hash && null == height)
    {
      TemplateRenderer.error(request, response, "No block specified", HttpServletResponse.SC_BAD_REQUEST);
    }
    else
    {
      Map<String, Object> ctx = TemplateRenderer.getBaseContext(request);
      try
      {
        if (null != height)
        {
          hash = client.getBlockHash(Integer.parseInt(height, 10));
        }

        if (hash.equals(request.getHeader("If-None-Match")))
        {
          response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
          return;
        }

        ctx.put("block", client.getBlock(hash));
        response.setHeader("ETag", hash);
        renderer.renderTemplate(response, ctx);
      }
      catch (ThoughtRPCException e)
      {
        TemplateRenderer.error(request, response, "No such block", HttpServletResponse.SC_NOT_FOUND);
      }
      catch (NumberFormatException e)
      {
        TemplateRenderer.error(request, response, "Invalid block height", HttpServletResponse.SC_BAD_REQUEST);
      }
    }
  }
}
