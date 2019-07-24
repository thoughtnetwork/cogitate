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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class ResourceServlet extends HttpServlet
{
  private static Map<String, String> mimetypes;
  private static final String RESOURCE_PATH = "static";

  private final long instantiationMilliseconds = new Date().getTime();

  // 86400 seconds = 1 day
  // This strikes a nice balance between reducing the load on the server and
  // allowing style updates every so often.
  public static final String CACHE_CONTROL = "max-age=86400";

  static
  {
    mimetypes = new HashMap<>();
    mimetypes.put("png", "image/png");
    mimetypes.put("jpeg", "image/jpeg");
    mimetypes.put("jpg", "image/jpeg");
    mimetypes.put("css", "text/css");
    mimetypes.put("js", "application/javascript");
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    try
    {
      String pathinfo = request.getPathInfo();
      String[] name_parts = pathinfo.split("\\.");
      String resource = RESOURCE_PATH + pathinfo;
      response.setContentType(mimetypes.getOrDefault(name_parts[name_parts.length - 1], "application/octet-stream"));
      response.setHeader("Cache-Control", CACHE_CONTROL);
      response.setStatus(HttpServletResponse.SC_OK);
      InputStream instream = Cogitate.class.getClassLoader().getResourceAsStream(resource);
      OutputStream os = response.getOutputStream();
      int b = instream.read();
      while (b != -1)
      {
        os.write(b);
        b = instream.read();
      }
      instream.close();
    }
    catch (Exception e)
    {
      TemplateRenderer.error(request, response, "An unknown error occurred", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      e.printStackTrace(System.err);
    }
  }

  // We can safely assume that resources will not be modified for the lifetime of the servlet.
  @Override
  protected long getLastModified(HttpServletRequest request) {
    return instantiationMilliseconds;
  }
}