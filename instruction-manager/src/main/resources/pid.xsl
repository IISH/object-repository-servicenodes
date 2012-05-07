<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pid="http://pid.socialhistoryservices.org/">
    <xsl:output omit-xml-declaration="yes"/>
    <xsl:template match="/">
        <xsl:value-of select="//pid:pid"/>
    </xsl:template>
</xsl:stylesheet>