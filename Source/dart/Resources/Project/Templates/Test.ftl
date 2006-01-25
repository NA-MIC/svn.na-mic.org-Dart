<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0//EN">
<#include "Macros.ftl"/>
<#assign testname = parameters["testname"][0]>
<#assign client = submission.getClientEntity()/>
<html>
  <head>
    <meta name="robots" content="noindex,nofollow">
    <meta http-equiv="Content-Type"
      content="text/html; charset=iso-8859-1">
    <meta http-equiv="Content-Script-Type" content="text/javascript">
    <title>Test - ${testname?html} - ${client.getSite()?html} - ${client.getBuildName()?html} - ${submission.type?html} - ${submission.timeStamp?datetime?html}</title>
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
<td align="center"><a href="/${projectName}/Dashboard/?trackid=${submission.trackId?url}"><img alt="Logo/Homepage link" src="/${projectName}/Resources/Icons/Logo.png"></a>
</td>
<td align="left" width="100%" class="title">
<h2>${projectName?html} Test - ${testname?html} - ${client.site?html} - ${client.buildName?html} - ${submission.type?html}</h2>
<h3>${submission.timeStamp?datetime?html}</h3>
<@displayMenu />
</td>

</tr>
</table>

<br/>

<div class="content">
<!-- Single submission, single tests -->
      <#assign test = submission.selectTest ( testname )/>
    <b>Site Name: </b><a href="TestCatalog?submissionid=${submission.submissionId}">${client.getSite()?html}</a><br>
    <b>Build Name: </b>${client.getBuildName()?html}<br>
      <#switch test.getStatus()>
      <#case "p"><#assign color="pass"/><#assign status="Passed"/><#break>
      <#case "f"><#assign color="fail"/><#assign status="Failed"/><#break>
      <#case "n"><#assign color="nr"/><#assign status="Not Run"/><#break>
      <#case "m"><#assign color="pass"/><#assign status="Meta Test"/><#break>
      </#switch>
    <b>Test Name: </b><a href="TestCrossReference?trackid=${submission.trackId?url}&testname=${testname?url}">${testname?html}</a> <font class="${color}">${status?html}</font><br>
    <#if submission.getLastSubmissionId()?exists>
    <#assign l = submission.getLastSubmission()>
    <b>Last Test: </b><a href="Test?testname=${testname?url}&submissionid=${l.getSubmissionId()}">${l.timeStamp?datetime?html}</a><br>
    </#if>
    <#if submission.getNextSubmissionId()?exists>
    <#assign n = submission.getNextSubmission()>
    <b>Next Test: </b><a href="Test?testname=${testname?url}&submissionid=${n.getSubmissionId()}">${n.timeStamp?datetime?html}</a><br>
    </#if>

    <#if test.status == "m">
    <#assign metaTestList = test.selectChildren().toList()/>
    <br><table border="0" cellpadding="3" cellspacing="1" bgcolor="#0000aa">
      <tr class="table-heading"><th>Subtest</th><th>Not Run</th><th>Failed</th><th>Passed</th></tr>
    
      <#assign row = 0/>
      <!-- Show the MetaTests that are children of this test -->
      <#list metaTestList?sort_by( "qualifiedName" ) as subtest>
        <#if row % 2 == 1>
          <tr class="tr-odd">
        <#else>
          <tr class="tr-even">
        </#if>
        <#assign row = row + 1/>
        <td><a href="Test?testname=${subtest.qualifiedName?url}&submissionid=${subtest.submissionId}">${subtest.name?html}</a></td>
        <#if subtest.status == "m">
          <td>${subtest.notRunSubTests?html}</td>
          <td>${subtest.failedSubTests?html}</td>
          <td>${subtest.passedSubTests?html}</td>
          </tr>
        <#else>
          <#switch test.status>
          <#case "p"><td colspan="3" align="center" class="pass"><#break>
          <#case "f"><td colspan="3" align="center" class="warning"><#break>
          <#case "n"><td colspan="3" align="center" class="nr"><#break>
          <#case "m"><td colspan="3" align="center" class="pass"><#break>
          </#switch>
            <#switch subtest.status>
            <#case "p">Passed<#break>
            <#case "f">Failed<#break>
            <#case "n">Not Run<#break>
            <#case "m">Meta<#break>
            </#switch>
          </td>
        </#if>
      </#list>
    </table>
    </#if>
      
    <#assign results = test.getResultList().toList()>
    <#assign hasExecutionTime = false/>
    <br>
    <table>
    <#list results as result>
      <#if result.getName() != "Output">
        <#assign plotableResult=false>
        <#if result.getType()?contains("numeric/")>
           <#assign plotableResult=true>
        </#if>
        <tr>
          <th class="measurement">
             <#if plotableResult>
                <a href="/${projectName}/Dashboard/Test?testname=${testname?url}&submissionid=${submission.submissionId?url}&measurement=${result.getName()?url}">${result.getName()?html}</a>
             <#else>
                ${result.getName()?html}
             </#if>                
          </th>
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
      <#else>
        <#assign output = result/>
      </#if>
      
      <#if result.getName() == "Execution Time">
        <#assign hasExecutionTime = true />
      </#if>
    </#list>
    </table>   
    <br>

    <#if hasExecutionTime>
      <img class="chart" src="/${projectName}/Chart?type=time&history=30&title=${client.site?url}-${client.buildName?url}-${submission.type?url}&xlabel=Date&ylabel=Time&legend=test&width=400&height=300&submissionid=${submission.submissionId?url}&testname=${testname?url}">
    </#if>
    <img class="chart" src="/${projectName}/Chart?type=time&measurement=Status&history=30&title=${client.site?url}-${client.buildName?url}-${submission.type?url}&xlabel=Date&ylabel=Status&legend=test&width=400&height=300&submissionid=${submission.submissionId?url}&testname=${testname?url}">
    <#if parameters.measurement?exists>
      <#assign measurement = parameters.measurement[0]>
      <img class="chart" src="/${projectName}/Chart?type=time&measurement=${measurement?url}&history=30&title=${client.site?url}-${client.buildName?url}-${submission.type?url}&xlabel=Date&ylabel=${measurement?url}&legend=test&width=400&height=300&submissionid=${submission.submissionId?url}&testname=${testname?url}"><br><br>
    </#if>    
    <br><br>

    <#if output?exists>
    <b>Test output</b>
        <#switch output.getType()>
          <#case "text/text"><pre>${fetchdata(output.getValue())?html}</pre><#break>
          <#case "text/html">${fetchdata(output.getValue())}<#break>
        </#switch>
    </#if>
</div>
</body>
</html>

