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
    <title>CoverageCatalog - ${client.site?html} - ${client.buildName?html} - ${submission.type?html} - ${submission.timeStamp?datetime?html}</title>
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
<h2>${projectName?html} CoverageCatalog - ${client.site?html} - ${client.buildName?html} - ${submission.type?html}</h2><h3>${submission.timeStamp?datetime?string("long")?html}</h3>
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
    <#assign showall=1/>
    <#if parameters.showall?exists>
      <#assign showall=parameters.showall[0]?number/>
    </#if>

    <!-- determine the sorting key -->
    <#assign sortByKey="CoverageMetric"/>
    <#assign order="ascending"/>
    <#assign reverseOrder="descending"/>
    <#assign orderIcon="/${projectName}/Resources/Icons/UpBlack.gif"/>
    <#if parameters.sortBy?exists && parameters.sortBy[0] == "name">
      <#assign sortByKey="name"/>
    <#elseif parameters.sortBy?exists && parameters.sortBy[0] == "percentage">
      <#assign sortByKey="PercentCoverage"/>
    <#elseif parameters.sortBy?exists && parameters.sortBy[0] == "locuntested">
      <#assign sortByKey="LOCUnTested"/>
    <#elseif parameters.sortBy?exists && parameters.sortBy[0] == "loctested">
      <#assign sortByKey="LOCTested"/>
    <#elseif parameters.sortBy?exists && parameters.sortBy[0] == "filestested">
      <#assign sortByKey="passedSubTests"/>
    <#elseif parameters.sortBy?exists && parameters.sortBy[0] == "filesuntested">
      <#assign sortByKey="failedSubTests"/>
    <#elseif parameters.sortBy?exists && parameters.sortBy[0] == "metric">
      <#assign sortByKey="CoverageMetric"/>
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

    <!-- See if we need to look at a particular (Meta)Test -->
    <#assign rootTestName=".Coverage"/>
    <#if parameters.roottest?exists>
      <#if parameters.roottest[0] != "" >
         <#assign rootTestName=parameters.roottest[0]/>
      </#if>
    </#if>

    <#if submission.lastSubmissionId?exists>
    <#assign l = submission.lastSubmission>
    <p><b>Last submission: </b><a href="CoverageCatalog?submissionid=${l.getSubmissionId()}&showall=${showall?url}&sortBy=${sortKey?url}&order=${order?url}&roottest=${rootTestName?url}">${l.timeStamp?datetime?html}</a>
    </#if>
    <#if submission.nextSubmissionId?exists>
    <#assign n = submission.nextSubmission>
    <p><b>Next submission: </b><a href="CoverageCatalog?submissionid=${n.getSubmissionId()}&showall=${showall?url}&sortBy=${sortKey?url}&order=${order?url}&roottest=${rootTestName?url}">${n.timeStamp?datetime?html}</a>
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
        &nbsp; <a href="CoverageCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&showall=${showall?url}&sortBy=${sortKey}&order=${order}&roottest=${qname}">${n} </a> &nbsp; / 
      </#if>
    </#list>
    </th>
    </tr>
    </table>
    <!-- Do the Meta tests first -->
    <#assign metaTestList = rootTest.selectChildren().toList()/>
    <table class="dart">
      <tr height="5"><td colspan="7" class="na"></td></tr>
      <tr class="table-heading">
        <#if sortByKey=="name">
          <th class="sort-key">
            <a href="CoverageCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&showall=${showall?url}&sortBy=name&roottest=${qname}&order=${reverseOrder}">Subtest</a>&nbsp;&nbsp;&nbsp;<img class="icon" src="${orderIcon}"> 
          </th>
        <#else>
          <th>
            <a href="CoverageCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&showall=${showall?url}&sortBy=name&roottest=${qname}&order=ascending">Subtest</a>
          </th>
        </#if>            
        <#if sortByKey=="PercentCoverage">
          <th class="sort-key">
            &nbsp;&nbsp;&nbsp;<a href="CoverageCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&showall=${showall?url}&sortBy=percentage&roottest=${qname}&order=${reverseOrder}">Percentage</a>&nbsp;&nbsp;&nbsp;<img src="${orderIcon}" class="icon">&nbsp;&nbsp;&nbsp;
          </th>
        <#else>
          <th>
            &nbsp;&nbsp;&nbsp;<a href="CoverageCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&showall=${showall?url}&sortBy=percentage&roottest=${qname}&order=ascending">Percentage</a>&nbsp;&nbsp;&nbsp;
          </th>
        </#if>
        </th>
        <#if sortByKey=="LOCTested">
          <th class="sort-key">
           <a href="CoverageCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&showall=${showall?url}&sortBy=loctested&roottest=${qname}&order=${reverseOrder}">Lines covered</a>&nbsp;&nbsp;&nbsp;<img src="${orderIcon}" class="icon">
          </th>
        <#else>
          <th>
           <a href="CoverageCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&showall=${showall?url}&sortBy=loctested&roottest=${qname}&order=descending">Lines covered</a>
          </th>
        </#if>
        <#if sortByKey=="LOCUnTested">
          <th class="sort-key">
           <a href="CoverageCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&showall=${showall?url}&sortBy=locuntested&roottest=${qname}&order=${reverseOrder}">Lines not covered</a>&nbsp;&nbsp;&nbsp;<img src="${orderIcon}" class="icon">
          </th>
        <#else>
          <th>
           <a href="CoverageCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&showall=${showall?url}&sortBy=locuntested&roottest=${qname}&order=descending">Lines not covered</a>
          </th>
        </#if>
        <#if sortByKey=="passedSubTests">
          <th class="sort-key">
           <a href="CoverageCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&showall=${showall?url}&sortBy=filestested&roottest=${qname}&order=${reverseOrder}">Files covered</a>&nbsp;&nbsp;&nbsp;<img src="${orderIcon}" class="icon">
          </th>
        <#else>
          <th>
           <a href="CoverageCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&showall=${showall?url}&sortBy=filestested&roottest=${qname}&order=descending">Files covered</a>
          </th>
        </#if>
        <#if sortByKey=="failedSubTests">
          <th class="sort-key">
           <a href="CoverageCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&showall=${showall?url}&sortBy=filesuntested&roottest=${qname}&order=${reverseOrder}">Files not covered</a>&nbsp;&nbsp;&nbsp;<img src="${orderIcon}" class="icon">
          </th>
        <#else>
          <th>
           <a href="CoverageCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&showall=${showall?url}&sortBy=filesuntested&roottest=${qname}&order=descending">Files not covered</a>
          </th>
        </#if>
        <#if sortByKey=="CoverageMetric">
          <th class="sort-key">
           <a href="CoverageCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&showall=${showall?url}&sortBy=metric&roottest=${qname}&order=${reverseOrder}">Coverage metric</a>&nbsp;&nbsp;&nbsp;<img src="${orderIcon}" class="icon">
          </th>
        <#else>
          <th>
           <a href="CoverageCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&showall=${showall?url}&sortBy=metric&roottest=${qname}&order=ascending">Coverage metric</a>
          </th>
        </#if>
      </tr>
        
      <#assign row = 0/>
      <!-- Show the MetaTests that are children of this test -->
      <!-- sort the testlist -->
      <#if order="ascending">
        <#assign sortedMetaTestList=metaTestList?sort_by(sortByKey)/>
      <#else>
        <#assign sortedMetaTestList=metaTestList?sort_by(sortByKey)?reverse/>
      </#if>

      <!-- present the information for the current group first -->
      <tr class="tr-odd">
        <td>&nbsp;<img class="icon" src="/${projectName}/Resources/Icons/Closed.gif">&nbsp;<a href="CoverageCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&showall=${showall?url}&sortBy=${sortKey}&order=${order}&roottest=${rootTest.qualifiedName}">.</a></td>
        <td align="right">
            <#if rootTest.getResultValue ( "PercentCoverage", "" ) != "">
              <#if rootTest.getResultValue ( "Log", "" ) != "" >
                  <a href="/${projectName}/Zip/${rootTest.getResultValue( "Log", "" )?replace('\\','/')}"/>${rootTest.PercentCoverage?string("#0.00")}</a>
              <#else>
                 ${rootTest.PercentCoverage?string("#0.00")}
              </#if>
            </#if>
        </td>
        <td align="right">
          <#if rootTest.getResultValue ( "LOCTested", "" ) != "" >${rootTest.LOCTested}</#if>
        </td>
        <td align="right">
          <#if rootTest.getResultValue ( "LOCUnTested", "" ) != "">${rootTest.LOCUnTested}</#if>
        </td>

        <td align="right">
          <#if rootTest.passedSubTests?string?has_content>${rootTest.passedSubTests}</#if>
        </td>
        <td align="right">
          <#if rootTest.failedSubTests?string?has_content>${rootTest.failedSubTests}</#if>
        </td>
        <td align="right">
          <#if rootTest.getResultValue ( "CoverageMetric", "" ) != "">${rootTest.CoverageMetric?string("#0.00")}</#if>
        </td>
      </tr>              
      <tr height="5"><td colspan="7" class="na"></td></tr>

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
          <td>&nbsp;<img class="icon" src="/${projectName}/Resources/Icons/Closed.gif">&nbsp;<a href="CoverageCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&showall=${showall?url}&sortBy=${sortKey}&order=${order}&roottest=${test.qualifiedName}">${test.name?html}</a></td>
          <td align="right">
            <#if test.getResultValue ( "PercentCoverage", "" ) != "">
              <#if test.getResultValue ( "CoverageLog", "" ) != "" >
                  <a href="/${projectName}/Zip/${test.getResultValue( "CoverageLog", "" )?replace('\\','/')}"/>${test.PercentCoverage?string("#0.00")}</a>
              <#else>
                 ${test.PercentCoverage?string("#0.00")}
              </#if>
            </#if>
          </td>
          <td align="right">
            <#if test.getResultValue ( "LOCTested", "" ) != "">${test.LOCTested}</#if>
          </td>
          <td align="right">
            <#if test.getResultValue ( "LOCUnTested", "" ) != "">${test.LOCUnTested}</#if>
          </td>
          <td align="right">
            <#if test.passedSubTests?string?has_content>${test.passedSubTests}</#if>
          </td>
          <td align="right">
            <#if test.failedSubTests?string?has_content>${test.failedSubTests}</#if>
          </td>
          <td align="right">
            <#if test.getResultValue ( "CoverageMetric", "" ) != "">${test.CoverageMetric?string("#0.00")}</#if>
          </td>
          </tr>
        </#if>
      </#list>

      <#if numberOfMeta != 0>
        <tr height="5"><td colspan="7" class="na"></td></tr>
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
    <#if showall==0>
       <b><a href="CoverageCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&sortBy=${sortKey?url}&roottest=${qname}&order=${order?url}&showall=1">Show all subtests</a></b>
    <#else>
       <b><a href="CoverageCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&sortBy=${sortKey?url}&roottest=${qname}&order=${order?url}&showall=0">Show direct subtests</a></b>
    </#if>
    <table class="dart">
      <tr class="table-heading">
        <#if sortByKey=="name">
          <th class="sort-key">
            <a href="CoverageCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&showall=${showall?url}&sortBy=name&roottest=${qname}&order=${reverseOrder}">Name</a>&nbsp;&nbsp;&nbsp;<img src="${orderIcon}" class="icon">            
          </th>
        <#else>
          <th>
            <a href="CoverageCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&showall=${showall?url}&sortBy=name&roottest=${qname}&order=ascending">Name</a>
          </th>
        </#if>            
          <th>
            &nbsp;&nbsp;&nbsp;Report&nbsp;&nbsp;&nbsp;
          </th>
        <#if sortByKey=="PercentCoverage">
          <th class="sort-key">
            &nbsp;&nbsp;&nbsp;<a href="CoverageCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&showall=${showall?url}&sortBy=percentage&roottest=${qname}&order=${reverseOrder}">Percentage</a>&nbsp;&nbsp;&nbsp;<img src="${orderIcon}" class="icon">&nbsp;&nbsp;&nbsp;
          </th>
        <#else>
          <th>
            &nbsp;&nbsp;&nbsp;<a href="CoverageCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&showall=${showall?url}&sortBy=percentage&roottest=${qname}&order=ascending">Percentage</a>&nbsp;&nbsp;&nbsp;
          </th>
        </#if>
        </th>
        <#if sortByKey=="LOCUnTested">
          <th class="sort-key">
           <a href="CoverageCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&showall=${showall?url}&sortBy=locuntested&roottest=${qname}&order=${reverseOrder}">Lines not covered</a>&nbsp;&nbsp;&nbsp;<img src="${orderIcon}" class="icon">
          </th>
        <#else>
          <th>
           <a href="CoverageCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&showall=${showall?url}&sortBy=locuntested&roottest=${qname}&order=descending">Lines not covered</a>
          </th>
        </#if>
        <#if sortByKey=="CoverageMetric">
          <th class="sort-key">
           <a href="CoverageCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&showall=${showall?url}&sortBy=metric&roottest=${qname}&order=${reverseOrder}">Coverage metric</a>&nbsp;&nbsp;&nbsp;<img src="${orderIcon}" class="icon">
          </th>
        <#else>
          <th>
           <a href="CoverageCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&showall=${showall?url}&sortBy=metric&roottest=${qname}&order=ascending">Coverage metric</a>
          </th>
        </#if>
      </tr>

      <!-- show direct children or all children-->
      <#if showall==0>
        <#assign testlist = rootTest.selectChildren().toList() />
      <#else>
        <#assign testlist = rootTest.selectAllChildren().toList() />
      </#if>

      <#assign row = 0/>
      <!-- sort the testlist -->
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
          <td align="right">
            <#if test.PercentCoverage?string?has_content>${test.PercentCoverage?string("#0.00")}</#if>
          </td>
          <td align="right">
            <#if test.LOCUnTested?string?has_content>${test.LOCUnTested}</#if>
          </td>
          <td align="right">
            <#if test.CoverageMetric?string?has_content>${test.CoverageMetric?string("#0.00")}</#if>
          </td>
          </tr>
        </#if>
       </#list>
       <#if numberOfTests = 0>
         <tr class="tr-odd"><td>&nbsp;</td><td></td><td></td><td></td><td></td></tr>
       </#if>
    </table>

    </form>

    <#else>
        <br>
        <b>No test for this submission at this level:</b>&nbsp; ${rootTestName} &nbsp; &nbsp; &nbsp;
        <a href="CoverageCatalog?trackid=${submission.trackId?url}&submissionid=${submission.submissionId?url}&showall=${showall?url}&sortBy=${sortKey}&order=${order}">(<img class="icon" src="/${projectName}/Resources/Icons/Closed.gif">&nbsp;Go to the root test)</a>
    </#if>
</div>
</body>
</html>

