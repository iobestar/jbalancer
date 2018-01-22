<#include "base.ftl">
<#macro page_head>
</#macro>


<#macro page_body>
<div class="container" ng-controller="NodeController" ng-cloak>

    <div>
        <h2>Nodes</h2>
    </div>
    <hr>
    <div>
        <div class="form-group">
            <textarea class="form-control" placeholder="Enter select expression..."></textarea>
        </div>
        <div class="form-group">
            <button class="btn btn-primary">Select</button>
            <div class="btn-group">
                <button type="button" class="btn btn-primary dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                    Action <span class="caret"></span>
                </button>
                <ul class="dropdown-menu">
                    <li><a href="#">Enable</a></li>
                    <li><a href="#">Disable</a></li>
                </ul>
            </div>
        </div>
    </div>
    <hr>
    <div>
        <table class="table table-striped">
            <thead>
            <tr>
                <th>Balancer</th>
                <th>Connection</th>
                <th>Status</th>
                <th>Active</th>
                <th>Alive</th>
                <th>Enabled</th>
            </tr>
            </thead>
            <tbody>
            <tr ng-repeat="node in nodes">
                <td>{{ node.balancer }}</td>
                <td>{{ node.connection }}</td>
                <td>{{ node.status }}</td>
                <td><span class="label" ng-class="{'label-success': node.active , 'label-danger': !node.active}">{{ node.active }}</span></td>
                <td><span class="label" ng-class="{'label-success': node.alive , 'label-danger': !node.alive}">{{ node.alive }}</span></td>
                <td><span class="label" ng-class="{'label-success': node.enabled , 'label-danger': !node.enabled}">{{ node.enabled }}</span></td>
            </tr>
            </tbody>
        </table>
    </div>
</div>
</#macro>

<@page/>