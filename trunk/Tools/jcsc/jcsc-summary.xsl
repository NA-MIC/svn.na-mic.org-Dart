<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="xml"/>
  <xsl:template match="/">
    <Style>
      <Test>
        <Name>.Style</Name>
        <Status>passed</Status>
        <Measurement name="FilesChecked" type="numeric/integer"><xsl:value-of select="jcsc/overview/classcount"/></Measurement>
        <Measurement name="LineCount" type="numeric/integer"><xsl:value-of select="jcsc/overview/linecount"/></Measurement>
        <Measurement name="Violations" type="numeric/integer"><xsl:value-of select="jcsc/overview/violationscount"/></Measurement>
      </Test>
    </Style>
  </xsl:template>
</xsl:stylesheet>
