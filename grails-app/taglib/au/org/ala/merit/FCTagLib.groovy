package au.org.ala.merit

import au.org.ala.cas.util.AuthenticationCookieUtils
import au.org.ala.merit.SettingPageType
import bootstrap.Attribute
import grails.converters.JSON
import groovy.xml.MarkupBuilder
import org.apache.commons.lang.WordUtils
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap

class FCTagLib {

    static namespace = "fc"

    def commonService
    def userService
    def settingService

    def textField = { attrs ->
        def outerClass = attrs.remove 'outerClass'
        if (outerClass) {
            out << """<div class="${outerClass}">"""
        }
        def id = attrs.id ?: attrs.name
        def forAttr = id ? " for='${id}'" : ''
        def label = attrs.remove 'label'
        if (label) {
            out << """<label class="control-label" ${forAttr}>${label}</label>"""
        }
        def value = attrs.remove 'value'
        //println "${id}: ${value?.toString()}: ${value?.getClass()}"
        if (!value || value == 'null' || value?.toString() == 'null') { value = ""}
        def classes = attrs.remove('class')
        if (outerClass) {
            if (classes) {
                classes += ' span12'
            } else {
                classes = 'span12'
            }
        }
        log.trace "data-bind = " + attrs['data-bind']
        //def dataBind = attrs.remove('data-bind')

        //out << "<div class='controls'>"
        out << "<input type='text' class='${classes}' value='${value}' " << Attribute.outputAttributes(attrs, out) << "/>"
        //out << "</div>"

        if (outerClass) {
            out << """</div>"""
        }
    }

    /**
     * @attr outerClass
     * @attr id
     * @attr name
     * @attr rows
     * @attr printable
     */
    def textArea = { attrs ->
        def outerClass = attrs.remove 'outerClass'
        def isprint = attrs.printable
        if (outerClass) {
            out << """<div class="${outerClass}">"""
        }
        def id = attrs.id ?: attrs.name
        def label = attrs.remove 'label'
        if (label) {
            out << """<label class="control-label" for="${id}">${label}</label>"""
        }
        def rows = attrs.rows ?: 3
        def value = attrs.remove 'value'
        if (!value || value == 'null' || value?.toString() == 'null') { value = ""}

        //out << "<div class='controls'>"
        out << "<textarea name='${id}' rows='${rows}' " << Attribute.outputAttributes(attrs, out) << ">${value}</textarea>"
        //out << "</div>"

        if (outerClass) {
            out << """</div>"""
        }
    }

    /**
     *
     */
    def select ={ attrs ->
        def isprint = attrs.printable
        def mb = new MarkupBuilder(out)

        mb.setEscapeAttributes(false)

        if (!isprint) {
            mb.select(attrs) {
                mkp.yieldUnescaped("&nbsp;")
            }
        } else {
            mb.span(class:'span12 printed-form-field') {
                mkp.yieldUnescaped("&nbsp;")
            }
        }

    }

    /**
     * @attr name
     * @attr targetField
     * @attr printable
     * @attr size optionally overrides the bootstrap size class for the input
     */
    def datePicker = { attrs ->
        /**
            <input data-bind="datepicker:startDate.date" name="startDate" id="startDate" type="text" size="16"
                data-validation-engine="validate[required]" class="input-xlarge"/>
            <span class="add-on open-datepicker"><i class="icon-th"></i></span>
         */

        def mb = new MarkupBuilder(out)

        if (!attrs.printable) {
            def inputAttrs = [
                "data-bind":"datepicker:${attrs.targetField}",
                name:"${attrs.name}",
                id:"${attrs.id ?: attrs.name}",
                type:'text',
                size:'16',
                class: attrs.size ?: 'input-xlarge'
            ]

            def ignoreList = ['name', 'id']
            attrs.each {
                if (!ignoreList.contains(it.key)) {
                    inputAttrs[it.key] = it.value
                }
            }

            if (attrs.required) {
                inputAttrs["data-validation-engine"] = "validate[required]"
            }

            mb.input(inputAttrs) {
            }

            mb.span(class:'add-on open-datepicker') {
                mb.i(class:'icon-th') {
                    mkp.yieldUnescaped("&nbsp;")
                }
            }
        } else {
            def inputAttrs = [
                name:"${attrs.name}",
                id:"${attrs.id ?: attrs.name}",
                class: (attrs.size ?: 'span6') + ' printed-form-field'
            ]

            def ignoreList = ['name', 'id']
            attrs.each {
                if (!ignoreList.contains(it.key)) {
                    inputAttrs[it.key] = it.value
                }
            }
            mb.span(inputAttrs) {
                mkp.yieldUnescaped("&nbsp;")
            }
        }
    }

    /**
     * @attr title
     * @attr printable Is this being printed?
     * body content should contain the help text
     */
    def iconHelp = { attrs, body ->
        if (!attrs.printable) {
            MarkupBuilder mb = new MarkupBuilder(out)
            String title = message(code:attrs.titleCode, default: attrs.title)
            String helpText
            if (attrs.helpTextCode) {
                helpText = message(code:attrs.helpTextCode)
            }
            else {
                helpText = body()
            }
            Map anchorAttrs = [href:'javascript:void(0);', class:'helphover', 'data-original-title':title, 'data-placement':'top', 'data-content':helpText, 'data-trigger':'click']
            if (attrs.container) {
                anchorAttrs << ['data-container':attrs.container]
            }
            if (attrs.html) {
                anchorAttrs << ['data-html':'true']
            }
            mb.a(anchorAttrs) {
                i(class:'fa fa-question-circle') {
                    mkp.yieldUnescaped("&nbsp;")
                }
            }
        }
    }

    def initialiseState = { attrs, body ->
        switch (body()) {
            case 'Queensland': out << 'QLD'; break
            case 'Victoria': out << 'VIC'; break
            case 'Tasmania': out << 'TAS'; break
            default:
                def words = body().tokenize(' ')
                out << words.collect({it[0]}).join()
        }
    }

    /**
     * @attr active
     */
    def navbar = { attrs ->

        def navbarConfig = [
                home:[
                        //icon:"icon-home",
                        link:createLink(controller: 'home'),
                        cssClass:'visible-desktop',
                        label:'Home'
                ],
                projectExplorer:[
                        //icon:"icon-search",
                        link:createLink(controller: 'home', action:'projectExplorer'),
                        cssClass:'visible-desktop',
                        label:'Project Explorer'
                ],
                about:[
                        //icon:"icon-info-sign",
                        link:createLink(controller: 'home', action: 'about'),
                        label:'About'
                ],
                help:[
                        //icon:"icon-question-sign",
                        link:createLink(controller: 'home', action: "help"),
                        label:'Help'
                ],
                contacts:[
                        //icon:"icon-envelope",
                        link:createLink(controller: 'home', action: 'contacts'),
                        label:'Contacts'
                ]
        ]

        def navDefaults = ['home', 'projectExplorer', 'about', 'help', 'contacts']
        def navItems = attrs.items ?: navDefaults

        def mb = new MarkupBuilder(out)

        navItems.each { navItem ->

            def config = navbarConfig[navItem]
            mb.li(class:attrs.active == navItem ? 'active' : '') {
                a(href:config.link, class:config.classClass?:'') {

                    i(class:config.icon) {
                        mkp.yieldUnescaped("&nbsp;")
                    }
                    mkp.yieldUnescaped("&nbsp;")
                    mkp.yield(message(code:'default.'+navItem+'.label', default: config.label))
                }
            }
        }

    }

    def navSeparator = { attrs, body ->
        out << "&nbsp;&#187;&nbsp;"
    }

    /**
     * @attr active
     * @attr title
     * @attr href
     */
    def breadcrumbItem = { attrs, body ->
        def active = attrs.active
        if (!active) {
            active = attrs.title
        }
        def current = pageProperty(name:'page.pageTitle')?.toString()

        def mb = new MarkupBuilder(out)
        mb.li(class: active == current ? 'active' : '') {
            a(href:attrs.href) {
                i(class:'icon-chevron-right') { mkp.yieldUnescaped('&nbsp;')}
                mkp.yield(attrs.title)
            }
        }
    }

    /**
     * Output search counts in the form of:
     *
     * "Showing 1 to 10 of 133 for "
     * TODO: encode words as i18n props
     *
     * @attr offset
     * @attr max
     * @attr total
     */
    def searchResultsCounts = { attrs, body ->
        def first = (attrs.offset?.toInteger()?:0) + 1
        def last = (attrs.offset?.toInteger()?:0) + attrs.max?.toInteger()?:10
        def total = attrs.total?.toInteger()
        def highest = (total > last) ? last : total

        if (total > 0) {
            out << "Showing " + first + " to " + highest + " of " + total + " for "
        } else {
            out << "No records found for "
        }
    }

    /**
     * Tag to simplify formatting a date from string
     *
     * <g:formatDate date="${Date.parse("yyyy-MM-dd'T'HH:mm:ss'Z'" , dateString)}" format="yyyy-MM-dd"/>
     *
     * @attr format
     * @attr date REQUIRED
     * @attr inputFormat optional
     *
     */
    def formatDateString = { attrs, body ->
        def inputFormat = attrs.inputFormat?:"yyyy-MM-dd'T'HH:mm:ss'Z'"
        def formattedDate = g.formatDate(
                date: Date.parse(inputFormat, attrs.date),
                format: attrs.format?:"yyyy-MM-dd"
        )
        out << formattedDate
    }

    /**
     * Given an entityType (e.g. au.org.ala.ecodata.Activity) output its controller name
     * for use in a g:link taglib.
     *
     * @attr entityType REQUIRED
     */
    def getControllerNameFromEntityType = { attrs, body ->
        def entityType = attrs.entityType
        def bits = entityType.tokenize(".")
        //log.debug "entityType = $entityType || bits = $bits"
        def controllerName

        if (bits.size() > 2) {
            controllerName = bits[-1].toLowerCase()
        }
        out << controllerName?:entityType
    }

    /**
     * Generate query string from params map, with optional requiredParams (comma separated)
     * list of params to include and optional excludeParam param string to remove/exclude from
     * the query string.
     *
     * @attr params REQUIRED
     * @attr requiredParams
     * @attr excludeParam
     */
    def formatParams = { attrs, body ->
        def outputParams = new GrailsParameterMap([:], request)
        def params = attrs.params
        def requiredList = attrs.requiredParams?.tokenize(",").collect { it.toLowerCase().trim() }

        params.each { k,v ->
            def vL = [v].flatten().findAll { it != null } // String[] and String both converted to List
            def includeThis = false

            // check if param name is needed
            if (!(requiredList && !requiredList.contains(k?.toLowerCase()))) {
                includeThis = true
            }

            // check against the excludeParams
            def vL2 = vL.findAll { it ->
                it != attrs.excludeParam
            }

            log.debug "$k - includeThis = $includeThis || vL2 = $vL2"

            if (includeThis && vL2) {
                outputParams.put(k, vL2)
            }
        }

        out << commonService.buildUrlParamsFromMap(outputParams)
    }

    /**
     * Output appropriate class names for CSS display of sorted column and sort direction
     *
     * @attr params REQUIRED
     * @attr field REQUIRED
     */
    def getSortCssClasses = { attrs, body ->
        def params = attrs.params
        def thisField = attrs.field
        def output = "header"

        if (thisField == params.sort) {
            if (params.order == "DESC") {
                output += " headerSortUp"
            } else {
                output += " headerSortDown"
            }
        }

        out << output
    }

    def currentUserDisplayName = { attrs, body ->
        def username = userService.currentUserDisplayName
        if (username) {
            def mb = new MarkupBuilder(out)
            mb.span(class:'username') {
                mkp.yield(username)
            }
        }
    }

    def currentUserId = { attrs, body ->
        def userId = userService.currentUserId
        if (userId) {
            out << userId
        }
    }

    def userDisplayName = { attrs, body ->
        def user = userService.lookupUser(attrs.userId)
        if (user) {
            out << user.displayName
        }
        else {
            out << attrs.defaultValue ?: ''
        }
    }

    /**
     * FC version of loginlogout taglib from ala-web-theme. Adds icon and button to link
     *
     * @attr logoutUrl
     * @attr loginReturnToUrl
     * @attr logoutReturnToUrl
     * @attr casLoginUrl
     * @attr casLogoutUrl
     * @attr cssClass
     */
    def loginLogoutButton = { attrs, body ->
        def serverUrl = grailsApplication.config.grails.serverURL
        def requestUri = removeContext(serverUrl) + request.forwardURI
        def logoutUrl = attrs.logoutUrl ?: serverUrl + "/session/logout"
        def loginReturnToUrl = attrs.loginReturnToUrl ?: requestUri
        def logoutReturnToUrl = attrs.logoutReturnToUrl ?: params.returnTo ?: requestUri
        def casLoginUrl = attrs.casLoginUrl ?: grailsApplication.config.security.cas.loginUrl ?: "https://auth.ala.org.au/cas/login"
        def casLogoutUrl = attrs.casLogoutUrl ?: grailsApplication.config.security.cas.logoutUrl ?: "https://auth.ala.org.au/cas/logout"
        def cssClass = attrs.cssClass?:"btn btn-small btn-inverse btn-login"
        def output

        def username = userService.currentUserDisplayName
        if (username) {
            output = "<a id='logout-btn' href='${logoutUrl}" +
                    "?casUrl=${casLogoutUrl}" +
                    "&appUrl=${logoutReturnToUrl}' " +
                    "class='${cssClass}'><i class=\"fa fa-sign-in\"></i> Logout</a>"
        } else {
            // currently logged out
            output =  "<a href='${casLoginUrl}?service=${loginReturnToUrl}' class='${cssClass}'><span><i class=\"fa fa-sign-in\"></i> Log in</span></a>"
        }
        out << output
    }

    def loginInNewWindow = { attr, body ->
        def casLoginUrl = grailsApplication.config.security.cas.loginUrl ?: "https://auth.ala.org.au/cas/login"
        out << "<a href=\"${casLoginUrl}?service=${createLink(absolute: true, controller: 'home', action:'close')}\" target=\"fieldcapture-login\">${body}</a>"
    }

    def userIsLoggedIn = { attr ->
        if (AuthenticationCookieUtils.cookieExists(request, AuthenticationCookieUtils.ALA_AUTH_COOKIE)) {
            out << true
        }
    }

    /**
     * Check if the logged in user has the requested role
     *
     * @attr role REQUIRED
     */
    def userInRole = { attrs ->
        if (userService.userInRole(attrs.role)) {
            out << true
        }
    }

    /**
     * Check if the logged in user is a site admin
     *
     */
    def userIsSiteAdmin = { attrs ->
        if (userService.userIsSiteAdmin()) {
            out << true
        }
    }

    def userIsAlaOrFcAdmin = { attrs ->
        if (userService.userIsAlaOrFcAdmin()) {
            out << true
        }
    }

    /**
     * Build HTML for drop down menu "My projects"
     */
    def userProjectList = { attrs ->
        def user = userService.user
        def maxItems = 10 // total for both list combined
        def mb = new MarkupBuilder(out)
        // common code as closures
        def maxItemsLink = {
            mb.p {
                a(href: g.createLink(controller: "user"), "[showing top ${maxItems} - see full list]")
            }
        }
        def listItem = { p ->
            mb.li {
                span {
                    a(href: g.createLink(controller: 'project', id: p.projectId), p.name)
                }
            }
        }

        if (user) {
            def j = 0 // keeps track of navItem count for both lists
            // Active projects
            def memberProjects = userService.getProjectsForUserId(user.userId)
            mb.div(class:'listHeading') { mkp.yield("Active projects") }
            mb.ul {
                memberProjects.eachWithIndex { p, i ->
                    if (j < maxItems) {
                        if(p && p.project){
                            listItem(p.project)
                        }
                        j++
                    }
                }?:mkp.yield("[No active projects]")
            }
            if (memberProjects.size() >= maxItems) {
                maxItemsLink()
                return // don't show starred projects as we've exceeded limit
            }
            // Starred projects
            def starredProjects = userService.getStarredProjectsForUserId(user.userId)
            mb.div(class:'listHeading') { mkp.yield("Favourite projects") }
            mb.ul {
                starredProjects.eachWithIndex { p, i ->
                    if (j < maxItems) {
                        if(p){
                            listItem(p)
                        }
                        j++
                    }
                }?:mkp.yield("[No starred projects]")
            }
            //if (j == maxItems) {
            if (memberProjects.size() + starredProjects.size() > maxItems) {
                maxItemsLink()
            }
        } else {
            mb.div { mkp.yield("Error: User not found") }
        }
    }

    /**
     * Build HTML for drop down menu "My Organisations"
     */
    def userOrganisationList = { attrs ->
        def user = userService.user
        def maxItems = 10 // total for both list combined
        def mb = new MarkupBuilder(out)
        // common code as closures
        def maxItemsLink = {
            mb.li (class:'dropdown-item') {
                a(href: g.createLink(controller: "user"), "[showing top ${maxItems} - see full list]")
            }
        }
        def listItem = { org ->
            mb.li(class:'dropdown-item') {
                a(href: g.createLink(controller: 'organisation', id: org.organisationId), org.name)
            }
        }

        if (user) {
            List memberOrgs = userService.getOrganisationsForUserId(user.userId)
            mb.ul(class:'dropdown-menu') {
                memberOrgs.eachWithIndex { org, i ->
                    if (i < maxItems) {
                        if (org && org.organisation){
                            listItem(org.organisation)
                        }
                    }
                }
                if (memberOrgs.size() >= maxItems) {
                    maxItemsLink()
                }
                if (memberOrgs.size() == 0) {
                    mb.li("[You aren't a member of any organisations]")
                }
                mb.hr()
                mb.li(class:'dropdown-item') {
                    a(href:g.createLink(controller:'organisation', action:'list'), "Find an organisation here")
                }
            }


        } else {
            mb.div { mkp.yield("Error: User not found") }
        }
    }

    def modelAsJavascript = { attrs ->
        def model = attrs.model
        if (!(model instanceof JSONObject) && !(model instanceof JSONArray) && !(model instanceof grails.converters.JSON)) {
            model = model as JSON

        }
        def json = (model?:attrs.default != null? attrs.default:[:] as JSON)
        def modelJson = json.toString()
        out << "JSON.parse('${modelJson.encodeAsJavaScript()}')"
    }

    def renderProject = { attrs ->
        Map project = attrs.project
        project.sites?.each { site ->
            site.remove('documents')
        }
        project.activities?.each { activity ->
            activity.documents = new JSONArray(activity.documents?.findAll{it.role != 'photoPoint'})
        }

        out << fc.modelAsJavascript(model:project)
    }

    /**
     * Remove the context path and params from the url.
     * @param urlString
     * @return
     */
    private String removeContext(urlString) {
        def url = urlString.toURL()
        def protocol = url.protocol != -1 ? url.protocol + "://" : ""
        def port = url.port != -1 ? ":" + url.port : ""
        return protocol + url.host + port
    }

    def toSingleWord = { attrs, body ->
        def name = attrs.name ?: body()
        out << name.replaceAll(' ','_')
    }


    def renderJsonObject = { attrs ->
        def object = attrs.object as JSONObject
        if (object) {
            def mb = new MarkupBuilder(out)
            renderObject(object, mb)
        }
    }

    def footerContent = { attrs ->
        def content = settingService.getSettingText(SettingPageType.FOOTER) as String
        if (content) {
            out << content.markdownToHtml()
        }
    }

    def announcementContent = { attrs ->
        def content = settingService.getSettingText(SettingPageType.ANNOUNCEMENT) as String
        if (content) {
            out << content.markdownToHtml()
        }
    }

    def homePageTitle = { attrs ->
        def content = settingService.getSettingText(SettingPageType.TITLE) as String
        if (content) {
            out << content
        }
    }

    /**
     * Output HTML content for the requested SettingPageType
     *
     * @attr settingType REQUIRED
     */
    def getSettingContent = { attrs ->
        SettingPageType settingType = attrs.settingType
        def content = settingService.getSettingText(settingType) as String
        if (content) {
            out << content.markdownToHtml()
        }
    }

    /** Render a list of bootstrap tabs based on content definitions */
    def tabList = {attrs ->

        def mb = new MarkupBuilder(out)
        attrs.tabs.each { name, details ->

            if (details.type == 'tab' && details.visible) {
                def liClass = details.default ? 'active':''
                def linkAttributes = [href:'#'+name, id:name+'-tab']
                if (!details.disabled) {
                    linkAttributes << ["data-toggle":"tab", class:'nav-link ']
                }

                mb.li(class:'nav-item '+liClass) {
                    a(linkAttributes, details.label)
                }
            }
        }
    }

    /** Render the content for a list of tabs based on content definitions */
    def tabContent = {attrs ->

        def tabClass = attrs.tabClass ?: 'tab-pane'
        attrs.tabs.each { name, details ->

            if (details.type == 'tab' && details.visible && !details.disabled) {
                def divClass = details.default ? "${tabClass} active":tabClass
                if (details.stopBinding) {
                    out << "<!-- ko stopBinding:true -->"
                }
                out << """<div id="${name}" class="${divClass}">"""
                out << g.render(template:details.template?:name, model:details)
                out << "</div>"
                if (details.stopBinding) {
                    out << "<!-- /ko -->"
                }
            }
        }
    }

    def truncate = { attrs ->
        String value = attrs.value
        int maxLength = Integer.parseInt(attrs.maxLength)

        String result = value

        if (value?.length() > maxLength) {
            result = value.substring(0, maxLength-3) + "..."
        }

        out << result
    }

    private void renderObject(Object object, MarkupBuilder mb) {

        if (object instanceof JSONObject) {
            mb.ul() {
                object.keys().each { key ->
                    def value = object.get(key)
                    mb.li() {
                        mkp.yield("${key}")
                        renderObject(value, mb)
                    }
                }
            }
        } else if (object instanceof JSONArray) {
            mb.ul() {
                def arr = object as JSONArray
                arr.eachWithIndex { def entry, int i ->
                    mb.li() {
                        renderObject(entry, mb)
                    }
                }
            }
        } else {
            mb.span() {
                mb.strong() {
                    mkp.yield(object?object.toString():'null')
                }
            }

        }
    }

    def attributeSafeValue = { attrs ->
        String value = attrs.value
        if (value) {
            out << value.replaceAll(/[^A-Za-z0-9]/, "")
        }
    }

    /**
     * Renders the value of a site geographic facet, accepting either a String or List typed value.
     * Both the value and label will be looked up in the message.properties under the key 'label.'<facetName/value>
     *
     * @param site the site to render the value of
     * @param facet the name of the facet to render
     * @param label optionally override the facet name
     * @param max the maximum number of values to display if the facet value is List typed
     */
    def siteFacet = {attrs ->
        Map site = attrs.site
        String facetName = attrs.facet

        MarkupBuilder mb = new MarkupBuilder(out)
        Map geom =  site?.extent?.geometry?:[:]

        Object facetValue = geom[facetName]
        if (facetValue) {
            String label = attrs.label ?: g.message(code:'label.'+facetName+'Facet', default:facetName)
            mb.span(class:"label label-success", label)

            if (!(facetValue instanceof List)) {
                facetValue = [facetValue]
            }
            StringBuilder value = new StringBuilder()
            int max = attrs.max ? Math.min(Integer.parseInt(attrs.max), facetValue.size()):facetValue.size()
            for (int i in 0..(max-1)) {
                def defaultValue = facetValue[i]
                if (attrs.titleCase) {
                    defaultValue = WordUtils.capitalize(defaultValue?defaultValue.toLowerCase():'')
                }

                value.append(g.message(code:'label.'+facetValue[i], default:defaultValue))
                if (i < max-1) {
                    value.append(', ')
                }

            }
            out << " "+value.toString()
        }
    }

    def documentType = {attrs ->
        Map document = attrs.document
        if (document.type == 'image') {
            out << 'image'
        }
        else {
            if (document.filename) {
                int i = document.filename.lastIndexOf('.')
                if (i >= 0 && i<document.filename.length() -1) {
                    out << document.filename.substring(i, document.filename.length())
                }
                else {
                    out << document.type
                }
            }
        }
    }

    def comparisonClass = { attrs ->

        if (attrs.current != null && attrs.previous != null) {
            if (attrs.current > attrs.previous) {
                out << "up"
            }
            else if (attrs.current < attrs.previous) {
                out << "down"
            }
        }

    }

    def renderComparison = { attrs ->

        List original = attrs.original
        List changed = attrs.changed
        int i = attrs.i
        String property = attrs.property

        out << '<span class="original hide">'
        if (original && original.size() > i) {
            out << original[i][property]
        }
        out << '</span>'
        out << '<span class="changed hide">'
        if (changed && changed.size() > i) {
            out << changed[i][property]
        }
        out << '</span>'
        out << '<span class="diff"></span>'
    }

    def status = { attrs ->
        String statusClass
        switch (attrs.status?.toLowerCase()) {
            case 'active':
                statusClass = 'badge-success'
                break
            case 'completed':
                statusClass = 'badge-info'
                break
        }

        MarkupBuilder mb = new MarkupBuilder(out)
        mb.span(class:'status badge '+statusClass, attrs.status)
    }

    def projectFunding = { attrs ->
        Map project = attrs.project
        out << (project?.custom?.details?.budget?.overallTotal?:project.funding)?:0

    }

    def displayDate = {}
}
