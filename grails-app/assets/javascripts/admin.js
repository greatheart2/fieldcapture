//= require pagination.js
//= require_self

var ApprovableImage = function(doc) {
    var self = this;
    var APPROVED = 'hp-y';
    var REJECTED = 'hp-n';
    var labelIndex = function(label) {
        return self.labels().indexOf(label);
    };
    var removeLabels = function() {
        var index = labelIndex(REJECTED);
        if (index >= 0) {
            self.labels().splice(index, 1);
        }
        index = labelIndex(APPROVED);
        if (index >= 0) {
            self.labels().splice(index, 1);
        }
    };
    $.extend(self, doc);
    self.labels = ko.observableArray(doc.labels);
    self.approved = ko.computed(function() {
        return self.labels().indexOf(APPROVED) >= 0;
    });
    self.rejected = ko.computed(function() {
        return self.labels().indexOf(REJECTED) >= 0;
    });
    self.approve = function() {
        removeLabels();
        self.labels.push(APPROVED);
    };
    self.reject = function() {
        removeLabels();
        self.labels.push(REJECTED);
    };

};


var ImageGallery = function() {
    var self = this;
    self.pagination = new PaginationViewModel({}, self);
    self.sort = ko.observable('labels');
    self.images = ko.observableArray([]);

    self.refreshPage = function(offset) {
        var url = fcConfig.homePageImagesUrl;
        $.get(url, {offset:offset, max:self.pagination.resultsPerPage(), sort:self.sort()}, function(data) {
            self.images($.map(data.documents, function(doc) {
                return new ApprovableImage(doc);
            }));

            if (offset == 0) {
                self.pagination.loadPagination(0, data.count);
            }
        });
    };
    self.approve = function(doc) {
        doc.approve();
        self.save(doc);
    };
    self.reject = function(doc) {
        doc.reject();
        self.save(doc);
    };
    self.save = function(doc) {
        var url = fcConfig.documentUpdateUrl+'/'+doc.documentId;
        $.post( url, {document:JSON.stringify({documentId:doc.documentId, labels:doc.labels()})}).fail(function() {
            alert("There was an error saving your change.");
        });
    };
    self.refreshPage(0);
    self.sort.subscribe(function() {
        self.refreshPage(0);
    });

};

/**
 * Knockout view model for Remove USer Permission pages.
 * @param options an object specifying the following options:
 * validationContainerSelector, searchUserDetailsUrl, removeUserDetailsUrl
 * @constructor
 */
var RemoveUserPermissionViewModel = function (options){
    var defaults = {
        validationContainerSelector: '.validationEngineContainer'
    };
    var self =this
    var config = $.extend({}, defaults, options);

    self.userId = ko.observable();
    self.emailAddress = ko.observable()
    self.firstName = ko.observable();
    self.lastName = ko.observable();
    self.email = ko.observable();



    self.searchUserDetails = function (details){
        var emailAddress = details.emailAddress
        if (emailAddress){
            $.get(config.searchUserDetailsUrl, {emailAddress: emailAddress}, undefined, "json").done(function (data){
                if (data.error === "error"){
                    bootbox.alert('<span class="label label-important">This Email Address is invalid: </span><p>' + data.emailAddress + '</p>');
                }else{
                    self.userId(data.userId);
                    self.firstName(data.firstName);
                    self.lastName(data.lastName);
                    self.email(data.emailAddress);

                }
            }).fail( function (){
                bootbox.alert('<span class="label label-important">This Email Address is invalid: </span><p>' + data.emailAddress + '</p>')
        });
        }else{
            bootbox.alert('<span class="label label-important">Please Enter the Email Address</span>');
        }

    };

    self.removeUserDetails = function (userDetails){
        var userId = userDetails.userId

        blockUIWithMessage("Removing User Permission...")
        $.post(config.removeUserDetailsUrl,{userId: userId},undefined, "json" ).done(function (data){
           if (data.error){
               bootbox.alert('<span class="label label-important">Failed to remove users from MERIT </span>'+'<p> Reason: '+data.error+'</p>');
               $.unblockUI();
           }else{
               blockUIWithMessage("Successfully Remove User Permission...")
               blockUIWithMessage("Refreshing page...");
               window.location.reload();
           }
        }).fail(function(data) {
            alert('An unhandled error occurred: ' + data.status + " Please refresh the page and try again");
            $.unblockUI();
        });
    };

};
