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

public abstract class CogitateStyler
{

  public static final String LEADER = 
      "<!DOCTYPE html>" +
      "<html lang='en'>" +
      "  <head>" +
      "    <title>Cogitate - Thought Network Blockchain Browser</title>" +
      "    <meta charset='utf-8'>" +
      "    <meta name='viewport' content='width=device-width, initial-scale=1'>" +
      "    <style>" +
      "* {" +
      "  box-sizing: border-box;" +
      "}" +
      "body {" +
      "  font-family: Arial, Helvetica, sans-serif;" +
      "  background-color: #0A0945;" +
      "}" +
      "header {" +
      "  background-color: #0A0945;" +
      "  padding: 30px;" +
      "  text-align: center;" +
      "  font-size: 35px;" +
      "  color: white;" +
      "}" +
      "section {" +
      "  display: -webkit-flex;" +
      "  display: flex;" +
      "}" +
      "nav {" +
      "  -webkit-flex: 1;" +
      "  -ms-flex: 1;" +
      "  flex: 1;" +
      "  background: #ccc;" +
      "  padding: 20px;" +
      "}" +
      "nav ul {" +
      "  list-style-type: none;" +
      "  padding: 0;" +
      "}" +
      "nav li a {" + 
      "  display: block;" + 
      "}" +
      "article {" +
      "  -webkit-flex: 3;" +
      "  -ms-flex: 3;" +
      "  flex: 3;" +
      "  background-color: #f1f1f1;" +
      "  padding: 10px;" +
      "}" +
      "footer {" +
      "  background-color: #0A0945;" +
      "  padding: 10px;" +
      "  text-align: center;" +
      "  color: white;" +
      "}" +
      "@media (max-width: 600px) {" +
      "  section {" +
      "    -webkit-flex-direction: column;" +
      "    flex-direction: column;" +
      "  }" +
      "}" +
      "    </style>" +
      "  </head>" +
      "  <body>";
  
  public static String HEADER =
      "<header>\r\n" + 
      "<img src='@@BASE_URL@@/resources/logo-green-white.png'>  <h2>Cogitate</h2> <h3>Thought Network Blockchain Browser</h3>\r\n" + 
      "</header>";
  
  public static String NAV = 
      "<nav>\r\n" + 
      "    <ul>\r\n" + 
      "      <li><a href='@@BASE_URL@@'>Home</a></li>\r\n" + 
      "    </ul>\r\n" + 
      "  </nav>";
  
  public static String FOOTER = 
    "<footer>\r\n" + 
    "  <p>Copyright 2017-2018 Thought Networks LLC</p>\r\n" + 
    "</footer>";
  
  public static final String TRAILER = "</body>\r\n</html>";
      
}
