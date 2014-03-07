<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
  <meta name="layout" content="${grailsApplication.config.layout.skin?:'main'}"/>
  <title>${project?.name.encodeAsHTML()} | Project | Field Capture</title>
    <script type="text/javascript" src="${grailsApplication.config.google.maps.url}"></script>
    <r:script disposition="head">
    var fcConfig = {
        serverUrl: "${grailsApplication.config.grails.serverURL}",
        projectUpdateUrl: "${createLink(action: 'ajaxUpdate', id: project.projectId)}",
        sitesDeleteUrl: "${createLink(controller: 'site', action: 'ajaxDeleteSitesFromProject', id:project.projectId)}",
        siteDeleteUrl: "${createLink(controller: 'site', action: 'ajaxDeleteSiteFromProject', id:project.projectId)}",
        siteViewUrl: "${createLink(controller: 'site', action: 'index')}",
        siteEditUrl: "${createLink(controller: 'site', action: 'edit')}",
        removeSiteUrl: "${createLink(controller: 'site', action: '')}",
        activityEditUrl: "${createLink(controller: 'activity', action: 'edit')}",
        activityEnterDataUrl: "${createLink(controller: 'activity', action: 'enterData')}",
        activityPrintUrl: "${createLink(controller: 'activity', action: 'print')}",
        activityCreateUrl: "${createLink(controller: 'activity', action: 'create')}",
        activityUpdateUrl: "${createLink(controller: 'activity', action: 'ajaxUpdate')}",
        activityDeleteUrl: "${createLink(controller: 'activity', action: 'ajaxDelete')}",
        activityViewUrl: "${createLink(controller: 'activity', action: 'index')}",
        siteCreateUrl: "${createLink(controller: 'site', action: 'createForProject', params: [projectId:project.projectId])}",
        siteSelectUrl: "${createLink(controller: 'site', action: 'select', params:[projectId:project.projectId])}&returnTo=${createLink(controller: 'project', action: 'index', id: project.projectId)}",
        siteUploadUrl: "${createLink(controller: 'site', action: 'uploadShapeFile', params:[projectId:project.projectId])}&returnTo=${createLink(controller: 'project', action: 'index', id: project.projectId)}",
        starProjectUrl: "${createLink(controller: 'project', action: 'starProject')}",
        addUserRoleUrl: "${createLink(controller: 'user', action: 'addUserAsRoleToProject')}",
        removeUserWithRoleUrl: "${createLink(controller: 'user', action: 'removeUserWithRole')}",
        projectMembersUrl: "${createLink(controller: 'project', action: 'getMembersForProjectId')}",
        spatialBaseUrl: "${grailsApplication.config.spatial.baseUrl}",
        spatialWmsCacheUrl: "${grailsApplication.config.spatial.wms.cache.url}",
        spatialWmsUrl: "${grailsApplication.config.spatial.wms.url}",
        sldPolgonDefaultUrl: "${grailsApplication.config.sld.polgon.default.url}",
        sldPolgonHighlightUrl: "${grailsApplication.config.sld.polgon.highlight.url}",
        organisationLinkBaseUrl: "${grailsApplication.config.collectory.baseURL + 'public/show/'}",
        returnTo: "${createLink(controller: 'project', action: 'index', id: project.projectId)}"
        },
        here = window.location.href;

    </r:script>

    <!--[if gte IE 8]>
        <style>
           .thumbnail > img {
                max-width: 400px;
            }
            .thumbnail {
                max-width: 410px;
            }
        </style>
    <![endif]-->
    <r:require modules="gmap3,mapWithFeatures,knockout,datepicker,amplify,jqueryValidationEngine,projects, attachDocuments, wmd"/>
</head>
<body>
<div class="container-fluid">

    <ul class="breadcrumb">
        <li>
            <g:link controller="home">Home</g:link> <span class="divider">/</span>
        </li>
        <li class="active">Projects <span class="divider">/</span></li>
        <li class="active" data-bind="text:name"></li>
    </ul>

    <div class="row-fluid">
        <div class="row-fluid">
            <div class="clearfix">
                <h1 class="pull-left" data-bind="text:name"></h1>
                <g:if test="${flash.errorMessage || flash.message}">
                    <div class="span5">
                        <div class="alert alert-error">
                            <button class="close" onclick="$('.alert').fadeOut();" href="#">×</button>
                            ${flash.errorMessage?:flash.message}
                        </div>
                    </div>
                </g:if>
                <div class="pull-right">
                    <g:set var="disabled">${(!user) ? "disabled='disabled' title='login required'" : ''}</g:set>
                    <g:if test="${isProjectStarredByUser}">
                        <button class="btn" id="starBtn"><i class="icon-star"></i> <span>Remove from favourites</span></button>
                    </g:if>
                    <g:else>
                        <button class="btn" id="starBtn" ${disabled}><i class="icon-star-empty"></i> <span>Add to favourites</span></button>
                    </g:else>
                </div>
            </div>
        </div>
    </div>

    <!-- content tabs -->
    <g:set var="tabIsActive"><g:if test="${user?.hasViewAccess}">tab</g:if></g:set>
    <ul id="projectTabs" class="nav nav-tabs big-tabs">
        <li class="active"><a href="#overview" id="overview-tab" data-toggle="tab">Overview</a></li>
        <li><a href="#plan" id="plan-tab" data-toggle="${tabIsActive}">Plans & Reports</a></li>
        <li><a href="#site" id="site-tab" data-toggle="${tabIsActive}">Sites</a></li>
        <li><a href="#dashboard" id="dashboard-tab" data-toggle="${tabIsActive}">Dashboard</a></li>
        <g:if test="${user?.isAdmin || user?.isCaseManager}"><li><a href="#admin" id="admin-tab" data-toggle="tab">Admin</a></li></g:if>
    </ul>
    <div class="tab-content" style="overflow:visible;">
        <div class="tab-pane active" id="overview">
            <!-- OVERVIEW -->
            <div class="row-fluid">
                <div class="clearfix" data-bind="visible:organisation()||organisationName()">
                    <h4>
                        Grant recipient:
                        <a data-bind="visible:organisation(),text:transients.collectoryOrgName,attr:{href:fcConfig.organisationLinkBaseUrl + organisation()}"></a>
                        <span data-bind="visible:organisationName(),text:organisationName"></span>
                    </h4>
                </div>
                <div class="clearfix" data-bind="visible:associatedProgram()">
                    <h4>
                        Funded by:
                        <span data-bind="text:associatedProgram"></span>
                        <span data-bind="text:associatedSubProgram"></span>
                    </h4>
                </div>
                <div class="clearfix" data-bind="visible:funding()">
                    <h4>
                        Approved funding (GST inclusive): <span data-bind="text:funding.formattedCurrency"></span>
                    </h4>

                </div>

                <div data-bind="visible:plannedStartDate()">
                    <h4>
                        Project start: <span data-bind="text:plannedStartDate.formattedDate"></span>
                        <span data-bind="visible:plannedEndDate()">Project finish: <span data-bind="text:plannedEndDate.formattedDate"></span></span>
                    </h4>
                </div>

                <div class="clearfix" style="font-size:14px;">
                    <div class="span4" data-bind="visible:grantId" style="margin-bottom: 0">
                        Grant Id:
                        <span data-bind="text:grantId"></span>
                    </div>
                    <div class="span4" data-bind="visible:externalId" style="margin-bottom: 0">
                        External Id:
                        <span data-bind="text:externalId"></span>
                    </div>
                    <div class="span4" data-bind="visible:manager" style="margin-bottom: 0">
                        Manager:
                        <span data-bind="text:manager"></span>
                    </div>
                </div>
                <div data-bind="visible:description()">
                    <p class="well well-small more" data-bind="text:description"></p>
                </div>
            </div>
            <div class="row-fluid">
                <!-- show any primary images -->
                <div data-bind="visible:primaryImages() !== null,foreach:primaryImages,css:{span5:primaryImages()!=null}">
                    <div class="thumbnail with-caption space-after">
                        <img class="img-rounded" data-bind="attr:{src:url, alt:name}" alt="primary image"/>
                        <p class="caption" data-bind="text:name"></p>
                        <p class="attribution" data-bind="visible:attribution"><small><span data-bind="text:attribution"></span></small></p>
                    </div>
                </div>

                <!-- show other documents -->
                <div id="documents" data-bind="css: { span3: primaryImages() != null, span7: primaryImages() == null }">
                    <h4>Project documents</h4>
                    <div data-bind="visible:documents().length == 0">
                        No documents are currently attached to this project.
                        <g:if test="${user?.isAdmin}">To add a document use the Documents section of the Admin tab.</g:if>
                    </div>
                    <g:render template="/shared/listDocuments"
                      model="[useExistingModel: true,editable:false,imageUrl:resource(dir:'/images/filetypes'),containerId:'overviewDocumentList']"/>
                </div>

                <div class="span4">
                    <div data-bind="visible:newsAndEvents()">
                        <h4>News and events</h4>
                        <div id="newsAndEventsDiv" data-bind="html:newsAndEvents" class="well"></div>
                    </div>
                    <div data-bind="visible:projectStories()">
                        <h4>Project stories</h4>
                        <div id="projectStoriesDiv" data-bind="html:projectStories" class="well"></div>
                    </div>
                </div>
            </div>
        </div>
        <g:if test="${user?.hasViewAccess}">
            <div class="tab-pane" id="plan">
                <!-- PLANS -->
                <g:if test="${useAltPlan}">
                    <g:render template="/shared/plan"
                              model="[activities:activities ?: [], sites:project.sites ?: [], showSites:true]"/>
                </g:if>
                <g:else>
                    <g:render template="/shared/activitiesPlan"
                              model="[activities:activities ?: [], sites:project.sites ?: [], showSites:true]"/>
                </g:else>
            </div>

            <div class="tab-pane" id="site">
                <!-- SITES -->
                <div data-bind="visible: sites.length == 0">
                   <p>No sites are currently associated with this project.</p>
                   <g:if test="${user?.isEditor}">
                   <div class="btn-group btn-group-horizontal ">
                        <button data-bind="click: $root.addSite" type="button" class="btn">Add new site</button>
                        <button data-bind="click: $root.addExistingSite" type="button" class="btn">Add existing site</button>
                        <button data-bind="click: $root.uploadShapefile" type="button" class="btn">Upload sites from shapefile</button>
                   </div>
                   </g:if>
                 </div>

                <div class="row-fluid"  data-bind="visible: sites.length > 0">
                    <div class="span5 well list-box">
                        <div class="span9">
                        <div class="control-group">
                            <div class="input-append">
                                <input type="text" class="filterinput input-medium"
                                             data-bind="value: sitesFilter, valueUpdate:'keyup'"
                                             title="Type a few characters to restrict the list." name="sites"
                                             placeholder="filter"/>
                                <button type="button" class="btn" data-bind="click:clearFilter"
                                        title="clear"><i class="icon-remove"></i></button>
                            </div>
                            <span id="site-filter-warning" class="label filter-label label-warning"
                                  style="display:none;margin-left:4px;"
                                  data-bind="visible:sitesFilter().length > 0,valueUpdate:'afterkeyup'">Filtered</span>
                        </div>

                        <div class="scroll-list">
                            <ul id="siteList" style="list-style: none; margin-left: 0px;"
                                data-bind="template: {foreach:displayedSites},
                                                  beforeRemove: hideElement,
                                                  afterAdd: showElement">
                                <li data-bind="event: {mouseover: $root.highlight, mouseout: $root.unhighlight}">
                                    <g:if test="${user?.isEditor}">
                                    <span>
                                        <button type="button" data-bind="click:$root.editSite" class="btn btn-container"><i class="icon-edit" title="Edit Site"></i></button>
                                        <button type="button" data-bind="click:$root.viewSite" class="btn btn-container"><i class="icon-eye-open" title="View Site"></i></button>
                                        <button type="button" data-bind="click:$root.deleteSite" class="btn btn-container"><i class="icon-remove" title="Delete Site"></i></button>
                                    </span>

                                    <a style="margin-left:10px;" data-bind="text:name, attr: {href:'${createLink(controller: "site", action: "index")}' + '/' + siteId}"></a>
                                    </g:if>
                                    <g:else>
                                        <span data-bind="text:name"></span>
                                    </g:else>
                                </li>
                            </ul>
                        </div>
                        <div id="paginateTable" data-bind="visible:sites.length>20">
                            <span id="paginationInfo" style="display:inline-block;float:left;margin-top:4px;"></span>
                            <div class="btn-group">
                                <button class="btn btn-small prev" data-bind="click:prevPage,enable:(offset()-pageSize) >= 0"><i class="icon-chevron-left"></i>&nbsp;previous</button>
                                <button class="btn btn-small next" data-bind="click:nextPage,enable:(offset()+pageSize) < filteredSites().length">next&nbsp;<i class="icon-chevron-right"></i></button>
                            </div>
                            <g:if env="development">
                                total: <span id="total" data-bind="text:filteredSites().length"></span>
                                offset: <span id="offset" data-bind="text:offset"></span>
                            </g:if>
                        </div>

                    </div>
                     %{--<div class="span5" id="sites-scroller">
                        <ul class="unstyled inline" data-bind="foreach: sites">
                            <li class="siteInstance" data-bind="event: {mouseover: $root.highlight, mouseout: $root.unhighlight}">
                                <a data-bind="text: name, click: $root.openSite"></a>
                                <button data-bind="click: $root.removeSite" type="button" class="close" title="delete">&times;</button>
                            </li>
                        </ul>
                    </div>--}%

                    <g:if test="${user?.isEditor}">

                    <div class="row-fluid">
                        <div class="span3">
                            <div class="btn-group btn-group-vertical pull-right">
                                <a data-bind="click: $root.addSite" type="button" class="btn ">Add new site</a>
                                <a data-bind="click: $root.addExistingSite" type="button" class="btn">Add existing site</a>
                                <a data-bind="click: $root.uploadShapefile" type="button" class="btn">Upload sites from shapefile</a>
                                <a data-bind="click: $root.removeAllSites" type="button" class="btn">Delete all sites</a>
                            </div>
                        </div>
                    </div>
                    </g:if>
                    </div>
                    <div class="span7">
                        <div id="map" style="width:100%"></div>
                    </div>
                </div>

            </div>

            <div class="tab-pane" id="dashboard">
                <!-- DASHBOARD -->
                <g:render template="dashboard"/>
            </div>
        </g:if>
        <g:if test="${user?.isAdmin || user?.isCaseManager}">
            <g:set var="activeClass" value="class='active'"/>
            <div class="tab-pane" id="admin">
            <!-- ADMIN -->
                <div class="row-fluid">
                    <div class="span2 large-space-before">
                        <ul id="adminNav" class="nav nav-tabs nav-stacked ">
                            <g:if test="${fc.userInRole(role: grailsApplication.config.security.cas.alaAdminRole) || fc.userInRole(role: grailsApplication.config.security.cas.adminRole)}">
                                <li ${activeClass}><a href="#settings" id="settings-tab" data-toggle="tab"><i class="icon-chevron-right"></i> Project settings</a></li>
                                <g:set var="activeClass" value=""/>
                            </g:if>
                            <li><a href="#editNewsAndEvents" id="editnewsandevents-tab" data-toggle="tab"><i class="icon-chevron-right"></i> News and events</a></li>
                            <li><a href="#editProjectStories" id="editprojectstories-tab" data-toggle="tab"><i class="icon-chevron-right"></i> Project stories</a></li>

                            <li ${activeClass}><a href="#permissions" id="permissions-tab" data-toggle="tab"><i class="icon-chevron-right"></i> Project access</a></li>
                            <li><a href="#species" id="species-tab" data-toggle="tab"><i class="icon-chevron-right"></i> Species of interest</a></li>
                            <li><a href="#edit-documents" id="documents-tab" data-toggle="tab"><i class="icon-chevron-right"></i> Documents</a></li>
                        </ul>
                    </div>
                    <div class="span10">
                        <div class="pill-content">
                            <g:set var="activeClass" value="active"/>
                            <g:if test="${fc.userInRole(role: grailsApplication.config.security.cas.alaAdminRole) || fc.userInRole(role: grailsApplication.config.security.cas.adminRole)}">
                                <!-- PROJECT SETTINGS -->
                                <div id="settings" class="pill-pane ${activeClass}">
                                    <h3>Project Settings</h3>
                                    <div class="row-fluid">
                                        <div id="save-result-placeholder"></div>
                                        <div class="span10 validationEngineContainer" id="settings-validation">
                                            <g:render template="editProject"
                                                      model="[project: project]"/>
                                        </div>
                                    </div>
                                </div>
                                <g:set var="activeClass" value=""/>
                            </g:if>
                            <div id="editNewsAndEvents" class="pill-pane">

                                <g:render template="editProjectContent" model="${[attributeName:'newsAndEvents', header:'News and events']}"/>
                            </div>

                            <div id="editProjectStories" class="pill-pane">
                                <g:render template="editProjectContent" model="${[attributeName:'projectStories', header:'Project stories']}"/>
                            </div>

                            <div id="permissions" class="pill-pane ${activeClass}">
                                <h3>Project Access</h3>
                                %{--<a name="permissions"></a>--}%
                                <g:render template="/admin/addPermissions" model="[projectId:project.projectId]"/>
                                <h4>Project Members</h4>
                                <div class="row-fluid">
                                    <div class="span6">
                                        <table class="table table-condensed" id="projectMembersTable" style="">
                                            <thead><tr><th width="10%">User&nbsp;Id</th><th>User&nbsp;Name</th><th width="15%">Role</th><th width="5%">&nbsp;</th><th width="5%">&nbsp;</th></tr></thead>
                                            <tbody class="membersTbody">
                                            <tr class="hide">
                                                <td class="memUserId"></td>
                                                <td class="memUserName"></td>
                                                <td class="memUserRole"><span style="white-space: nowrap">&nbsp;</span><g:render template="/admin/userRolesSelect" model="[roles:roles, selectClass:'hide']"/></td>
                                                <td class="clickable memEditRole"><i class="icon-edit tooltips" title="edit this user and role combination"></i></td>
                                                <td class="clickable memRemoveRole"><i class="icon-remove tooltips" title="remove this user and role combination"></i></td>
                                            </tr>
                                            <tr id="spinnerRow"><td colspan="5">loading data... <g:img dir="images" file="spinner.gif" id="spinner2" class="spinner" alt="spinner icon"/></td></tr>
                                            <tr id="messageRow" class="hide"><td colspan="5">No project members set</td></tr>
                                            </tbody>
                                        </table>
                                    </div>
                                    <div class="span5">
                                        <div id="formStatus" class="hide alert alert-success">
                                            <button class="close" onclick="$('.alert').fadeOut();" href="#">×</button>
                                            <span></span>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <!-- SPECIES -->
                            %{--<div class="border-divider large-space-before">&nbsp;</div>--}%
                            <div id="species" class="pill-pane">
                                %{--<a name="species"></a>--}%
                                <g:render template="/species/species" model="[project:project, activityTypes:activityTypes]"/>
                            </div>
                            <!-- DOCUMENTS -->
                            <div id="edit-documents" class="pill-pane">
                                <h3>Project Documents</h3>
                                <div class="row-fluid">
                                    <div class="span10">
                                        <g:render template="/shared/listDocuments"
                                                  model="[useExistingModel: true,editable:true,imageUrl:resource(dir:'/images/filetypes'),containerId:'adminDocumentList']"/>
                                    </div>
                                </div>
                                %{--The modal view containing the contents for a modal dialog used to attach a document--}%
                                <g:render template="/shared/attachDocument"/>
                                <div class="row-fluid attachDocumentModal">
                                    <button class="btn" id="doAttach" data-bind="click:attachDocument">Attach Document</button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </g:if>
    </div>

    <g:if env="development">
    <hr />
    <div class="expandable-debug">
        <h3>Debug</h3>
        <div>
            <h4>KO model</h4>
            <pre data-bind="text:ko.toJSON($root,null,2)"></pre>
            <h4>Activities</h4>
            <pre>${activities?.encodeAsHTML()}</pre>
            <h4>Sites</h4>
            <pre>${project.sites?.encodeAsHTML()}</pre>
            <h4>Project</h4>
            <pre>${project?.encodeAsHTML()}</pre>
            <h4>Features</h4>
            <pre>${mapFeatures}</pre>
            <h4>activityTypes</h4>
            <pre>${activityTypes}</pre>
        </div>
    </div>
    </g:if>
</div>
    <r:script>
        var organisations = ${institutions};

        // custom validator to ensure that only one of two fields is populated
        function exclusive (field, rules, i, options) {
            var otherFieldId = rules[i+2], // get the id of the other field
                otherValue = $('#'+otherFieldId).val(),
                thisValue = field.val(),
                message = rules[i+3];
            // checking thisValue is technically redundant as this validator is only called
            // if there is a value in the field
            if (otherValue !== '' && thisValue !== '') {
                return message;
            } else {
                return true;
            }
        }

        $(window).load(function () {
            var map;
            // setup 'read more' for long text
            $('.more').shorten({
                moreText: 'read more',
                showChars: '1000'
            });
            // setup confirm modals for deletions
            $(document).on("click", "a[data-bb]", function(e) {
                e.preventDefault();
                var type = $(this).data("bb"),
                    href = $(this).attr('href');
                if (type === 'confirm') {
                    bootbox.confirm("Delete this entire project? Are you sure?", function(result) {
                        if (result) {
                            document.location.href = href;
                        }
                    });
                }
            });

            $('#settings-validation').validationEngine();

            $('.helphover').popover({animation: true, trigger:'hover'});

            $('#cancel').click(function () {
                document.location.href = "${createLink(action: 'index', id: project.projectId)}";
            });

            var Site = function (site, feature) {
                var self = this;
                this.name = ko.observable(site.name);
                this.siteId = site.siteId;
                this.state = ko.observable('');
                this.nrm = ko.observable('');
                this.address = ko.observable("");
                this.feature = feature;
                this.setAddress = function (address) {
                    if (address.indexOf(', Australia') === address.length - 11) {
                        address = address.substr(0, address.length - 11);
                    }
                    self.address(address);
                };
            };

            function ViewModel(project, newsAndEvents, projectStories, sites, activities, isUserEditor) {
                var self = this;
                self.name = ko.observable(project.name);
                self.description = ko.observable(project.description);
                self.externalId = ko.observable(project.externalId);
                self.grantId = ko.observable(project.grantId);
                self.manager = ko.observable(project.manager);
                self.plannedStartDate = ko.observable(project.plannedStartDate).extend({simpleDate: false});
                self.plannedEndDate = ko.observable(project.plannedEndDate).extend({simpleDate: false});
                self.funding = ko.observable(project.funding).extend({currency:{}});

                self.regenerateProjectTimeline = ko.observable(false);
                self.projectDatesChanged = ko.computed(function() {
                    return project.plannedStartDate != self.plannedStartDate() ||
                           project.plannedEndDate != self.plannedEndDate();
                });

                self.organisation = ko.observable(project.organisation);
                self.organisationName = ko.observable(project.organisationName);
                self.associatedProgram = ko.observable(); // don't initialise yet - we want the change to trigger dependents
                self.associatedSubProgram = ko.observable(project.associatedSubProgram);
                self.newsAndEvents = ko.observable(newsAndEvents);
                self.projectStories = ko.observable(projectStories);
                self.mapLoaded = ko.observable(false);

                self.transients = {};
                self.transients.organisations = organisations;
                self.transients.collectoryOrgName = ko.computed(function () {
                    if (self.organisation() === undefined || self.organisation() === '') {
                        return "";
                    } else {
                        return $.grep(self.transients.organisations, function (obj) {
                            return obj.uid === self.organisation();
                        })[0].name;
                    }
                });
                self.transients.programs = [];
                self.transients.subprograms = {};
                self.transients.subprogramsToDisplay = ko.computed(function () {
                    return self.transients.subprograms[self.associatedProgram()];
                });

                self.loadPrograms = function (programsModel) {
                    $.each(programsModel.programs, function (i, program) {
                        self.transients.programs.push(program.name);
                        self.transients.subprograms[program.name] = $.map(program.subprograms,function (obj, i){return obj.name});
                    });
                    self.associatedProgram(project.associatedProgram); // to trigger the computation of sub-programs
                };
                self.removeTransients = function (jsData) {
                    delete jsData.transients;
                    return jsData;
                };

                // settings
                self.saveSettings = function () {
                    if ($('#settings-validation').validationEngine('validate')) {

                        // only collect those fields that can be edited in the settings pane
                        var jsData = {
                            name: self.name(),
                            description: self.description(),
                            externalId: self.externalId(),
                            grantId: self.grantId(),
                            manager: self.manager(),
                            plannedStartDate: self.plannedStartDate(),
                            plannedEndDate: self.plannedEndDate(),
                            organisation: self.organisation(),
                            organisationName: self.organisationName(),
                            associatedProgram: self.associatedProgram(),
                            associatedSubProgram: self.associatedSubProgram(),
                            funding: new Number(self.funding())
                        };
                        if (self.regenerateProjectTimeline()) {
                            var dates = {
                                plannedStartDate: self.plannedStartDate(),
                                plannedEndDate: self.plannedEndDate()
                            };
                            addTimelineBasedOnStartDate(dates);
                            jsData.timeline = dates.timeline;
                        }
                        // this call to stringify will make sure that undefined values are propagated to
                        // the update call - otherwise it is impossible to erase fields
                        var json = JSON.stringify(jsData, function (key, value) {
                            return value === undefined ? "" : value;
                        });
                        var id = "${project?.projectId}";
                        $.ajax({
                            url: "${createLink(action: 'ajaxUpdate', id: project.projectId)}",
                            type: 'POST',
                            data: json,
                            contentType: 'application/json',
                            success: function (data) {
                                if (data.error) {
                                    showAlert("Failed to save settings: " + data.detail + ' \n' + data.error,
                                        "alert-error","save-result-placeholder");
                                } else {
                                    showAlert("Project settings saved","alert-success","save-result-placeholder");
                                }
                            },
                            error: function (data) {
                                var status = data.status;
                                alert('An unhandled error occurred: ' + data.status);
                            }
                        });
                    }
                };

                // documents
                self.documents = ko.observableArray();
                self.addDocument = function(doc) {
                    // check permissions
                    if ((isUserEditor && doc.role !== 'approval') ||  doc.public) {
                        self.documents.push(new DocumentViewModel(doc));
                    }
                };
                self.attachDocument = function() {
                    var url = '${g.createLink(controller:"proxy", action:"documentUpdate")}';
                    showDocumentAttachInModal( url,new DocumentViewModel({role:'information'},{key:'projectId', value:'${project.projectId}'}), '#attachDocument')
                        .done(function(result){self.documents.push(new DocumentViewModel(result))});
                };
                self.editDocumentMetadata = function(document) {
                    %{--var url = '${g.createLink(controller:"proxy", action:"documentUpdate")}' + "/" + document.documentId;
                    showDocumentAttachInModal( url, document, '#attachDocument')
                        .done(function(result){
                            document.name(result.name);
                        });--}%
                };
                self.deleteDocument = function(document) {
                    var url = '${g.createLink(controller:"proxy", action:"deleteDocument")}/'+document.documentId;
                    $.post(url, {}, function() {self.documents.remove(document);});

                };
                // this supports display of the project's primary images
                this.primaryImages = ko.computed(function () {
                    var pi = $.grep(self.documents(), function (doc) {
                        return ko.utils.unwrapObservable(doc.isPrimaryProjectImage);
                    });
                    return pi.length > 0 ? pi : null;
                });

                $.each(project.documents, function(i, doc) {
                    self.addDocument(doc);
                });
                // sites
                var mapFeatures = $.parseJSON('${mapFeatures?.encodeAsJavaScript()}');
                var features = [];
                if (mapFeatures.features) {
                    features = mapFeatures.features;
                }
                self.sites = $.map(sites, function (obj,i) {return new Site(obj, features[i])});
                self.sitesFilter = ko.observable("");
                self.throttledFilter = ko.computed(self.sitesFilter).extend({throttle:400});
                self.filteredSites = ko.observableArray(self.sites);
                self.displayedSites = ko.observableArray();
                self.offset = ko.observable(0);
                self.pageSize = 10;
                self.isUserEditor = ko.observable(isUserEditor);
                self.getSiteName = function (siteId) {
                    var site;
                    if (siteId !== undefined && siteId !== '') {
                        site = $.grep(self.sites, function(obj, i) {
                                return (obj.siteId === siteId);
                        });
                        if (site.length > 0) {
                             return site[0].name();
                        }
                    }
                    return '';
                };
                // Animation callbacks for the lists
                self.showElement = function(elem) { if (elem.nodeType === 1) $(elem).hide().slideDown() };
                self.hideElement = function(elem) { if (elem.nodeType === 1) $(elem).slideUp(function() { $(elem).remove(); }) };
                self.clearSiteFilter = function () {
                    self.sitesFilter("");
                };
                self.nextPage = function() {
                    self.offset(self.offset() + self.pageSize);
                    self.displaySites();
                };
                self.prevPage = function() {
                    self.offset(self.offset() - self.pageSize);
                    self.displaySites();
                };
                self.displaySites = function() {
                    map.clearFeatures();

                    self.displayedSites(self.filteredSites.slice(self.offset(), self.offset()+self.pageSize));

                    var features = $.map(self.displayedSites(), function(obj, i) { return obj.feature; });
                    map.replaceAllFeatures(features);

                };

                self.throttledFilter.subscribe(function (val) {
                    self.offset(0);

                    self.filterSites(val);
                });

                self.filterSites = function(filter) {
                    if (filter) {
                        var regex = new RegExp('\\b' + filter, 'i');

                        self.filteredSites([]);
                        $.each(self.sites, function(i, site) {
                            if (regex.test(site.name())) {
                                self.filteredSites.push(site);
                            }
                        });
                        self.displaySites();
                    }
                    else {
                        self.filteredSites(self.sites);
                        self.displaySites();
                    }
                };
                self.clearFilter = function (model, event) {

                    self.sitesFilter("");
                };
                this.highlight = function () {
                    map.highlightFeatureById(this.name());
                };
                this.unhighlight = function () {
                    map.unHighlightFeatureById(this.name());
                };
                this.removeAllSites = function () {
                    bootbox.confirm("Are you sure you want to remove these sites? This will remove the links to this project but will NOT remove the sites from the site.", function(result){
                       if(result){
                           var that = this;
                           $.get(fcConfig.sitesDeleteUrl, function (data) {
                               if (data.status === 'deleted') {
                                   //self.sites.remove(that);
                               }
                               //FIXME - currently doing a page reload, not nice
                               document.location.href = here;
                           });
                       }
                    });
                };
                this.editSite = function(site) {
                    var url = fcConfig.siteEditUrl+'/'+site.siteId+'?returnTo='+fcConfig.returnTo;
                    document.location.href = url;
                };
                this.deleteSite = function(site) {
                    bootbox.confirm("Are you sure you want to remove this site from this project?", function(result){
                       if(result){

                           $.get(fcConfig.siteDeleteUrl+'?siteId='+site.siteId, function (data) {
                               $.each(self.sites, function(i, tmpSite) {
                                   if (site.siteId === tmpSite.siteId) {
                                       self.sites.splice(i, 1);
                                       return false;
                                   }
                               });
                               self.filterSites(self.sitesFilter());
                           });

                       }
                    });
                };
                this.viewSite = function(site) {
                    var url = fcConfig.siteViewUrl+'/'+site.siteId+'?returnTo='+fcConfig.returnTo;
                    document.location.href = url;
                };
                this.addSite = function () {
                     document.location.href = fcConfig.siteCreateUrl;
                };
                this.addExistingSite = function () {
                    document.location.href = fcConfig.siteSelectUrl;
                };
                this.uploadShapefile = function() {
                    document.location.href = fcConfig.siteUploadUrl;
                }
                self.notImplemented = function () {
                    alert("Not implemented yet.")
                };
                self.triggerGeocoding = function () {
                    ko.utils.arrayForEach(self.sites, function (site) {
                        if (map) {
                            map.getAddressById(site.name(), site.setAddress);
                        }
                    });
                };


            }

            var newsAndEventsMarkdown = '${(project.newsAndEvents?:"").markdownToHtml().encodeAsJavaScript()}';
            var projectStoriesMarkdown = '${(project.projectStories?:"").markdownToHtml().encodeAsJavaScript()}';
            var viewModel = new ViewModel(
                checkAndUpdateProject(${project}),
                newsAndEventsMarkdown,
                projectStoriesMarkdown,
                ${project.sites},
                ${activities ?: []},
                ${user?.isEditor?:false});
            viewModel.loadPrograms(${programs});
            ko.applyBindings(viewModel);

            // retain tab state for future re-visits
            // and handle tab-specific initialisations
            var planTabInitialised = false;

            $('#projectTabs a[data-toggle="tab"]').on('shown', function (e) {
                var tab = e.currentTarget.hash;
                amplify.store('project-tab-state', tab);
                // only init map when the tab is first shown
                if (tab === '#site' && map === undefined) {
                    var mapOptions = {
                        zoomToBounds:true,
                        zoomLimit:16,
                        highlightOnHover:true,
                        features:[],
                        featureService: "${createLink(controller: 'proxy', action:'feature')}",
                        wmsServer: "${grailsApplication.config.spatial.geoserverUrl}"
                    };

                    map = init_map_with_features({
                            mapContainer: "map",
                            scrollwheel: false,
                            featureService: "${createLink(controller: 'proxy', action:'feature')}",
                            wmsServer: "${grailsApplication.config.spatial.geoserverUrl}"
                        },
                        mapOptions
                    );
                    // set trigger for site reverse geocoding
                    viewModel.triggerGeocoding();
                    viewModel.displaySites();
                }
                if (tab === '#plan' && !planTabInitialised) {
                    $.event.trigger({type:'planTabShown'});
                    planTabInitialised = true;
                }
            });

            var newsAndEventsInitialised = false;
            $('#editnewsandevents-tab').on('shown', function() {
                if (!newsAndEventsInitialised) {
                    var newsAndEventsViewModel = new window.newsAndEventsViewModel(viewModel, newsAndEventsMarkdown);
                    ko.applyBindings(newsAndEventsViewModel, $('#editnewsAndEventsContent')[0]);
                    newsAndEventsInitialised = true;
                }

            });
            var projectStoriesInitialised = false;
            $('#editprojectstories-tab').on('shown', function() {
                if (!projectStoriesInitialised) {
                    var projectStoriesViewModel = new window.projectStoriesViewModel(viewModel, projectStoriesMarkdown);
                    ko.applyBindings(projectStoriesViewModel, $('#editprojectStoriesContent')[0]);
                    projectStoriesInitialised = true;
                }
            });

            // re-establish the previous tab state
            var storedTab = amplify.store('project-tab-state');
            var isEditor = ${user?.isEditor?:false};
            if (storedTab === '') {
                $('#overview-tab').tab('show');
            } else if (isEditor) {
                $(storedTab + '-tab').tab('show');
            }

            // Non-editors should get tooltip and popup when trying to click other tabs
            $('#projectTabs li a').not('[data-toggle="tab"]').css('cursor', 'not-allowed') //.data('placement',"right")
            .attr('title','Only available to project members').addClass('tooltips');

            // Star button click event
            $("#starBtn").click(function(e) {
                var isStarred = ($("#starBtn i").attr("class") == "icon-star");
                toggleStarred(isStarred);
            });

            // BS tooltip
            $('.tooltips').tooltip();

        });// end window.load

       /**
        * Star/Unstar project for user - send AJAX and update UI
        *
        * @param boolean isProjectStarredByUser
        */
        function toggleStarred(isProjectStarredByUser) {
            var basUrl = fcConfig.starProjectUrl;
            var query = "?userId=${user?.userId}&projectId=${project?.projectId}";
            if (isProjectStarredByUser) {
                // remove star
                $.getJSON(basUrl + "/remove" + query, function(data) {
                    if (data.error) {
                        alert(data.error);
                    } else {
                        $("#starBtn i").removeClass("icon-star").addClass("icon-star-empty");
                        $("#starBtn span").text("Add to favourites");
                    }
                }).fail(function(j,t,e){ alert(t + ":" + e);}).done();
            } else {
                // add star
                $.getJSON(basUrl + "/add" + query, function(data) {
                    if (data.error) {
                        alert(data.error);
                    } else {
                        $("#starBtn i").removeClass("icon-star-empty").addClass("icon-star");
                        $("#starBtn span").text("Remove from favourites");
                    }
                }).fail(function(j,t,e){ alert(t + ":" + e);}).done();
            }
        }
    </r:script>
    <g:if test="${user?.isAdmin || user?.isCaseManager}">
        <r:script>
            // Admin JS code only exposed to admin users
            $(window).load(function () {

                // remember state of admin nav (vertical tabs)
                $('#adminNav a[data-toggle="tab"]').on('shown', function (e) {
                    var tab = e.currentTarget.hash;
                    amplify.store('project-admin-tab-state', tab);
                });
                var storedAdminTab = amplify.store('project-admin-tab-state');
                // restore state if saved
                if (storedAdminTab === '') {
                    $('#permissions-tab').tab('show');
                } else {
                    $(storedAdminTab + "-tab").tab('show');
                }

                // click event on the "remove" button on Project Members table
                $('.membersTbody').on("click", "td.memRemoveRole", function(e) {
                    var $this = this;
                    var userId = $($this).parent().data("userid");
                    var role = $($this).parent().data("role");
                    bootbox.confirm("Are you sure you want to remove this user's access?", function(result) {
                        if (result) {
                            if (userId && role) {
                                removeUserRole(userId, role);
                            } else {
                                alert("Error: required params not provided: userId & role");
                            }
                        }
                    });
                });

                // hide/show the role select for editting role
                $('.membersTbody').on("click", "td.memEditRole", function(e) {
                    if ($(this).parent().find("span").is(':visible')) {
                        $(this).parent().find("span").hide();
                        $(this).parent().find("select").fadeIn();
                    } else {
                        $(this).parent().find("span").fadeIn();
                        $(this).parent().find("select").hide();
                    }
                });

                // detect change on "role" select in table
                $('.membersTbody').on("change", ".memUserRole select", function() {
                    var role = $(this).val();
                    var currentRole = $(this).siblings('span').text();
                    var userId = $(this).attr('id'); // Couldn't get $(el).data('userId') to work for some reason
                    bootbox.confirm("Are you sure you want to change this user's access from " + currentRole + " to " + decodeCamelCase(role) + "?", function(result) {
                        if (result) {
                            addUserWithRole(userId, role, "${project.projectId}");
                        } else {
                            loadProjectMembers(); // reload table
                        }
                    });
                });

                // load initial list of project members
                loadProjectMembers();

            }); // end window.load

            /**
            * This populates the "Project Members" table via an AJAX call
            * It uses the jQuery clone pattern to generate HTML using a plain
            * HTML template, found in the table itself.
            * See: http://stackoverflow.com/a/1091493/249327
            */
            function loadProjectMembers() {
                $("#spinnerRow").show();
                $('.membersTbody tr.cloned').remove();
                $.ajax({
                    url: fcConfig.projectMembersUrl + "/${project.projectId}"
                })
                .done(function(data) {
                    //alert("Done data = " + data);
                    if (data.length > 0) {
                        $("#messageRow").hide();
                        $.each(data, function(i, el) {
                            var $clone = $('.membersTbody tr.hide').clone();
                            $clone.removeClass("hide");
                            $clone.addClass("cloned");
                            $clone.data("userid", el.userId);
                            $clone.data("role", el.role);
                            $clone.find('.memUserId').text(el.userId);
                            $clone.find('.memUserName').text(el.displayName);
                            $clone.find('.memUserRole select').val(el.role);
                            $clone.find('.memUserRole select').attr("id", el.userId);
                            $clone.find('.memUserRole span').text(decodeCamelCase(el.role).replace('Case','Grant')); // TODO: i18n this
                            $('.membersTbody').append($clone);
                        });
                    } else {
                        $("#messageRow").show();
                    }
                 })
                .fail(function(jqXHR, textStatus, errorThrown) { alert(jqXHR.responseText); })
                .always(function() { $("#spinnerRow").hide(); });
            }

            function updateStatusMessage2(msg) {
                $('#formStatus span').text(''); // clear previous message
                $('#formStatus span').text(msg).parent().fadeIn();
            }


           /**
            * Modify a user's role
            *
            * @param userId
            * @param role
            */
            function removeUserRole(userId, role) {
                $.ajax( {
                    url: fcConfig.removeUserWithRoleUrl,
                    data: {userId: userId, role: role, projectId: "${project.projectId}" }
                })
                .done(function(result) { updateStatusMessage2("user was removed."); })
                .fail(function(jqXHR, textStatus, errorThrown) { alert(jqXHR.responseText); })
                .always(function(result) {
                    $("#spinner1").hide();
                    loadProjectMembers(); // reload table
                });
            }

        </r:script>
    </g:if>
</body>
</html>