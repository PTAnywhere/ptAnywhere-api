<!DOCTYPE html>
<html>
    <head lang="en">
        <meta charset="UTF-8">
        <title>Console</title>

        <base href="${base}" target="_blank">

        <style>
            * { margin: 0; padding: 0; box-sizing: border-box; }
            body { font: 10pt monospace, Helvetica, Arial; }
            #messages { margin: 0; padding: 0; white-space: pre; }
            #lastLine { float: left; margin-right: 4px; }
            #current { display: block; height: 10pt; }
        </style>

        <script src="jquery/jquery.js"></script>
        <script>
             var ws = null;

            function setConnected(connected) {
                $('#current').prop( "disabled", !connected);
            }

            function scrollToBottom() {
                document.location.replace(window.location.pathname + "#bottom");
                // document.location.replace("#bottom"); // Only works if base property is unset.
                // Another alternative registering "redirection" in the browser history.
                // window.location.href = "#bottom"
            }

            function connect(target) {
                console.log('Connecting to websocket endpoint... (' + target + ')');

                if ('WebSocket' in window) {
                    ws = new WebSocket(target);
                } else if ('MozWebSocket' in window) {
                    ws = new MozWebSocket(target);
                } else {
                    alert('WebSocket is not supported by this browser.');
                    return;
                }
                ws.onopen = function () {
                    setConnected(true);
                    console.log('Info: WebSocket connection opened.');
                };
                ws.onmessage = function (event) {
                    var lines = event.data.split("\n");
                    if (lines.length>1) {
                        for (var i=0; i<lines.length-1; i++) { // Unnecessary
                            if (i==0) {
                                var lastLine = $("#lastLine").text();
                                if (lastLine.trim()!=="--More--")
                                    $("#messages").append(lastLine);
                                $("#lastLine").text('');
                            }
                            $("#messages").append(lines[i] + "<br />");
                        }
                    }
                    $("#lastLine").append(lines[lines.length-1]);
                    scrollToBottom();
                    $("#current").focus();
                };
                ws.onerror = function (event) {
                    setConnected(false);
                    console.log('Info: WebSocket error, Code: ' + event.code + (event.reason == "" ? "" : ", Reason: " + event.reason));
                };
                ws.onclose = function (event) {
                    setConnected(false);
                    console.log('Info: WebSocket connection closed, Code: ' + event.code + (event.reason == "" ? "" : ", Reason: " + event.reason));
                };
            }

            function configureEvents() {
                $("#current").keypress(function(e) {
                    if (e.which == 13) {
                        ws.send($("#current").text()); /* It has not '\n' */
                        $("#current").text('');
                    }
                });

                $("#current").keyup(function() {
                    /* In PT, when '?' is pressed, the command is send as it is. */
                    var written = $("#current").text();
                    var lastChar = written.slice(-1).charCodeAt(0);
                    if (lastChar == 63) {
                        ws.send(written); /* It has '?' */
                        $("#current").text('');
                    }
                });

                $("#lastLine").click(function() {
                    $("#current").focus();
                });
            }

            $(function() {
                connect('${websocketURL}');
                configureEvents();
            });
        </script>
    </head>
    <body>
        <div id="messages"></div>
        <div style="width: 100%">
            <span id="lastLine"></span>
            <span id="current" contentEditable="true">ping 10.0.0.2</span>
        </div>
        <a name="bottom"></a>
    </body>
</html>