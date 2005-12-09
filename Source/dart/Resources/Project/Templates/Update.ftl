<#include "Macros.ftl"/>
<#assign client = submission.clientEntity/>
<#assign track = submission.trackEntity/>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0//EN">
<html>
  <head>
    <meta name="robots" content="noindex,nofollow">
    <meta http-equiv="Content-Type"
      content="text/html; charset=iso-8859-1">
    <meta http-equiv="Content-Script-Type" content="text/javascript">
    <title>Updates - ${client.site?html} - ${client.buildName?html} - ${submission.type?html} - ${submission.timeStamp?datetime?html}</title>
    <link rel="stylesheet" href="/${projectName}/Resources/Style.css" type="text/css">
    <link rel="shortcut icon" href="/${projectName}/Resources/Icons/favicon.ico" type="image/x-icon" />
<!--[if IE]>
    <script language="javascript" src="/${projectName}/Resources/cssMenuHelper.js" type="text/javascript"></script>
<![endif]-->
  </head>
<body>
<table border="0" cellpadding="0" cellspacing="2" width="100%">
<tr>
<td align="center" valign="middle" height="100%"><a href="/${projectName}/Dashboard/?trackid=${submission.trackId?url}"><img alt="Logo/Homepage link" src="/${projectName}/Resources/Icons/Logo.png" ></a>
</td>
<td align="left" width="100%" class="title">
<h2>${projectName?html} Update - ${client.site?html} - ${client.buildName?html} - ${submission.type?html}</h2>
<h3>${submission.timeStamp?datetime?html}</h3>
<@displayMenu />
</td>

</tr>
</table>

<div class="content">
<h3>Changed files as of ${submission.timeStamp?date?html}</h3>
<script type="text/javascript">
var Icons = "/${projectName}/Resources/Icons/";
</script>
<script type="text/javascript">
<!--
var total=1;
var db = new Array();

// -- Enter Values Here --
// Format: dbAdd(parent[true|false] , description, URL [blank for nohref], level , TARGET [blank for "content"], image [1=yes])

// Get current cookie setting
var current=getCurrState()
function getCurrState() {
  var label = "currState="
  var labelLen = label.length
  var cLen = document.cookie.length
  var i = 0
  while (i < cLen) {
    var j = i + labelLen
    if (document.cookie.substring(i,j) == label) {
      var cEnd = document.cookie.indexOf(";",j)
      if (cEnd == -1) { cEnd = document.cookie.length }
      return unescape(document.cookie.substring(j,cEnd))
    }
    i++
  }
  return ""
}

// Record current settings in cookie
function setCurrState(setting) {
  var expire = new Date();
  expire.setTime(expire.getTime() + ( 60*60*1000 ) ); // expire in 1 hour
  document.cookie = "currState=" + escape(setting) + "; expires=" + expire.toGMTString();
  }

// Add an entry to the database
function dbAdd(mother,display,URL,indent,top,open,author,mailto,comment) {
  db[total] = new Object;
  db[total].mother = mother
  db[total].display = display
  db[total].URL = URL
  db[total].indent = indent
  db[total].top = top
  db[total].open = open
  db[total].image = ""
  db[total].author = author
  db[total].mailto = mailto
  db[total].comment = comment
  total++
  }

// toggles an outline mother entry, storing new value in the cookie
function toggle(n) {
  if (n != 0) {
    var newString = ""
    var expanded = current.substring(n-1,n) // of clicked item
    newString += current.substring(0,n-1)
    newString += expanded ^ 1 // Bitwise XOR clicked item
    newString += current.substring(n,current.length)
    setCurrState(newString) // write new state back to cookie
  }
}

// Reload page
function reload() {
  //   if (navigator.userAgent.toLowerCase().indexOf('opera') == -1) {
  //        history.go(0);
  //     } else {   
      if (document.images) {
         location.replace(location.href);
      } else {
         location.href(location.href);
      }
  //     }
}

// returns padded spaces (in mulTIPles of 2) for indenting
function pad(n) {
  var result = ""
  for (var i = 1; i <= n; i++) { result += "&nbsp;&nbsp;&nbsp;&nbsp;" }
  return result
}

// Expand everything
function explode() {
  current = "";
  initState="";
  for (var i = 1; i < db.length; i++) { 
    initState += "1"
    current += "1"
    }
  setCurrState(initState);
  reload();
  }

// Collapse everything
function contract() {
  current = "";
  initState="";
  for (var i = 1; i < db.length; i++) { 
    initState += "0"
    current += "0"
    }
  setCurrState(initState);
  reload();
  }

function tree_close() {
  window.parent.location = window.parent.content.location;
  }

//end -->
</script>   
[<a href="javascript:reload()" onMouseOver="window.parent.status='Expand all';return true;" onClick="explode()">Expand all</a>&nbsp;|&nbsp;<a href="javascript:reload()" onMouseOver="window.parent.status='Collapse all';return true;" onClick="contract()">Collapse all</a>]

<p></p>

<!-- Function to construct appropriate url for web access to revision diffs -->
<#function GenerateURL project file revision prior>
<#if project == "" >
   <#assign key = ""/>
<#else>
   <#assign key = project + "."/>
</#if>
<#if projectProperties[key + "RepositoryURL"]?exists>
<#switch projectProperties[key + "RepositoryURL.Type"]>
   <#case "cvsweb">
     <#assign url = projectProperties[key + "RepositoryURL"] + file + "?r1=" + prior + "&r2=" + revision + "&cvsroot=" + projectProperties[key + "RepositoryURL.Repository"]>
     <#break>
   <#case "viewcvs">
     <#assign url = projectProperties[key + "RepositoryURL"] + file + "?r1=" + prior + "&r2=" + revision + "&cvsroot=" + projectProperties[key + "RepositoryURL.Repository"]>
     <#break>
   <#case "websvn">
     <#assign url = projectProperties[key + "RepositoryURL"] +"diff.php?repname=" + projectProperties[key + "RepositoryURL.Repository"] + "&path=" + file + "&rev" + revision>
     <#break>
   <#case "cvstrac">
     <#assign url = projectProperties[key + "RepositoryURL"] + "filediff?f=" + file + "&v1=" + prior + "&v2=" + revision>
     <#break>
   <#default>
</#switch>
</#if>
<#return url/>
</#function>

<#if submission.project?exists>
   <#assign project = submission.project/>
<#else>
   <#assign project = ""/>
</#if>

<#assign update = submission.selectTest ( ".Update.Update" )/>

<script LANGUAGE="JavaScript">
dbAdd (true, "Updated files  (${update.selectChildren().size()})", "", 0, "", "1", "", "", "")
<#if true>
<#list update.selectChildren().toList() as file>
     dbAdd ( false, 
"${file.getResultValue("Directory", "")?html}/${file.getResultValue ( "File", "Unknown File" )?html} Revision: ${file.getResultValue ( "Revision", "Unknown" )?html}", 
"${GenerateURL ( project, file.getResultValue ( "Directory", "" ) + "/" + file.getResultValue ( "File", "Unknown" ), file.getResultValue ( "Revision", "Unknown" ), file.getResultValue ( "PriorRevision", "Unknown" ) )}",
1,
"",
"1",
"${file.getResultValue ( "Author", "Unknown Author" )?html}",
"${file.getResultValue ( "Email", "Unknown Email" )?html}",
"${file.getResultValue ( "Log", "<Empty Log>" )?replace ( "\\s+", " ", "r" )?html}" )
</#list></#if>
      </script><script type="text/javascript">
<!--
	// Set the initial state if no current state or length changed
	if (current == "" || current.length != (db.length-1)) {
	current = ""
	initState = ""
	for (i = 1; i < db.length; i++) { 
	initState += db[i].open
	current += db[i].open
	}
	setCurrState(initState)
	}
	var prevIndentDisplayed = 0
	var showMyDaughter = 0
	// end -->
	 
	<!--
      	var Outline=""
	// cycle through each entry in the outline array
	for (var i = 1; i < db.length; i++) {
	  var currIndent = db[i].indent           // get the indent level
	  var expanded = current.substring(i-1,i) // current state
	 var top = db[i].top
		 if (top == "") { top="content" }
		// display entry only if it meets one of three criteria
			if ((currIndent == 0 || currIndent <= prevIndentDisplayed || (showMyDaughter == 1 && (currIndent - prevIndentDisplayed == 1)))) {
			Outline += pad(currIndent)

		// Insert the appropriate GIF and HREF
		 image = "Blank";
		 if (db[i].image==1) { image="_bullet"; }
		 if (db[i].image==2) { image="_search"; }
		 if (db[i].image==3) { image="_cal"; }
		 if (db[i].image==4) { image="_upd"; }
		 if (db[i].image==5) { image="_admin"; }
		 if (!(db[i].mother)) {
    Outline += ""
		  } 
		 else { 
		  if (current.substring(i-1,i) == 1) {
			Outline += "<A HREF=\"javascript:reload()\" onMouseOver=\"window.parent.status=\'Click to collapse\';return true;\" onClick=\"toggle(" + i + ")\">"
			Outline += "<IMG SRC=\"" + Icons + "Minus.gif\" WIDTH=16 HEIGHT=16 ><IMG SRC=\"" + Icons + "Open.gif\" WIDTH=16 HEIGHT=16 >"
			Outline += "</A>"
			}
		  else {
			Outline += "<A HREF=\"javascript:reload()\" onMouseOver=\"window.parent.status=\'Click to expand\';return true;\" onClick=\"toggle(" + i + ")\">"
			Outline += "<IMG SRC=\"" + Icons + "Plus.gif\" WIDTH=16 HEIGHT=16 ><IMG SRC=\"" + Icons + "Closed.gif\" WIDTH=16 HEIGHT=16 >"
			Outline += "</A>"
			}
		  }
		Outline += "&nbsp;";
     
		if (db[i].URL == "" || db[i].URL == null) {
		  Outline += " " + db[i].display      // no link, just a listed item  
		  }
		else {
		  Outline += " <A HREF=\"" + db[i].URL + "\">" + db[i].display + "</A>"
		  }
                if ( db[i].author != "" && db[i].author != null )
                {
                  if ( db[i].mailto == "" || db[i].mailto == null )
                  {
                    Outline += " by " + db[i].author
                  }
                  else
                  {
                    Outline += " by <a href=\"mailto:" + db[i].mailto + "\">" + db[i].author + "</a>"
                  }
                }
                if ( db[i].comment != null && db[i].comment != "" )
                {
                  Outline += "<br>" + pad(currIndent) + db[i].comment + "<br>"
                }
		// Bold if at level 0
		if (currIndent == 0) { 
		  Outline = "<B>" + Outline + "</B>"
		  }
//		if (currIndent == 1) {
//		    Outline += "&nbsp;<a href=details_project.html><img src=../images/document_select.gif  align=bottom></a>"
//		  }
		//if (currIndent == 2) {
		//  Outline += "&nbsp;&nbsp;<a href=overview.html><img src=../images/document_overv.gif ></a>&nbsp;<a href=list.html><img src=../images/document_list.gif ></a>"
		//  }
//		if (currIndent == 3) {
//		  Outline += "&nbsp;<a href=details_part.html><img src=../images/document_select.gif  align=bottom></a>"
//		  }
		//if (currIndent == 4) {
		//  Outline += "&nbsp;&nbsp;<a href=overview.html><img src=../images/document_overv.gif ></a>&nbsp;<a href=definition.html><img src=../images/document_definition.gif ></a>"
		//  }
		Outline += "<BR>"
		prevIndentDisplayed = currIndent
		showMyDaughter = expanded
		// if (i == 1) { Outline = ""}
		if (db.length > 25) {
		  document.write(Outline)
			 Outline = ""
								  }
								}
		 }
	document.write(Outline)
	// end -->

</script><br>
[<a href="javascript:reload()" onMouseOver="window.parent.status='Expand all';return true;" onClick="explode()">Expand all</a>&nbsp;|&nbsp;<a href="javascript:reload()" onMouseOver="window.parent.status='Collapse all';return true;" onClick="contract()">Collapse all</a>]
<br><br>
	<br/>
</div>
</body>
</html>
