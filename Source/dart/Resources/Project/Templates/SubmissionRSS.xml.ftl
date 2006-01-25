<#include "Macros.ftl"/>
<?xml version="1.0"?>
<rss version="2.0">
  <channel>
  <title>Recent Dart submissions for ${projectName?xml}</title>
   <#assign baseURL = request.scheme + "://" + request.serverName + ":" + request.serverPort + "/" + projectName/>
  <link>${baseURL}/Dashboard/</link>
  <description>Dartboard for ${projectName?xml}</description>
  <generator>Dart</generator>
  <image>
    <title>Recent Dart submissions for ${projectName?xml}</title>
    <link>${baseURL}/Dashboard/</link>
    <url>${baseURL}/Resources/Icons/DartLogoSmallSmooth.png</url>
  </image>
  <#assign submissions = submissionFinder.find("order by CreatedTimeStamp desc")>
  <#if (submissions.size() > 0)>
  <lastBuildDate>${submissions.get(0).createdTimeStamp?datetime?xml}</lastBuildDate>
    <#assign count=0>
    <#-- getList() is supposed to return the actual list not a copy of it -->
    <#list submissions.getList() as submission>
    <#if (count >= 5)>
       <#break>
    </#if>
    <#assign count=count+1>
	
    <#-- tally the coverage errors -->
    <#assign percentCoverage=0>
    <#assign hasCoverage=false>
    <#if submission.selectTest( ".Coverage" )?exists> 
      <#assign test = submission.selectTest( ".Coverage" )/> 
      <#if test.PercentCoverage?string?has_content>
         <#assign percentCoverage=test.PercentCoverage>
         <#assign hasCoverage=true>
      </#if>
    </#if>

    <#-- tally the dynamic analysis errors -->
    <#assign dynamicAnalysisCount=0>
    <#assign hasDynamicAnalysis=false>
    <#if submission.selectTest( ".DynamicAnalysis" )?exists> 
      <#assign test = submission.selectTest( ".DynamicAnalysis" )/> 
      <#assign results = test.getResultList().toList()>
      <#list results as result>
        <#if result.getType()?contains("numeric/")>
          <#assign dynamicAnalysisCount = dynamicAnalysisCount + result.getValue()?number>
          <#assign hasDynamicAnalysis=true>
        </#if>
      </#list>
    </#if>

    <item>
       <title>Dart(${projectName?xml}) - ${submission.site?xml} - ${submission.buildName?xml} - ${submission.type?xml} - ${submission.timeStamp?datetime?xml} - <#if (submission.errorCount >= 0)>${submission.errorCount} errors, </#if><#if (submission.warningCount >= 0)>${submission.warningCount} warnings, </#if><#if (submission.notRunCount >= 0)>${submission.notRunCount} not run, </#if><#if (submission.failedCount >= 0)>${submission.failedCount} failed, </#if><#if hasCoverage>${percentCoverage} percent coverage, </#if><#if hasDynamicAnalysis>${dynamicAnalysisCount} dynamic analysis errors, </#if>

</title>
       <link>${baseURL}/Dashboard/Submission?submissionid=${submission.submissionId}</link>
       <#if (submission.errorCount >= 0 || submission.warningCount >=0 || submission.notRunCount >= 0 || submission.failedCount >= 0 || hasCoverage || hasDynamicAnalysis)>
       <description>A new ${submission.type?xml} submission from ${submission.site?xml} - ${submission.buildName?xml} is available: <#if (submission.errorCount >= 0)>${submission.errorCount} errors, </#if><#if (submission.warningCount >= 0)>${submission.warningCount} warnings, </#if><#if (submission.notRunCount >= 0)>${submission.notRunCount} not run, </#if><#if (submission.failedCount >= 0)>${submission.failedCount} failed, </#if><#if hasCoverage>${percentCoverage} percent coverage, </#if><#if hasDynamicAnalysis>${dynamicAnalysisCount} dynamic analysis errors, </#if>
</description>
       <#else>
       <description>A new ${submission.type} submission from ${submission.site?xml} - ${submission.buildName?xml} is available. This submission contains no data.</description>
       </#if>
       <pubDate>${submission.createdTimeStamp?datetime?xml}</pubDate>
    </item>
    </#list>
  </#if>
  </channel>
</rss>