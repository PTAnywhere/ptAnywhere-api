<!DOCTYPE html>
<html>
<head>
    <title>${title}</title>

    <link rel="icon" type="image/png" href="${base}app.png">

    <link rel="stylesheet" href="${base}jquery/jquery-ui.min.css">
    <link rel="stylesheet" href="${base}jquery/jquery-ui.structure.min.css">
    <link rel="stylesheet" href="${base}jquery/jquery-ui.theme.min.css">
    <script src="${base}jquery/jquery.js"></script>
    <script src="${base}jquery/jquery-ui.min.js"></script>
    <script src="${base}jquery/jquery.ui.touch-punch.min.js"></script>

    <link href="${base}vis/vis.css" rel="stylesheet" type="text/css"/>
    <script type="text/javascript" src="${base}vis/vis.js"></script>

    <link href="${base}widget.css" rel="stylesheet" type="text/css"/>
    <script type="text/javascript" src="${base}widget.js"></script>
    <script>
        api_url = "${session_api}";
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
    <div id="network">
        <img id="loading" src="${base}loading.gif" alt="Loading network topology..." />
        <div style="text-align: center;">
            <p>Loading topology...<p>
            <p id="loadingMessage"></p>
        </div>
    </div>
    <fieldset id="creation-fieldset">
        <legend>To create a new device, drag it to the network map</legend>
        <div id="creation-menu">
            <figure>
                <img id="cloud" alt="cloud" src="${base}cloud.png" style="width: 120px;">
                <figcaption>Cloud</figcaption>
            </figure>
            <figure>
                <img id="router" alt="router" src="${base}router.png" style="width: 80px;">
                <figcaption>Router</figcaption>
            </figure>
            <figure>
                <img id="switch" alt="switch" src="${base}switch.png" style="width: 90px;">
                <figcaption>Switch</figcaption>
            </figure>
            <figure>
                <img id="pc" alt="PC" src="${base}PC.png" style="width: 90px;">
                <figcaption>PC</figcaption>
            </figure>
        </div>
    </fieldset>
</div>
<div class="footer">
    <div class="logos">
        <a href="https://www.netacad.com"><img src="${base}Cisco_academy_logo.png" alt="Cisco logo" class="cisco-logo"></a>
        <a href="http://www.open.ac.uk"><img src="${base}ou_logo.png" alt="Open University logo" class="ou-logo"></a>
        <a href="http://kmi.open.ac.uk"><img src="${base}kmi_logo.png" alt="Knowledge Media Institute logo" class="kmi-logo"></a>
    </div>
</div>
<div id="create-device" title="Create new device">
    <form name="create-device">
        <fieldset style="margin-top: 15px;">
            <div>
                <label for="create-name">Name: </label>
                <input type="text" name="name" id="create-name" style="float: right;">
            </div>
            <div style="margin-top: 20px;">
                <label for="device-type">Device type: </label>
                <span style="float: right;">
                    <select name="type" id="device-type">
                        <option value="cloud" data-class="cloud">Cloud</option>
                        <option value="router" data-class="router">Router</option>
                        <option value="switch" data-class="switch">Switch</option>
                        <option value="pc" data-class="pc">PC</option>
                    </select>
                </span>
            </div>
        </fieldset>
    </form>
</div>
<div id="modify-device">
    <div id="modify-dialog-tabs">
        <form name="modify-device">
            <input type="hidden" name="deviceId" value="">
            <ul>
                <li><a href="#tabs-1">Global Settings</a></li>
                <li><a href="#tabs-2">Interfaces</a></li>
            </ul>
            <div id="tabs-1">
                Name: <input type="text" name="displayName"><br />
                <span id="defaultGw">Default gateway: <input type="text" name="defaultGateway"></span>
            </div>
            <div id="tabs-2">
                <div class="loading">
                    Loading info...
                </div>
                <div id="loadedPanel" class="loaded">
                    Name: <select id="interface" name="interface" size="1">
                        <option value="loading">Loading...</option>
                    </select>
                    <hr>
                    <div id="ifaceDetails">
                        IP address: <input type="text" name="ipAddress"><br>
                        Subnet mask: <input type="text" name="subnetMask">
                    </div>
                    <div id="noIfaceDetails">
                        No settings can be specified for this type of interface.
                    </div>
                </div>
            </div>
        </form>
    </div>
</div>
<div id="link-devices" title="Link two devices">
    <form name="link-devices">
        <input type="hidden" name="fromDeviceId" value="">
        <input type="hidden" name="toDeviceId" value="">
        <div class="loading">
                Loading info...
        </div>
        <div class="loaded">
            <p>Please select which ports to connect...</p>
            <p><span id="fromDeviceName">Device 1</span>:
                <select id="linkFromInterface" name="linkFromInterface" size="1">
                    <option value="loading">Loading...</option>
                </select>
            </p>
            <p><span id="toDeviceName">Device 2</span>:
                <select id="linkToInterface" name="linkToInterface" size="1">
                    <option value="loading">Loading...</option>
                </select>
            </p>
        </div>
        <div class="error">
            <p>Sorry, something went wrong during the link creation.</p>
            <p class="error-msg"></p>
        </div>
    </form>
</div>
<div id="command-line" title="Command line">
    <div id="replace"></div>
</div>
<div id="notFound" style="display: none;">
    <div class="message">
        <h1>Topology not found.</h1>
        <p>The topology could not be loaded probably because the session does not exist (e.g., if it has expired).</p>
        <p><a href="../index.html">Click here</a> to initiate a new one.</p>
    </div>
</div>
</body>
</html>
