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


      <!-- Generate the navigation menu -->
      <p>
      <ul>
      <#visit build.getTree("DisplayMenu")>
      </ul>
      </p>

      <#macro @DisplayMenu>
        <#local stagename = .node.getResultValue("StageName","NotAStage")>
        <#if stagename != "NotAStage" >
          <li>
           <a href="#stage${.node.name}">${stagename?html}</a> (${.node.getResultValue("ErrorCount","Unknown")?html} errors, ${.node.getResultValue("WarningCount","Unknown")?html} warnings)
           <ul><#recurse/></ul>
          </li>
        <#else>
          <#recurse/>
        </#if>
      </#macro>


      <!-- Generate the actual build error entries -->
      <#visit build.getTree("DisplayDetails")>

      <#macro @DisplayDetails>
        <#local iserror = .node.name?matches(".*Error[^.]*")>
        <#local iswarning = .node.name?matches(".*Warning[^.]*")>

        <#if iserror || iswarning>
          <#local child = .node>
          <hr/>
           <h3><#if iserror> Error <#else> Warning </#if>Build Log Line ${child.getResultValue ( "BuildLogLine", "Unknown" )?html}</h3>
           File: <b> ${child.getResultValue ( "SourceFile", "Unknown"  )?html}</b>
           Line: <b> ${child.getResultValue ( "SourceLineNumber", "Unknown" )?html}</b>
           <pre>${child.getResultValue ( "PreContext", "" )}
<b>${child.getResultValue ( "Text", "" )}</b>
${child.getResultValue ( "PostContext", "" )}</pre>
        <#else>
          <#local stagename = .node.getResultValue("StageName","NotAStage")>
          <#if stagename != "NotAStage" >
            <hr/>
            <h2>
              <a name="stage${.node.name}">
              Stage: ${stagename?html} (${.node.getResultValue("ErrorCount","Unknown")?html} Errors, ${.node.getResultValue("WarningCount","Unknown")?html} Warnings)
              </a>
            </h2>
            <br><b>Build command: </b><tt>${.node.getResultValue("BuildCommand","(Unknown)")?html}</tt>
            <br><b>Build return status: </b>${.node.getResultValue("BuildStatus","(Unknown)")?html}
            <br><b>Start Time: </b>${.node.getResultValue("StartDateTime","(Unknown)")?html}
            <br><b>End Time: </b>${.node.getResultValue("EndDateTime","(Unknown)")?html}
            <#local log = .node.getResultValue("Log","NotAvailable")>
            <#if log != "NotAvailable">
            <br><b>Log: </b><pre>${log?html}</pre>
            </#if>
          </#if>
        </#if>
        <#recurse/>
      </#macro>
    </#if>
</div>
</body>
</html>

