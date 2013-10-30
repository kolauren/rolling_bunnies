/**
 * Singleton class for all utils
 *
 */
Utils = (function() {

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