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
<body>

<a name="pagetop"></a>
<table border="0" cellpadding="0" cellspacing="2" width="100%">
<tr>
<td align="center" valign="middle" height="100%"><a href="/${projectName}/Dashboard/?trackid=${submission.trackId?url}"><img alt="Logo/Homepage link" src="/${projectName}/Resources/Icons/Logo.png"></a>
</td>
<td align="left" width="100%" class="title">
<h2>${projectName?html} Build - ${client.site?html} - ${client.buildName?html} - ${submission.type?html}</h2><h3>${submission.timeStamp?datetime?html}</h3>
<@displayMenu />
</td></tr>
</table>

<br/>

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

      <br><h3>${errorcount.getValue()?html} Errors, ${warningcount.getValue()?html} Warnings total</h3>

      <#assign stages = build.selectChildren().toArray()/>

      <table cellspacing=3 cellpadding=1>
         <#list stages as stage >
           <#assign stagename = stage.selectResult ( "StageName" ).toList()[0].getValue() />
           <#assign errorcount = stage.selectResult ( "ErrorCount" ) />
           <#assign warningcount = stage.selectResult ( "WarningCount" ) />
           <tr><td valign=top><#if stage_index==0>Stages:</#if></td>
           <td valign=top>
             <a href="#stage${stage_index}">${stagename}</a></td><td>
 (<@displaySingleResult value=errorcount/> errors, <@displaySingleResult value=warningcount/> warnings)</td></tr>
           </#list>
      </table>

      <#list stages as stage >
        <#assign stagename = stage.selectResult ( "StageName" ).toList()[0].getValue() />
        <#assign errorcount = stage.selectResult ( "ErrorCount" ).toList()[0] />
        <#assign warningcount = stage.selectResult ( "WarningCount" ).toList()[0] />
        <#assign log = stage.selectResult ( "Log" ).toList() />

        <hr/>
        <h2>
          <a name="stage${stage_index}">
           Stage: ${stagename?html} (${errorcount.getValue()?html} Errors, ${warningcount.getValue()?html} Warnings)
          </a>
        </h2>
        <font size=-1><a href="#pagetop">[top]</a></font>

        <br><b>Start time:</b> <@displaySingleResult value=stage.selectResult("StartDateTime")/>
        <br><b>End time:</b> <@displaySingleResult value=stage.selectResult("EndDateTime")/>
        <br><b>Command:</b> <tt><@displaySingleResult value=stage.selectResult("BuildCommand")/></tt>
        <br><b>Return status:</b> <@displaySingleResult value=stage.selectResult("BuildStatus")/>
    
        <#if log?size == 1>
          <h3>Log</h3>
<pre>
${log[0].getValue()?html}
</pre>
        <#else>
          <#assign children = stage.selectChildren().toArray()?sort/>
          <#list children as child >
            <hr/>
            <h3><#if child.name?matches ( ".*Error.*" )> Error <#else> Warning </#if>Build Log Line ${child.getResultValue ( "BuildLogLine", "Unknown" )?html}</h3>
            <br/>
        File: <b> ${child.getResultValue ( "SourceFile", "Unknown"  )?html}</b>
        Line: <b> ${child.getResultValue ( "SourceLineNumber", "Unknown" )?html}</b>
        <pre>${child.getResultValue ( "PreContext", "" )}
<b>${child.getResultValue ( "Text", "" )}</b>
${child.getResultValue ( "PostContext", "" )}</pre>
          </#list>
        </#if>
      </#list>
    </#if>
</div>
</body>
</html>

