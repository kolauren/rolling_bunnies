// global variables
var g = g || {};
// global classes
var c = c || {};

/**
 * Singleton class for all utils
 *
 */
c.Utils = (function() {

    var instance;

    function init() {
        return {
            /**
             * @param hue, saturation, lightness
             * @return a stringified random hsl value
             **/
            getColour: function(h, s, l) {
                return "hsl(" + h + "," + s + "%," + l + "%)";
            }
        };
    }

    return {
        getInstance: function() {
            if(!instance) instance = init();
            return instance;
        }
    }
})();