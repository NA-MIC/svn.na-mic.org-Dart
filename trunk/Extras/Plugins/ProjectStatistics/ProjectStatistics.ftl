<#include "Macros.ftl"/>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0//EN">
<html>
  <head>
    <meta name="robots" content="noindex,nofollow">
    <meta http-equiv="Content-Type"
      content="text/html; charset=iso-8859-1">
    <title>${projectName} Statistics -- ${date?datetime?html}</title>
    <link rel="stylesheet" href="/${projectName}/Resources/Style.css" type="text/css">
  </head>
  <body bgcolor="#ffffff">

<table border="0" cellpadding="0" cellspacing="2" width="100%">
<tr>
<td align="center" valign="middle" height="100%"><a href="/${projectName}/Dashboard/"><img alt="Logo/Homepage link" src="/${projectName}/Resources/Icons/Logo.jpg" border="0"></a>
</td>
<td align="left" width="100%" class="title">
<h2>${projectName} Statistics - ${date?datetime?html}</h2>
<h3>${date?date?html}</h3>
</td>

</tr>
</table>

<br>

<table border="0" cellpadding="3" cellspacing="1" bgcolor="#0000aa">
  <tr class="table-heading">
    <td balign="center"><h3>Statistic</h3></td>
    <td align="center"><h3>Value</h3></td>
  </tr>

  <#assign row = 0/>
  <#list projectStats?keys as key>
    <#if row % 2 == 1>
    <tr class="tr-odd">
    <#else>
    <tr class="tr-even">
    </#if>
    <#assign row = row + 1/>

      <td>${key}</td> <td align="right">${projectStats[key]?if_exists}</td>
    </tr>
  </#list>    
</table>


  </body>
</html>

