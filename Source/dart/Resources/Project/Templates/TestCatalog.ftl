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
    <title>TestCatalog - ${client.site?html} - ${client.buildName?html} - ${submission.type?html} - ${submission.timeStamp?datetime?html}</title>
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
<td align="center" valign="middle" height="100%"><a href="/${projectName}/Dashboard/?trackid=${submission.trackId?url}"><img alt="Logo/Homepage link" src="/${projectName}/Resources/Icons/Logo.png"></a>
</td>
<td align="left" width="100%" class="title">
<h2>${projectName?html} TestCatalog - ${client.site?html} - ${client.buildName?html} - ${submission.type?html}</h2><h3>${submission.timeStamp?datetime?html}</h3>
<@displayMenu />
</td></tr>
</table>

<br/>

<div class="content">
<!-- Single submission, all tests -->
    <p><b>Site Name: </b>${client.site?html}</p>
    <p><b>Build Name: </b>${client.buildName?html}</p>
    <p><b>Time: </b>${submission.timeStamp?datetime?html}</p>
    <p><b>Track: </b>${submission.type?html}</p>

    <#-- Determine if we need to all generations of tests -->
    <#assign showall=0/>
    <#if parameters.showall?exists>
      <#assign showall=parameters.showall[0]?number/>
    </#if>

    <#-- determine the sorting key -->
    <#assign sortByKey="status"/>
    <#assign order="ascending"/>
    <#assign reverseOrder="descending"/>
    <#assign orderIcon="/${projectName}/Resources/Icons/UpBlack.gif"/>
    <#if parameters.sortBy?exists && parameters.sortBy[0] == "name">
      <#assign sortByKey="name"/>
    <#elseif parameters.sortBy?exists && parameters.sortBy[0] == "time">
      <#assign sortByKey="executionTime"/>
    <#elseif parameters.sortBy?exists && parameters.sortBy[0] == "status">
      <#assign sortByKey="status"/>
    <#elseif parameters.sortBy?exists && parameters.sortBy[0] == "detail">
      <#assign sortByKey="completionStatus"/>
    </#if>

    <#if parameters.sortBy?exists>
      <#assign sortKey = parameters.sortBy[0]/>
    <#else>
      <#assign sortKey = "name"/>
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

    <#-- See if we need to look at a particular (Meta)Test -->
    <#assign rootTestName=""/>
    <#if parameters.roottest?exists>
      <#assign rootTestName=parameters.roottest[0]/>
    </#if>

    <#if submission.lastSubmissionId?exists>
    <#assign l = submission.lastSubmission>
    <p><b>Last submission: </b><a href="TestCatalog?submissionid=${l.getSubmissionId()}&showall=${showall?url}&sortBy=${sortKey?url}&order=${order?url}&roottest=${rootTestName?url}">${l.timeStamp?datetime?html}</a>
    </#if>
    <#if submission.nextSubmissionId?exists>
    <#assign n = submission.nextSubmission>
    <p><b>Next submission: </b><a href="TestCatalog?submissionid=${n.getSubmissionId()}&showall=${showall?url}&sortBy=${sortKey?url}&order=${order?url}&roottest=${rootTestName?url}">${n.timeStamp?datetime?html}</a>
    </#if>
    
    <br>
    
    <#if submission.selectTest( rootTestName )?exists>
      <#assign rootTest = submission.selectTest ( rootTestName )/>

    <!-- create a form and cache current url parameters-->
    <form method=Get enctype="application/x-www-form-urlencoded">
    <input type="hidden" name="trackid" value=${submission.trackId?url}>
    <input type="hidden" name="submissionid" value=${submission.submissionId?url}>
    <input type="hidden" name="sortBy" value=${sortKey}>
    <input type="hidden" name="order" value=${order}>
    <input type="hidden" name="roottest" value=${rootTestName}>
    <input type="hidden" name="showall" value=${showall}>

    <!-- Print out the hierarchy -->
    <table class="dart">
    <tr height="5"><td class="na"></td></tr>
    <tr class="table-heading">
    <th>&nbsp;<img class="icon" src="/${projectName}/Resources/Icons/Closed.gif">
    <#assign qname = ""/>
    <#list rootTest.splitQualifiedName() as n>
      <#if n == "">
        <a href="TestCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&showall=${showall?url}&sortBy=${sortKey}&order=${order}&roottest=${qname}">All</a>&nbsp; /
      <#else>
        <#assign qname = qname + "." + n />
        &nbsp; <a href="TestCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&showall=${showall?url}&sortBy=${sortKey}&order=${order}&roottest=${qname}">${n} </a> &nbsp; / 
      </#if>
    </#list>
    </th>
    </tr>
    </table>
    <!-- Do the Meta tests first -->
    <#assign metaTestList = rootTest.selectChildren().toList()/>
    <table class="dart">
      <tr height="5"><td colspan="4" class="na"></td></tr>
      <tr class="table-heading"><th>Subtest</th><th>Not Run</th><th>Failed</th><th>Passed</th></tr>
    
      <!-- present the information for the current group first -->
      <tr class="tr-odd">
        <td>&nbsp;<img class="icon" src="/${projectName}/Resources/Icons/Closed.gif">&nbsp;<a href="TestCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&showall=${showall?url}&sortBy=${sortKey}&order=${order}&roottest=${rootTest.qualifiedName}">.</a></td>
        <td align="right">${rootTest.notRunSubTests?html}</td>
        <td align="right">${rootTest.failedSubTests?html}</td>
        <td align="right">${rootTest.passedSubTests?html}</td>
      </tr>              
      <tr height="5"><td colspan="4" class="na"></td></tr>

      <!-- present the rest of the meta -->
      <#assign row = 0/>
      <!-- Show the MetaTests that are children of this test -->
      <#assign numberOfMeta=0/>
      <#list metaTestList?sort_by( "qualifiedName" ) as test>
        <#if test.qualifiedName?starts_with ( qname ) && test.status == "m">
          <#if row % 2 == 1>
            <tr class="tr-odd">
          <#else>
            <tr class="tr-even">
          </#if>
          <#assign row = row + 1/>
          <#assign numberOfMeta = numberOfMeta + 1/>
          <td>&nbsp;<img class="icon" src="/${projectName}/Resources/Icons/Closed.gif">&nbsp;<a href="TestCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&showall=${showall?url}&sortBy=${sortKey}&order=${order}&roottest=${test.qualifiedName}">${test.name?html}</a></td>
          <td align="right">${test.notRunSubTests?html}</td>
          <td align="right">${test.failedSubTests?html}</td>
          <td align="right">${test.passedSubTests?html}</td>
          </tr>
        </#if>
      </#list>

      <#if numberOfMeta != 0>
        <tr height="5"><td colspan="4" class="na"></td></tr>
      </#if>
    </table>

    <!-- plot any selected tests -->
    <#if parameters.testname?exists>
    <#assign testnameParameters=""/>
    <#list parameters.testname as name>
    <#assign testnameParameters=testnameParameters+"&testname="+name/>
    </#list>
    <br>
    <img class="chart" src="/${projectName}/Chart?type=time&history=30&title=${client.site?url}-${client.buildName?url}-${submission.type?url}&xlabel=Date&ylabel=Time&legend=test&width=400&height=300&submissionid=${submission.submissionId?url}${testnameParameters}">
    </#if>

    <br><input type="submit" value="Chart times" caption="Chart execution times of selected tests" action="TestCatalog">

    <!-- show direct children -->
    <br>
    <#if showall==0>
       <b><a href="TestCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&sortBy=${sortKey?url}&roottest=${qname}&order=${order?url}&showall=1">Show all subtests</a></b>
    <#else>
       <b><a href="TestCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&sortBy=${sortKey?url}&roottest=${qname}&order=${order?url}&showall=0">Show direct subtests</a></b>
    </#if>
    <br><table class="dart">
      <tr class="table-heading">
        <th>Select</th>
        <#if sortByKey=="name">
          <th class="sort-key">
            <a href="TestCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&showall=${showall?url}&sortBy=name&roottest=${qname}&order=${reverseOrder}">Name</a>&nbsp;&nbsp;&nbsp;<img src="${orderIcon}" class="icon">      
          </th>
        <#else>
          <th>
            <a href="TestCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&showall=${showall?url}&sortBy=name&roottest=${qname}&order=ascending">Name</a>
          </th>
        </#if>            
        <#if sortByKey=="status">
          <th class="sort-key">
            &nbsp;&nbsp;&nbsp;<a href="TestCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&showall=${showall?url}&sortBy=status&roottest=${qname}&order=${reverseOrder}">Status</a>&nbsp;&nbsp;&nbsp;<img src="${orderIcon}" class="class">&nbsp;&nbsp;&nbsp;
          </th>
        <#else>
          <th>
            &nbsp;&nbsp;&nbsp;<a href="TestCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&showall=${showall?url}&sortBy=status&roottest=${qname}&order=ascending">Status</a>&nbsp;&nbsp;&nbsp;
          </th>
        </#if>
        <#if sortByKey=="executionTime">
          <th class="sort-key">
            &nbsp;&nbsp;&nbsp;<a href="TestCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&showall=${showall?url}&sortBy=time&roottest=${qname}&order=${reverseOrder}">Time</a>&nbsp;&nbsp;&nbsp;<img src="${orderIcon}" class="icon"">&nbsp;&nbsp;&nbsp;
          </th>
        <#else>
          <th>
            &nbsp;&nbsp;&nbsp;<a href="TestCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&showall=${showall?url}&sortBy=time&roottest=${qname}&order=descending">Time</a>&nbsp;&nbsp;&nbsp;
          </th>
        </#if>
        </th>
        <#if sortByKey=="completionStatus">
          <th class="sort-key">
           <a href="TestCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&showall=${showall?url}&sortBy=detail&roottest=${qname}&order=${reverseOrder}">Detail</a>&nbsp;&nbsp;&nbsp;<img src="${orderIcon}" class="icon">
          </th>
        <#else>
          <th>
           <a href="TestCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&showall=${showall?url}&sortBy=detail&roottest=${qname}&order=ascending">Detail</a>
          </th>
        </#if>
      </tr>

      <#-- show direct children or all children -->
      <#if showall==0>
        <#assign testlist = rootTest.selectChildren().toList() />
      <#else>
        <#assign testlist = rootTest.selectAllChildren().toList() />
      </#if>

      <#assign row = 0/>
      <#-- sort the testlist -->
      <#if order="ascending">
        <#assign sortedTestList=testlist?sort_by(sortByKey)/>
      <#else>
        <#assign sortedTestList=testlist?sort_by(sortByKey)?reverse/>
      </#if>

      <#assign numberOfTests = 0 />
      <#list sortedTestList as test>
        <#if test.status != "m">

        <#assign row = row + 1/>
        <#if row % 2 == 1>
        <tr class="tr-odd">
        <#else>
        <tr class="tr-even">
        </#if>
        <#assign numberOfTests = numberOfTests + 1/>

        <td align="center"><input type="checkbox" name="testname" value="${test.qualifiedName?url}"></td>
        <td><a href="TestCrossReference?trackid=${submission.trackId}&testname=${test.qualifiedName?url}">${test.name?html}</a></td>

        <#switch test.status>
        <#case "p"><td align="center" class="pass"><#break>
        <#case "f"><td align="center" class="warning"><#break>
        <#case "n"><td align="center" class="nr"><#break>
        <#case "m"><td align="center" class="pass"><#break>
        </#switch>
         <a href="Test?testname=${test.qualifiedName?url}&submissionid=${submission.submissionId?url}">
        <#switch test.status>
        <#case "p">Passed<#break>
        <#case "f">Failed<#break>
        <#case "n">Not Run<#break>
        <#case "m">Meta<#break>
        </#switch>
         </a></td>
            <#if (test.executionTime >= 0.0)>
              <td align="right">&nbsp;&nbsp;&nbsp;${test.executionTime?string("#0.000")}&nbsp;&nbsp;&nbsp;</td>
            <#else>
              <td align="right">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
            </#if>
            <td align="center">${test.completionStatus?if_exists}</td>
          </tr>
          </#if>
          </#list>
       <#if numberOfTests = 0>
         <tr class="tr-odd"><td>&nbsp;</td><td></td><td></td><td></td><td></td></tr>
       </#if>
    </table>

    <input type="submit" value="Chart times" caption="Chart execution times of selected tests" action="TestCatalog">
    </form>

    <#else>
        <br>
        <b>No tests for this submission at this level:</b>&nbsp; ${rootTestName} &nbsp; &nbsp; &nbsp;
        <a href="TestCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&showall=${showall?url}&sortBy=${sortKey}&order=${order}">(<img class="icon" src="/${projectName}/Resources/Icons/Closed.gif">&nbsp;Go to the root test)</a>
    </#if>
</div>
</body>
</html>

