app.controller('BalancerController', ['$scope', '$log', 'balancerService',
    function ($scope, $log, balancerService) {

        $scope.editorOptions = {
            lineNumbers: false,
            tabSize: 2,
            viewportMargin: Infinity,
            mode: 'yaml'
        };

        var location = window.location.pathname;

        var isAdd = location.match(/\/balancers\/add/);

        if (!isAdd) {
            var balancerId = location.split("/")[3];
            balancerService.getById(balancerId).then(
                function (balancer) {
                    $scope.name = balancer.id;
                    $scope.yamlNodes = balancer.yamlNodes;
                },
                function (response) {
                    $log.error(response);
                }
            )
        }

        $scope.save = function (form) {

            if (!form.$invalid) {
                var name = isAdd ? form.name.$modelValue :location.split("/")[3];
                var yamlNodes = form.yamlNodes.$modelValue;
                balancerService.save({
                    id: name,
                    yamlNodes: yamlNodes
                }).then(
                    function (balancer) {
                        window.location = "/balancers/view/" + balancer.id;
                    },
                    function (response) {
                        $log.error(response);
                    }
                )
            }
        };
    }
]);


app.controller('BalancersController', ['$scope', '$log', 'balancerService',
    function ($scope, $log, balancerService) {

        $scope.balancers = [];

        balancerService.getAll().then(
            function (balancers) {
                $scope.balancers = balancers;
            }, function (response) {
                $log.error(response);
            }
        );

        $scope.open = function (balancer) {
            window.location = "/balancers/view/" + balancer.id;
        };

        $scope.add = function () {
            window.location = "/balancers/add";
        }
    }
]);