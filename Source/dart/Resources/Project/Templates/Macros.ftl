<#setting url_escaping_charset='ISO-8859-1'>
<#setting number_format="0"/>
<#setting datetime_format="short">
<#setting date_format="short">
<#setting time_format="short">
<#function FormatParameters parameters>
<#assign out = "?">
<#list parameters?keys as key>
  <#list parameters[key] as v>
    <#assign out = out + key?url + "=" + v?url + "&"/>
  </#list>
</#list>
<#return out/>
</#function>

<#-- for each test in the list, set the default result value -->
<#macro setDefaultResultValue list value=0>
<#list list as test>
  ${test.setDefaultResultValue( value )}
</#list>
</#macro>

<#-- method to display the menu -->
<#macro displayMenu>
<#if projectProperties["Menu"]?exists>
<div id="nav">
${projectProperties["Menu"]}
</div>
</#if>
</#macro>

<#-- method to display the user -->
<#macro displayLogin>
<#if session?exists && session.user?exists>
<div id="login">
<#if session.DisplayName?exists>
<img class="loginicon" src="/${projectName}/Resources/Icons/User.png"><a href="/${projectName}/User/User">${session.DisplayName?html}</a> | <a href="/${projectName}/User/User?Logout=true">logout</a>
<#else>
<img class="loginicon" src="/${projectName}/Resources/Icons/User.png"><a href="/${projectName}/User/User">${session.user?html}</a> | <a href="/${projectName}/User/User?Logout=true">logout</a>
</#if>
</div>
<#else>
<div id="login">
<img class="loginicon" src="/${projectName}/Resources/Icons/User.png"><a href="/${projectName}/User/UserLogin">login or create account</a>
</div>
</#if>
</#macro>

<#-- for each test in the list, set the default result value -->
<#macro displaySingleResult value unavail="(unavailable)">
<#local vallist = value.toList()>
<#if vallist?size == 1>
${vallist[0].getValue()?html}
<#else>
${unavail}
</#if>
</#macro>
