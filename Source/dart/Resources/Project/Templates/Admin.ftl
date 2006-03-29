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
    <title>Admin</title>
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
<h2>${projectName?html} - Admin</h2>
<h3>${date?datetime?string("long")?html}</h3>
<@displayMenu />
</td>

</tr>
</table>

<br>

<div class="content">
<#if session?exists && session.user?exists>
  <#-- user logged in, check roles -->
  <#if realm.isUserInRole( session.user, "Dart.Administrator") || realm.isUserInRole( session.user, projectName + ".Administrator")>
  <ul>
  <li><a href="Clients">Configure clients</a>
  <li><a href="Users">List users</a>
  </ul>
  <#else>
  <div style="margin-left: 50px; margin-right: 50px"> 
  You must be a Dart server administrator or an administrator for project ${projectName?html} to access the project administration page.
  </div>
  </#if>
<#else>
  <div style="margin-left: 50px; margin-right: 50px"> 
  You must be a Dart server administrator or an administrator for project ${projectName?html} to access the project administration page.
  </div>
</#if>

</div>

</body>
</html>
