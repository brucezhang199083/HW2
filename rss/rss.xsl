<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/">
		<html>
			<head>
				<link rel="stylesheet" href="metro/css/metro-bootstrap.css"/>
			</head>
			<body class="metro" style="width:80%;">
				<header><h2>RSS Contains 'war' and 'peace'</h2></header>
				<xsl:for-each select="documentcollection/document/rss/channel">
					<div class="panel">
						<div class="panel-header">
							<a>
								<xsl:attribute name="href">
									<xsl:value-of select="./link"/>
								</xsl:attribute>
								<h3><xsl:value-of select="./title"/></h3>
							</a>
						</div>
						<div class="panel-content">
						<dl>
							<xsl:for-each select="./item">
								<xsl:variable name="haslink" select="./link"/>
								<xsl:variable name="hastitle" select="./title"/>
								<xsl:choose>
								<xsl:when test="(./link) and (./title)">
									<dt><a><xsl:attribute name="href"><xsl:value-of select="./link"/>
										</xsl:attribute>
										<xsl:value-of select="./title"/></a></dt>
								</xsl:when>
								<xsl:when test="not(./link)">
									<dt><xsl:value-of select="./title"/> (No link available)</dt>
								</xsl:when>
								<xsl:otherwise>
									<dt>Title Not Exist</dt>
								</xsl:otherwise>
								</xsl:choose>
								<xsl:choose>
								<xsl:when test="./description">
									<dd><xsl:value-of select="./description"/></dd>
								</xsl:when>
								<xsl:otherwise>
									<dd>Description Not Exist</dd>
								</xsl:otherwise>
								</xsl:choose>
							</xsl:for-each>
						</dl>
						</div>
					</div>
				</xsl:for-each>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>


