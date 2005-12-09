<#setting url_escaping_charset='ISO-8859-1'>
<#setting number_format="0"/>
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