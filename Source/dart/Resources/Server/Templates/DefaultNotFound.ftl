<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0//EN">
<html>
  <head>
    <meta name="robots" content="noindex,nofollow">
    <meta http-equiv="Content-Type"
      content="text/html; charset=iso-8859-1">
    <title>Dart</title>
    <link rel="stylesheet" href="/DartServer/Resources/Style.css" type="text/css">
    <link rel="shortcut icon" href="/DartServer/Resources/Icons/favicon.ico" type="image/x-icon" />
  </head>
  <body bgcolor="#ffffff">

<table border="0" cellpadding="0" cellspacing="2" width="100%">
<tr>
<td align="center"><a href="/"><img alt="Logo/Homepage link" src="/DartServer/Resources/Icons/Logo.png" border="0"></a>
</td>
<td align="left" width="100%" class="title">
<h2>Dart Server on ${request.getHost()?html}</h2>
<h3>${date?datetime?html}</h3>
</td>

</tr>
</table>

<br/>
<table border="0" cellpadding="3" cellspacing="1" bgcolor="#0000aa">
  <tr class="table-heading">
  <th align="left" colspan="4"/>Available Dartboards</th>
  </tr>
  <tr class="table-heading">
     <th align="center">Project</th>
     <th align="center">Submissions</th>
     <th align="center">Tests</th>
     <th align="center">Last activity</th>
  </tr>
<#assign row = 1/>
<#list projects?values as project>
  <#if row % 2 == 1>
    <tr class="tr-odd">
  <#else>
    <tr class="tr-even">
  </#if>
  <#assign row = row + 1/>

  <td><a href="/${project.getTitle()}/Dashboard/"><img alt="Small logo" src="/DartServer/Resources/Icons/DartLogoSmallSmooth.png" border="0"></a> &nbsp <a href="/${project.getTitle()}/Dashboard/">${project.getTitle()?html}</a></td>
  <td align="right">${project.getStats()["Submissions"]?default(0)}</td>
  <td align="right">${project.getStats()["TestsProcessed"]?default(0)}</td>
  <td align="right">${project.getStats()["LastActivity"]?default("none")}</td>
  <tr>
</#list>
</table>  

<br/>

  </body>
</html>

