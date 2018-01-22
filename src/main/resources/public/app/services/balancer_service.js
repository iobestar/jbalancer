
app.factory('balancerService', ['$q', '$http', function ($q, $http) {
    return {
        getAll : function () {
            return $http.get(baseUrl + '/balancer/find-all').then(
                function (response) {
                    return response.data;
                },
                function (response) {
                    return $q.reject(response);
                }
            );
        },
        save : function (balancer) {
            return $http.post(baseUrl + '/balancer/save', balancer).then(
                function (response) {
                    return response.data;
                },
                function (response) {
                    return $q.reject(response);
                }
            );
        },
        getById : function (balancerName) {
            return $http.get(baseUrl + '/balancer/find-by-name/' + balancerName).then(
                function (response) {
                    return response.data;
                },
                function (response) {
                    return $q.reject(response);
                }
            );
        }
    }
}]);