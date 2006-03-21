<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0//EN">
<html>
  <head>
    <meta name="robots" content="noindex,nofollow">
    <meta http-equiv="Content-Type"
      content="text/html; charset=iso-8859-1">
    <title>Dart Login</title>
    <link rel="stylesheet" href="/DartServer/Resources/Style.css" type="text/css">
    <link rel="shortcut icon" href="/DartServer/Resources/Icons/favicon.ico" type="image/x-icon" />
  </head>
  <body bgcolor="#ffffff">

<table class="pagetitle">
<tr>
<td align="center"><a href="/"><img alt="Logo/Homepage link" src="/DartServer/Resources/Icons/Logo.png" border="0"></a>
</td>
<td align="left" width="100%" class="title">
<h2>Dart Login</h2>
<h3>Login or create a new account</h3>
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
   <li>Cookies must be enabled to log user.</li>
   <#break>
 <#case "2">
   <li>Error authenticating user.</li>
   <#break>
 <#case "3">
   <li>Cannot create user. A user with this email address already exists.</li>
   <#break>
 <#case "4">
   <li>Email address must be specified.</li>
   <#break>
 <#case "5">
   <li>Password cannot be empty.</li>
   <#break>
 <#case "6">
   <li>Retype of password cannot be empty.</li>
   <#break>
 <#case "7">
   <li>Password and retype of password do not match.</li>
   <#break>
 <#case "8">
   <li>First name must be specified.</li>
   <#break>
 <#case "9">
   <li>Last name must be specified.</li>
   <#break>
</#switch>
</#list>
</ul>
</div>
<br/>
</#if>

<form name="UserLogin" id="UserLogin" method="post">

<table border='0'>

<tr>
<td align='right'>Email:</td>
<td align='left'>
<#if session?exists && session.Email?exists>
<input tabindex='1' type='text' name="Email" value="${session.Email}" size='25' />
<#else>
<input tabindex='1' type='text' name="Email" value="" size='25' />
</#if>
</td>
<td align='left'>
<input tabindex='3' type='submit' name="Login" value="Log in" />
<input tabindex='8' type='submit' name="Logout" value="Log out" />
</td>
</tr>

<tr>
<td align='right'>Password:</td>
<td align='left'>
<input tabindex='2' type='password' name="Password" value="" size='25' />
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
<input tabindex='4' type='password' name="Retype" value="" size='25' />
</td>
<td align='left'>(new users only)</td>
</tr>

<tr>
<td align='right'>First name:</td>
<td align='left'>
<#if session?exists && session.First?exists>
<input tabindex='5' type='text' name="First" value="${session.First}" size='25' />
<#else>
<input tabindex='5' type='text' name="First" value="" size='25' />
</#if>
</td>
<td>&nbsp;</td>
</tr>

<tr>
<td align='right'>Last name:</td>
<td align='left'>
<#if session?exists && session.Last?exists>
<input tabindex='6' type='text' name="Last" value="${session.Last}" size='25' />
<#else>
<input tabindex='6' type='text' name="Last" value="" size='25' />
</#if>
</td>
<td align='left'>
<input tabindex='7' type='submit' name="CreateUser" value="Create new account" />
</td>
</tr>


</table>
</form>

<br/>

  </body>
</html>

