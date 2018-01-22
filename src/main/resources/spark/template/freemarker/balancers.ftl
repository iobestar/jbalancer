<#include "base.ftl">
<#macro page_head>
</#macro>

<#macro page_body>
<div class="container" ng-controller="BalancersController" ng-cloak>

    <div>
        <h2>Balancers</h2>
    </div>
    <hr>
    <div>
        <button type="button" class="btn btn-primary" aria-label="Add" style="margin: 10px" ng-click="add()"><span class="glyphicon glyphicon-plus" aria-hidden="true"></span></button>

        <table class="table table-striped">
            <thead>
            </thead>
            <tbody>
            <tr ng-repeat="balancer in balancers">
                <td>{{ balancer.id }}</td>
                <td style="text-align: right">
                    <button type="button" class="btn btn-default" aria-label="Open" ng-click="open(balancer)"><span class="glyphicon glyphicon-folder-open" aria-hidden="true"></span></button>
                    <button type="button" class="btn btn-default" aria-label="Remove"><span class="glyphicon glyphicon-trash" aria-hidden="true"></span></button>
                </td>
            </tr>
            </tbody>
        </table>
    </div>
</div>
</#macro>

<@page/>