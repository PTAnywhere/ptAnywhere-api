var nodes, edges, network;

function requestJSON(verb, url, data, callback) {
    return $.ajax({
        headers: {
            Accept: 'application/json',
            'Content-Type': 'application/json'
        },
        type: verb,
        url: url,
        data: JSON.stringify(data),
        dataType: 'json',
        success: callback
    });
};

$.postJSON = function(url, data, callback) {
    return requestJSON('POST', url, data, callback);
};

$.putJSON = function(url, data, callback) {
    return requestJSON('PUT', url, data, callback);
};

$.deleteHttp = function(url, callback) {
    return $.ajax({
        type: 'DELETE',
        url: url,
        success: callback
    });
};




// Canvas' (0,0) does not correspond with the network map's (0,0) position.
function toNetworkMapCoordinate(x, y) {
    var net =$("#network");
    var htmlElement = {
        topLeft: [net.offset().left, net.offset().top],
        width: net.width(),
        height: net.height()
    };

    var relativePercentPosition = [];
    relativePercentPosition[0] = (x - htmlElement.topLeft[0]) / htmlElement.width;
    relativePercentPosition[1] = (y - htmlElement.topLeft[1]) / htmlElement.height;

    // FIXME what if network does not exist yet?
    var canvas = {
        width: network.canvasBottomRight.x - network.canvasTopLeft.x,
        height: network.canvasBottomRight.y - network.canvasTopLeft.y
    };

    var ret = [];
    ret[0] = relativePercentPosition[0] * canvas.width + network.canvasTopLeft.x;
    ret[1] = relativePercentPosition[1] * canvas.height + network.canvasTopLeft.y;

    return ret;
}

function addDevicePositioned(type, elOffset, callback) {
    var x = elOffset.left;
    var y = elOffset.top;
    var position = toNetworkMapCoordinate(x, y);
    return addDevice({
        "group": type,
        "x": position[0],
        "y": position[1]
    }, callback);
}

function addDeviceWithName(label, type, x, y, callback) {
    return addDevice({
        "label": label,
        "group": type,
        "x": x,
        "y": y
    }, callback);
}

function addDevice(newDevice, callback) {
    $.postJSON( api_url + "/devices", newDevice,
        function(data) {
            console.log("The device was created successfully.");
            nodes.add(data);
        }).done(callback)
        .fail(function(data) { console.error("Something went wrong in the device creation."); });
}

function deleteDevice(deviceId) {
    $.deleteHttp(nodes.get(deviceId).url,
        function(result) {
            console.log("The device has been deleted successfully.");
        }).fail(function(data) { console.error("Something went wrong in the device removal."); });
}

function deleteEdge(edgeId) {
    $.getJSON(edges.get(edgeId).url,
        function(data) {
            $.deleteHttp(data.endpoints[0] + "/link",
                function(result) {
                    console.log("The link has been deleted successfully.");
                }
            ).fail(function(data) { console.error("Something went wrong in the link removal."); });
        }
    ).fail(function(data) { console.error("Something went wrong getting this link " + edgeId + "."); });
}

function modifyDevice(deviceId, callback) {
    // General settings: PUT to /devices/id
    var modification = {
        label: $("form[name='modify-device'] input[name='displayName']").val()
    }
    $.putJSON(api_url + "/devices/" + deviceId, modification,
        function(result) {
            console.log("The device has been modified successfully.");
            nodes.update(result);  // As the device has the same id, it should replace the older one.
    }).done(callback)
    .fail(function(data) { console.error("Something went wrong in the device modification."); });
}

function modifyPort(deviceId, portName, modForm, callback) {
    // Send new IP settings
    var modification = {
        portIpAddress: $("input[name='ipAddress']", modForm).val(),
        portSubnetMask: $("input[name='subnetMask']", modForm).val()
    }
    $.putJSON(api_url + "/devices/" + deviceId + "/ports/" + portName, modification,
        function(result) {
            console.log("The port has been modified successfully.");
    }).done(callback)
    .fail(function(data) { console.error("Something went wrong in the port modification."); });
}

function createLink(fromDeviceId, fromPortName, toDevice, toPort, doneCallback, successCallback) {
    var modification = {
        toDevice: toDevice,
        toPort: toPort
    }
    $.postJSON(api_url + "/devices/" + fromDeviceId + "/ports/" + fromPortName + "/link", modification,
        function(response) {
            console.log("The link has been created successfully.");
            successCallback(response.id, response.url);
    }).done(doneCallback)
    .fail(function(data) { console.error("Something went wrong in the link creation."); });
}

function getAvailablePorts(deviceId, selectEl, csuccess, cfail) {
    $.getJSON(api_url + "/devices/" + deviceId + "/ports?free=true", function(ports) {
        csuccess(selectEl, ports);
    }).fail(cfail);
}

function loadAvailablePorts(fromDeviceId, toDeviceId, linkForm, bothLoadedSuccess, bothLoadedFail) {
    oneLoaded = false; // It must be global for the magic to happen ;)
    afterLoadingSuccess = function(selectPortsEl, ports) {
        // TODO Right now it returns a null, but it would be much logical to return an empty array.
        if (ports==null || ports.length==0) {
            bothLoadedFail("One of the devices you are trying to link has no available interfaces.");
        } else {
            loadPortsInSelect(ports, selectPortsEl, null);
            if (oneLoaded) { // Check race conditions!
                bothLoadedSuccess();
            } else {
                oneLoaded = true;
            }
        }
    }
    afterLoadingError = function(data) {
        console.error("Something went wrong getting this devices' available ports " + deviceId + ".")
        bothLoadedFail("Unable to get " + deviceId + " device's ports.");
    }

    getAvailablePorts(fromDeviceId, $("#linkFromInterface", linkForm), afterLoadingSuccess, afterLoadingError);
    getAvailablePorts(toDeviceId, $("#linkToInterface", linkForm), afterLoadingSuccess, afterLoadingError);
}

function onLinkCreation(fromDeviceId, toDeviceId) {
    $("#link-devices .loading").show();
    $("#link-devices .loaded").hide();
    $("#link-devices .error").hide();

    var linkForm = $("form[name='link-devices']");
    // Not needed
    /*$("input[name='toDeviceId']", linkForm).val(toDeviceId);
    $("input[name='fromDeviceId']", linkForm).val(fromDeviceId);*/
    var fromDeviceName = nodes.get(fromDeviceId).label;
    var toDeviceName = nodes.get(toDeviceId).label;
    $("#fromDeviceName", linkForm).text(fromDeviceName);
    $("#toDeviceName", linkForm).text(toDeviceName);


    var dialog = $("#link-devices").dialog({
        title: "Connect two devices",
        autoOpen: false, height: 300, width: 400, modal: true, draggable: false,
        buttons: {
            "SUBMIT": function() {
                var doneCallback = function() {
                    dialog.dialog( "close" );
                };
                var successfulCreationCallback = function(edgeId, edgeUrl) {
                    edges.add([{
                        id: edgeId,
                        url: edgeUrl,
                        from: fromDeviceId,
                        to: toDeviceId,
                    }]);
                };
                var fromPortName = $("#linkFromInterface option:selected", linkForm).text().replace("/", "%20");
                var toPortName = $("#linkToInterface option:selected", linkForm).text();
                createLink(fromDeviceId, fromPortName, toDeviceName, toPortName,
                            doneCallback, successfulCreationCallback);
            },
            Cancel: function() {
                $( this ).dialog( "close" );
            }
        }, close: function() { /*console.log("Closing dialog...");*/ }
     });
    var form = dialog.find( "form" ).on("submit", function( event ) { event.preventDefault(); });
    dialog.dialog( "open" );

    loadAvailablePorts(fromDeviceId, toDeviceId, linkForm,
        function() {
            $("#link-devices .loading").hide();
            $("#link-devices .loaded").show();
            $("#link-devices .error").hide();
        },
        function(errorMessage) {
            $(".error .error-msg", linkForm).text(errorMessage);
            // TODO find a less error-prone way to refer to the SUBMIT button (not its ordinal position!).
            $("button:first", dialog).attr('disabled','disabled');  // Disables the submit button
            $("#link-devices .loading").hide();
            $("#link-devices .loaded").hide();
            $("#link-devices .error").show();
        });
}

function handleModificationSubmit(callback) {
    // Check the tab
    var modForm = $("form[name='modify-device']");
    var selectedTab = $("li.ui-state-active", modForm).attr("aria-controls");
    var deviceId = $("input[name='deviceId']", modForm).val();
    if (selectedTab=="tabs-1") { // General settings
        modifyDevice(deviceId, callback);
    } else if (selectedTab=="tabs-2") { // Interfaces
        var selectedFromInterface = $("#interface", modForm).val().replace("/", "%20");
        // Room for improvement: the following request could be avoided when nothing has changed
        modifyPort(deviceId, selectedFromInterface, modForm, callback);  // In case just the port details are modified...
    } else {
        console.error("ERROR. Selected tab unknown.");
    }
}

function onDeviceAdd(x, y) {
    var dialog = $("#create-device").dialog({
        title: "Create new device",
        autoOpen: false, height: 300, width: 400, modal: true, draggable: false,
        buttons: {
            "SUBMIT": function() {
                var callback = function() {
                    dialog.dialog( "close" );
                };
                name = document.forms["create-device"]["name"].value;
                type = document.forms["create-device"]["type"].value;
                addDeviceWithName(name, type, x, y, callback);
            },
            Cancel:function() {
                $(this).dialog( "close" );
            }
        }, close: function() { /*console.log("Closing dialog...");*/ }
     });
    dialog.parent().attr("id", "create-dialog");
    var form = dialog.find( "form" ).on("submit", function( event ) { event.preventDefault(); });
    $("#device-type").iconselectmenu().iconselectmenu("menuWidget").addClass("ui-menu-icons customicons");
    dialog.dialog( "open" );
}

function selectOptionWithText(selectEl, text) {
    $("option", selectEl).filter(function () { return $(this).html() == text; }).prop('selected', true);
}

/**
 * @param defaultSelection It can be an int with the number of the option to be selected or a "null" (for any choice).
 * @return Selected port.
 */
function loadPortsInSelect(ports, selectElement, defaultSelection) {
    var ret = null;
    selectElement.html(""); // Remove everything
    for (var i = 0; i < ports.length; i++) {
        var portName = ports[i].portName;
        var htmlAppend = '<option value="' + portName + '"';
        if (i == defaultSelection) {
            htmlAppend += ' selected';
            ret = ports[i];
        }
        selectElement.append(htmlAppend + '>' + portName + '</option>');
    }
    return ret;
}

function updateInterfaceInformation(port, formToUpdate) {
    $('input[name="ipAddress"]', formToUpdate).val(port.portIpAddress);
    $('input[name="subnetMask"]', formToUpdate).val(port.portSubnetMask);
}

function loadPortsForInterface(ports, selectedDevice, formToUpdate) {
    var selectedPort = loadPortsInSelect(ports, $("#interface", formToUpdate), 0);
    if (selectedPort!=null) {
        updateInterfaceInformation(selectedPort, formToUpdate);
        $("#tabs-2>.loading").hide();
        $("#tabs-2>.loaded").show();
    }
    $("#interface", formToUpdate).change(function () {
        $("option:selected", this).each(function(index, element) { // There is only one selection
            var selectedIFace = $(element).text();
            for (var i = 0; i < ports.length; i++) {  // Instead of getting its info again (we save one request)
                if ( selectedIFace == ports[i].portName ) {
                    updateInterfaceInformation(ports[i], formToUpdate);
                    break;
                }
            }
        });
    });
}

function updateEditForm(node) {
    $("#tabs-2>.loading").show();
    $("#tabs-2>.loaded").hide();

    var current = nodes.get(node);
    var modForm = $("form[name='modify-device']");
    $("input[name='deviceId']", modForm).val(node);
    $("input[name='displayName']", modForm).val(current.label);

    $.getJSON(api_url + "/devices/" + node + "/ports", function(data) {
        loadPortsForInterface(data, current, modForm);
    }).fail(function() {
        console.error("Ports for the device " + node + " could not be loaded. Possible timeout.");
    });
}

function onDeviceEdit(node) {
    updateEditForm(node);
    $("#modify-dialog-tabs").tabs();
    var dialog = $("#modify-device").dialog({
        title: "Modify device",
        height: 350, width: 450,
        autoOpen: false, modal: true, draggable: false,
        buttons: {
            "SUBMIT": function() {
                var dialog = $(this);
                var callback = function() {
                    dialog.dialog( "close" );
                };
                handleModificationSubmit(callback);
            },
            Cancel: function() {
                $(this).dialog( "close" );
            }
        }, close: function() { /*console.log("Closing dialog...");*/ }
     });
    dialog.parent().attr("id", "modify-dialog");
    var form = dialog.find( "form" ).on("submit", function( event ) { event.preventDefault(); });
    dialog.dialog( "open" );
}

function getCommandLineURL(nodeId) {
    var keyUrlPart = "/widget/p/";
    var ret = window.location.href.substr(0, window.location.href.search(keyUrlPart));
    var sessionId = window.location.href.substr(window.location.href.search(keyUrlPart) + keyUrlPart.length);
    return ret + "/widget/sessions/" + sessionId + "/devices/" + nodeId + "/console";
}

function openCommandLine() {
    var selected = network.getSelection();
    if (selected.nodes.length==1) { // Only if just one is selected
        var dialog = $("#command-line").dialog({
            autoOpen: false, height: 400, width: 600, modal: true, draggable: false,
            close: function() { dialog.html(""); }
        });
        dialog.html('<div class="iframeWrapper"><iframe class="terminal" src="' + getCommandLineURL(selected.nodes[0]) + '"></iframe></div>');
        dialog.dialog( "open" );
    }
}

function loadTopology(responseData) {
    // Initialize data sets if needed
    if (nodes==null) {
        nodes = new vis.DataSet();
    }
    if (edges==null) {
        edges = new vis.DataSet();
    }

    // Load data
    if (responseData.devices!=null) {
        nodes.clear();
        nodes.add(responseData.devices);
    }
    if (responseData.edges!=null) {
        edges.clear();
        edges.add(responseData.edges);
    }

    // Create network element if needed (only the first time)
    if (network==null) {
        // create a network
        var container = $('#network').get(0);
        var visData = { nodes : nodes, edges : edges };
        var options = {
            //dragNetwork : false,
            //dragNodes : true,
            //zoomable : false,
            stabilize: true,
            dataManipulation: true,
            edges: {
                width: 3,
                widthSelectionMultiplier: 1.4,
                color: {
                    color:'#606060',
                    highlight:'#000000',
                    hover: '#000000'
                }
             },
            groups : {
                // TODO room for improvement, static URL vs relative URL
                cloudDevice : {
                    shape : 'image',
                    image : "../../static/cloud.png"
                },
                routerDevice : {
                    shape : 'image',
                    image : "../../static/router.png"
                },
                switchDevice : {
                    shape : 'image',
                    image : "../../static/switch.png"
                },
                pcDevice : {
                    shape : 'image',
                    image : "../../static/PC.png"
                }
            },
            onAdd: function(data, callback) {
                onDeviceAdd(data.x, data.y);
            },
            onConnect: function(data, callback) {
                onLinkCreation(data.from, data.to);
            },
            onEdit: function(data, callback) {
                onDeviceEdit(data.id);
            },
            onDelete: function(data, callback) {
                if (data.nodes.length>0) {
                    deleteDevice(data.nodes[0]);
                } else if (data.edges.length>0) {
                    deleteEdge(data.edges[0]);
                }
                // This callback is important, otherwise it received 3 consecutive onDelete events.
                callback(data);
            }
        };
        network = new vis.Network(container, visData, options);
        network.on('doubleClick', openCommandLine);
    }
}

// convenience method to stringify a JSON object
function toJSON(obj) {
    return JSON.stringify(obj, null, 4);
}

function redrawTopology() {
    redrawTopology(null);
}

/**
 * @arg callback If it is null, it is simply ignored.
 */
function redrawTopology(callback) {
    $.ajax({
        url: api_url + "/network",
        type : 'GET',
        dataType: 'json',
        success: function(data) {
            loadTopology(data);
            if (callback!=null)
                callback();
        },
        tryCount : 0,
        retryLimit : 3,
        timeout: 2000,
        error : function(xhr, textStatus, errorThrown ) {
            if (textStatus == 'timeout') {
                console.error("The topology could not be loaded: timeout.");
                this.tryCount++;
                if (this.tryCount <= this.retryLimit) {
                    //try again
                    $.ajax(this);
                    return;
                }
                return;
            } else {
                console.error("The topology could not be loaded: " + errorThrown + ".");
                if(xhr.status==404) {
                    $(".view").html($("#notFound").html());
                }
            }
        }  // Apparently status code 304 is an error for this method :-S
    });
}

// From: http://www.jquerybyexample.net/2012/06/get-url-parameters-using-jquery.html
function getURLParameter(sParam) {
    var sPageURL = window.location.search.substring(1);
    var sURLVariables = sPageURL.split('&');
    for (var i = 0; i < sURLVariables.length; i++) {
        var sParameterName = sURLVariables[i].split('=');
        if (sParameterName[0] == sParam) {
            return sParameterName[1];
        }
    }
}

// Source: http://stackoverflow.com/questions/5419134/how-to-detect-if-two-divs-touch-with-jquery
function collisionWithCanvas(element) {
    var x1 = $("#network").offset().left;
    var y1 = $("#network").offset().top;
    var h1 = $("#network").outerHeight(true);
    var w1 = $("#network").outerWidth(true);
    var b1 = y1 + h1;
    var r1 = x1 + w1;
    var x2 = element.offset().left;
    var y2 = element.offset().top;
    var h2 = element.outerHeight(true);
    var w2 = element.outerWidth(true);
    var b2 = y2 + h2;
    var r2 = x2 + w2;

    if (b1 < y2 || y1 > b2 || r1 < x2 || x1 > r2) return false;
    return true;
}

function initDraggable(element) {
    element.animate({'opacity':'1'}, 1000, function() {
        element.css({ // would be great with an animation too, but it doesn't work
            'left':element.data('originalLeft'),
            'top':element.data('originalTop')
        });
    });
}

function configureDraggableCreationElement(element, creation_function) {
    element.data({ // Or we could also record it in the 'start' event.
        'originalLeft': element.css('left'),
        'originalTop': element.css('top')
    });
    element.draggable({
        helper: "clone",
        opacity: 0.4,
        /*revert: true, // It interferes with the position I want to capture in the 'stop' event
        revertDuration: 2000,*/
        start: function(event, ui) {
            $(this).css({'opacity':'0.7'});
        },
        /*drag: function(event, ui ) {
            console.log(event);
        },*/
        stop: function(event, ui) {
            if (collisionWithCanvas(ui.helper)) {
                var image = $('<img alt="Temporary image" src="' + ui.helper.attr("src") + '">');
                image.css("width", ui.helper.css("width"));
                var warning = $('<div class="text-in-image"><span>Creating...</span></div>');
                warning.prepend(image);
                $("body").append(warning);
                warning.css({'position': 'absolute',
                             'left': ui.offset.left,
                             'top': ui.offset.top});
                creation_function(ui.offset, function() {
                    initDraggable(element);
                    warning.remove();
                });
            } else {
                initDraggable(element);
            }
        }
    });
}

$(function() {
    var debugMode = getURLParameter('debug');
    if (debugMode!=null) {
        $.getScript("debug.js", function() {
            console.log("DEBUG MODE ON.");
        });
    }
    if (location.port==8000) {
        // If the page is deployed in the port 8000, it assumes that the python simple server is running
        // and the API is working in a different server.
        api_url = "http://localhost:8080/webPacketTracer/api";
        console.log("Using an API deployed in a different HTTP server: " + api_url)
    }

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

    // Better with CSS?
    $("#create-device").hide();
    $("#modify-device").hide();
    $("#link-devices").hide();

    configureDraggableCreationElement($("#cloud"), function(elementOffset, callback) {
        addDevicePositioned("cloud", elementOffset, callback);
    });
    configureDraggableCreationElement($("#router"), function(elementOffset, callback) {
        addDevicePositioned("router", elementOffset, callback);
    });
    configureDraggableCreationElement($("#switch"), function(elementOffset, callback) {
        addDevicePositioned("switch", elementOffset, callback);
    });
    configureDraggableCreationElement($("#pc"), function(elementOffset, callback) {
        addDevicePositioned("pc",elementOffset, callback);
    });

    redrawTopology(null);
});