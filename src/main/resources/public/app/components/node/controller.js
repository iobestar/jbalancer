
app.controller('NodeController', ['$scope', '$log', 'nodeService',
    function($scope, $log, nodeService) {
        $scope.nodes = [];

        nodeService.getAll().then(
            function(nodes){
                $scope.nodes = nodes;
            }, function(response){
                $log.error(response);
            }
        );
    }
]);