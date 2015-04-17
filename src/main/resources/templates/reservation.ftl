<!DOCTYPE html>
<html>
<head>
    <title>Reserve instance</title>

    <link rel="icon" type="image/png" href="${base}app.png">

    <link rel="stylesheet" href="${base}jquery/jquery-ui.min.css">
    <link rel="stylesheet" href="${base}jquery/jquery-ui.structure.min.css">
    <link rel="stylesheet" href="${base}jquery/jquery-ui.theme.min.css">
    <script src="${base}jquery/jquery.js"></script>
    <script src="${base}jquery/jquery-ui.min.js"></script>

    <link href="${base}widget.css" rel="stylesheet" type="text/css"/>
    <script>
        function createSession(event) {
            $.post("${api}sessions", function(data, status, xhr) {
                var newResource = xhr.getResponseHeader('Location');
                $.get(newResource, function(data) {
                    window.location.href =  "p/" + data;
                });
            }).fail(function(data) {
                $( "#dialog-message" ).dialog({
                    modal: true,
                    width: 500,
                    buttons: {
                        Ok: function() {
                            $( this ).dialog( "close" );
                        }
                    }
                });
            });
        }

        $(function() {
            $("button").button().click(createSession);
        });
    </script>
</head>
<body>
<div class="widget-header">
    <h2>
        <img src="${base}app.png" alt="PacketTracer icon" >
        ${title}
    </h2>
</div>
<div class="view">
    <div class="message">
        <p>Start using the widget by reserving a PacketTracer instance!</p>
        <button style="margin-top: 4%;">Reserve</button>
    </div>
</div>
<div class="footer">
    <div class="logos">
        <a href="https://www.netacad.com"><img src="${base}Cisco_academy_logo.png" alt="Cisco logo" class="cisco-logo"></a>
        <a href="http://www.open.ac.uk"><img src="${base}ou_logo.png" alt="Open University logo" class="ou-logo"></a>
        <a href="http://kmi.open.ac.uk"><img src="${base}kmi_logo.png" alt="Knowledge Media Institute logo" class="kmi-logo"></a>
    </div>
</div>
<div id="dialog-message" title="Unavailable PT instances" style="display: none;">
  <p>
    Sorry, there are <b>no PacketTracer instances available</b> right now to initiate a session.
  </p>
  <p>
    Please, wait a little bit and <b>try it again</b>.
  </p>
</div>
</body>
</html>
