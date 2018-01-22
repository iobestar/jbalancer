
var baseUrl = "/api/v1";

var app = angular.module('app', ['ui.codemirror'],function($locationProvider){
    $locationProvider.html5Mode(true);
});