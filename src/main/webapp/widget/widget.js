var api_url = "../webapi";
// var api_url = "http://localhost:8080/ptsmith-rest/ptsmith";
// "http://carre.kmi.open.ac.uk/forge/ptsmith"

var nodes, edges, network;
var tappedDevice;


$.postJSON = function(url, data, callback) {
    return jQuery.ajax({
    headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
    },
    'type': 'POST',
    'url': url,
    'data': JSON.stringify(data),
    'dataType': 'json',
    'success': callback
    });
};


function chooseinterface() {
    var current = nodes.get(tappedDevice);
    var currentinterfaceselection = document.getElementById("interface").value;
    console.log("currentinterface " + currentinterfaceselection);
    console.log(toJSON(current));
    for (i = 0; i < current.ports.length; i++) {
        portname = current.ports[i].portName;

        if (portname == currentinterfaceselection) {
            portipaddress = current.ports[i].portIpAddress;
            portsubnetmask = current.ports[i].portSubnetMask;
            console.log("portIP: " + portipaddress);
            console.log("portSubnet: " + portsubnetmask);
            if (portipaddress != null) {
                document.forms["ipconfig"]["ipaddress"].value = portipaddress;
            } else {
                console.log("portipaddress is null");
            }
            if (portsubnetmask != null) {
                document.forms["ipconfig"]["subnetmask"].value = portsubnetmask;
            } else {
                console.log("portsubnetmask is null");
            }
        }
    }
}

function chooselinkinterface() {
    var current = nodes.get(tappedDevice);
    var currentinterfaceselection = document.getElementById("linkinterface").value;
    console.log("currentlinkinterface " + currentinterfaceselection);
    console.log(toJSON(current));

}

function addDevice(callback) {
    newDevice = {
        "label": document.forms["create-device"]["name"].value,
        "group": document.forms["create-device"]["type"].value
    }
    console.log("Adding device " + newDevice.name + " of type " + newDevice.type);

    $.postJSON( api_url + "/devices", newDevice,
        function(data) { console.log("The device was created successfully."); })
        .done(function(data) { console.log("The device was created successfully."); })
        .fail(function(data) { console.error("Something went wrong in the device creation.") })
        .always(callback);
}

function onDeviceClick(deviceType) {
    $("form[name='create-device'] input[name='type']").val(deviceType);
    dialog = $( "#create-device-dialog" ).dialog({ autoOpen: false, height: 300, width: 350, modal: true, buttons: {
                    "SUBMIT": function() {
                        callback = function() { dialog.dialog( "close" ); };
                        addDevice(callback);
                    },
                    Cancel: function() {
                        $( this ).dialog( "close" );
                    }
                }, close: function() { console.log("Closing dialog..."); }
            });
    form = dialog.find( "form" ).on("submit", function( event ) { event.preventDefault(); });
    dialog.dialog( "open" );
}

function overlay(node) {
    console.log("overlay network " + network);

    document.forms["ipconfig"]["interface"].options.length = 0;

    current = nodes.get(node);

    console.log("overlay node " + current.id);
    console.log("overlay node " + current.label);
    for (i = 0; i < current.ports.length; i++) {
        portname = current.ports[i].portName;
        portipaddress = current.ports[i].portIpAddress;
        portsubnetmask = current.ports[i].portSubnetMask;
        defaultselected = false;
        if (i == 0) {
            defaultselected = true;
        }
        document.forms["ipconfig"]["interface"].options[i] = new Option(
                portname, portname, defaultselected, false);
        if (i == 0) {
            document.forms["ipconfig"]["ipaddress"].value = portipaddress;
            document.forms["ipconfig"]["subnetmask"].value = portsubnetmask;
        }
    }

    currentlinkcount = 0;
    document.forms["ipconfig"]["linkinterface"].options[0] = new Option("--", "--", true, false);

    console.log("Nodes " + JSON.stringify(nodes));
    console.log("Nodes _data length " + nodes._data.length);

    for (var key in nodes._data) {
        if (nodes.get(key).label != current.label) {
            console.log("Possible link node " + JSON.stringify(nodes.get(key)));
            console.log("Ports: " + JSON.stringify(nodes.get(key).ports));
            if ('undefined' !== typeof nodes.get(key).ports) {
                for (j = 0; j < nodes.get(key).ports.length; j++) {
                    var optionName = nodes.get(key).label + ":" + nodes.get(key).ports[j].portName;
                    document.forms["ipconfig"]["linkinterface"].options[currentlinkcount + 1] = new Option(
                            optionName, optionName, false, false);
                    currentlinkcount++;
                }
            }
        }
    }

    $("#overlay").toggle();
}

function configureIP() {
    var configureIPRequest = new XMLHttpRequest();
    configureIPRequest.onreadystatechange = function() {
        if (configureIPRequest.readyState == 4
                && configureIPRequest.status == 200) {
            var toDelete = document.forms["ipconfig"]["delete-device"].checked;
            var toDeleteLink = document.forms["ipconfig"]["delete-link"].checked;
            var linkId = document.forms["ipconfig"]["linkinterface"].value;

            if (toDelete || toDeleteLink || linkId != "--") {
                location.reload();
            }
            var current = nodes.get(tappedDevice);
            var currentinterfaceselection = document
                    .getElementById("interface").value;
            console.log("currentinterface " + currentinterfaceselection);
            console.log(toJSON(current));
            for (i = 0; i < current.ports.length; i++) {
                portname = current.ports[i].portName;

                if (portname == currentinterfaceselection) {
                    current.ports[i].portIpAddress = document.forms["ipconfig"]["ipaddress"].value;
                    ;
                    current.ports[i].portSubnetMask = document.forms["ipconfig"]["subnetmask"].value;
                    ;
                    console.log("portIP: " + portipaddress);
                    console.log("portSubnet: " + portsubnetmask);
                }
            }
            console.log("Configure IP Request worked");

        }
    }
    var toDelete = document.forms["ipconfig"]["delete-device"].checked;
    var toDeleteLink = document.forms["ipconfig"]["delete-link"].checked;
    var linkId = document.forms["ipconfig"]["linkinterface"].value;
    console.log("delete checked " + toDelete);
    if (linkId != "--") {
        var currentinterfaceselection = document.getElementById("linkinterface").value;
        var interfacename = document.getElementById("interface").value;
        var nodeid = tappedDevice;
        console.log("currentlinkinterface " + currentinterfaceselection);
        console.log(toJSON(current));
        var splitlinkdetails = currentinterfaceselection.split(":");
        var deviceName = splitlinkdetails[0];
        var otherInterfaceName = splitlinkdetails[1];
        console.log("NODE ID is " + nodes.get(nodeid).label);
        if (deviceName != null && otherInterfaceName != null) {
            var params = JSON.stringify({
                "linksource" : nodes.get(nodeid).label,
                "linksourceinterface" : interfacename,
                "linktarget" : deviceName,
                "linktargetinterface" : otherInterfaceName
            });
            configureIPRequest.open('POST', api_url, true); // `false` makes the request synchronous
            configureIPRequest.setRequestHeader("Content-type",
                "application/json; charset=utf-8");

            configureIPRequest.send(params);
            el = document.getElementById("overlay");
            el.style.visibility = (el.style.visibility == "visible") ? "hidden"
                : "visible";
        }
    } else if (toDeleteLink) {
        var params = JSON.stringify({
            "deletelinkdevice" : nodes.get(nodeid).label,
            "deletelinkinterface" : interfacename
        });
        configureIPRequest.open('POST', api_url, true); // `false` makes the request synchronous
        configureIPRequest.setRequestHeader("Content-type",
                "application/json; charset=utf-8");

        configureIPRequest.send(params);
        el = document.getElementById("overlay");
        el.style.visibility = (el.style.visibility == "visible") ? "hidden"
                : "visible";
    } else if (toDelete) {
        var params = JSON.stringify({
            "deletedevice" : tappedDevice
        })
        configureIPRequest.open('POST', api_url, true); // `false` makes the request synchronous
        configureIPRequest.setRequestHeader("Content-type",
                "application/json; charset=utf-8");

        configureIPRequest.send(params);
        el = document.getElementById("overlay");
        el.style.visibility = (el.style.visibility == "visible") ? "hidden" : "visible";
    } else {
        var ip = document.forms["ipconfig"]["ipaddress"].value;
        var subnet = document.forms["ipconfig"]["subnetmask"].value;
        var defaultgateway = document.forms["ipconfig"]["defaultgateway"].value;
        var interfacename = document.getElementById("interface").value;
        var nodeid = tappedDevice;

        var params = JSON.stringify({
            "ipaddress" : ip,
            "subnetmask" : subnet,
            "defaultgateway" : defaultgateway,
            "deviceid" : nodeid,
            "interfacename" : interfacename
        });

        console.log()

        configureIPRequest.open('POST', api_url, true); // `false` makes the request synchronous
        configureIPRequest.setRequestHeader("Content-type",
                "application/json; charset=utf-8");

        configureIPRequest.send(params);
        el = document.getElementById("overlay");
        el.style.visibility = (el.style.visibility == "visible") ? "hidden"
                : "visible";
    }
}

function loadTopology(data) {
    nodesJson = data.devices;
    edgesJson = data.edges;

    // create an array with nodes
    nodes = new vis.DataSet();
    nodes.subscribe('*', function() {
        $('#nodes').html(toJSON(nodes.get()));
    });
    if (nodesJson != null) {
        nodes.add(nodesJson);
    } else {
        nodes.add([
            {
                "id" : "{b8c1ad78-ae35-4bf3-bf21-1c7e8cfe208d}",
                "label" : "Access Point1"
            },
            {
                "id" : "{9ced95e5-b478-474e-8775-7006cd295963}",
                "label" : "Multilayer Switch0"
            },
            {
                "id" : "{3898c3e2-aec5-4b45-bfa7-ce282c20c50a}",
                "label" : "PC0"
            },
        ]);
    }

    // create an array with edges
    edges = new vis.DataSet();
    edges.subscribe('*', function() {
        $('#edges').html(toJSON(edges.get()));
    });

    if (edgesJson != null) {
        edges.add(edgesJson);
    } else {
        edges.add([
            {
                "id" : "{01e3c88b-1b76-4a28-bb56-9f355e4c278a}",
                "from" : "{b8c1ad78-ae35-4bf3-bf21-1c7e8cfe208d}",

                "to" : "{9ced95e5-b478-474e-8775-7006cd295963}"
            },
            {
                "id" : "{a89b4e59-9e06-4d11-91df-f36f9f7fc97f}",
                "from" : "{9ced95e5-b478-474e-8775-7006cd295963}",

                "to" : "{3898c3e2-aec5-4b45-bfa7-ce282c20c50a}"
            }, ]);
    }

    // create a network
    var container = $('#network').get(0);
    var data = { nodes : nodes, edges : edges };
    var options = {
        dragNetwork : 'false',
        dragNodes : 'false',
        zoomable : 'false',

        groups : {
            cloudDevice : {
                shape : 'image',
                image : "cloud.png"
            },

            routerDevice : {
                shape : 'image',
                image : "router.png"
            },
            switchDevice : {
                shape : 'image',
                image : "switch.png"
            },
            pcDevice : {
                shape : 'image',
                image : "PC.png"
            }
        }
    };
    network = new vis.Network(container, data, options);
    network.on('click', onTap);
}

// convenience method to stringify a JSON object
function toJSON(obj) {
    return JSON.stringify(obj, null, 4);
}

function onTap(properties) {
    overlay(properties.nodes[0])
    if (properties.nodes != null) {
        for (i = 0; i < properties.nodes.length; i++) {
            console.log(properties.nodes[i])
        }
        tappedDevice = properties.nodes[0];
        console.log("tappedDevice");
        console.log(tappedDevice);
    }
}

$(function() {
    $("#overlay").hide();
    $("#overlay .btnSubmit").click(configureIP);
    $("#overlay .btnCancel").click(function() { $("#overlay").toggle() });

    $("#add-device-overlay").hide();
    $("#add-device-overlay .btnSubmit").click(addDevice);
    $("#add-device-overlay .btnCancel").click(function() { $("#add-device-overlay").toggle() });

    $("#create-device-dialog").hide();

    $("#cloud").click(function() { onDeviceClick("cloud") });
    $("#router").click(function() { onDeviceClick("router") });
    $("#switch").click(function() { onDeviceClick("switch") });
    $("#pc").click(function() { onDeviceClick("pc") });

    // http://localhost:8080/webPacketTracer/widget/fake.json
    $.getJSON(api_url + "/all", loadTopology).fail(loadTopology);  // Apparently status code 304 is an error for this method :-S
});