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
    <title>DynamicAnalysisCatalog - ${client.site?html} - ${client.buildName?html} - ${submission.type?html} - ${submission.timeStamp?datetime?html}</title>
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
<td align="center" valign="middle" height="100%"><a href="/${projectName}/Dashboard/?trackid=${submission.trackId?url}"><img alt="Logo/Homepage link" src="/${projectName}/Resources/Icons/Logo.png" class="icon"></a>
</td>
<td align="left" width="100%" class="title">
<h2>${projectName?html} DynamicAnalysisCatalog - ${client.site?html} - ${client.buildName?html} - ${submission.type?html}</h2><h3>${submission.timeStamp?datetime?string("long")?html}</h3>
<@displayMenu />
</td></tr>
</table>

<br/>

<div class="content">
<!-- Single submission, coverage report -->
    <p><b>Site Name: </b>${client.site?html}</p>
    <p><b>Build Name: </b>${client.buildName?html}</p>
    <p><b>Time: </b>${submission.timeStamp?datetime?html}</p>
    <p><b>Track: </b>${submission.type?html}</p>

    <!-- Determine if we need to all generations of tests -->
    <#assign showall=0/>
    <#if parameters.showall?exists>
      <#assign showall=parameters.showall[0]?number/>
    </#if>

    <!-- determine the sorting key -->
    <#assign order="ascending"/>
    <#assign reverseOrder="descending"/>
    <#assign orderIcon="/${projectName}/Resources/Icons/UpBlack.gif"/>
    <#if parameters.sortBy?exists>
      <#assign sortByKey=parameters.sortBy[0]/>
    <#else>
      <#assign sortByKey="name"/>
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

    <!-- See if we need to look at a particular (Meta)Test -->
    <#assign rootTestName=".DynamicAnalysis"/>
    <#if parameters.roottest?exists>
      <#if parameters.roottest[0] != "" >
         <#assign rootTestName=parameters.roottest[0]/>
      </#if>
    </#if>

    <#if submission.lastSubmissionId?exists>
    <#assign l = submission.lastSubmission>
    <p><b>Last submission: </b><a href="DynamicAnalysisCatalog?submissionid=${l.getSubmissionId()}&showall=${showall?url}&sortBy=${sortByKey?url}&order=${order?url}&roottest=${rootTestName?url}">${l.timeStamp?datetime?html}</a>
    </#if>
    <#if submission.nextSubmissionId?exists>
    <#assign n = submission.nextSubmission>
    <p><b>Next submission: </b><a href="DynamicAnalysisCatalog?submissionid=${n.getSubmissionId()}&showall=${showall?url}&sortBy=${sortByKey?url}&order=${order?url}&roottest=${rootTestName?url}">${n.timeStamp?datetime?html}</a>
    </#if>

    <br> 

    <#if submission.selectTest( rootTestName )?exists>
      <#assign rootTest = submission.selectTest ( rootTestName )/>

    <!-- get the results for rootTest to use as table headings -->
    <#assign rootTestResults = rootTest.getResultList().toList()>
    <#assign numberOfColumns = rootTestResults?size + 1>

    <!-- now that we know the columns, we can check if this level has the 
         right column -->
    <#if !rootTest.getResultValueAsObject( sortByKey, null)?exists>
      <#assign sortByKey="name"/>
    </#if>        

    
    <!-- create a form and cache current url parameters-->
    <form method=Get enctype="application/x-www-form-urlencoded">
    <input type="hidden" name="trackid" value=${submission.trackId?url}>
    <input type="hidden" name="submissionid" value=${submission.submissionId?url}>
    <input type="hidden" name="sortBy" value=${sortByKey}>
    <input type="hidden" name="order" value=${order}>
    <input type="hidden" name="roottest" value=${rootTestName}>
    <input type="hidden" name="showall" value=${showall}>

    <!-- table to hold summary and plot -->
    <table>
    <tr>
    <td>
    <!-- Print out the hierarchy -->
    <table class="dart">
    <tr height="5"><td class="na"></td></tr>
    <tr class="table-heading">
    <th>&nbsp;<img class="icon" src="/${projectName}/Resources/Icons/Closed.gif">
    <#assign qname = ""/>
    <#list rootTest.splitQualifiedName() as n>
      <#if n != "">
        <#assign qname = qname + "." + n />
        &nbsp; <a href="DynamicAnalysisCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&showall=${showall?url}&sortBy=${sortByKey}&order=${order}&roottest=${qname}">${n} </a> &nbsp; / 
      </#if>
    </#list>
    </th>
    </tr>
    </table>
    <!-- Do the Meta tests first -->
    <#assign metaTestList = rootTest.selectChildren().toList()/>
    <table class="dart">
      <tr height="5"><td colspan="${numberOfColumns}" class="na"></td></tr>
      <tr class="table-heading">
        <#if sortByKey=="name">
          <th class="sort-key">
            <a href="DynamicAnalysisCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&showall=${showall?url}&sortBy=name&roottest=${qname}&order=${reverseOrder}">Subtest</a>&nbsp;&nbsp;&nbsp;<img class="icon" src="${orderIcon}"> 
          </th>
        <#else>
          <th>
            <a href="DynamicAnalysisCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&showall=${showall?url}&sortBy=name&roottest=${qname}&order=ascending">Subtest</a>
          </th>
        </#if>            

        <#list rootTestResults as result>
          <#if sortByKey==result.getName()>
            <th class="sort-key">
              <a href="DynamicAnalysisCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&showall=${showall?url}&sortBy=${result.getName()?url}&roottest=${qname}&order=${reverseOrder}">${result.getName()?html}</a>&nbsp;&nbsp;&nbsp;<img class="icon" src="${orderIcon}"> 
            </th>
          <#else>
            <th>
              <a href="DynamicAnalysisCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&showall=${showall?url}&sortBy=${result.getName()?url}&roottest=${qname}&order=ascending">${result.getName()?html}</a>
            </th>
          </#if>            
        </#list>
      </tr>
        
      <#assign row = 0/>
      <!-- present the information for the current group first -->
      <tr class="tr-odd">
        <td>&nbsp;<img class="icon" src="/${projectName}/Resources/Icons/Closed.gif">&nbsp;<a href="DynamicAnalysisCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&showall=${showall?url}&sortBy=${sortByKey}&order=${order}&roottest=${rootTest.qualifiedName}">.</a></td>

        <#list rootTestResults as result>
        <td align="right">${rootTest.getResultValueAsObject(result.getName(), 0)?html}</td>
        </#list>

      </tr>              
      <tr height="5"><td colspan="${numberOfColumns}" class="na"></td></tr>

      <!-- Show the MetaTests that are children of this test -->
      <@setDefaultResultValue list=metaTestList value=0/>
      <#if order="ascending">
        <#assign sortedMetaTestList=metaTestList?sort_by(sortByKey)/>
      <#else>
        <#assign sortedMetaTestList=metaTestList?sort_by(sortByKey)?reverse/>
      </#if>
      <#assign numberOfMeta=0/>
      <#list sortedMetaTestList as test>
        <#if test.qualifiedName?starts_with ( qname ) && test.status == "m">
          <#if row % 2 == 1>
            <tr class="tr-odd">
          <#else>
            <tr class="tr-even">
          </#if>
          <#assign row = row + 1/>
          <#assign numberOfMeta = numberOfMeta + 1/>
          <td>&nbsp;<img class="icon" src="/${projectName}/Resources/Icons/Closed.gif">&nbsp;<a href="DynamicAnalysisCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&showall=${showall?url}&sortBy=${sortByKey}&order=${order}&roottest=${test.qualifiedName}">${test.name?html}</a></td>

          <#list rootTestResults as result>
          <td align="right">${test.getResultValueAsObject(result.getName(), 0)?html}</td>
          </#list>
        
          </tr>
        </#if>
      </#list>

      <#if numberOfMeta != 0>
        <tr height="5"><td colspan="${numberOfColumns}" class="na"></td></tr>
      </#if>
    </table>
    </td>
    <td>&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td>
    <#if sortByKey != "name">
      <img class="chart" src="/${projectName}/Chart?type=time&measurement=${sortByKey?url}&history=30&title=${client.site?url}-${client.buildName?url}-${submission.type?url}&xlabel=Date&ylabel=${sortByKey?url}&legend=test&width=400&height=300&submissionid=${submission.submissionId?url}&testname=${rootTestName?url}">
    </#if>
    </td>
    </tr>
    </table>  
    <br>
    <!-- show direct children of the rootTest -->
    <#if showall==0>
       <b><a href="DynamicAnalysisCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&sortBy=${sortByKey?url}&roottest=${qname}&order=${order?url}&showall=1">Show all subtests</a></b>
    <#else>
       <b><a href="DynamicAnalysisCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&sortBy=${sortByKey?url}&roottest=${qname}&order=${order?url}&showall=0">Show direct subtests</a></b>
    </#if>
    <table class="dart">
      <tr class="table-heading">
        <#if sortByKey=="name">
          <th class="sort-key">
            <a href="DynamicAnalysisCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&showall=${showall?url}&sortBy=name&roottest=${qname}&order=${reverseOrder}">Name</a>&nbsp;&nbsp;&nbsp;<img class="icon" src="${orderIcon}"> 
          </th>
        <#else>
          <th>
            <a href="DynamicAnalysisCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&showall=${showall?url}&sortBy=name&roottest=${qname}&order=ascending">Name</a>
          </th>
        </#if>            
        <th>Report</th>

        <#list rootTestResults as result>
          <#if sortByKey==result.getName()>
            <th class="sort-key">
              <a href="DynamicAnalysisCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&showall=${showall?url}&sortBy=${result.getName()?url}&roottest=${qname}&order=${reverseOrder}">${result.getName()?html}</a>&nbsp;&nbsp;&nbsp;<img class="icon" src="${orderIcon}"> 
            </th>
          <#else>
            <th>
              <a href="DynamicAnalysisCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&showall=${showall?url}&sortBy=${result.getName()?url}&roottest=${qname}&order=ascending">${result.getName()?html}</a>
            </th>
          </#if>            
        </#list>
      </tr>

      <!-- show direct children or all children-->
      <#if showall==0>
        <#assign testlist = rootTest.selectChildren().toList() />
      <#else>
        <#assign testlist = rootTest.selectAllChildren().toList() />
      </#if>

      <#assign row = 0/>
      <!-- sort the testlist -->
      <@setDefaultResultValue list=testlist value=0/>
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

          <td><a href="TestCrossReference?trackid=${submission.trackId}&testname=${test.qualifiedName?url}">${test.name?html}</a></td>
          <td align="center"><a href="Test?testname=${test.qualifiedName?url}&submissionid=${submission.submissionId?url}"><img alt="Report" src="/${projectName}/Resources/Icons/Document.gif" class="icon"></a></td>

          <#list rootTestResults as result>
          <td align="right">${test.getResultValueAsObject(result.getName(), 0)?html}</td>
          </#list>

          </tr>
        </#if>
       </#list>
       <#if numberOfTests = 0>
         <tr class="tr-odd"><td>&nbsp;</td><td></td>
         <#list rootTestResults as result>
            <td></td>
         </#list>
         </tr>
       </#if>
    </table>

    <#else>
        <br>
        <b>No test for this submission at this level:</b>&nbsp; ${rootTestName} &nbsp; &nbsp; &nbsp;
        <a href="DynamicAnalysisCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&showall=${showall?url}&sortBy=${sortByKey?url}&order=${order?url}">(<img class="icon" src="/${projectName}/Resources/Icons/Closed.gif">&nbsp;Go to the root test)</a>
    </form>
    </#if>
</div>
</body>
</html>

