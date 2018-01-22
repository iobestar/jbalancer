<#include "base.ftl">
<#macro page_head>
</#macro>

<#macro page_body>
<div class="container" ng-controller="BalancerController" ng-cloak>

    <div>
        <#if mode == "add">
            <h2>Add new balancer</h2>
        <#else>
            <h2>${balancerId}</h2>
        </#if>
    </div>
    <hr>
    <form name="balancerForm" ng-submit="save(balancerForm)" novalidate>
        <#if mode == "add">
            <div class="form-group">
                <label for="nameId">Name:</label>
                <input type="text" class="form-control" id="nameId" name="name" ng-model="name" style="width: 50%" required>
                <p ng-show="balancerForm.name.$invalid && !balancerForm.name.$pristine" class="help-block">Name is required</p>
            </div>
        </#if>
        <div class="form-group">
            <label for="yamlNodesId">YAML nodes:</label>
            <textarea ui-codemirror ui-codemirror-opts="editorOptions" class="form-control" id="yamlNodesId" name="yamlNodes" ng-model="yamlNodes" style="width: 100%;" required></textarea>
            <p ng-show="balancerForm.yamlNodes.$invalid && !balancerForm.yamlNodes.$pristine" class="help-block">Nodes YAML definition is required</p>
        </div>
        <button type="submit" class="btn btn-primary" ng-disabled="balancerForm.$invalid">Save</button>
    </form>
</div>
</#macro>

<@page/>