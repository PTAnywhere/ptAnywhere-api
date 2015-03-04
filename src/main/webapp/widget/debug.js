// FUNCTIONS USED IN THE SELENIUM TESTs


// Useful methods for programmatically using vis.js:
//    (mainly for testing as selecting inside a canvas becomes difficult)
// network.selectNodes(["{b388b180-4aab-485e-8ba8-cd401a09b2c8}"])
// network.selectNodes([]);  <-- Unselect
// network._toggleEditMode()
// network._deleteSelected()
// network._loadManipulationSystem()


function getNodesByName(label) {
    return nodes.get({
        filter: function (item) {
                    return (item.label == label);
                }
    });
}

function getNodeByName(label) {
    return getNodesByName(label)[0];
}

function selectNode(nodeId) {
    network.selectNodes([nodeId,])
}


// FUNCTIONS USED WHEN DEBUGGING CANVAS

function createPointerInHtml(x, y, text) {
    var pointer = $("<div>" + text + "</div>");
    $("body").append(pointer);
    pointer.css({'position': 'absolute',
                 'left':x,
                 'top':y});
}

// This function should be called from the loadTopology() function.
// Sample points that might be useful to use them as references:
//      createPointerInCanvas(0, 0, "0");
//      createPointerInCanvas(100, 100, "1");
function createPointerInCanvas(x, y, text) {
    nodes.add({label: text, x:x , y:y});
}
