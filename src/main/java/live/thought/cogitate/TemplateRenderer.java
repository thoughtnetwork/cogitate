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
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.owasp.encoder.Encode;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.loader.ClasspathLoader;
import com.mitchellbosecke.pebble.template.PebbleTemplate;

public class TemplateRenderer
{
  private static final PebbleEngine engine;
  private static final PebbleTemplate errorTemplate;
  private final PebbleTemplate template;
  
  static
  {
    ClasspathLoader loader = new ClasspathLoader(Cogitate.class.getClassLoader());
    loader.setPrefix("templates/");
    engine = new PebbleEngine.Builder().loader(loader).strictVariables(true).build();
    errorTemplate = engine.getTemplate("error.html");
  }

  public TemplateRenderer(String templateName)
  {
    template = engine.getTemplate(templateName);
  }
  
  private static String baseUrl(HttpServletRequest request)
  {
    return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
  }
  
  public static Map<String, Object> getBaseContext(HttpServletRequest request)
  {
    Map<String, Object> ctx = new HashMap<>();
    ctx.put("baseUrl", baseUrl(request));
    ctx.put("copyrightYear", new GregorianCalendar().get(GregorianCalendar.YEAR));
    return ctx;
  }

  public void renderTemplate(HttpServletResponse response, Map<String, Object> ctx)
      throws ServletException, IOException
  {
    response.setContentType("text/html");
    response.setStatus(HttpServletResponse.SC_OK);
    template.evaluate(response.getWriter(), ctx);
  }

  public static void error(HttpServletRequest req, HttpServletResponse resp, String msg, int code) throws ServletException, IOException
  {
    // Error pages should never be cached.
    resp.setHeader("Cache-Control", "max-age=0");
    resp.setContentType("text/html");
    resp.setStatus(code);
    Map<String, Object> ctx = getBaseContext(req);
    ctx.put("message", msg);
    errorTemplate.evaluate(resp.getWriter(), ctx);
  }
}
