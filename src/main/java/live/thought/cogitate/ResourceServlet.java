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
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class ResourceServlet extends HttpServlet
{
  private class ResourceInfo
  {
    public final String etag;
    public final long length;

    public ResourceInfo(String etag, long length)
    {
      this.etag = etag;
      this.length = length;
    }
  }

  private static Map<String, String> mimetypes;
  private static Map<String, ResourceInfo> resourceInfo;
  private static final String RESOURCE_PATH = "static";
  private static final ClassLoader loader = Cogitate.class.getClassLoader();

  static
  {
    mimetypes = new HashMap<>();
    mimetypes.put("png", "image/png");
    mimetypes.put("jpeg", "image/jpeg");
    mimetypes.put("jpg", "image/jpeg");
    mimetypes.put("css", "text/css");
    mimetypes.put("js", "application/javascript");

    resourceInfo = new HashMap<>();
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    try
    {
      String pathinfo = request.getPathInfo();
      String[] name_parts = pathinfo.split("\\.");
      String resource = RESOURCE_PATH + pathinfo;

      InputStream stream = loader.getResourceAsStream(resource);

      if (stream == null)
      {
        TemplateRenderer.error(request, response, "Resource not found", HttpServletResponse.SC_NOT_FOUND);
        return;
      }

      ResourceInfo info = resourceInfo.getOrDefault(resource, null);

      if (info == null)
      {
        MessageDigest digest = MessageDigest.getInstance("md5");
        long length = Util.BlockCopy(stream, (block, size) -> digest.update(block, 0, size));
        info = new ResourceInfo(Util.HexDigest(digest), length);
      }
      response.addHeader("ETag", info.etag);

      if (info.etag.equals(request.getHeader("If-None-Match")))
      {
        response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        return;
      }

      response.setContentLengthLong(info.length);
      response.setStatus(HttpServletResponse.SC_OK);
      response.setContentType(mimetypes.getOrDefault(name_parts[name_parts.length - 1], "application/octet-stream"));
      Util.CopyStream(loader.getResourceAsStream(resource), response.getOutputStream());
    }
    catch (Exception e)
    {
      TemplateRenderer.error(request, response, "An unknown error occurred",
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      e.printStackTrace(System.err);
    }
  }
}
