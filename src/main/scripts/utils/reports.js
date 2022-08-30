/**
 * Iterates through a management unit project and managementUnit report configuration and adds a description
 * to any reports matching the supplied category.
 */
function addDescriptionToMUReports(category, description) {
    var mus = db.managementUnit.find({status:{$ne:'deleted'}});
    while (mus.hasNext()) {
        var mu = mus.next();

        if (mu.config.projectReports) {
            for (var i=0; i<mu.config.projectReports.length; i++) {
                if (mu.config.projectReports[i].category == category) {
                    mu.config.projectReports[i].description = description;
                    print("Updating report description for MU: "+mu.name);
                    db.managementUnit.save(mu);
                }
            }
        }
        if (mu.config.managementUnitReports) {
            for (var i=0; i<mu.config.managementUnitReports.length; i++) {
                if (mu.config.managementUnitReports[i].category == category) {
                    mu.config.managementUnitReports[i].description = description;

                    print("Updating report description for MU: "+mu.name);
                    db.managementUnit.save(mu);
                }
            }
        }

    }
}

function addDescriptionToProgramReports(category, description, programs) {
    var criteria = {status:{$ne:'deleted'}};
    if (programs) {
        criteria.name = {$in:programs};
    }
    var programs = db.program.find(criteria);

    while (programs.hasNext()) {
        var program = programs.next();
        if (program.config.projectReports) {
            for (var i=0; i<program.config.projectReports.length; i++) {
                if (program.config.projectReports[i].category == category) {
                    program.config.projectReports[i].description = description;
                    print("Updating report description for program: "+program.name);
                    db.program.save(program);
                }
            }
        }
    }
}

/**
 * Iterates backwards through project reports, undoing the effects of a bug that can result
 * in reports being pushed to the next reporting period when a start date change is made on
 * projects with existing reports.
 * @param projectId the project id affected by the bug
 * @param reportTypes an array containing the activityType of the reports to change
 * @param adminUserId the userId to include in the audit trail
 */
function repairProjectAffectedByDateChangeBug(projectId, reportTypes, adminUserId) {

    for (var i=0; i<reportTypes.length; i++) {

        var reports = db.report.find({projectId:projectId, activityType:reportTypes[i], status:{$ne:'deleted'}}).sort({toDate:1});

        var currentReport = null;
        var currentActivity = null;

        while (reports.hasNext()) {

            var previousReport = reports.next();
            var copyOfPreviousReport = db.report.find({reportId:previousReport.reportId}).next();

            var previousActivity = db.activity.find({activityId:previousReport.activityId}).next();
            var copyOfPreviousActivity = db.activity.find({activityId:previousReport.activityId}).next();

            print("current: "+(currentReport && currentReport.name));
            print("previous: "+previousReport.name);
            print("********");
            if (currentReport && previousReport) {

                print("Moving "+currentReport.name+" to "+previousReport.name);

                previousReport.name = currentReport.name;
                previousReport.description = currentReport.description;
                previousReport.toDate = currentReport.toDate;
                previousReport.fromDate = currentReport.fromDate;
                previousReport.lastUpdated = ISODate();
                previousReport.submissionDate = currentReport.submissionDate;

                db.report.save(previousReport);
                audit(previousReport, previousReport.reportId, 'au.org.ala.ecodata.Report', adminUserId);

                previousActivity.plannedStartDate = previousReport.fromDate;
                previousActivity.startDate = previousReport.fromDate;
                previousActivity.plannedEndDate = previousReport.toDate;
                previousActivity.endDate = previousReport.toDate;
                previousActivity.lastUpdated = ISODate();
                previousActivity.description = currentActivity.description;
                db.activity.save(previousActivity);
                audit(previousActivity, previousActivity.activityId, 'au.org.ala.ecodata.Activity', adminUserId);

            }

            currentReport = copyOfPreviousReport;
            currentActivity = copyOfPreviousActivity;
        }
    }

}

/** Undoes an accidental status of "Not required" for a report. */
function removeNotRequiredStatus(reportId, reason, adminUserId) {
    var report = db.report.findOne({reportId:reportId});
    report.publicationStatus = 'unpublished';
    report.statusChangeHistory.push({
            "changedBy" : adminUserId,
            "comment" : reason,
            "dateChanged" : ISODate(),
            "status" : report.publicationStatus
        });
    db.report.save(report);
    audit(report, report.reportId, 'au.org.ala.ecodata.Report', adminUserId);

    if (report.activityId) {
        var activity = db.activity.findOne({activityId:report.activityId});
        activity.publicationStatus = 'unpublished';
        db.activity.save(activity);
        audit(activity, activity.activityId, 'au.org.ala.ecodata.Activity', adminUserId);
    }
}
