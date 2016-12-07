var app = require('express')();
var http = require('http').Server(app);
var io = require('socket.io')(http, {'destroy buffer size': 1e6});
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

        throw new EventException();

        db.put(data.name, data, function (err) {
            res.json({message: 'Channel created'});
        });
    });
});

db.keys().forEach(function (key) {
    // each key in DB is a channel or 'namespace'
    initChannel(key);

});

function initChannel(key) {
    console.log('init channel ' + key);
    var channel = io.of('/' + key);

    channel.on('connection', function (socket) {
        socket.emit('message', 'HELLO');


        var channel = db.get(key);
        channel.lastSeen = new Date();
        db.put(channel.name , channel);

        socket.on('message', function (msg) {
            socket.emit('message', msg); // relay msg to all clients
        });
    });
}


http.listen(3000, function () {
    console.log('listening on *:3000');
});

