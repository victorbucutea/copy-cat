var express = require('express');
var app = express();
// make public as the static content dir
app.use(express.static('public'));
var http = require('http').Server(app);
var io = require('socket.io')(http);

var db = require('flat-file-db').sync('db/channels.db');

app.get('/', function (req, res) {
    res.sendFile(__dirname + '/index.html');
});


app.post('/channel', function (req, res) {

    req.on('data', function (data) {
        data = JSON.parse(data);
        // Too much POST data, kill the connection!
        // 1e6 ~ 1MB
        if (data.length > 1e6)
            req.connection.destroy();

        db.put(data.id, data, function (err) {
            res.json({message: 'Channel created'});
        });

        initChannel(data.id);
    });
});

db.keys().forEach(function (key) {
    // each key in DB is a channel or 'namespace'
    initChannel(key);

});

function initChannel(key) {

    if (io.nsps && io.nsps['/' + key])
        return;

    console.log('creating channel ' + key);
    var channel = io.of('/' + key);

    channel.on('connection', function (socket) {
        var channel = db.get(key);
        channel.lastSeen = new Date();
        db.put(channel.name, channel);
        socket.on('message', function (msg, source) {
            socket.broadcast.emit('message', msg, source); // relay msg to all clients
            console.log('incoming message on ' + key + ' :' + msg + ', ' + source);
        });

        //socket.emit('message','Android','hello from the other side')
    });
}

http.listen(process.env.PORT || 3000, function () {
    console.log('listening on *:' + (process.env.PORT || 3000) );
});

