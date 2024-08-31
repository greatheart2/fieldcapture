package pages.modules

import geb.Module

class OrganisationAdminTab extends Module{

    static content = {

        editButton{ $('[data-bind="click:editOrganisation"]')}
        deleteButton{ $('[data-bind="click:deleteOrganisation"]')}

        editTab { $('#edit-program-details-tab') }

        configTab(required:false) { $('#config-tab')}
        config(required:false) { $('#config').module OrganisationConfigModule }

        reportingSectionTab(required:false) { $('#reporting-config-tab') }
        reportingSection(required:false) { $('#reporting-config').module OrganisationAdminReportSection }

        documentsTab { $('#edit-documents-tab') }
        documents { $('#edit-documents').module AdminDocumentsTab }

        adminColumn { $("#admin .flex-column a") }

        permissionAccessTab {$('#mu-permissions-tab')}
        permissionAccess { $('#managementUnit-permissions').module PermissionsAdminModule }

    }

    def viewDocumentsSection() {
        documentsTab.click()
        waitFor { documents.header.displayed }
    }

    def attachDocument() {
        viewDocumentsSection()
        documents.attachDocumentButton.click()

        waitFor {
            Thread.sleep(1000) // Wait for the dialog to animate into view
            println documents.attachDocumentDialog.title.attr('parentNode')
            documents.attachDocumentDialog.title.displayed
        }

        documents.attachDocumentDialog
    }

    def viewEditSection() {
        waitFor { editTab.displayed }
        editTab.click()
        waitFor { editButton.displayed }
    }

    def openConfig() {
        configTab.click()
        waitFor { config.displayed }
    }

    def viewReportingSection() {
        waitFor{ reportingSectionTab.displayed }
        reportingSectionTab.click()
        waitFor {
            reportingSection.displayed
        }
        reportingSection
    }

}
