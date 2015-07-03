<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="${hubConfig.skin}"/>
    <title>Edit | Announcements | Field Capture</title>
    <script type="text/javascript" src="//cdnjs.cloudflare.com/ajax/libs/jstimezonedetect/1.0.4/jstz.min.js"></script>
    <r:script disposition="head">
    var fcConfig = {
        organisationViewUrl: "${createLink(controller:'organisation', action:'index', id:organisation.organisationId)}",
        saveAnnouncementsUrl: "${createLink(controller:'organisation', action:'saveAnnouncements', id:organisation.organisationId, params:[format:'json'])}",
        returnTo: "${params.returnTo}"
        },
        here = document.location.href;
    </r:script>
    <r:require modules="knockout,jqueryValidationEngine,datepicker,slickgrid,jQueryFileUpload,jQueryFileDownload,amplify,merit_projects"/>
    <style type="text/css">
    input.editor-text {box-sizing:border-box; width: 100%;}
    .slick-column-name { white-space: normal; }
    .slick-header-column.ui-state-default { background: #DAE0B9; height: 100%; font-weight: bold;}
    .slick-header { background: #DAE0B9; }

    </style>
</head>

<body>
<div class="container-fluid">
    <ul class="breadcrumb">
        <li><g:link controller="home">Home</g:link> <span class="divider">/</span></li>
        <li><a href="${createLink(controller:'organisation', action:'index', id:organisation.organisationId)}" class="clickable">Organisation</a> <span class="divider">/</span></li>
        <li class="active">
            Edit Announcements
        </li>
    </ul>
    <div class="row-fluid">
        <h2>Edit Announcements</h2>
    </div>

    <div id="announcementsTable" class="table table-striped" style="width:100%;">

    </div>

    <div class="row-fluid">

        <div class="form-actions" >
            <span class="span3">
                <button type="button" id ="bulkUploadTrigger" class="btn btn-small"><i class="icon-upload"></i> Upload data for this table</button>
                <div id="bulkUpload" style="display:none;">
                    <div class="text-left" style="margin:5px">
                        <a target="_blank" id="downloadTemplate" class="btn btn-small">Step 1 - Download template (.xlsx)</a>
                    </div>

                    <div class="text-left" style="margin:5px">
                        <span class="btn btn-small fileinput-button">yh
                            Step 2 - Upload populated template <input id="fileupload" type="file" name="templateFile">
                        </span>
                    </div>
                </div>
            </span>
            <span class="span9"style="text-align:right">
                <button type="button" id="save" data-bind="click:save" class="btn btn-primary" title="Save edits and return to the previous page">Save</button>
                <buttom type="button" id="cancel" data-bind="click:cancel" class="btn btn" title="Cancel edits and return to previous page">Cancel</buttom>
            </span>
        </div>
    </div>

    <g:render template="/shared/timeoutMessage" plugin="fieldcapture-plugin" model="${[newWindow:true]}"/>

<r:script>
    $(function() {

        function controlsFormatter() {
            return "<button class='btn btn-container' title='Adds a new announcement for this project in the row below this'><i class='icon-plus'></i></button> <button class='btn btn-container' title='Deletes this announcement'><i class='icon-remove'></i></button>";
        }

        var events = <fc:modelAsJavascript model="${events}"></fc:modelAsJavascript>;
        var organisationId = '${organisation.organisationId}';
        var projectList = <fc:modelAsJavascript model="${projectList}"/>
        var columns =  [
            {id:'grantID', name:'Grant ID', width:80, field:'grantId'},
            {id:'projectName', name:'Project Name', width:200, field:'name', options:projectList, optionLabel:'name', optionValue:'name', editor: ComboBoxEditor},
            {id:'date', name:'Proposed Date of event / announcement (if known)', width:80, field:'eventDate', formatter:dateFormatter, editor: DateEditor2},
            {id:'event', name:'Proposed event/announcement', width:200, field:'eventName', editor: Slick.Editors.Text},
            {id:'eventDescription', name:'Description of the event', width:200, field:'eventDescription', editor: Slick.Editors.LongText},
            {id:'media', name:'Will there be, or do you intend there to be, media involvement in this event?', width:100, editor:SelectEditor, formatter:optionsFormatter, options:[{label:'Yes', value:'yes'}, {label:'No', value:'no'}], field:'media'},
            {id:'controls', name:'', width:20, formatter:controlsFormatter}
            ];

        var options = {
            editable:true,
            enableAddRow: true,
            enableCellNavigation: true,
            forceFitColumns:true,
            autoHeight:true,
            explicitInitialization:true,
            enableColumnReorder:false,
            enableTextSelectionOnCells:true,
            editFocusRight:true
        };


        var grid = new Slick.Grid("#announcementsTable", [], columns, options);

        var editAnnouncementsViewModel = new EditAnnouncementsViewModel(grid, events);
        grid.init();

        var options = {
            storageKey : 'BULK_ANNOUNCEMENTS'+organisationId,
            blockUIOnSave: true,
            blockUISaveMessage: "Saving announcements...",
            preventNavigationIfDirty: true
        };

        autoSaveModel(editAnnouncementsViewModel, fcConfig.saveAnnouncementsUrl, options);

        ko.applyBindings(editAnnouncementsViewModel);

    });
</r:script>
</div>
</body>
</html>