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