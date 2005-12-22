<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <xsl:output method="xml"/>

  <xsl:template match="/coverage">
    <Coverage>
      <Test>
        <Name>.Coverage</Name>
        <Status>passed</Status>
        <Measurement name="ClassesChecked" type="numeric/integer"><xsl:number level="any" value="count(descendant::class)"/></Measurement>
        <Measurement name="PercentCoverage" type="numeric/float"><xsl:value-of select="100 * /coverage/@line-rate"/></Measurement>
      </Test>
    </Coverage>
  </xsl:template>

</xsl:stylesheet>
