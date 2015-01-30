var api_url = "../webapi";
// var api_url = "http://localhost:8080/ptsmith-rest/ptsmith";
// "http://carre.kmi.open.ac.uk/forge/ptsmith"

var nodes, edges, network;

function requestJSON(verb, url, data, callback) {
    return $.ajax({
    headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
    },
    'type': verb,
    'url': url,
    'data': JSON.stringify(data),
    'dataType': 'json',
    'success': callback
    });
};

$.postJSON = function(url, data, callback) {
    return requestJSON('POST', url, data, callback);
};

$.putJSON = function(url, data, callback) {
    return requestJSON('PUT', url, data, callback);
};

function addDevice(callback) {
    var newDevice = {
        "label": document.forms["create-device"]["name"].value,
        "group": document.forms["create-device"]["type"].value
    }
    console.log("Adding device " + newDevice.label + " of type " + newDevice.group);

    $.postJSON( api_url + "/devices", newDevice,
        function(data) {
            console.log("The device was created successfully.");
        }).done(callback)
        .fail(function(data) { console.error("Something went wrong in the device creation.") });
}

function getDeviceToModify() {
    return $("form[name='modify-device'] input[name='deviceId']").val();
}

function deleteDevice(deviceId) {
    $.ajax({
        url: api_url + "/devices/" + deviceId,
        type: 'DELETE',
        success: function(result) {
            console.log("The device has been deleted successfully.");
        }
    }).done(redrawTopology)
    .fail(function(data) { console.error("Something went wrong in the device creation.") });
}

function modifyDevice(deviceId, callback) {
    // General settings: PUT to /devices/id
    var modification = {
        label: $("form[name='modify-device'] input[name='displayName']").val()
    }
    $.putJSON(api_url + "/devices/" + deviceId, modification,
        function(result) {
            console.log("The device has been modified successfully.");
    }).done(callback)
    .fail(function(data) { console.error("Something went wrong in the device modification.") });
}

function modifyPort(deviceId, portName) {
    // Send new IP settings
    var modification = {
        portIpAddress: $("form[name='modify-device'] input[name='ipAddress']").val(),
        portSubnetMask: $("form[name='modify-device'] input[name='subnetMask']").val()
    }
    $.putJSON(api_url + "/devices/" + deviceId + "/ports/" + portName, modification,
        function(result) {
            console.log("The port has been modified successfully.");
    })
    .fail(function(data) { console.error("Something went wrong in the port modification.") });
}

function deleteLink(deviceId, portName, callback) {
    $.ajax({
        url: api_url + "/devices/" + deviceId + "/ports/" + portName + "/link",
        type: 'DELETE',
        success: function(result) {
            console.log("The link has been deleted successfully.");
        }
    }).done(callback)
    .fail(function(data) { console.error("Something went wrong in the link deletion.") });
}

function createLink(fromDeviceId, fromPortName, toDeviceAndPort, callback) {
    var slices = toDeviceAndPort.split(":"); // FIXME what if the port name or the device label have a ":"
    var modification = {
        toDevice: slices[0],
        toPort: slices[1]
    }
    $.postJSON(api_url + "/devices/" + fromDeviceId + "/ports/" + fromPortName + "/link", modification,
        function(result) {
            console.log("The link has been created successfully.");
    }).done(callback)
    .fail(function(data) { console.error("Something went wrong in the link creation.") });
}

function handleModificationSubmit(callback) {
    // Check the tab
    var selectedTab = $("li.ui-state-active").attr("aria-controls");
    var deviceId = getDeviceToModify();
    if (selectedTab=="tabs-1") { // General settings
        modifyDevice(deviceId, callback);
    } else if (selectedTab=="tabs-2") { // Interfaces
        var selectedPort = $("#interface").val().replace("/", "%20");
        // Room for improvement: the following request could be avoided when nothing has changed
        modifyPort(deviceId, selectedPort);
        // The following requests can be done simultaneously
        // b. If link has changed
        var previousLink = document.forms["modify-device"]["linkInterfacePrevious"].value;
        var selectedConnection = $('#linkInterface').val();
        if (previousLink!=selectedConnection) {
            if (previousLink!="none") {
                // b1. DELETE to /devices/id/ports/id/link
                deleteLink(deviceId, selectedPort, function() {
                    if (selectedConnection!="none") {
                        // b2. If connection is not to "none" => POST to /devices/id/ports/id/link
                        createLink(deviceId, selectedPort, selectedConnection, callback); // create after delete
                    } else callback();
                });
            } else {
                if (selectedConnection!="none") {
                    // b2. If connection is not to "none" => POST to /devices/id/ports/id/link
                    createLink(deviceId, selectedPort, selectedConnection, callback);
                } else callback();
            }
        }
    } else {
        console.error("ERROR. Selected tab unknown.");
    }
}

function onDeviceClick() {
    //$("#device-type").iconselectmenu().iconselectmenu("menuWidget").addClass("ui-menu-icons customicons");
    var dialog = $("#create-device-dialog").dialog({
        title: "Create new device",
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
    var form = dialog.find( "form" ).on("submit", function( event ) { event.preventDefault(); });
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
            //console.log("Connected to: " + connectedToPort);
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
            "SUBMIT": function() {
                handleModificationSubmit(callback);
            },
            Cancel:function() {
                $( this ).dialog( "close" );
            }
        }, close: function() { /*console.log("Closing dialog...");*/ }
     });
    form = dialog.find( "form" ).on("submit", function( event ) { event.preventDefault(); });
    dialog.dialog( "open" );
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
        //dragNetwork : false,
        //dragNodes : true,
        //zoomable : false,
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
        },
        stabilize: false,
        dataManipulation: true,
        onAdd: function(data,callback) {
          var span = document.getElementById('operation');
          var idInput = document.getElementById('node-id');
          var labelInput = document.getElementById('node-label');
          var saveButton = document.getElementById('saveButton');
          var cancelButton = document.getElementById('cancelButton');
          var div = document.getElementById('network-popUp');
          span.innerHTML = "Add Node";
          idInput.value = data.id;
          labelInput.value = data.label;
          onDeviceClick();
          //saveButton.onclick = saveData.bind(this,data,callback);
          //cancelButton.onclick = clearPopUp.bind();
          //div.style.display = 'block';
        },
        onEdit: function(data,callback) {
          var span = document.getElementById('operation');
          var idInput = document.getElementById('node-id');
          var labelInput = document.getElementById('node-label');
          var saveButton = document.getElementById('saveButton');
          var cancelButton = document.getElementById('cancelButton');
          //var div = document.getElementById('network-popUp');
          span.innerHTML = "Edit Node";
          idInput.value = data.id;
          labelInput.value = data.label;
          overlay(data.id);
          //saveButton.onclick = saveData.bind(this,data,callback);
          //cancelButton.onclick = clearPopUp.bind();
          //div.style.display = 'block';
        },
        onDelete: function(data,callback) {
          var span = document.getElementById('operation');
          var idInput = document.getElementById('node-id');
          var labelInput = document.getElementById('node-label');
          //var div = document.getElementById('network-popUp');
          idInput.value = data.id;
          labelInput.value = data.label;
          if (data.nodes.length>0)
            deleteDevice(data.nodes[0])
          else if (data.edges.length>0)
            console.log("The edge deletion has been disabled. Use the dialog.");
        },
        onConnect: function(data,callback) {
          if (data.from == data.to) {
            var r=confirm("Do you want to connect the node to itself?");
            if (r==true) {
              callback(data);
            }
          }
          else {
            callback(data);
          }
        }
    };
    network = new vis.Network(container, visData, options);
    //network.on('click', onTap);
}

// convenience method to stringify a JSON object
function toJSON(obj) {
    return JSON.stringify(obj, null, 4);
}

function onTap(properties) {
    if (properties.nodes != null) {
        overlay(properties.nodes[0]);
    }
}

function redrawTopology() {
    //$.getJSON(api_url + "/all", loadTopology).fail(function() {
    $.getJSON("fake.json", loadTopology).fail(function() {
        console.log("The topology could not be loaded. Possible timeout.");
    });  // Apparently status code 304 is an error for this method :-S
}

$(function() {
    $.widget( "custom.iconselectmenu", $.ui.selectmenu, {
        _renderItem: function( ul, item ) {
            var li = $( "<li>", { text: item.label } );
            if ( item.disabled ) {
                li.addClass( "ui-state-disabled" );
            }
            $( "<span>", {
                style: item.element.attr( "data-style" ),
                "class": "ui-icon " + item.element.attr( "data-class" )
             }).appendTo( li );
             return li.appendTo( ul );
        }
    });
    $("#create-device-dialog").hide();
    $("#modify-device-dialog").hide();
    redrawTopology();
});