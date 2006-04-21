<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0//EN">
<#assign client = submission.clientEntity/>
<#assign track = submission.trackEntity/>
<#include "Macros.ftl"/>

<html>
  <head>
    <meta name="robots" content="noindex,nofollow">
    <meta http-equiv="Content-Type"
      content="text/html; charset=iso-8859-1">
    <meta http-equiv="Content-Script-Type" content="text/javascript">
    <title>Build - ${client.site?html} - ${client.buildName?html} - ${submission.type?html} - ${submission.timeStamp?datetime?html}</title>
    <link rel="stylesheet" href="/${projectName}/Resources/Style.css" type="text/css">
    <link rel="shortcut icon" href="/${projectName}/Resources/Icons/favicon.ico" type="image/x-icon" />
<!--[if IE]>
    <script language="javascript" src="/${projectName}/Resources/cssMenuHelper.js" type="text/javascript"></script>
<![endif]-->
  </head>
<body id="top">

<@displayLogin />
<table class="pagetitle">
<tr>
<td align="center" valign="middle" height="100%"><a href="/${projectName}/Dashboard/?trackid=${submission.trackId?url}"><img alt="Logo/Homepage link" src="/${projectName}/Resources/Icons/Logo.png"></a>
</td>
<td align="left" width="100%" class="title">
<h2>${projectName?html} Build - ${client.site?html} - ${client.buildName?html} - ${submission.type?html}</h2><h3>${submission.timeStamp?datetime?string("long")?html}</h3>
<@displayMenu />
</td></tr>
</table>

<br/>

<#macro displayStageNav>
<div class="tracknav">
[<a href="Build?submissionid=${submission.submissionId?url}#top">Top</a>]
<#list stages as stage>
[<a href="Build?submissionid=${submission.submissionId?url}#${stage.name?url}">${stage.StageName?html}</a><#if (stage.ErrorCount>0)>|<a href="Build?submissionid=${submission.submissionId?url}#${stage.name?url}Errors">E</a></#if><#if (stage.WarningCount>0)>|<a href="Build?submissionid=${submission.submissionId?url}#${stage.name}Warnings">W</a></#if>]
</#list>
</div>
</#macro>

<div class="content">
    <!-- Build log for a single submission -->
    <br><b>Site Name: </b>${client.site?html}
    <br><b>Build Name: </b>${client.buildName?html}
    <br><b>Time: </b>${submission.timeStamp?datetime?html}
    <br><b>Track: </b>${submission.type?html}

    <#if submission.lastSubmissionId?exists>
    <#assign l = submission.lastSubmission>
    <p><b>Last submission: </b><a href="Build?submissionid=${l.getSubmissionId()}">${l.timeStamp?datetime?html}</a>
    </#if>
    <#if submission.nextSubmissionId?exists>
    <#assign n = submission.nextSubmission>
    <p><b>Next submission: </b><a href="Build?submissionid=${n.getSubmissionId()}">${n.timeStamp?datetime?html}</a>
    </#if>

    <#assign build = submission.selectTestList ( ".Build" ).toList()/>
    <#if build?size != 1>
      <br><h3>No build information available</h3></br>
    <#else>
      <#assign build = build[0] />
      <#assign errorcount = build.selectResult ( "ErrorCount" ).toList()[0] />
      <#assign warningcount = build.selectResult ( "WarningCount" ).toList()[0] />

      <#-- Navigation list -->
      <br/><br/>
      <#assign stages=build.selectChildren().toList()?sort_by("qualifiedName")>
      <table class="dart">
      <tr class="table-heading">
        <th>Stage</th><th>Errors</th><th>Warnings</th>
      </tr>
      <#assign row=1/>
      <#list stages as stage>
       <#if row % 2 == 1>
        <tr class="tr-odd">
       <#else>
        <tr class="tr-even">
       </#if>
       <#assign row = row + 1/>
        <td><a href="#${stage.name}"><b>${stage.StageName}</b></a></td>
        <td align="right"><b><#if (stage.ErrorCount>0)><a href="#${stage.name}Errors">${stage.ErrorCount}</a><#else>0</#if></b></td>
        <td align="right"><b><#if (stage.WarningCount>0)><a href="#${stage.name}Warnings">${stage.WarningCount}</a><#else>0</#if></b></td>
       </tr>
      </#list>
      </table>
      <br/>

      <#-- For each stage -->
      <#list stages as stage>
        <div class="title-divider" id="${stage.name?url}">
          <@displayStageNav/>
          Stage: ${stage.StageName?html} (${stage.getResultValue("ErrorCount","Unknown")?html} errors, ${stage.getResultValue("WarningCount","Unknown")?html} warnings)
        </div>
        <br><b>Build command: </b><tt>${stage.getResultValue("BuildCommand","(Unknown)")?html}</tt>
        <br><b>Build return status: </b>${stage.getResultValue("BuildStatus","(Unknown)")?html}
        <br><b>Start Time: </b>${stage.getResultValue("StartDateTime","(Unknown)")?html}
        <br><b>End Time: </b>${stage.getResultValue("EndDateTime","(Unknown)")?html}

        <#assign logs = stage.selectResult("Log").toList()>
        <#list logs as log>
        <br><b>Log: </b>
        <#switch log.getType()>
        <#case "text/text"><pre>${fetchdata(log.getValue())?html}</pre><#break>
        <#case "text/html">${fetchdata(log.getValue())}<#break>
        <#case "text/xml"><pre>${fetchdata(log.getValue())?html}</pre><#break>
        <#case "archive/zip"><a href="/${projectName}/Zip/${log.getValue()?replace('\\','/')}"/>link</a><#break>
        <#default><pre>${log.getValue()?html}</pre><#break/>
        </#switch>
        </#list>
        <br/>
        <br/>

        <#-- iterate over errors -->
        <#if (stage.ErrorCount > 0)>
        <div class="title-divider" id="${stage.name?url}Errors"><@displayStageNav/>${stage.StageName?html} Errors (${stage.ErrorCount?html})</div>
        <#list submission.selectTestListLike(stage.qualifiedName+".Error%").toList() as test>        
           <h3>Error Build Log Line ${test.getResultValue ( "BuildLogLine", "Unknown" )?html}</h3>
           File: <b> ${test.getResultValue ( "SourceFile", "Unknown"  )?html}</b>
           Line: <b> ${test.getResultValue ( "SourceLineNumber", "Unknown" )?html}</b>p
           <pre>${test.getResultValue ( "PreContext", "" )}
<b>${test.getResultValue ( "Text", "" )}</b>
${test.getResultValue ( "PostContext", "" )}</pre>
        </#list>
        </#if>

        <#-- iterate over warnings -->
        <#if (stage.WarningCount > 0)>
        <div class="title-divider" id="${stage.name?url}Warnings"><@displayStageNav/>${stage.StageName?html} Warnings (${stage.WarningCount?html})</div>
        <#list submission.selectTestListLike(stage.qualifiedName+".Warning%").toList() as test>        
           <h3>Warning Build Log Line ${test.getResultValue ( "BuildLogLine", "Unknown" )?html}</h3>
           File: <b> ${test.getResultValue ( "SourceFile", "Unknown"  )?html}</b>
           Line: <b> ${test.getResultValue ( "SourceLineNumber", "Unknown" )?html}</b>
           <pre>${test.getResultValue ( "PreContext", "" )}
<b>${test.getResultValue ( "Text", "" )}</b>
${test.getResultValue ( "PostContext", "" )}</pre>
        </#list>
        </#if>
    
      </#list>

    </#if>
</div>
</body>
</html>

