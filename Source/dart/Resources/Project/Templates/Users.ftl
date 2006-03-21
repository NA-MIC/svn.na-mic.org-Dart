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
    <title>Users</title>
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
<h2>${projectName?html} - Users</h2>
<h3>${date?datetime?html}</h3>
<@displayMenu />
</td>

</tr>
</table>

<br>

<div class="content">
<table class="dart">
<tr class="table-heading">
  <td colspan="4"><h3>Users</h3></td>
</tr>
<tr class="table-heading">
  <th>Id</th><th>Email</th><th>Last Name</th><th>First Name</th>
</tr>
<#assign users = userFinder.find("order by email")>
<#list users.toList() as user>
 <#if user_index % 2 == 0>
  <tr class="tr-odd">
 <#else>
  <tr class="tr-even">
 </#if>
<#--   <td align="right"><a href="User?userid=${user.userId?url}">${user.userId?html}</a></td> -->
   <td align="right">${user.userId?html}</td>
   <td>${user.email?html}</td>
   <td>${user.lastName?html}</td>
   <td>${user.firstName?html}</td>
  </tr>
</#list>
</table>

</div>

</body>
</html>
