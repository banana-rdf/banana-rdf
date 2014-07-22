(function(){

    TestJSObject = function(x) {
        this.x = x;
    };

    TestJSObject.prototype.getValue = function() {
        return this.x;
    };

    TestJSObject.prototype.toString = function() {
        return "TEST:"+this.x;
    };

    TestJSObject.prototype.equals = function(that) {
        console.log("EQUALS JS "+that.x+" VS "+this.x)
        return that.x && that.x === this.x
    };

    TestJSObject.prototype.hashCode = function() {
        return 41 * (41 + this.x);
    };


}).call(this)
