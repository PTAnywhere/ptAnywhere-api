# Simple HTTP server to test JS side of the widget.
# Take into account that the webapi should run in a CORS enabled server as the pages from this server will be in a different port.
# In tomcat7: http://tomcat.apache.org/tomcat-7.0-doc/config/filter.html#CORS_Filter

import SimpleHTTPServer
import SocketServer

PORT = 8000  # If you change this, take it into account for widget.js too.


Handler = SimpleHTTPServer.SimpleHTTPRequestHandler
httpd = SocketServer.TCPServer(("", PORT), Handler)

print "serving at port", PORT
httpd.serve_forever()


