<!DOCTYPE html>
<html>
<head>
    <title>List of current sessions</title>

    <link rel="icon" type="image/png" href="${base}app.png">

    <link rel="stylesheet" href="${base}jquery/jquery-ui.min.css">
    <link rel="stylesheet" href="${base}jquery/jquery-ui.structure.min.css">
    <link rel="stylesheet" href="${base}jquery/jquery-ui.theme.min.css">
    <script src="${base}jquery/jquery.js"></script>
    <script src="${base}jquery/jquery-ui.min.js"></script>

    <link href="${base}widget.css" rel="stylesheet" type="text/css"/>

    <script>
        $(function() {
            $("button.goto").button().click(function () {
                var url = $(this).attr("formaction");
                $.get(url, function() {
                    // We could do this directly without $.get(), but this way we can capture the error.
                    window.location.href =  url;
                })
                .fail(function() {
                    console.log("The session has probably expired.");
                });
            });
            $("button.release").button().click(function () {
                var liEl = $(this).parent();
                var url = $(this).attr("formaction");
                $.ajax({
                    type: 'DELETE',
                    url: url,
                    success: function () {
                        liEl.remove();
                    }
                });
            });
        });
    </script>
</head>
<body>
    <h1>Sessions in use</h1>
    <ul>
        <#list sessions as session>
            <li>
                <a href="${api}sessions/${session}">Session ${session}</a>
                <button formaction="p/${session}" class="goto">Go to widget</button>
                <button formaction="${api}sessions/${session}" class="release">Release</button>
            </li>
        </#list>
    </ul>
</body>
</html>
