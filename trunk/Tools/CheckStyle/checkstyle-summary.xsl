<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="xml"/>
<xsl:template match="/">
  <CheckStyle>
    <Test>
      <Name>Style.CheckStyle</Name>
      <Status>passed</Status>
      <Measurement name="FilesChecked" type="numeric/integer"><xsl:number level="any" value="count(descendant::file)"/></Measurement>
      <Measurement name="FilesWithErrors" type="numeric/integer"><xsl:number level="any" value="count(descendant::file[error])"/></Measurement>
      <Measurement name="TotalErrors" type="numeric/integer"><xsl:number level="any" value="count(descendant::error)"/></Measurement>
    </Test>
  </CheckStyle>
</xsl:template>

</xsl:stylesheet>
