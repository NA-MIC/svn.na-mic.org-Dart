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
    <#if client?exists>
    <title>Client - ${site?html} - ${buildname?html}</title>
    <#else>
    <title>Client - no client specified</title>
    </#if>
    <link rel="stylesheet" href="/${projectName}/Resources/Style.css" type="text/css">
    <link rel="shortcut icon" href="/${projectName}/Resources/Icons/favicon.ico" type="image/x-icon" />
<!--[if IE]>
    <script language="javascript" src="/${projectName}/Resources/cssMenuHelper.js" type="text/javascript"></script>
<![endif]-->
  </head>
<body>

<@displayLogin />
<table border="0" cellpadding="0" cellspacing="2" width="100%">
<tr>
<td align="center" valign="middle" height="100%">
<a href="/${projectName}/Dashboard/">
<img alt="Logo/Homepage link" src="/${projectName}/Resources/Icons/Logo.png"></a>
</td>
<td align="left" width="100%" class="title">
<#if client?exists>
<h2>${projectName?html} Client - ${site?html} - ${buildname?html}</h2>
<#else>
<h2>${projectName?html} Client - no client specified</h2>
</#if>
<h3>${date?datetime?html}</h3>
<@displayMenu />
</td>

</tr>
</table>

<br>

<div class="content">
<table border="0" cellpadding="3" cellspacing="1" bgcolor="#0000aa">
  <tr class="table-heading">
    <td colspan="2" valign="middle">
     <h3>Client</h3>
    </td>
  </tr>
  <tr class="tr-odd">
    <td>Client id</td><td>${client.clientId?html}</td>
  </tr>
  <tr class="tr-even">
    <td>Site</td><td>${client.site?html}</td>
  </tr>
  <tr class="tr-odd">
    <td>BuildName</td><td>${client.buildName?html}</td>
  </tr>
  <tr class="tr-even">
    <td>DisplayName</td><td>${client.displayName?default("")?html}</td>
  </tr>
  <tr class="tr-odd">
    <td>OS</td><td>${client.oS?default("")?html}</td>
  </tr>
  <tr class="tr-even">
    <td>OS Version</td><td>${client.oSVersion?default("")?html}</td>
  </tr>
  <tr class="tr-odd">
    <td>Branch</td><td>${client.branch?default("")?html}</td>
  </tr>
  <tr class="tr-even">
    <td>Comment</td><td>${client.comment?default("")?html}</td>
  </tr>
  <tr class="tr-odd">
    <td>Configuration</td><td>${client.configuration?default("")?html}</td>
  </tr>
</table>