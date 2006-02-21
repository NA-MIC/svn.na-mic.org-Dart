<#include "Macros.ftl"/>
<#if client?exists>
<#assign site = client.site>
<#assign buildname = client.buildName>
</#if>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0//EN">
<html>
  <head>
    <meta name="robots" content="noindex,nofollow">
    <meta http-equiv="Content-Type"
      content="text/html; charset=iso-8859-1">
    <meta http-equiv="Content-Script-Type" content="text/javascript">
    <#if client?exists>
    <title>Client - ${site?html} - ${buildname?html}</title>
    <#else>
    <title>Client - no client specified</title>
    </#if>
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
<td align="center" valign="middle" height="100%">
<a href="/${projectName}/Dashboard/">
<img alt="Logo/Homepage link" src="/${projectName}/Resources/Icons/Logo.png"></a>
</td>
<td align="left" width="100%" class="title">
<#if client?exists>
<h2>${projectName?html} Client - ${site?html} - ${buildname?html}</h2>
<#else>
<h2>${projectName?html} Client - no client specified</h2>
</#if>
<h3>${date?datetime?html}</h3>
<@displayMenu />
</td>

</tr>
</table>

<br>

<div class="content">
<#if client?exists>
<table border="0" cellpadding="3" cellspacing="1" bgcolor="#0000aa">
  <tr class="table-heading">
    <td colspan="2" valign="middle">
     <h3>Client</h3>
    </td>
  </tr>
  <tr class="tr-odd">
    <td>Client id</td><td align="right">${client.clientId?html}</td>
  </tr>
  <tr class="tr-even">
    <td>Site</td><td align="right">${client.site?html}</td>
  </tr>
  <tr class="tr-odd">
    <td>BuildName</td><td align="right">${client.buildName?html}</td>
  </tr>
  <tr class="tr-even">
    <td>DisplayName</td><td align="right">${client.displayName?default("")?html}</td>
  </tr>
  <tr class="tr-odd">
    <td>OS</td><td align="right">${client.oS?default("")?html}</td>
  </tr>
  <tr class="tr-even">
    <td>OS Version</td><td align="right">${client.oSVersion?default("")?html}</td>
  </tr>
  <tr class="tr-odd">
    <td>Branch</td><td align="right">${client.branch?default("")?html}</td>
  </tr>
  <tr class="tr-even">
    <td>Comment</td><td align="right">${client.comment?default("")?html}</td>
  </tr>
  <tr class="tr-odd">
    <td>Configuration</td><td align="right">${client.configuration?default("")?html}</td>
  </tr>
  <#if client.clientPropertyList?exists && (client.clientPropertyList.toList()?size > 0)>
    <tr class="tr-even">
      <td align="center" colspan="2"><b>Properties</b></td>
    </tr>
    <#list client.clientPropertyList.toList() as property>
    <#if property_index % 2 == 0>
    <tr class="tr-odd">
    <#else>
    <tr class="tr-even">
    </#if>
      <td>${property.name?html}</td>
      <#-- may need to map values from userids to usernames -->
      <#if property.name?starts_with("Expected.") 
        && property.name?ends_with(".Notify.UserId")>
         <#assign userList=userFinder.selectByUserIdList(property.value?number)>
         <#if (userList?size > 0)>
           <td align="right">${userList.toList()[0].email?html}</td>
         <#else>
           <td align="right">${property.value?html}</td>
         </#if>
      <#else>
         <td align="right">${property.value?html}</td>
      </#if>
    </tr>
    </#list>
  </#if>
</table>



<#-- Number of submissions from this client on each track? -->

<#-- if the user has suitable permissions, then allow them to modify the designation -->
<#if session?exists && session.user?exists>
  <#-- user logged in, check roles -->
  <#if realm.isUserInRole( session.user, "Dart.Administrator") || realm.isUserInRole( session.user, projectName + ".Administrator")>
<br/>
  Client properties and client property values:
  <ul>
    <li> Expected.&lt;TrackName&gt; - indicates the client is expected to submit on a particular track. Value should be set to "true".
    <li> Expected.&lt;TrackName&gt;.Notify.UserId - user to contact if the client has not submitted within the alloted time period. Value is specified as a username (email address) of a Dart user.
  </ul>
  <br/>

  <form name="ClientProperty" id="ClientProperty" method="post">
  Property name: <input tabindex='1' type="text" name="PropertyName" value="" size='25'/>
Property value: <input tabindex='2' type="text" name="PropertyValue" value="" size='25'/>
  <input tabindex='3' type='submit' name="AddClientProperty" value="Add property"/>
  <input tabindex='4' type='submit' name="RemoveClientProperty" value="Remove property"/>   
  </form>
  </#if>
</#if>


<#else>
#-- No client specified -->
<table border="0" cellpadding="3" cellspacing="1" bgcolor="#0000aa">
  <tr class="table-heading">
    <td colspan="2" valign="middle">
     <h3>Client</h3>
    </td>
  </tr>
  <tr class="tr-odd">
    <td colspan="2" valign="middle">
    No client specified
    </td>
  </tr>
</table>
</#if>

</div>
</body>
</html>
