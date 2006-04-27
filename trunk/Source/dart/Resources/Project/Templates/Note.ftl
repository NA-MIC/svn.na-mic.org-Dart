<#include "Macros.ftl"/>
<#if submission?exists>
<#assign site = submission.site>
<#assign buildname = submission.buildName>
<#assign trackname = submission.type>
</#if>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0//EN">
<html>
  <head>
    <meta name="robots" content="noindex,nofollow">
    <meta http-equiv="Content-Type"
      content="text/html; charset=iso-8859-1">
    <meta http-equiv="Content-Script-Type" content="text/javascript">
    <#if submission?exists>
    <title>Note - ${site?html} - ${buildname?html} - ${trackname?html} - ${date?datetime?html}</title>
    <#else>
    <title>Note - no submission specified</title>
    </#if>
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
<a href="/${projectName}/Dashboard/?trackid=${submission.trackId?url}">
<img alt="Logo/Homepage link" src="/${projectName}/Resources/Icons/Logo.png"></a>
</td>
<td align="left" width="100%" class="title">
<h2>${projectName?html} Note - ${site?html} - ${buildname?html} - ${trackname?html}</h2>
<h3>${date?datetime?string("long")?html}</h3>
<@displayMenu />
</td>

</tr>
</table>

<br>

<div class="content">

<#if submission.getNoteCount() != 0>
<#assign notes=submission.selectTest(".Note").selectChildren().toList()/>
<b>Number of notes:</b> ${notes?size}<br>
<br>

<#list notes as note>
 <div class="title-divider">Note #${note_index}</div>
 <table>
 <#assign results = note.getResultList().toList()>
 <#list results as result>
   <tr>
     <th class="measurement">${result.getName()?html}</th>
     <td>
            <#switch result.getType()>
            <#case "text/text"><pre>${fetchdata(result.getValue())?html}</pre><#break>
            <#case "text/html">${fetchdata(result.getValue())}<#break>
            <#case "text/xml"><pre>${fetchdata(result.getValue())?html}</pre><#break>
            <#case "image/png"><img src="/${projectName}/Data/${result.getValue()?replace('\\','/')}"/><#break>
            <#case "image/jpeg"><img src="/${projectName}/Data/${result.getValue()?replace('\\','/')}"/><#break>
            <#case "archive/zip"><a href="/${projectName}/Zip/${result.getValue()?replace('\\','/')}"/>link</a><#break>
            <#default>${result.getValue()?html}<#break/>
            </#switch>
          </td>
        </tr>
 </#list>
 </table>
</#list>

</#if>

</div>

</body>
</html>
