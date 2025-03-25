<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Degrees of Management</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            max-width: 800px;
            margin: 0 auto;
            padding: 20px;
            background-color: #121212;
            color: #e0e0e0;
        }
        h1 {
            color: #bb86fc;
            text-align: center;
            margin-bottom: 30px;
        }
        .instance-card {
            background-color: #1e1e1e;
            border-radius: 8px;
            padding: 15px;
            margin-bottom: 15px;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
            border: 1px solid #333;
        }
        .instance-name {
            font-size: 1.2em;
            font-weight: bold;
            margin-bottom: 10px;
            color: #bb86fc;
        }
        .instance-info {
            margin-bottom: 10px;
            color: #b0b0b0;
        }
        .item-list {
            margin-left: 20px;
        }
        .item-list-item {
            display: block;
            margin-bottom: 3px;
            color: #d0d0d0;
        }
        .label {
            font-weight: bold;
            color: #03dac6;
        }
        .launch-button {
            display: inline-block;
            background-color: #bb86fc;
            color: #121212;
            padding: 8px 16px;
            text-decoration: none;
            border-radius: 4px;
            font-weight: bold;
            margin-top: 10px;
            transition: background-color 0.2s;
        }
        .launch-button:hover {
            background-color: #9d4edd;
        }
        p {
            text-align: center;
            color: #b0b0b0;
        }
    </style>
</head>
<body>
<h1>Degrees of Lewdity</h1>

<#list instances as instance>
    <div class="instance-card">
        <div class="instance-name">${instance.name?html}</div>
        <div class="instance-info">ID: ${instance.id?html}</div>
        <#if instance.foundation??>
            <div class="instance-info">
                <span class="label">Base Game:</span>
                <div class="item-list">
                    <span class="item-list-item">${instance.foundation?html}</span>
                </div>
            </div>
        </#if>
        <#if instance.layers?size gt 0>
            <div class="instance-info">
                <span class="label">Layers:</span>
                <div class="item-list">
                    <#list instance.layers as layer>
                        <span class="item-list-item">${layer?html}</span>
                    </#list>
                </div>
            </div>
        </#if>
        <#if instance.mods?size gt 0>
            <div class="instance-info">
                <span class="label">Mods:</span>
                <div class="item-list">
                    <#list instance.mods as mod>
                        <span class="item-list-item">${mod?html}</span>
                    </#list>
                </div>
            </div>
        </#if>
        <a href="/play/${instance.id}/index" class="launch-button">Launch Game</a>
    </div>
<#else>
    <p>No instances available</p>
</#list>

<div class="instance-card" style="text-align: center;">
    <div class="instance-name">Administration</div>
    <div class="instance-info">Perform management actions</div>
    <!--<form action="/edit" method="post" style="margin-top: 15px;">
        <button type="submit" class="launch-button">Manage Instances</button>
    </form>-->
    <form action="/edit/reload" method="post" style="margin-top: 15px;">
        <button type="submit" class="launch-button">Reload Registry</button>
    </form>
</div>

</body>
</html>
