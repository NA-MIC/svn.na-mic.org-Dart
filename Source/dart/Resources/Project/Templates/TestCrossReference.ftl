<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0//EN">
<#include "Macros.ftl"/>
<#assign testname = parameters["testname"][0]>
<html>
  <head>
    <meta name="robots" content="noindex,nofollow">
    <meta http-equiv="Content-Type"
      content="text/html; charset=iso-8859-1">
    <meta http-equiv="Content-Script-Type" content="text/javascript">
    <title>TestCrossReference - ${testname?html}</title>
    <link rel="stylesheet" href="/${projectName}/Resources/Style.css" type="text/css">
    <link rel="shortcut icon" href="/${projectName}/Resources/Icons/favicon.ico" type="image/x-icon" />
<!--[if IE]>
    <script language="javascript" src="/${projectName}/Resources/cssMenuHelper.js" type="text/javascript"></script>
<![endif]-->
  </head>
  <body bgcolor="#ffffff">

<@displayLogin />
<table border="0" cellpadding="0" cellspacing="2" width="100%">
<tr>
<td align="center" valign="middle"><a href="/${projectName}/Dashboard/?trackid=${parameters.trackid[0]?url}"><img alt="Logo/Homepage link" src="/${projectName}/Resources/Icons/Logo.png"></a>
</td>
<td align="left" width="100%" class="title">
<h2>${projectName?html} TestCrossReference - ${testname?html}</h2>
<h3>${date?date?html}</h3>
<@displayMenu />
</td>

</tr>
</table>

<br/>

<#-- determine the sorting key -->
<#assign sortByKey="buildName"/>
<#assign order="ascending"/>
<#assign reverseOrder="descending"/>
<#assign orderIcon="/${projectName}/Resources/Icons/UpBlack.gif"/>
<#if parameters.sortBy?exists && parameters.sortBy[0] == "site">
  <#assign sortByKey="site"/>
<#elseif parameters.sortBy?exists && parameters.sortBy[0] == "name">
  <#assign sortByKey="buildName"/>
<#elseif parameters.sortBy?exists && parameters.sortBy[0] == "time">
  <#assign sortByKey="executionTime"/>
<#elseif parameters.sortBy?exists && parameters.sortBy[0] == "timestamp">
  <#assign sortByKey="timeStamp"/>
<#elseif parameters.sortBy?exists && parameters.sortBy[0] == "status">
  <#assign sortByKey="status"/>
<#elseif parameters.sortBy?exists && parameters.sortBy[0] == "detail">
  <#assign sortByKey="completionStatus"/>
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


    <#if parameters.sortBy?exists>
      <#assign sortKey = parameters.sortBy[0]/>
    <#else>
      <#assign sortKey = "name"/>
    </#if>

<div class="content">
    <!-- create a form and cache current url parameters-->
    <form method=Get enctype="application/x-www-form-urlencoded">
    <input type="hidden" name="trackid" value=${parameters.trackid[0]?url}>
    <input type="hidden" name="sortBy" value=${sortKey}>
    <input type="hidden" name="order" value=${order}>
    <input type="hidden" name="testname" value=${testname}>

    
<div class="left-content">
    <input type="submit" value="Chart times" caption="Chart execution times of selected tests" action="TestCatalog">

    <!-- All submissions, single test -->
    <table border="0" cellpadding="3" cellspacing="1" width="100%" bgcolor="#0000aa">
        <#assign row = 0/>

        <#list tracks?values as track>
        <#assign submissions = track.getSubmissionList()>
          <tr class="table-heading"><th colspan="7" align="left" valign="middle"><h4>
            <#if track.getLastTrackId()?exists>
               <a href="TestCrossReference?trackid=${track.getLastTrackId()}&testname=${testname}"><img alt="Last" src="/${projectName}/Resources/Icons/LeftBlack.gif" align="absmiddle"/></a>
            </#if>
            ${track.getName()} -- ${track.getStartTime()?datetime?html} to ${track.getEndTime()?datetime?html}
            <#if track.getNextTrackId()?exists>
               <a href="TestCrossReference?trackid=${track.getNextTrackId()}&testname=${testname}"><img alt="Next" src="/${projectName}/Resources/Icons/RightBlack.gif" align="absmiddle"/></a>
            </#if>
          </h4></th></tr>
        <#if submissions.toList()?size gt 0>
        <tr class="table-heading">
          <th>Select</th>
          <#if sortByKey=="site">
            <th class="sort-key"><a href="TestCrossReference?trackid=${track.trackId?url}&sortBy=site&order=${reverseOrder}&testname=${testname?url}">Site</a> &nbsp;&nbsp;&nbsp;<img src="${orderIcon}"></th>
          <#else>
            <th><a href="TestCrossReference?trackid=${track.trackId?url}&sortBy=site&order=ascending&testname=${testname?url}">Site</a></th>
          </#if>
          <#if sortByKey=="buildName">
            <th class="sort-key"><a href="TestCrossReference?trackid=${track.trackId?url}&sortBy=name&order=${reverseOrder}&testname=${testname?url}">Build Name</a> &nbsp;&nbsp;&nbsp;<img src="${orderIcon}"></th>
          <#else>
            <th><a href="TestCrossReference?trackid=${track.trackId?url}&sortBy=name&order=ascending&testname=${testname?url}">Build Name</a></th>
          </#if>
          <#if sortByKey=="timeStamp">
            <th class="sort-key"><a href="TestCrossReference?trackid=${track.trackId?url}&sortBy=timestamp&order=${reverseOrder}&testname=${testname?url}">Build Stamp</a> &nbsp;&nbsp;&nbsp;<img src="${orderIcon}"></th>
          <#else>
            <th><a href="TestCrossReference?trackid=${track.trackId?url}&sortBy=timestamp&order=ascending&testname=${testname?url}">Build Stamp</a></th>
          </#if>
<!--- Can't sort on these columns yet 
          <#if sortByKey=="status">
            <th class="sort-key"><a href="TestCrossReference?trackid=${track.trackId?url}&sortBy=status&order=${reverseOrder}&testname=${testname?url}">Status</a> &nbsp;&nbsp;&nbsp;<img src="${orderIcon}"></th>
          <#else>
            <th><a href="TestCrossReference?trackid=${track.trackId?url}&sortBy=status&order=ascending&testname=${testname?url}">Status</a></th>
          </#if>
          <#if sortByKey=="executionTime">
            <th class="sort-key"><a href="TestCrossReference?trackid=${track.trackId?url}&sortBy=time&order=${reverseOrder}&testname=${testname?url}">Time</a> &nbsp;&nbsp;&nbsp;<img src="${orderIcon}"></th>
          <#else>
            <th><a href="TestCrossReference?trackid=${track.trackId?url}&sortBy=time&order=ascending&testname=${testname?url}">Time</a></th>
          </#if>
          <#if sortByKey=="completionStatus">
            <th class="sort-key"><a href="TestCrossReference?trackid=${track.trackId?url}&sortBy=detail&order=${reverseOrder}&testname=${testname?url}">Detail</a> &nbsp;&nbsp;&nbsp;<img src="${orderIcon}"></th>
          <#else>
            <th><a href="TestCrossReference?trackid=${track.trackId?url}&sortBy=detail&order=ascending&testname=${testname?url}">Detail</a></th>
          </#if>
-->
          <th>Status</th>
          <th>Time</th>
          <th>Detail</th>
        </tr>

        </tr>
        </#if>

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
        <#assign client = submission.getClientEntity()>
        <#if row % 2 == 1>
        <tr class="tr-odd">
        <#else>
        <tr class="tr-even">
        </#if>
        <#assign row = row + 1/>
        <td align="center"><input type="checkbox" name="submissionid" value="${submission.getSubmissionId()?url}"></td>          
        <td>${client.getSite()?html}</td>
        <td><a href="TestCatalog?submissionid=${submission.getSubmissionId()?url}">${client.getBuildName()?html}</a>
        </td>
        <td>${submission.getTimeStamp()?datetime?html}</td>
        <#-- The server enforces unique Test names in the DB during
        the subission.  This list will only contain 0 or 1 Tests. -->
        <#assign tests = submission.selectTestList ( testname ).toList()/>
        <#if tests?size == 1>
            <#assign test = tests[0]>
            <#switch test.getStatus()>
            <#case "m">
              <#if test.failedSubTests + test.notRunSubTests gt 0>
                <td align="center" class="warning">
              <#else>
                <td align="center" class="pass">
              </#if>
              <#break>
            <#case "p"><td align="center" class="pass"><#break>
            <#case "f"><td align="center" class="warning"><#break>
            <#case "n"><td align="center" class="nr"><#break>
            </#switch>
            <a href="Test?testname=${testname?url}&submissionid=${submission.getSubmissionId()?url}">
            <#switch test.getStatus()>
            <#case "p">Passed<#break>
            <#case "f">Failed<#break>
            <#case "n">Not Run<#break>
            <#case "m"><b>${test.passedSubTests}</b> passed, <b>${test.failedSubTests}</b> failed, <b>${test.notRunSubTests}</b> not run<#break>

            </#switch>
            </a></td>
            <td><#if (test.executionTime >= 0)>${test.executionTime?string("#0.000")}</#if></td>
            <td>${test.completionStatus}</td>
        <#else>
          <#-- not enough tests in thelist -->
          <td class="na">NA</td>
          <td></td>
          <td></td>
        </#if>
        </tr>
          </#list>
          </#list>
      </table>

      <input type="submit" value="Chart times" caption="Chart execution times of selected tests" action="TestCatalog">
</div>


<div class="right-content">
    <#if parameters.submissionid?exists>
    <#assign submissionidParameters=""/>
    <#list parameters.submissionid as id>
    <#assign submissionidParameters=submissionidParameters+"&submissionid="+id/>
    </#list>
    <br>
    <img class="chart" src="/${projectName}/Chart?type=time&history=30&title=${testname?url}&xlabel=Date&ylabel=Time&legend=submission&width=400&height=300&testname=${testname?url}${submissionidParameters}">
    </#if>
</div>

      </form>
</div>
</body>
</html>

