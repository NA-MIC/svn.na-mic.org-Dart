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
    <title>Submission - ${site?html} - ${buildname?html} - ${trackname?html} - ${date?datetime?html}</title>
    <#else>
    <title>Submission - no submission specified</title>
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
<#if submission?exists>
  <a href="/${projectName}/Dashboard/?trackid=${submission.trackId?url}">
<#else>
  <a href="/${projectName}/Dashboard/">
</#if>
<img alt="Logo/Homepage link" src="/${projectName}/Resources/Icons/Logo.png"></a>
</td>
<td align="left" width="100%" class="title">
<#if submission?exists>
<h2>${projectName?html} Submission - ${site?html} - ${buildname?html} - ${trackname?html}</h2>
<#else>
<h2>${projectName?html} Submission - no submission specified</h2>
</#if>
<h3>${date?datetime?string("long")?html}</h3>
<@displayMenu />
</td>

</tr>
</table>

<br>

<div class="content">
<#if submission?exists>

<#assign submissionid=submission.submissionId/>

<#if submission.lastSubmissionId?exists>
<#assign l = submission.lastSubmission>
<p><b>Last submission: </b><a href="Submission?submissionid=${l.getSubmissionId()}">${l.timeStamp?datetime?html}</a>
</#if>
<#if submission.nextSubmissionId?exists>
<#assign n = submission.nextSubmission>
<p><b>Next submission: </b><a href="Submission?submissionid=${n.getSubmissionId()}">${n.timeStamp?datetime?html}</a>
</#if>

<div class="left-content">
<table class="dart" width="100%">
  <#-- Generator -->
  <tr class="table-heading">
     <td colspan="2" valign="middle">
     <h3>Generator</h3>
     </td>
   <tr class="tr-odd">
     <td><b>Generator</b></td>
      <td align="right"><#if submission.generator?has_content><b>${submission.generator?html}</b></#if></td>
   </tr>
  <#-- Notes -->
  <tr class="table-heading">
     <td colspan="2" valign="middle">
     <h3>Notes</h3>
     </td>
   <tr class="tr-odd">
     <td><b>Notes</b></td>
      <td align="right"><#if (submission.noteCount > 0)><b><a href="Note?submissionid=${submissionid}">${submission.noteCount}</a></b></#if></td>
   </tr>
  <#-- Update -- >
  <!-- Table heading for track -->
  <tr class="table-heading">
    <td colspan="2" valign="middle">
     <h3>Update
     </h3>
    </td>
  </tr>
  <tr class="tr-odd">
     <td><b>Updated files</b></td>
     <#assign updatecount=submission.updateCount/>
     <td align="right"><#if (updatecount >= 0)><b><a href="Update?submissionid=${submissionid}">${updatecount?html}</a></b></#if></td>
  </tr>

  <#-- Configure -- >
  <tr class="table-heading">
    <td colspan="2" valign="middle">
     <h3>Config
     </h3>
    </td>
  </tr>
  <tr class="tr-odd">
     <td><b>Config errors</b></td>
     <#assign configcount=submission.configCount/>
     <td align="right"><#if (configcount >= 0)><b><a href="Config?submission=${submissionid}">${configcount?html}</a></b></#if></td>
  </tr>

  <#-- Build -- >
  <!-- Table heading for track -->
  <tr class="table-heading">
    <td colspan="2" valign="middle">
     <h3>Build      
     </h3>
    </td>
  </tr>
  <tr class="tr-odd">
     <td><b><a href="Submission?submissionid=${submissionid?url}&measurement=ErrorCount">Errors</a></b></td>
     <#assign errorcount=submission.errorCount/>
     <#if (errorcount > 0)>
       <td align="right" class="error"><b><a href="Build?submissionid=${submissionid}">${errorcount?html}</a></b></td>
     <#elseif (errorcount == 0)>
       <td align="right" class="normal"><b><a href="Build?submissionid=${submissionid}">${errorcount?html}</a></b></td>
     <#else>
       <td></td>
     </#if>
  </tr>
  <tr class="tr-odd">
     <td><b><a href="Submission?submissionid=${submissionid?url}&measurement=WarningCount">Warnings</a></b></td>
     <#assign warningcount=submission.warningCount/>
     <#if (warningcount > 0)>
       <td align="right" class="warning"><b><a href="Build?submissionid=${submissionid}">${warningcount?html}</a></b></td>
     <#elseif (warningcount == 0)>
       <td align="right" class="normal"><b><a href="Build?submissionid=${submissionid}">${warningcount?html}</a></b></td>
     <#else>
       <td></td>
     </#if>
  </tr>
  <tr class="tr-odd">
     <td><b>Time</b></td>
     <#assign elapsedbuildtime=submission.elapsedBuildTime/>
     <td align="right"><#if (elapsedbuildtime >= 0)>${elapsedbuildtime?string("#0.0")}</#if></td>
  </tr>

  <#-- Test -- >
  <!-- Table heading for track -->
  <tr class="table-heading">
    <td colspan="2" valign="middle">
     <h3>Test
     </h3>
    </td>
  </tr>
  <tr class="tr-odd">
     <td><b><a href="Submission?submissionid=${submissionid?url}&measurement=NotRunCount">Not Run</a></b></td>
     <#assign notruncount=submission.notRunCount/>
     <#if (notruncount > 0)>
       <td align="right" class="error"><b><a href="TestCatalog?submissionid=${submissionid}">${notruncount?html}</a></b></td>
     <#elseif (notruncount == 0)>
       <td align="right" class="normal"><b><a href="TestCatalog?submissionid=${submissionid}">${notruncount?html}</a></b></td>
     <#else>
        <td></td>
     </#if>
  </tr>
  <tr class="tr-odd">
     <td><b><a href="Submission?submissionid=${submissionid?url}&measurement=FailedCount">Failed</a></b></td>   
     <#assign failedcount=submission.failedCount/>
     <#if (failedcount > 0)>
        <td align="right" class="warning"><b><a href="TestCatalog?submissionid=${submissionid}">${failedcount?html}</a></b></td>
     <#elseif (failedcount == 0)>
        <td align="right" class="normal"><b><a href="TestCatalog?submissionid=${submissionid}">${failedcount?html}</a></b></td>
     <#else>
        <td></td>
     </#if>
  </tr>
  <tr class="tr-odd">
     <td><b><a href="Submission?submissionid=${submissionid?url}&measurement=PassedCount">Passed</a></b></td>  
     <#assign passedcount=submission.passedCount/>
     <#if (notruncount + failedcount > 0)>
        <td align="right" class="warning"><b><a href="TestCatalog?submissionid=${submissionid}">${passedcount?html}</a></b></td>
     <#elseif (notruncount + failedcount == 0)>
        <td align="right" class="normal"><b><a href="TestCatalog?submissionid=${submissionid}">${passedcount?html}</a></b></td>
     <#else>
        <td></td>
     </#if>
  </tr>
  <tr class="tr-odd">
     <td><b>Time</b></td>  
     <#assign elapsedtesttime=submission.elapsedTestTime/>
     <td align="right"><#if (elapsedtesttime >= 0)>${elapsedtesttime?string("#0.0")}</#if></td>
  </tr>

  <#-- Coverage -- >
  <!-- Table heading for track -->
  <tr class="table-heading">
    <td colspan="2" valign="middle">
     <h3>Coverage
     </h3>
    </td>
  </tr>

  <#if submission.selectTest( ".Coverage" )?exists>
  <#assign test = submission.selectTest( ".Coverage" )/>

  <#assign statusStyle = "pass">
  <#if test.PercentCoverage?string?has_content>
    <#assign percent = test.PercentCoverage?number>
    <#if percent < 0.70>
      <#assign statusStyle = "fail">
    </#if>
  </#if>

  <tr class="tr-odd">
     <td><b><a href="Submission?submissionid=${submissionid?url}&measurement=PercentCoverage">Percentage</a></b></td>
     <td align="right" class="${statusStyle}"><#if test.PercentCoverage?string?has_content><b><a href="CoverageCatalog?submissionid=${submissionid}">${test.PercentCoverage?string("#0.00")}</a></b></#if></td>
  </tr>
  <tr class="tr-odd">
     <td><b><a href="Submission?submissionid=${submissionid?url}&measurement=LOCTested">Lines Covered</a></b></td>
     <td align="right"><#if test.getResultValue ( "LOCTested", "" ) != ""><b><a href="CoverageCatalog?submissionid=${submissionid}">${test.LOCTested}</a></b></#if></td>
  </tr>
  <tr class="tr-odd">
     <td><b><a href="Submission?submissionid=${submissionid?url}&measurement=LOCUnTested">Lines not covered</a></b></td>
     <td align="right"><#if test.getResultValue ( "LOCUnTested", "" ) != "" ><b><a href="CoverageCatalog?submissionid=${submissionid}">${test.LOCUnTested}</a></b></#if></td>
  </tr>
  <tr class="tr-odd">
     <td><b><a href="Submission?submissionid=${submissionid?url}&measurement=FilesCovered">Files covered</a></b></td>
     <td align="right"><#if test.passedSubTests?string?has_content><b><a href="CoverageCatalog?submissionid=${submissionid}">${test.passedSubTests}</a></b></#if></td>
  </tr>
  <tr class="tr-odd">
     <td><b><a href="Submission?submissionid=${submissionid?url}&measurement=FilesNotCovered">Files not covered</b></td>
     <td align="right"><#if test.failedSubTests?string?has_content><b><a href="CoverageCatalog?submissionid=${submissionid}">${test.failedSubTests}</a></b></#if></td>
  </tr>
  <#else>
     <tr class="tr-odd">
        <td colspan="2"><b>No source code coverage information</b></td>
     </tr>
  </#if>

  <#-- Dynamic analysis -- >
  <!-- Table heading for track -->
  <tr class="table-heading">
    <td colspan="2" valign="middle">
     <h3>Dynamic Analysis
     </h3>
    </td>
  </tr>

  <#if submission.selectTest( ".DynamicAnalysis" )?exists>
  <#assign test = submission.selectTest( ".DynamicAnalysis" )/>

  <#assign statusStyle = "pass">
  <#assign numberOfDefects = 0>
  <#assign results = test.getResultList().toList()>
  <#list results as result>
     <#if result.getType()?contains("numeric/")>
       <#assign numberOfDefects = numberOfDefects + result.getValue()?number>
     </#if>
  </#list>
  <#if (numberOfDefects > 0)>
    <#assign statusStyle = "fail">
  </#if>

  <tr class="tr-odd">
  <td><b>Checker</b></td>
  <td></td>
  </tr>

  <tr class="tr-odd">
  <td><b>Defects</b></td>
  <td align="right" class=${statusStyle}><b><a href="DynamicAnalysisCatalog?submissionid=${submissionid}">${numberOfDefects}</a></b></td>
  </tr>
  <#else>
     <tr class="tr-odd">
        <td colspan="2"><b>No dynamic analysis information</b></td>
     </tr>
  </#if>
</table>
</div>

<div class="right-content">
<#assign measurement = "FailedCount">
<#assign testname="FailedSubTests">
<#if parameters.measurement?exists>
  <#assign measurement = parameters.measurement[0]>
  <#switch measurement>
  <#case "UpdateCount">
    <#assign testname=".Update.Update">
    <#break>
  <#case "WarningCount">
  <#case "ErrorCount">
    <#assign testname=".Build">
    <#break>
  <#case "NotRunCount">
    <#assign testname=".Test">
    <#assign measurement="NotRunSubTests">
    <#break>
  <#case "FailedCount">
    <#assign testname=".Test">
    <#assign measurement="FailedSubTests">
    <#break>
  <#case "PassedCount">
    <#assign testname=".Test">
    <#assign measurement="PassedSubTests">
    <#break>
  <#case "PercentCoverage">
  <#case "LOCTested">
  <#case "LOCUnTested">
    <#assign testname=".Coverage">
    <#break>
  <#case "FilesCovered">
    <#assign testname=".Coverage">
    <#assign measurement="PassedSubTests">
    <#break>
  <#case "FilesNotCovered">
    <#assign testname=".Coverage">
    <#assign measurement="FailedSubTests">
    <#break>
  <#-- Need total dynamic analysis defects to be rolled up to a result
  <#case "DefectsCount">
    <#assign testname=".DynamicAnalysis">
    <#break>
  -->
  </#switch>
  <img class="chart" src="/${projectName}/Chart?type=time&measurement=${measurement?url}&history=30&title=${submission.site?url}-${submission.buildName?url}-${submission.type?url}&xlabel=Date&ylabel=${measurement?url}&width=400&height=300&submissionid=${submission.submissionId?url}&testname=${testname}"><br><br>
</#if>    
</div>

<#else>
<#-- No submission specified -->
<p>Either a valid submission id must be specified or a submission must be specified with a valid tuple (site, buildname, track, timestamp). Submission results are not immediately available after transfering data to the Dart server. This page may be available in a few minutes.</p>
<hr>
<br>
<b>submissionid</b> = <#if parameters.submissionid?exists>${parameters.submissionid[0]}<#else>&lt;Unknown&gt;</#if><br><br>
<hr>
<br>
<b>site</b> = <#if parameters.site?exists>${parameters.site[0]}<#else>&lt;Unknown&gt;</#if>
<br>
<b>buildname</b> = <#if parameters.buildname?exists>${parameters.buildname[0]}<#else>&lt;Unknown&gt;</#if><br>
<b>track</b> = <#if parameters.track?exists>${parameters.track[0]}<#else>&lt;Unknown&gt;</#if><br>
<b>timestamp</b> = <#if parameters.timestamp?exists>${parameters.timestamp[0]}<#else>&lt;Unknown&gt;</#if><br>

</#if>
</div>
</body>
</html>

