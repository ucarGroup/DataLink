Date.prototype.format = function() {
    if(this == null || this == ''){
        return '';
    }
    var datetime = this.getFullYear() + "-"
        + ((this.getMonth() + 1) > 10 ? (this.getMonth() + 1) : "0" + (this.getMonth() + 1)) + "-"
        + (this.getDate() < 10 ? "0" + this.getDate() : this.getDate()) + " "
        + (this.getHours() < 10 ? "0" + this.getHours() : this.getHours()) + ":"
        + (this.getMinutes() < 10 ? "0" + this.getMinutes() : this.getMinutes()) + ":"
        + (this.getSeconds() < 10 ? "0" + this.getSeconds() : this.getSeconds());
    return datetime;
}

function formatTs(ts){
    if(ts == null || ts == ''){
        return '';
    }
    return new Date(ts).format();
}