<!DOCTYPE html>
<html class="plain">
<head>
    <title>List of current sessions</title>

    <link rel="icon" type="image/png" href="${base}icon.png">
    <script src="//code.jquery.com/jquery-1.11.3.min.js"></script>

    <script>
        $(function() {
            $("button.goto").click(function () {
                var url = $(this).attr("formaction");
                $.get(url, function() {
                    // We could do this directly without $.get(), but this way we can capture the error.
                    window.location.href =  url;
                })
                .fail(function() {
                    console.log("The session has probably expired.");
                });
            });
            $("button.release").click(function () {
                //var liEl = $(this).parent();
                var url = $(this).attr("formaction");
                $.ajax({
                    type: 'DELETE',
                    url: url,
                    success: function () {
                        //liEl.remove();
                        window.location.reload(true);
                    }
                });
            });
        });
    </script>

    <style>
        td {
            padding: 0 20px;
        }
    </style>
</head>
<body>
    <h1>Sessions in use</h1>
    <#if (sessions?size > 0) >
        <ul>
            <#list sessions as session>
                <li>
                    <a href="${api}sessions/${session}">Session ${session}</a>
                    <button formaction="${api}sessions/${session}" class="release">Release</button>
                </li>
            </#list>
        </ul>
    <#else>
        <p>No sessions have been initiated.</p>
    </#if>

    <#if (instances?size > 0) >
        <h3>PT instances used by the application</h3>
        <table>
            <tr>
                <th>Host</th><th>Port</th>
            </tr>
            <#list instances as instance>
            <tr>
                <td>${instance.host}</td><td>${instance.port}</td>
            </tr>
            </#list>
        </table>
    </#if>
</body>
</html>
