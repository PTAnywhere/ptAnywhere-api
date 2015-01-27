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
        function(data) {
            console.log("The device was created successfully.");
        }).done(callback)
        .fail(function(data) { console.error("Something went wrong in the device creation.") });
}


function deleteDevice(callback) {
    deviceId = $("form[name='modify-device'] input[name='deviceId']").val();
    console.log("Deleting device " + deviceId);
    $.ajax({
        url: api_url + "/devices/" + deviceId,
        type: 'DELETE',
        success: function(result) {
            console.log("The device was deleted successfully.");
        }
    }).done(callback)
    .fail(function(data) { console.error("Something went wrong in the device creation.") });
}

function modifyDevice(callback) {
    // Check the tab
    var selectedTab = $("li.ui-state-active").attr("aria-controls");
    if (selectedTab=="tabs-1") {
        // General settings: PUT to /devices/id
        console.log("PUT to /devices/id");
    } else if (selectedTab=="tabs-2") {
        // Interfaces
        // a. Send new IP settings
        console.log("PUT to /devices/id/ports/port");
        // b. If link has changed
        var previousLink = document.forms["modify-device"]["linkInterfacePrevious"].value;
        var selectedConnection = $('#linkInterface').val();
        if (previousLink!=selectedConnection) {
            if (previousLink!="none") {
                // b1. DELETE to /devices/id/ports/id/link
                console.log("DELETE to /devices/id/ports/id/link (" + previousLink + ")");
            }
            if (selectedConnection!="none") {
                // b2. If connection is not to "none" => POST to /devices/id/ports/id/link
                console.log("POST to /devices/id/ports/id/link (" + selectedConnection + ")");
            }
        }
    } else {
        console.error("ERROR. Selected tab unknown.");
    }
    // FIXME
    callback();
}

function onDeviceClick(deviceType) {
    $("form[name='create-device'] input[name='type']").val(deviceType);
    dialog = $("#create-device-dialog").dialog({
        title: "Create new " + deviceType,
        autoOpen: false, height: 300, width: 350, modal: true, draggable: false,
        buttons: {
            "SUBMIT": function() {
                var callback = function() {
                    dialog.dialog( "close" );
                    redrawTopology();
                };
                addDevice(callback);
            },
            Cancel:function() {
                $( this ).dialog( "close" );
            }
        }, close: function() { /*console.log("Closing dialog...");*/ }
     });
    form = dialog.find( "form" ).on("submit", function( event ) { event.preventDefault(); });
    dialog.dialog( "open" );
}

function getFullPortNameForLink(linkId, notInThisDevice) {
    for (var key in nodes._data) {
        var node = nodes.get(key);
        if (node.label != notInThisDevice.label) {
            for (i = 0; i < node.ports.length; i++) {
                var port = node.ports[i];
                if ('undefined' != typeof port.link) {
                    if (port.link==linkId) {
                        return node.label + ":" + port.portName;
                    }
                }
            }
        }
    }
    return null;
}

function selectLinkedInterface(device, port) {
    if ('undefined' == typeof port.link) {
        // Select last option file
        //$('#linkInterface').val('none');
        document.forms["modify-device"]["linkId"].value = "";
        $('#linkInterface option:contains("None")').prop('selected', true);
        document.forms["modify-device"]["linkInterfacePrevious"].value = "none";
    } else {
        document.forms["modify-device"]["linkId"].value = port.link;
        var connectedToPort = getFullPortNameForLink(port.link, device);
        if (connectedToPort==null) {
            console.error("Error. The link " + port.link + " must be connected to a device.")
        } else {
            console.log("Connected to: " + connectedToPort);
            document.forms["modify-device"]["linkInterfacePrevious"].value = connectedToPort;
            $('#linkInterface').val(connectedToPort);
        }
    }
}

function updateInterfaceInformation(device, port) {
    document.forms["modify-device"]["ipAddress"].value = port.portIpAddress;
    document.forms["modify-device"]["subnetMask"].value = port.portSubnetMask;
    selectLinkedInterface(device, port);
}

function populateConnectedToSelect(current, nodes) {
    currentLinkCount = 0;
    for (var key in nodes._data) {
        if (nodes.get(key).label != current.label) {
            if ('undefined' !== typeof nodes.get(key).ports) {
                for (j = 0; j < nodes.get(key).ports.length; j++) {
                    var port = nodes.get(key).ports[j];
                    var optionName = nodes.get(key).label + ":" + port.portName;
                    document.forms["modify-device"]["linkInterface"].options[currentLinkCount + 1] = new Option(
                        optionName, optionName, false, false);
                    currentLinkCount++;
                }
            }
        }
    }
    document.forms["modify-device"]["linkInterface"].options[0] = new Option("None", "none", false, false);
}

function updateOverlay(node) {
    var current = nodes.get(node);
    var modForm = $("form[name='modify-device']");
    $("input[name='deviceId']", modForm).val(node);
    $("input[name='displayName']", modForm).val(current.label);

    populateConnectedToSelect(current, nodes);

    document.forms["modify-device"]["interface"].options.length = 0;
    for (i = 0; i < current.ports.length; i++) {
        var portName = current.ports[i].portName;
        var defaultSelected = (i == 0);
        document.forms["modify-device"]["interface"].options[i] = new Option(
                portName, portName, defaultSelected, false);
        if (defaultSelected) {
            updateInterfaceInformation(current, current.ports[i]);
        }
    }
    $("#interface").change(function () {
        $("option:selected", this).each(function() { // There is only one selection
            var selectedIFace = $(this).text();
            for (i = 0; i < current.ports.length; i++) {
                if ( selectedIFace == current.ports[i].portName ) {
                    updateInterfaceInformation(current, current.ports[i]);
                    break;
                }
            }
        });
    });
}

function overlay(node) {
    updateOverlay(node);
    var callback = function() {
        dialog.dialog( "close" );
        redrawTopology();
    };
    $("#modify-dialog-tabs").tabs();
    dialog = $("#modify-device-dialog").dialog({
        title: "Modify device",
        autoOpen: false, height: 350, width: 450, modal: true, draggable: false,
        buttons: {
            "Delete": function() {
                deleteDevice(callback);
             },
            "SUBMIT": function() {
                modifyDevice(callback);
            },
            Cancel:function() {
                $( this ).dialog( "close" );
            }
        }, close: function() { console.log("Closing dialog..."); }
     });
    form = dialog.find( "form" ).on("submit", function( event ) { event.preventDefault(); });
    dialog.dialog( "open" );
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

function loadTopology(responseData) {
    nodesJson = responseData.devices;
    edgesJson = responseData.edges;

    // create an array with nodes
    nodes = new vis.DataSet();
    nodes.subscribe('*', function() {
        $('#nodes').html(toJSON(nodes.get()));
    });
    if (nodesJson != null) {
        nodes.add(nodesJson);
    }

    // create an array with edges
    edges = new vis.DataSet();
    edges.subscribe('*', function() {
        $('#edges').html(toJSON(edges.get()));
    });
    if (edgesJson != null) {
        edges.add(edgesJson);
    }

    // create a network
    var container = $('#network').get(0);
    var visData = { nodes : nodes, edges : edges };
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
    network = new vis.Network(container, visData, options);
    network.on('click', onTap);
}

// convenience method to stringify a JSON object
function toJSON(obj) {
    return JSON.stringify(obj, null, 4);
}

function onTap(properties) {
    if (properties.nodes != null) {
        overlay(properties.nodes[0]);
        for (i = 0; i < properties.nodes.length; i++) {
            console.log(properties.nodes[i])
        }
        tappedDevice = properties.nodes[0];
        console.log("tappedDevice " + tappedDevice);
    }
}

function redrawTopology() {
    //$.getJSON(api_url + "/all", loadTopology).fail(function() {
    $.getJSON("fake.json", loadTopology).fail(function() {
        console.log("The topology could not be loaded. Possible timeout.");
    });  // Apparently status code 304 is an error for this method :-S
}

$(function() {
    $("#create-device-dialog").hide();
    $("#modify-device-dialog").hide();
    $("#creation-menu figure img").each(function() {
        var deviceId = $(this).attr("id");
        $(this).click(function() { onDeviceClick(deviceId); });
    });
    redrawTopology();
});