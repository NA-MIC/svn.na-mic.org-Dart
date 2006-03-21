<#include "Macros.ftl"/>
<#if client?exists>
<#assign site = client.site>
<#assign buildname = client.buildName>
</#if>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0//EN">
<html>
  <head>
    <meta name="robots" content="noindex,nofollow">
    <meta http-equiv="Content-Type"
      content="text/html; charset=iso-8859-1">
    <meta http-equiv="Content-Script-Type" content="text/javascript">
    <title>Clients</title>
    <link rel="stylesheet" href="/${projectName}/Resources/Style.css" type="text/css">
    <link rel="shortcut icon" href="/${projectName}/Resources/Icons/favicon.ico" type="image/x-icon" />
<!--[if IE]>
    <script language="javascript" src="/${projectName}/Resources/cssMenuHelper.js" type="text/javascript"></script>
<![endif]-->
  </head>
<body>

<@displayLogin />
<table class="pagetitle">
<tr>
<td align="center" valign="middle" height="100%">
<a href="/${projectName}/Dashboard/">
<img alt="Logo/Homepage link" src="/${projectName}/Resources/Icons/Logo.png"></a>
</td>
<td align="left" width="100%" class="title">
<h2>${projectName?html} - Clients</h2>
<h3>${date?datetime?html}</h3>
<@displayMenu />
</td>

</tr>
</table>

<br>

<div class="content">
<#-- query for all clients, could show last submission and what tracks -->
<table class="dart"">
<tr class="table-heading">
  <td colspan="3"><h3>Clients</h3></td>
</tr>
<tr class="table-heading">
  <th>Id</th><th>Site</th><th>BuildName</th>
</tr>
<#assign clients = clientFinder.find("order by site,buildname")>
<#list clients.toList() as client>
 <#if client_index % 2 == 0>
  <tr class="tr-odd">
 <#else>
  <tr class="tr-even">
 </#if>
   <td align="right"><a href="Client?clientid=${client.clientId?url}">${client.clientId?html}</a></td>
   <td>${client.site?html}</td>
   <td>${client.buildName?html}</td>
  </tr>
</#list>
</table>

</div>

</body>
</html>
