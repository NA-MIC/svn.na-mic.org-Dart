<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0//EN">
<html>
  <head>
    <meta name="robots" content="noindex,nofollow">
    <meta http-equiv="Content-Type"
      content="text/html; charset=iso-8859-1">
    <title>Population View</title>
    <link rel="stylesheet" href="/${qedName}/Resources/Style.css" type="text/css">
    <link rel="shortcut icon" href="/${qedName}/Resources/Icons/favicon.ico" type="image/x-icon" />
  </head>
  <body bgcolor="#ffffff">

    <#list populations.toList() as pop>
    ${pop.getName()}<br>
    </#list>
  </body>
</html>

