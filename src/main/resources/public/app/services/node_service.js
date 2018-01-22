
app.factory('nodeService', ['$q', '$http', function ($q, $http) {
    return {
        getAll : function () {
            return $http.get(baseUrl + '/node/find-all').then(
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