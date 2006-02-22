<#include "Macros.ftl"/>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0//EN">
<html>
  <head>
    <meta name="robots" content="noindex,nofollow">
    <meta http-equiv="Content-Type"
      content="text/html; charset=iso-8859-1">
    <meta http-equiv="Content-Script-Type" content="text/javascript">
    <title>Dashboard - ${date?datetime?html}</title>
    <link rel="stylesheet" href="/${projectName}/Resources/Style.css" type="text/css">
    <link rel="shortcut icon" href="/${projectName}/Resources/Icons/favicon.ico" type="image/x-icon" />
    <link rel="alternate" type="application/rss+xml" title="Dart(${projectName?url} Submission RSS Feed" href="SubmissionRSS.xml">
<!--[if IE]>
    <script language="javascript" src="/${projectName}/Resources/cssMenuHelper.js" type="text/javascript"></script>
<![endif]-->
  </head>
<body>


<@displayLogin />
<table border="0" cellpadding="0" cellspacing="2" width="100%">
<tr>
<td align="center" valign="middle" height="100%">
<#if parameters.trackid?exists>
  <a href="/${projectName}/Dashboard/?trackid=${parameters.trackid[0]?url}">
<#else>
  <a href="/${projectName}/Dashboard/">
</#if>
<img alt="Logo/Homepage link" src="/${projectName}/Resources/Icons/Logo.png"></a>
</td>
<td align="left" width="100%" class="title">
<h2>${projectName?html} Dashboard - ${date?datetime?html}</h2>
<h3>${date?date?html}</h3>
<@displayMenu />
<div align="right"><a href="SubmissionRSS.xml"><img class="icon" src="/${projectName?url}/Resources/Icons/feed-icon16x16.png"></a></div>

</td>
</tr>

</table>

<br>



<!-- determine the sorting key -->
<#assign sortByKey="buildName"/>
<#assign order="ascending"/>
<#assign reverseOrder="descending"/>
<#assign orderIcon="/${projectName}/Resources/Icons/UpBlack.gif"/>
<#if parameters.sortBy?exists && parameters.sortBy[0] == "site">
  <#assign sortByKey="site"/>
<#elseif parameters.sortBy?exists && parameters.sortBy[0] == "name">
  <#assign sortByKey="buildName"/>
<#elseif parameters.sortBy?exists && parameters.sortBy[0] == "update">
  <#assign sortByKey="updateCount"/>
<#elseif parameters.sortBy?exists && parameters.sortBy[0] == "config">
  <#assign sortByKey="configCount"/>
<#elseif parameters.sortBy?exists && parameters.sortBy[0] == "error">
  <#assign sortByKey="errorCount"/>
<#elseif parameters.sortBy?exists && parameters.sortBy[0] == "warning">
  <#assign sortByKey="warningCount"/>
<#elseif parameters.sortBy?exists && parameters.sortBy[0] == "elapsedbuildtime">
  <#assign sortByKey="elapsedBuildTime"/>
<#elseif parameters.sortBy?exists && parameters.sortBy[0] == "passed">
  <#assign sortByKey="passedCount"/>
<#elseif parameters.sortBy?exists && parameters.sortBy[0] == "failed">
  <#assign sortByKey="failedCount"/>
<#elseif parameters.sortBy?exists && parameters.sortBy[0] == "notrun">
  <#assign sortByKey="notRunCount"/>
<#elseif parameters.sortBy?exists && parameters.sortBy[0] == "elapsedtesttime">
  <#assign sortByKey="elapsedTestTime"/>
<#elseif parameters.sortBy?exists && parameters.sortBy[0] == "timestamp">
  <#assign sortByKey="timeStamp"/>
</#if>

<#if parameters.order?exists && parameters.order[0] == "ascending">
  <#assign order="ascending"/>
  <#assign reverseOrder="descending"/>
  <#assign orderIcon="/${projectName}/Resources/Icons/UpBlack.gif"/>
<#elseif parameters.order?exists && parameters.order[0] == "descending">
  <#assign order="descending"/>
  <#assign reverseOrder="ascending"/>
  <#assign orderIcon="/${projectName}/Resources/Icons/DownBlack.gif"/>
</#if>


<div class="content">
<!-- For each track, display a table with the submissions in that track -->
<#list tracks?values as track>
<#if !parameters.showtrack?exists || (parameters.showtrack?exists && parameters.showtrack?seq_contains(track.name) )>
<#assign submissions = track.getSubmissionList()>
    <table border="0" cellpadding="3" cellspacing="1" width="100%" bgcolor="#0000aa">
        <!-- Table heading for track -->
        <tr class="table-heading">
          <td colspan="13" valign="middle">
            <h3>
            <#if track.getLastTrackId()?exists>
               <a href="Dashboard?trackid=${track.getLastTrackId()}"><img alt="Last" src="/${projectName}/Resources/Icons/LeftBlack.gif" align="absmiddle"/></a>
            </#if>
            ${track.getName()} -- ${track.getStartTime()?datetime?html} to ${track.getEndTime()?datetime?html}

            <#if track.nextTrackId?exists>
               <a href="Dashboard?trackid=${track.nextTrackId}"><img alt="Last" src="/${projectName}/Resources/Icons/RightBlack.gif" align="absmiddle"/></a>
            </#if>
            </h3>
          </td>
        </tr>

        <!-- Columns to display for the track -->
        <tr class="table-heading">
          <#if sortByKey=="site">
            <th class="sort-key" align="center" rowspan="2"><a href="Dashboard?trackid=${track.trackId?url}&sortBy=site&order=${reverseOrder}">Site</a> &nbsp;&nbsp;&nbsp;<img src="${orderIcon}"></th>
          <#else>
            <th align="center" rowspan="2"><a href="Dashboard?trackid=${track.trackId?url}&sortBy=site&order=ascending">Site</a></th>
          </#if>
          <#if sortByKey=="buildName">
            <th class="sort-key" align="center" rowspan="2"><a href="Dashboard?trackid=${track.trackId?url}&sortBy=name&order=${reverseOrder}">Build Name</a> &nbsp;&nbsp;&nbsp;<img src="${orderIcon}"></th>
          <#else>
            <th align="center" rowspan="2"><a href="Dashboard?trackid=${track.trackId?url}&sortBy=name&order=ascending">Build Name</a></th>
          </#if>
          <#if sortByKey=="updateCount">
            <th class="sort-key" align="center" rowspan="2"><a href="Dashboard?trackid=${track.trackId?url}&sortBy=update&order=${reverseOrder}">Update</a> &nbsp;&nbsp;&nbsp;<img src="${orderIcon}"></th>
          <#else>
            <th align="center" rowspan="2"><a href="Dashboard?trackid=${track.trackId?url}&sortBy=update&order=ascending}">Update</a></th>
          </#if>
          <#if sortByKey=="configCount">
            <th class="sort-key" align="center" rowspan="2"><a href="Dashboard?trackid=${track.trackId?url}&sortBy=config&order=${reverseOrder}">Config</a> &nbsp;&nbsp;&nbsp;<img src="${orderIcon}"></th>
          <#else>
            <th align="center" rowspan="2"><a href="Dashboard?trackid=${track.trackId?url}&sortBy=config&order=ascending}">Config</a></th>
          </#if>
          <th align="center" colspan="3">Build</th>
          <th align="center" colspan="5">Test</th>
          <#if sortByKey=="timeStamp">
            <th align="center" class="sort-key" rowspan="2"><a href="Dashboard?trackid=${track.trackId?url}&sortBy=timestamp&order=${reverseOrder}">TimeStamp</a> &nbsp;&nbsp;&nbsp;<img src="${orderIcon}"></th>
          <#else>
            <th align="center" rowspan="2"><a href="Dashboard?trackid=${track.trackId?url}&sortBy=timestamp&order=ascending">TimeStamp</a></th>
          </#if>
        </tr>
        <tr class="table-heading">
          <#if sortByKey=="errorCount">
            <th class="sort-key" align="center"><a href="Dashboard?trackid=${track.trackId?url}&sortBy=error&order=${reverseOrder}">Error</a> &nbsp;&nbsp;&nbsp;<img src="${orderIcon}"></th>
          <#else>
            <th align="center"><a href="Dashboard?trackid=${track.trackId?url}&sortBy=error&order=descending">Error</a></th>
          </#if>
          <#if sortByKey=="warningCount">
            <th class="sort-key" align="center"><a href="Dashboard?trackid=${track.trackId?url}&sortBy=warning&order=${reverseOrder}">Warning</a> &nbsp;&nbsp;&nbsp;<img src="${orderIcon}"></th>
          <#else>
            <th align="center"><a href="Dashboard?trackid=${track.trackId?url}&sortBy=warning&order=descending">Warning</a></th>
          </#if>
          <#if sortByKey=="elapsedBuildTime">
            <th class="sort-key" align="center"><a href="Dashboard?trackid=${track.trackId?url}&sortBy=elapsedbuildtime&order=${reverseOrder}">Time</a> &nbsp;&nbsp;&nbsp;<img src="${orderIcon}"></th>
          <#else>
            <th align="center"><a href="Dashboard?trackid=${track.trackId?url}&sortBy=elapsedbuildtime&order=descending">Time</a></th>
          </#if>
          <#if sortByKey=="notRunCount">
            <th class="sort-key" align="center"><a href="Dashboard?trackid=${track.trackId?url}&sortBy=notrun&order=${reverseOrder}">NotRun</a> &nbsp;&nbsp;&nbsp;<img src="${orderIcon}"></th>
          <#else>
            <th align="center"><a href="Dashboard?trackid=${track.trackId?url}&sortBy=notrun&order=descending">NotRun</a></th>
          </#if>
          <#if sortByKey=="failedCount">
            <th class="sort-key" align="center"><a href="Dashboard?trackid=${track.trackId?url}&sortBy=failed&order=${reverseOrder}">Failed</a> &nbsp;&nbsp;&nbsp;<img src="${orderIcon}"></th>
          <#else>
            <th align="center"><a href="Dashboard?trackid=${track.trackId?url}&sortBy=failed&order=descending">Failed</a></th>
          </#if>
          <#if sortByKey=="passedCount">
            <th class="sort-key" align="center"><a href="Dashboard?trackid=${track.trackId?url}&sortBy=passed&order=${reverseOrder}">Passed</a> &nbsp;&nbsp;&nbsp;<img src="${orderIcon}"></th>
          <#else>
            <th align="center"><a href="Dashboard?trackid=${track.trackId?url}&sortBy=passed&order=descending">Passed</a></th>
          </#if>
          <th align="center">NA</th>
          <#if sortByKey=="elapsedTestTime">
            <th class="sort-key" align="center"><a href="Dashboard?trackid=${track.trackId?url}&sortBy=elapsedtesttime&order=${reverseOrder}">Time</a> &nbsp;&nbsp;&nbsp;<img src="${orderIcon}"></th>
          <#else>
            <th align="center"><a href="Dashboard?trackid=${track.trackId?url}&sortBy=elapsedtesttime&order=descending">Time</a></th>
          </#if>
        </tr>

      <#assign row = 1/>
      <#if (submissions.size() > 0)>
        <#if order="ascending">
          <#assign sortedSubmissions=submissions.toList()?sort_by(sortByKey)/>
        <#else>
          <#assign sortedSubmissions=submissions.toList()?sort_by(sortByKey)?reverse/>
        </#if>
      <#else>
        <#assign sortedSubmissions=submissions.toList()>
      </#if>


      <#list sortedSubmissions as submission>

        <#if row % 2 == 1>
          <tr class="tr-odd">
        <#else>
          <tr class="tr-even">
        </#if>
        <#assign row = row + 1/>
    
        <#assign submissionid = submission.submissionId/>
  
          <td><a href="Submission?submissionid=${submissionid}">${submission.site?replace(".", ".&shy;")}</a></td>
          <td><a href="Submission?submissionid=${submissionid}">${submission.buildName?replace(".", ".&shy;")}</a></td>
          
          <#assign updatecount=submission.updateCount/>
          <td align="center"><#if (updatecount >= 0)><b><a href="Update?submissionid=${submissionid}">${updatecount?html}</a></b></#if></td>

          <#assign configcount=submission.configCount/>
          <td align="center"><#if (configcount >= 0)><b><a href="Config?submission=${submissionid}">${configcount?html}</a></b></#if></td>

          <#assign errorcount=submission.errorCount/>
          <#if (errorcount > 0)>
            <td align="center" class="error"><b><a href="Build?submissionid=${submissionid}">${errorcount?html}</a></b></td>
          <#elseif (errorcount == 0)>
            <td align="center" class="normal"><b><a href="Build?submissionid=${submissionid}">${errorcount?html}</a></b></td>
          <#else>
            <td></td>
          </#if>

          <#assign warningcount=submission.warningCount/>
          <#if (warningcount > 0)>
            <td align="center" class="warning"><b><a href="Build?submissionid=${submissionid}">${warningcount?html}</a></b></td>
          <#elseif (warningcount == 0)>
            <td align="center" class="normal"><b><a href="Build?submissionid=${submissionid}">${warningcount?html}</a></b></td>
          <#else>
            <td></td>
          </#if>

          <#assign elapsedbuildtime=submission.elapsedBuildTime/>
          <td align="right"><#if (elapsedbuildtime >= 0)>${elapsedbuildtime?string("#0.0")}</#if></td>

          <#assign notruncount=submission.notRunCount/>
          <#if (notruncount > 0)>
             <td align="center" class="error"><b><a href="TestCatalog?submissionid=${submissionid}">${notruncount?html}</a></b></td>
          <#elseif (notruncount == 0)>
             <td align="center" class="normal"><b><a href="TestCatalog?submissionid=${submissionid}">${notruncount?html}</a></b></td>
          <#else>
             <td></td>
          </#if>

          <#assign failedcount=submission.failedCount/>
          <#if (failedcount > 0)>
             <td align="center" class="warning"><b><a href="TestCatalog?submissionid=${submissionid}">${failedcount?html}</a></b></td>
          <#elseif (failedcount == 0)>
             <td align="center" class="normal"><b><a href="TestCatalog?submissionid=${submissionid}">${failedcount?html}</a></b></td>
          <#else>
             <td></td>
          </#if>

          <#assign passedcount=submission.passedCount/>
          <#if (notruncount + failedcount > 0)>
             <td align="center" class="warning"><b><a href="TestCatalog?submissionid=${submissionid}">${passedcount?html}</a></b></td>
          <#elseif (notruncount + failedcount == 0)>
             <td align="center" class="normal"><b><a href="TestCatalog?submissionid=${submissionid}">${passedcount?html}</a></b></td>
          <#else>
             <td></td>
          </#if>
          <td></td>

          <#assign elapsedtesttime=submission.elapsedTestTime/>
          <td align="right"><#if (elapsedtesttime >= 0)>${elapsedtesttime?html}</#if></td>
        
          <td>${submission.getTimeStamp()?html}</td>

        </tr>
        </#list>
    </table><br>
    </#if>
    </#list>


<#-- check to determine if we have Coverage, Style, or DynamicAnalysis -->
<#assign hasCoverage=false>
<#assign hasStyle=false>
<#assign hasDynamicAnalysis=false>
<#list tracks?values as track>
  <#assign submissions = track.getSubmissionList()>
  <#list submissions.toList() as submission>
     <#assign submissionid = submission.submissionId/>
     <#if !hasCoverage && submission.selectTest( ".Coverage" )?exists>
        <#assign hasCoverage=true>
     </#if>
     <#if !hasStyle && submission.selectTest( ".Style" )?exists>
        <#assign hasStyle=true>
     </#if>
     <#if !hasDynamicAnalysis && submission.selectTest( ".DynamicAnalysis" )?exists>
        <#assign hasDynamicAnalysis=true>
     </#if>
  </#list>
</#list>


<!-- Make a track for coverage -->
<#if !parameters.showtrack?exists || (parameters.showtrack?exists && parameters.showtrack?seq_contains("Coverage") )>
<#-- Check to see if any coverage submissions are available, if not skip this section -->
<#if hasCoverage>
    <table border="0" cellpadding="3" cellspacing="1" width="100%" bgcolor="#0000aa">
        <!-- Table heading for track -->
        <tr class="table-heading">
          <td colspan="8" valign="middle">
            <h3>Coverage</h3>
          </td>
        </tr>
        <tr class="table-heading">
        <th>Site</th>
        <th>Build Name</th>
        <th>Percentage</th>
        <th>Lines covered</th>
        <th>Lines not covered</th>
        <th>Files covered</th>
        <th>Files not covered</th>
        <th>TimeStamp</th>
        </tr>

      <#assign row = 1/>
      <#list tracks?values as track>
        <#assign submissions = track.getSubmissionList()>
        <#list submissions.toList() as submission>
          <#assign submissionid = submission.submissionId/>
          <#if submission.selectTest( ".Coverage" )?exists>
          <#assign test = submission.selectTest( ".Coverage" )/>
            <#if row % 2 == 1>
              <tr class="tr-odd">
            <#else>
              <tr class="tr-even">
            </#if>
            <#assign row = row + 1/>

            <#assign statusStyle = "pass">

            <#if test.getResultValue ( "PercentCoverage", "" ) != "" && test.PercentCoverage?string?has_content>
              <#assign percent = test.PercentCoverage?number>
              <#if percent < 0.70>
                <#assign statusStyle = "fail">
              </#if>
            </#if>

              <td><a href="Submission?submissionid=${submissionid}">${submission.site?replace(".", ".&shy;")}</a></td>
              <td><a href="Submission?submissionid=${submissionid}">${submission.buildName?replace(".", ".&shy;")}</a></td>
              <td align="center" class="${statusStyle}">
                <#if test.getResultValue ( "PercentCoverage", "" ) != "" && test.PercentCoverage?string?has_content><b>
                  <a
href="CoverageCatalog?submissionid=${submissionid}">${test.PercentCoverage?string("#0.00")}</a></b>
                </#if></td>
              <td align="center"><#if test.getResultValue ( "LOCTested", "" ) != ""><b><a href="CoverageCatalog?submissionid=${submissionid}">${test.LOCTested}</a></b></#if></td>
              <td align="center"><#if test.getResultValue ( "LOCUnTested", "" ) != ""><b><a href="CoverageCatalog?submissionid=${submissionid}">${test.LOCUnTested}</a></b></#if></td>
              <td align="center"><#if test.passedSubTests?string?has_content><b><a href="CoverageCatalog?submissionid=${submissionid}">${test.passedSubTests}</a></b></#if></td>
              <td align="center"><#if test.failedSubTests?string?has_content><b><a href="CoverageCatalog?submissionid=${submissionid}">${test.failedSubTests}</a></b></#if></td>

              <td>${submission.getTimeStamp()?html}</td>
            </tr>
          </#if>
        </#list>
      </#list>


    </table><br>
</#if>
</#if>

<!-- Make a track for style -->
<#if !parameters.showtrack?exists || (parameters.showtrack?exists && parameters.showtrack?seq_contains("Style") )>
<#if hasStyle>
    <table border="0" cellpadding="3" cellspacing="1" width="100%" bgcolor="#0000aa">
        <!-- Table heading for track -->
        <tr class="table-heading">
          <td colspan="8" valign="middle">
            <h3>Style</h3>
          </td>
        </tr>
        <tr class="table-heading">
        <th>Site</th>
        <th>Build Name</th>
        <th>Files checked</th>
        <th>Violations</th>
        <th>TimeStamp</th>
        </tr>

      <#assign row = 1/>
      <#list tracks?values as track>
        <#assign submissions = track.getSubmissionList()>
        <#list submissions.toList() as submission>
          <#assign submissionid = submission.submissionId/>
          <#if submission.selectTest( ".Style" )?exists>
          <#assign test = submission.selectTest( ".Style" )/>
            <#if row % 2 == 1>
              <tr class="tr-odd">
            <#else>
              <tr class="tr-even">
            </#if>
            <#assign row = row + 1/>

            <#assign statusStyle = "pass">

              <td><a href="Submission?submissionid=${submissionid}">${submission.site?replace(".", ".&shy;")}</a></td>
              <td><a href="Submission?submissionid=${submissionid}">${submission.buildName?replace(".", ".&shy;")}</a></td>
              <td align="center" class="${statusStyle}">
                <#if test.getResultValue ( "FilesChecked", "" ) != "" && test.FilesChecked?string?has_content>
                  <a href="/${projectName}/Zip/${test.getResultValue( "Log", "" )?replace('\\','/')}"/>${test.FilesChecked}</a></b>
                </#if>
              </td>
              <#if test.Violations?number != 0>
                <#assign statusStyle = "fail"/>
              </#if>
              <td align="center" class="${statusStyle}">
                <#if test.getResultValue ( "Violations", "" ) != "" && test.Violations?string?has_content>
                  <a href="/${projectName}/Zip/${test.getResultValue( "Log", "" )?replace('\\','/')}"/>${test.Violations}</a></b>
                </#if>
              </td>
              <td>${submission.getTimeStamp()?html}</td>
            </tr>
          </#if>
        </#list>
      </#list>
    </table><br>
</#if>
</#if>



<!-- Make a track for dynamic analysis -->
<#if !parameters.showtrack?exists || (parameters.showtrack?exists && parameters.showtrack?seq_contains("DynamicAnalysis") )>
<#-- Check to see if any dynamic analysis submissions are available, if not skip this section -->
<#if hasDynamicAnalysis>
    <table border="0" cellpadding="3" cellspacing="1" width="100%" bgcolor="#0000aa">
        <!-- Table heading for track -->
        <tr class="table-heading">
          <td colspan="5" valign="middle">
            <h3>Dynamic Analysis</h3>
          </td>
        </tr>
        <tr class="table-heading">
        <th>Site</th>
        <th>Build Name</th>
        <th>Checker</th>
        <th>Defects</th>
        <th>TimeStamp</th>
        </tr>

      <#assign row = 1/>
      <#list tracks?values as track>
        <#assign submissions = track.getSubmissionList()>
        <#list submissions.toList() as submission>
          <#assign submissionid = submission.submissionId/>
          <#if submission.selectTest( ".DynamicAnalysis" )?exists>
          <#assign test = submission.selectTest( ".DynamicAnalysis" )/>
            <#if row % 2 == 1>
              <tr class="tr-odd">
            <#else>
              <tr class="tr-even">
            </#if>
            <#assign row = row + 1/>

            <#assign statusStyle = "pass">

            <td><a href="Submission?submissionid=${submissionid}">${submission.site?replace(".", ".&shy;")}</a></td>
            <td><a href="Submission?submissionid=${submissionid}">${submission.buildName?replace(".", ".&shy;")}</a></td>
            <!-- Checker -->
            <td></td>

            <!-- Rollup of defects -->
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
            <td align="center" class=${statusStyle}><b><a href="DynamicAnalysisCatalog?submissionid=${submissionid}">${numberOfDefects}</a></b></td>
            <td align="center">${submission.getTimeStamp()?html}</td>
            </tr>
          </#if>
        </#list>
        </#list>
    </table><br>
</#if>
</#if>
</div>

  </body>
</html>

