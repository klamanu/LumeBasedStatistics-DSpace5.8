<!--

    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    http://www.dspace.org/license/

-->
<xsl:stylesheet
    xmlns:i18n="http://apache.org/cocoon/i18n/2.1"
    xmlns:dri="http://di.tamu.edu/DRI/1.0/"
    xmlns:mets="http://www.loc.gov/METS/"
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:dim="http://www.dspace.org/xmlns/dspace/dim"
    xmlns:mods="http://www.loc.gov/mods/v3"
    xmlns:xlink="http://www.w3.org/TR/xlink/"
    xmlns:fn="http://www.w3.org/2005/xpath-functions"
    xmlns:confman="org.dspace.core.ConfigurationManager"	
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:xhtml="http://www.w3.org/1999/xhtml"
    xmlns:util="org.dspace.app.xmlui.utils.XSLUtils">

    <xsl:output indent="yes"/>
		
	<!-- traduz um título na estatística -->
	<xsl:template match="/dri:document/dri:body/dri:div[@id='aspect.statistics.Stats.div.current']/dri:p[1]">
		<xsl:variable name="externalMetadataURL">
			<xsl:text>cocoon://metadata/</xsl:text>
			<xsl:value-of select="substring-before(/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='request'][@qualifier='URI'],'/stats')"/>
			<xsl:text>/mets.xml</xsl:text>
		</xsl:variable>
		<p class="ds-paragraph">
			<xsl:choose>
			<!-- se for item -->
				<xsl:when test="contains(/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='focus'][@qualifier='containerType'],'type:item')">
					<a href="{concat('/',substring-before(/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='request'][@qualifier='URI'],'/stats'))}">
						<xsl:copy-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='title'][last()]" />
					</a>
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name="traduzTituloComunidadeColecao">
						<xsl:with-param name="mets" select="document($externalMetadataURL)"/>
						<xsl:with-param name="counting" select="no"/>
					</xsl:call-template>      
				</xsl:otherwise>
			</xsl:choose>
				
		</p>
    </xsl:template>
		
	<!-- Traduz lista de estatísticas de download -->
	<xsl:template match="dri:list[@id='aspect.statistics.Downloads.list.estatisticas']//dri:xref">
		<xsl:call-template name="traduzTituloComunidadeColecaoComNome">
			<xsl:with-param name="originalTitle" select="./text()"/>
			<xsl:with-param name="originalHandle" select="substring-after(@target,'10183/')"/>
		</xsl:call-template>
	</xsl:template>
	
	<!-- Adiciona o link de estatíticas para os metadados author and subject em cima do título -->
	<!--<xsl:template match="/dri:document/dri:body/dri:div[@id='aspect.artifactbrowser.ConfigurableBrowse.div.browse-by-author' 
						or @id='aspect.artifactbrowser.ConfigurableBrowse.div.browse-by-subject']">
		<xsl:if test="contains(/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='request'][@qualifier='queryString'],'value=')">
			<div style="margin-bottom: 15px;">
				<div class="icon-bar-chart"></div>
				<a>
					<xsl:attribute name="href">
						<xsl:text>/browsestats?</xsl:text>
						<xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='request'][@qualifier='queryString']"/>
					</xsl:attribute>
					<i18n:text>xmlui.administrative.Navigation.statistics</i18n:text>
				</a>
			</div>
		</xsl:if>
		<div id="aspect_artifactbrowser_ConfigurableBrowse_div_browse-by-author" class="ds-static-div primary">
			<xsl:apply-templates />
		</div>
	</xsl:template>-->
	
	<!-- Adiciona o link de estatíticas para os metadados author, subject, and type abaixo do título -->
	<!--<xsl:template match="/dri:document/dri:body/dri:div[@id='aspect.artifactbrowser.ConfigurableBrowse.div.browse-by-author' 
						or @id='aspect.artifactbrowser.ConfigurableBrowse.div.browse-by-subject'
						or @id='aspect.artifactbrowser.ConfigurableBrowse.div.browse-by-tipo']/dri:div[@rend='browse-navigation-wrapper hidden-print']">-->
	<xsl:template match="/dri:document/dri:body/dri:div[@id='aspect.artifactbrowser.ConfigurableBrowse.div.browse-by-author' 
						or @id='aspect.artifactbrowser.ConfigurableBrowse.div.browse-by-subject']/dri:div[@rend='browse-navigation-wrapper hidden-print']">
		<script type="text/javascript">		
			<xsl:text>function dynlink(handle,query)
			{
				if(handle.includes('handle'))
				{
					window.location.href = ('browsestats?').concat(query);
				}
				else
				{
					window.location.href = handle.concat('stats?').concat(query);
				}
			}
			</xsl:text>
		</script>
		<xsl:if test="contains(/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='request'][@qualifier='queryString'],'value=')">
			<!--<div id="statisticsLinkLume" class="ds-static-div primary repository community">-->
			<div style="margin-bottom: 15px;">
				<a>
					<xsl:attribute name="href">
						<xsl:text>#</xsl:text>
					</xsl:attribute>
					<xsl:attribute name="rel">
						<xsl:text>nofollow</xsl:text>
					</xsl:attribute>
					<xsl:attribute name="onclick">
						<xsl:text>dynlink('</xsl:text>
						<xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@qualifier='URI']"/>
						<xsl:text>','</xsl:text>
						<xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@qualifier='queryString']"/>
						<xsl:text>')</xsl:text>
					</xsl:attribute>
					<span>
						<div class="icon-bar-chart" style="margin-right:3px;"></div>
						<!--<i18n:text>xmlui.administrative.Navigation.statistics</i18n:text>-->
						<xsl:choose>
							<xsl:when test="/dri:document/dri:body/dri:div[@id='aspect.artifactbrowser.ConfigurableBrowse.div.browse-by-author']">
								<i18n:text>xmlui.ArtifactBrowser.ItemViewer.exibir_estatisticas_autor</i18n:text>
							</xsl:when>
							<xsl:otherwise>
								<i18n:text>xmlui.ArtifactBrowser.ItemViewer.exibir_estatisticas_assunto</i18n:text>
							</xsl:otherwise>
						</xsl:choose>
					</span>
				</a>
			</div>
		</xsl:if>
		<div class="ds-static-div browse-navigation-wrapper hidden-print" >
			<xsl:apply-templates />
		</div>
	</xsl:template>
	
	<!-- Adiciona o link de estatíticas para os metadados author and subject quando possui conteúdo no parâmetro query e resultados em cima do título-->
	<!--<xsl:template match="/dri:document/dri:body/dri:div[@id='aspect.discovery.SimpleSearch.div.search']">
		<xsl:if test="not(contains(/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='request'][@qualifier='queryString'],'query=&amp;'))
	and string-length(substring-after(/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='request'][@qualifier='queryString'],'query=')) > 0
	and count(/dri:document/dri:body/dri:div/dri:div/dri:list/dri:list[@id='aspect.discovery.SimpleSearch.list.item-result-list']/dri:list) > 0">
			<div style="margin-bottom: 15px;">
				<div class="icon-bar-chart"></div>
				<a>
					<xsl:attribute name="href">
						<xsl:text>/querystats?</xsl:text>
						<xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='request'][@qualifier='queryString']"/>
					</xsl:attribute>
					<i18n:text>xmlui.administrative.Navigation.statistics</i18n:text>
				</a>
			</div>
		</xsl:if>
		<div id="aspect_discovery_SimpleSearch_div_search" class="ds-static-div primary">
			<xsl:apply-templates />
		</div>
	</xsl:template>-->
	
	<!-- Adiciona o link de estatíticas para os metadados author and subject quando possui conteúdo no parâmetro query e resultados abaixo do título -->
	<xsl:template match="/dri:document/dri:body/dri:div[@id='aspect.discovery.SimpleSearch.div.search']/dri:div[@id='aspect.discovery.SimpleSearch.div.discovery-search-box']">
		<script type="text/javascript">		
			<xsl:text>function dynlink(query)
			{
				window.location.href = 'querystats?'.concat(query);
			}
			</xsl:text>
		</script>

	<!-- 
		not(contains(/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='request'][@qualifier='queryString'],'query=&amp;')) -> Verifica se a query não está vazia
		string-length(substring-after(/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='request'][@qualifier='queryString'],'query=')) > 0 -> Verifica se possui algum conteúdo em query
		count(//dri:cell[@rend='discovery-filter-input-cell']) > 1 -> conta quantos filtros avançados foram submetidos
		count(/dri:document/dri:body/dri:div/dri:div/dri:list/dri:list[@id='aspect.discovery.SimpleSearch.list.item-result-list']/dri:list) > 0 -> verifica se há algum resultado
		
	-->
		<xsl:if test="count(/dri:document/dri:meta/dri:pageMeta/dri:trail[@target]) = 1">
			<xsl:if test="((not(contains(/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='request'][@qualifier='queryString'],'query=&amp;'))
		and string-length(substring-after(/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='request'][@qualifier='queryString'],'query=')) > 0)
		or count(//dri:cell[@rend='discovery-filter-input-cell']) > 1)
		and count(/dri:document/dri:body/dri:div/dri:div/dri:list/dri:list[@id='aspect.discovery.SimpleSearch.list.item-result-list']/dri:list) > 0">
				
				<!-- <div id="statisticsLinkLume" class="ds-static-div primary repository community">-->
				<div style="margin-bottom: 15px;">
					<a>
						<xsl:attribute name="href">
							<xsl:text>#</xsl:text>
						</xsl:attribute>
						<xsl:attribute name="rel">
							<xsl:text>nofollow</xsl:text>
						</xsl:attribute>
						<xsl:attribute name="onclick">
							<xsl:text>dynlink('</xsl:text>
							<xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='request'][@qualifier='queryString']"/>
							<xsl:text>')</xsl:text>
						</xsl:attribute>
						<span>
							<div class="icon-bar-chart" style="margin-right:3px;"></div>
							<!--<i18n:text>xmlui.administrative.Navigation.statistics</i18n:text>-->
							<i18n:text>xmlui.ArtifactBrowser.ItemViewer.exibir_estatisticas_termo</i18n:text>
						</span>
					</a>
				</div>		
				
			</xsl:if>
		</xsl:if>
		<div id="aspect_discovery_SimpleSearch_div_discovery-search-box" class="ds-static-div discoverySearchBox">
			<xsl:apply-templates />
		</div>
	</xsl:template>
	
</xsl:stylesheet>
