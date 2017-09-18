/**
 * Created by victor on 12/18/16.
 */



var main = angular.module('main', []).config(function ($routeProvider) {
    $routeProvider.when('/main', {
        templateUrl: '/main'
    }).when('/clip', {
        templateUrl: '/clip'
    }).otherwise({redirectTo: '/main'});
});

main.factory('FacebookService', ['$q', '$rootScope', function ($q, $rootScope) {
    function init() {
        $rootScope.$apply(function () {
            defer.resolve();
        });
    }

    var defer = $q.defer();

    defer.promise.then(function () {
        FB.init({
            appId: '583133798551881',
            xfbml: true,
            version: 'v2.8'
        });
        FB.AppEvents.logPageView();
    });

    window.fbAsyncInit = angular.bind(this, init);

    return defer;
}]);

main.directive('contenteditable', function () {
    return {
        require: 'ngModel',
        link: function (scope, elm, attrs, ctrl) {
            // view -> model
            elm.bind('blur', function () {
                scope.$apply(function () {
                    ctrl.$setViewValue(elm.html());
                });
            });

            // model -> view
            ctrl.$render = function () {
                elm.html(ctrl.$viewValue);
            };

            // load init value from DOM

        }
    }
});


main.controller('MainCtrl', function ($scope, $location, $http, FacebookService) {

    FacebookService.promise.then(function () {

        FB.getLoginStatus(function (response) {
            if (response.status === 'connected') {

                // query for email
                FB.api('/me', {
                    fields: 'email'
                }, function (response) {
                    if (!response || response.error) {
                        console.error('error while querying user email', response);
                        return
                    }

                    $scope.connected = true;

                    window.uid = response.email.replace("@","_");
                    window.socket = io('/' + uid);

                    $scope.msgs = [];

                    var clearSyncStatus = function () {
                        $scope.msgs.forEach(function (item) {
                            item.synchronized = false;
                        });
                    };

                    var pushMessage = function(msg) {
                        if ($scope.msgs.length == 5) {
                            $scope.msgs.splice(4,1);
                        }
                        $scope.msgs.unshift(msg);
                    };

                    $http.post('/channel', {id: uid}).then(function () {
                    }, function () {
                        $('#errorModal').modal('show');
                    });


                    socket.on('message', function (msg, source) {
                        clearSyncStatus();
                        pushMessage({message: msg, src: source, synchronized: true, date: new Date()});
                        $scope.$apply();
                    });

                    $scope.sendMessage = function (newMsg) {
                        clearSyncStatus();
                        socket.emit('message', newMsg, 'Web interface');
                        var msg = {message: newMsg, src: 'Web interface', synchronized: true, date: new Date()};
                        pushMessage(msg);
                    };

                    $scope.resendMessage = function (newMsg) {
                        clearSyncStatus();
                        socket.emit('message', newMsg.message, 'Web interface');
                        newMsg.synchronized = true;
                    };

                    $scope.sendNotif = function (newMsg) {
                        var socket = io('/' + uid);
                        socket.emit('message', 'Happy new message', 'Nexus 5');
                    };

                    $scope.label = function (msg) {
                        if (!msg) {
                            return;
                        }
                        return 'Copied ' + moment(msg.date.getTime()).fromNow() + ' from ' + msg.src;
                    };

                    $location.path('/clip');
                    $scope.$apply();

                });

            } else if (response.status === 'not_authorized') {
                // the user is logged in to Facebook,
                // but has not authenticated your app
            } else {
                // the user isn't logged in to Facebook.
            }
        });

    });
})
;


(function (d, s, id) {
    var js, fjs = d.getElementsByTagName(s)[0];
    if (d.getElementById(id)) {
        return;
    }
    js = d.createElement(s);
    js.id = id;
    js.src = "//connect.facebook.net/en_US/sdk.js";
    fjs.parentNode.insertBefore(js, fjs);
}(document, 'script', 'facebook-jssdk'));