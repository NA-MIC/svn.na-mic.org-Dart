<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0//EN">
<html>
  <head>
    <meta name="robots" content="noindex,nofollow">
    <meta http-equiv="Content-Type"
      content="text/html; charset=iso-8859-1">
    <title>Dart User</title>
    <link rel="stylesheet" href="/DartServer/Resources/Style.css" type="text/css">
    <link rel="shortcut icon" href="/DartServer/Resources/Icons/favicon.ico" type="image/x-icon" />
  </head>
  <body bgcolor="#ffffff">

<table border="0" cellpadding="0" cellspacing="2" width="100%">
<tr>
<td align="center"><a href="/${projectName}/Dashboard"><img alt="Logo/Homepage link" src="/DartServer/Resources/Icons/Logo.png" border="0"></a>
</td>
<td align="left" width="100%" class="title">
<h2>Dart User</h2>
<#if user?exists && user.email?exists>
<h3>${user.email?html}</h3>
<#else>
n<h3>Unknown user</h3>
</#if>
</td>

</tr>
</table>

<br/>

<#if parameters.error?exists>
<div class="errorBlock">
<ul>
<#list parameters.error as error>
<#switch error>
 <#case "1">
   <li>Password and retype of password do not match.</li>
   <#break>
 <#case "2">
   <li>First name must be specified.</li>
   <#break>
 <#case "3">
   <li>Last name must be specified.</li>
   <#break>
</#switch>
</#list>
</ul>
</div>
<br/>
</#if>


<form name="UserInformation" id="UserInformation" method="post">

<table border='0'>

<tr>
<td align='right'>Email:</td>
<td align='left'>
<#if user?exists && user.email?exists>
${user.email}
</#if>
</td>
<td align='left'>
&nbsp;
</td>
</tr>

<tr>
<td align='right'>Password:</td>
<td align='left'>
<#if user?exists && user.password?exists>
<input tabindex='1' type='password' name="Password" size='25' />
<#else>
<input tabindex='1' type='password' name="Password" size='25' />
</#if>
</td>
<td align='left'>
&nbsp;
</td>
</tr>

<tr>
<td colspan='3'>&nbsp;</td>
</tr>

<tr>
<td align='right'>Retype password:</td>
<td align='left'>
<#if user?exists && user.password?exists>
<input tabindex='2' type='password' name="Retype" size='25' />
<#else>
<input tabindex='2' type='password' name="Retype" size='25' />
</#if>
</td>
<td>&nbsp</td>
</tr>

<tr>
<td align='right'>First name:</td>
<td align='left'>
<#if user?exists && user.firstName?exists>
<input tabindex='3' type='text' name="First" value="${user.firstName}" size='25' />
<#else>
<input tabindex='3' type='text' name="First" value="" size='25' />
</#if>
</td>
<td>&nbsp;</td>
</tr>

<tr>
<td align='right'>Last name:</td>
<td align='left'>
<#if user?exists && user.lastName?exists>
<input tabindex='4' type='text' name="Last" value="${user.lastName}" size='25' />
<#else>
<input tabindex='4' type='text' name="Last" value="" size='25' />
</#if>
</td>
<td align='left'>
<input tabindex='5' type='submit' name="UpdateUser" value="Update information" />
</td>
</tr>

</table>
</form>
<br/>


<#-- show/edit the repository ids specified for the user -->
<form name="RepositoryIds" id="RepositoryIds" method="post">
<table border="0" cellpadding="3" cellspacing="1" bgcolor="#0000aa">
<tr class="table-heading">
<th colspan="2">Repository UserIds</th>
</tr>
<tr class="table-heading">
<th>Project</th><th>UserId</th>
</tr>
<#if user.userPropertyList?exists & (user.userPropertyList.toList()?size > 0)>
<#assign row=0/>        
<#list user.userPropertyList.toList() as property>
<#if property.name?ends_with(".RepositoryId")>
<#if row % 2 == 1>
<tr class="tr-even">
<#else>
<tr class="tr-odd">
</#if>
<td>${property.name?replace(".RepositoryId", "")?html}</td>
<td>${property.value?html}</td>
</tr>        
<#assign row=row+1/>
</#if>
</#list>
<#if row==0>
<tr class="tr-odd">
<td colspan="2">No repository ids registered</td>
</tr>
</#if>
<#else>
<tr class="tr-odd">
<td colspan="2">No repository ids registered</td>
</tr>
</#if>
</table>
Project: <input tabindex='6' type="text" name="ProjectName" value="" size='25'/>
Repository id: <input tabindex='7' type="text" name="RepositoryId" value="" size='25'/>
<input tabindex='8' type='submit' name="AddRepositoryId" value="Add repository id"/>
<input tabindex='9' type='submit' name="RemoveRepositoryId" value="Remove repository id"/>
</form>
<br/>

<#-- User controlled display properies-->
<#--
<form name="DisplayProperties" id="DisplayProperties" method="post">
<table border='0'>
Look for *.PlotDuration or just PlotDuration in UserPropertyList
</table>
<input type='submit' name="UpdateDisplayProperties" value="Update display propertes"/>
</form>
<br/>
-->

<#-- Current set of roles for the user -->
<#--
<table border="0" cellpadding="3" cellspacing="1" bgcolor="#0000aa">
<tr class="table-heading">
<th>Roles</th>
</tr>
<#if user.roleList?exists & (user.roleList.toList()?size > 0)>
<#list user.roleList.toList() as role>
<#if role_index % 2 == 1>
<tr class="tr-even">
<#else>
<tr class="tr-odd">
</#if>
<td align='left'>
${role.name}
</td>
</tr>
</#list>
<#else>
<tr class="tr-odd"><td>No roles assigned</td></tr>
</#if>
</table>
<br/>
-->

<#-- Notifications -->
<#-- 
<table border="0" cellpadding="3" cellspacing="1" bgcolor="#0000aa">
<tr class="table-heading">
<th>Notifications</th>
</tr>
<tr><td class="tr-odd">No notifications specified</td></tr>
</table>
<br/>
-->

<#-- Queries -->
<#--
<table border="0" cellpadding="3" cellspacing="1" bgcolor="#0000aa">
<tr class="table-heading">
<th>Queries</th>
</tr>
<tr class="tr-odd"><td>No stored queries</td></tr>
</table>
<br/>
-->

<#-- Tools -->
<#if session?exists && session.user?exists>
  <#-- user logged in, check roles -->
  <#if realm.isUserInRole( session.user, "Dart.Administrator") || realm.isUserInRole( session.user, projectName + ".Administrator")>
  Administration tools:
  <ul>
    <li> <a href="/${projectName}/Admin/Admin">Administration Tools</a>
  </ul>
  <br/>
  </#if>
</#if>


<#--
<#if referer?exists && referer?has_content>
${referer?html}
</#if>
<a href="/TestProject/Dashboard">Test link</a>
-->

  </body>
</html>

