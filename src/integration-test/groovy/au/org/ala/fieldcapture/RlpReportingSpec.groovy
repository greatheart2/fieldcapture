package au.org.ala.fieldcapture

import com.icegreen.greenmail.util.GreenMail
import com.icegreen.greenmail.util.GreenMailUtil
import pages.ProjectIndex
import pages.RlpProjectPage
import pages.ReportPage
import pages.ViewReportPage
import pages.modules.ReportCategory
import spock.lang.Shared
import spock.lang.Stepwise

import javax.mail.internet.MimeMessage

@Stepwise
class RlpReportingSpec extends StubbedCasSpec {

    @Shared
    GreenMail greenMail = new GreenMail()

    def setupSpec() {
        useDataSet('dataset2')
        greenMail.start()
    }

    def cleanupSpec() {
        logout(browser)
        greenMail.stop()
    }


    def "the reports can be regenerated by a MERIT administrator"() {

        setup:
        String projectId = '1'
        loginAsMeritAdmin(browser)

        when: "Display the admin tab, navigate to the settings section then press the re-generate reports button"
        to RlpProjectPage, projectId
        adminTab.click()
        waitFor { adminContent.displayed }
        adminContent.projectSettingsTab.click()
        waitFor { adminContent.projectSettings.displayed }
        adminContent.projectSettings.regenerateReports()

        then:
        waitFor 20, { hasBeenReloaded() }

        when:
        displayReportingTab()
        projectReports.reportsByCategory.each { ReportCategory reportCategory ->
            if (reportCategory.showAllReportsCheckbox.displayed) {
                reportCategory.showAllReports()
            }
        }

        then:
        waitFor { projectReports.displayed }

        and:

        waitFor {
            projectReports.reports.size() == 27
            projectReports.reports[1].name != ""
        }
        projectReports.reports[0].name == "Year 2018/2019 - Quarter 1 Outputs Report"
        projectReports.reports[0].fromDate == "01-07-2018"
        projectReports.reports[0].toDate == "30-09-2018"

        and: "The end date of the report finishing on the same day of the project is not the day before like other reports"
        projectReports.reports[26].name == "Outcomes Report 2 for Project 1"
        projectReports.reports[26].fromDate == "01-07-2018"
        projectReports.reports[26].toDate == "01-07-2023"

    }

    def "a user with the MERIT siteReadOnly role can view a report"() {
        setup:
        String projectId = '1'
        loginAsReadOnlyUser(browser)

        when:
        to RlpProjectPage, projectId

        then:
        waitFor { at RlpProjectPage }

        when: "Displays the report tab page"
        reportingTab.click()

        then: "List of reports will be displayed, report categories will be asserted"
        waitFor { projectReports.displayed }
        waitFor {
            projectReports.reportCategories == ["Outputs Reporting", "Annual Progress Reporting", "Outcomes Report 1", "Outcomes Report 2"]
        }

        when: "Edit a report, user with MERIT siteReadOnly should not be able to navigate to the selected report page"
        projectReports.reportsByCategory[0].showAllReports()
        projectReports.reports[0].edit()

        then:
        waitFor { hasBeenReloaded() }

    }

    def "Report Attach Document should not be add a new empty doc in the table when the dialog box is cancel"() {
        setup:
        String projectId = '1'
        loginAsUser('1', browser)

        when:
        to RlpProjectPage, projectId

        then:
        displayReportingTab()

        when:
        projectReports.reports[2].edit()

        then:
        waitFor 10, {
            at ReportPage
        }

        when: "Click on Attach Document"
        moveToDocumentAttachSection()
        openAttachDocumentDialog()

        then:
        waitFor 10, {
            attachDocumentModal.displayed
            attachDocumentModal.cancelButton.displayed
        }

        when:
        attachDocumentModal.cancel()

        then:
        attachDocument.displayed

        and:
        //when the document is attached, attach document btn will be remove and swap with document
        // otherwise it will not remove the attach document btn
        attachDocument.text() == "Attach Document"
    }

    def "A project editor can edit the report"() {
        setup:
        String projectId = '1'
        loginAsUser('10', browser)

        when:
        to RlpProjectPage, projectId

        then:
        waitFor { at RlpProjectPage }

        when:
        reportingTab.click()

        then:
        waitFor { projectReports.displayed }

        when:
        projectReports.reports[0].edit()

        then:
        waitFor { at ReportPage }

        when:
        List formSections = getFormSections()

        then:
        formSections == [
                'koRLP_-_Output_WHS',
                'koRLP_-_Change_Management',
                'koRLP_-_Baseline_data',
                'koRLP_-_Communication_materials',
                'koRLP_-_Community_engagement',
                'koRLP_-_Controlling_access',
                'koRLP_-_Pest_animal_management',
                'koRLP_-_Management_plan_development',
                'koRLP_-_Debris_removal',
                'koRLP_-_Erosion_Management',
                'koRLP_-_Maintaining_feral_free_enclosures',
                'koRLP_-_Establishing_ex-situ_breeding_programs',
                'koRLP_-_Establishing_Agreements',
                'koRLP_-_Establishing_monitoring_regimes',
                'koRLP_-_Farm_Management_Survey',
                'koRLP_-_Fauna_survey',
                'koRLP_-_Fire_management',
                'koRLP_-_Flora_survey',
                'koRLP_-_Habitat_augmentation',
                'koRLP_-_Identifying_sites',
                'koRLP_-_Improving_hydrological_regimes',
                'koRLP_-_Improving_land_management_practices',
                'koRLP_-_Disease_management',
                'koRLP_-_Negotiations',
                'koRLP_-_Obtaining_approvals',
                'koRLP_-_Pest_animal_survey',
                'koRLP_-_Plant_survival_survey',
                'koRLP_-_Project_planning',
                'koRLP_-_Remediating_riparian_and_aquatic_areas',
                'koRLP_-_Weed_treatment',
                'koRLP_-_Revegetating_habitat',
                'koRLP_-_Site_preparation',
                'koRLP_-_Skills_and_knowledge_survey',
                'koRLP_-_Soil_testing',
                'koRLP_-_Emergency_Interventions',
                'koRLP_-_Water_quality_survey',
                'koRLP_-_Weed_distribution_survey']

        when: "We complete the form and save, marking optional sections as not applicable"
        hideFloatingToolbar()
        field('whsRequirementsMet').value('Met requirements')
        field('variationSubmitted').value('No')
        field('meriOrWorkOrderChangesRequired').value('No')
        getFormSections().each {
            if (isOptional(it)) {
                markAsNotApplicable(it)
            }
        }
        restoreFloatingToolbar()
        markAsComplete()
        save()
        exitReport()

        then:
        waitFor { at RlpProjectPage }

        then: "The editor is not able to submit the report"
        //projectReports.reports[0].markedAsComplete()
        !projectReports.reports[0].canBeSubmitted()

        and:"The not required button is not visible to the editor"
        !projectReports.reports[0].notRequired()
    }

    def "A project admin can submit the report"() {
        setup:
        String projectId = '1'
        loginAsUser('1', browser)

        when:
        to RlpProjectPage, projectId

        then:
        waitFor { at RlpProjectPage }

        when:
        reportingTab.click()

        then: "The submit button is visible to the project admin and able to verify that the report is mark as completed"
        waitFor { projectReports.displayed }
        projectReports.reports[0].markedAsComplete()
        projectReports.reports[0].canBeSubmitted()

        when:
        projectReports.reports[0].submit()

        then:
        waitFor { projectReports.reportDeclaration.displayed }


        when:
        waitFor 20, {
            projectReports.acceptTerms()
            projectReports.submitDeclaration()
        }


        then:
        waitFor { hasBeenReloaded() }
        projectReports.reports[0].isSubmitted()

        waitFor 20, {
            MimeMessage[] messages = greenMail.getReceivedMessages()
            messages?.length == 1
            messages[0].getSubject() == "Report submitted subject"
            GreenMailUtil.getBody(messages[0]) == "<p>Report submitted body</p>"
        }
    }

    def "A project admin cannot mark the report as not required as the button is only visible to a Site Admin"() {
        setup:
        String projectId = '1'
        loginAsUser('1', browser)

        when:
        to RlpProjectPage, projectId

        then:
        waitFor { at RlpProjectPage }

        when:
        reportingTab.click()

        then: "The first report is marked as submitted"
        waitFor { projectReports.displayed }
        projectReports.reports[0].isSubmitted()

        and:"The not required button is not visible to the project admin"
        !projectReports.reports[0].notRequired()

    }

    def "The Site Admin user can mark the report as not required"() {
        setup:
        String projectId = '1'
        loginAsMeritAdmin(browser)

        when:
        to RlpProjectPage, projectId

        then:
        waitFor { at RlpProjectPage }

        when:
        reportingTab.click()

        then:
        waitFor { projectReports.displayed }

        then: "The first report is marked as submitted"
        projectReports.reports[0].isSubmitted()

        and:"The not required button is visible to the unsubmitted report and to the site admin"
        projectReports.reports[2].notRequired()


        when:"the site admin clicks the not required button"
        projectReports.reports[2].cancelReport()

        then:
        waitFor {
            projectReports.reasonModal.displayed
        }

        when: "the site admin enters the reason and confirm the cancellation"
        waitFor 20, {
            projectReports.cancellationReason()
            projectReports.confirmCancellation()
        }

        then:
        waitFor { hasBeenReloaded() }

        and:
        projectReports.reports[2].isSubmitted()

    }

    def "A user with the grant manager role can approve reports"() {
        setup:
        String projectId = '1'
        loginAsGrantManager(browser)

        when: "Display the reporting tab"
        to RlpProjectPage, projectId
        waitFor { at RlpProjectPage }
        reportingTab.click()

        then:
        waitFor { projectReports.displayed }

        then: "The first report is marked as submitted"
        projectReports.reports[0].isSubmitted()

        and:"The not required button is not visible to the grant manager"
        !projectReports.reports[0].notRequired()

        when:
        projectReports.reports[0].approve()

        then:
        waitFor {hasBeenReloaded()}

        when:
        waitFor { projectReports.displayed }

        then:
        projectReports.reports[0].isApproved()

    }

    def "Report can be submit and restored after Timeout"(){
        setup:
        String projectId = '1'
        loginAsUser('10', browser)

        when:
        to RlpProjectPage, projectId

        then:
        waitFor { at RlpProjectPage }

        when:
        reportingTab.click()

        then:
        waitFor { projectReports.displayed }

        when:
        projectReports.reports[1].edit()

        then:
        waitFor { at ReportPage }

        when:
        List formSections = getFormSections()

        then:
        formSections == [
                'koRLP_-_Output_WHS',
                'koRLP_-_Change_Management',
                'koRLP_-_Baseline_data',
                'koRLP_-_Communication_materials',
                'koRLP_-_Community_engagement',
                'koRLP_-_Controlling_access',
                'koRLP_-_Pest_animal_management',
                'koRLP_-_Management_plan_development',
                'koRLP_-_Debris_removal',
                'koRLP_-_Erosion_Management',
                'koRLP_-_Maintaining_feral_free_enclosures',
                'koRLP_-_Establishing_ex-situ_breeding_programs',
                'koRLP_-_Establishing_Agreements',
                'koRLP_-_Establishing_monitoring_regimes',
                'koRLP_-_Farm_Management_Survey',
                'koRLP_-_Fauna_survey',
                'koRLP_-_Fire_management',
                'koRLP_-_Flora_survey',
                'koRLP_-_Habitat_augmentation',
                'koRLP_-_Identifying_sites',
                'koRLP_-_Improving_hydrological_regimes',
                'koRLP_-_Improving_land_management_practices',
                'koRLP_-_Disease_management',
                'koRLP_-_Negotiations',
                'koRLP_-_Obtaining_approvals',
                'koRLP_-_Pest_animal_survey',
                'koRLP_-_Plant_survival_survey',
                'koRLP_-_Project_planning',
                'koRLP_-_Remediating_riparian_and_aquatic_areas',
                'koRLP_-_Weed_treatment',
                'koRLP_-_Revegetating_habitat',
                'koRLP_-_Site_preparation',
                'koRLP_-_Skills_and_knowledge_survey',
                'koRLP_-_Soil_testing',
                'koRLP_-_Emergency_Interventions',
                'koRLP_-_Water_quality_survey',
                'koRLP_-_Weed_distribution_survey']

        when: "We complete the form and save, marking optional sections as not applicable"
        hideFloatingToolbar()
        field('whsRequirementsMet').value('Met requirements')
        field('variationSubmitted').value('No')
        field('meriOrWorkOrderChangesRequired').value('No')
        getFormSections().each {
            if (isOptional(it)) {
                markAsNotApplicable(it)
            }
        }
        restoreFloatingToolbar()
        simulateTimeout(browser)
        save()


        then: "The save will fail an a dialog is displayed to explain the situation"
        waitFor 20, { timeoutModal.displayed }

        when: "Click the re-login link and log back in"
        Thread.sleep(1000) // wait for the timeoutModal animation to complete.
        timeoutModal.loginLink.click()

        then: "The page will be reloaded, and dialog is displayed to say there are unsaved edits"
        waitFor 20, { unsavedEdits.displayed }

        when:
        okBootbox()

        then:
        field('whsRequirementsMet').value() == 'Met requirements'
        field('variationSubmitted').value() == 'No'
        field('meriOrWorkOrderChangesRequired').value() == 'No'

        when:
        save()

        then:
        waitFor 20, {
            field('whsRequirementsMet').value('Met requirements')
            field('variationSubmitted').value('No')
            field('meriOrWorkOrderChangesRequired').value('No')
        }
    }

    def "Grant manager can update the project start/end dates and can generate the project reports when there's no data in any report"() {

        setup:
        loginAsGrantManager(browser)

        when:
        to ProjectIndex, 'project_application'

        then:
        waitFor { at ProjectIndex }
        adminTab.click()
        def meriplan = waitFor { admin.openMeriPlan() }
        waitFor {
            meriplan.approveButton.displayed
        }

        then:
        meriplan.approveButton.@disabled
        meriplan.externalIds.displayed
        !meriplan.projectStartDate.displayed

        when:
        meriplan.projectStartDate = "01/06/2018"
        meriplan.externalIds.addExternalId()
        meriplan.externalIds.externalIds[0].idType = "INTERNAL_ORDER_NUMBER"
        meriplan.externalIds.externalIds[0].externalId = "56789"
        admin.meriPlanTab.click() // Ensure focus moves so the button binding triggers

        then:
        waitFor { !meriplan.approveButton.@disabled }

        when:
        waitFor { meriplan.approveButton.click() }

        then:
        waitFor {
            meriplan.approvePlanDialog.changeOrderNumbers.displayed
        }

        when:
        meriplan.approvePlanDialog.changeOrderNumbers = 'CO56789'
        meriplan.approvePlanDialog.comment = 'test approved'

        meriplan.approvePlanDialog.approve()

        then:
        waitFor{hasBeenReloaded()}
        at ProjectIndex

        when:
        adminTab.click()

        then:
        def updatedMeriPlan = waitFor { admin.openMeriPlan() }
        updatedMeriPlan.modifyApprovedPlanButton.displayed

        when:
        overviewTab.click()

        then:
        overview.projectStatus[1].text() == 'ACTIVE'

        when:"project is active the grant manager can generate the project report and update the project start date in the reporting tab"
        reportingTab.click()

        then:
        waitFor 20, {
            projectReports.displayed
        }

        when:
        projectReports.projectStartDate = "01/06/2018"
        projectReports.projectEndDate = "30/06/2023" // These dates match existing dates - ensuring the reports are generated correctly
        projectReports.generateButton.click()

        then:
        waitFor 30, { hasBeenReloaded() }

        and: "List of reports will be displayed"
        waitFor {
            projectReports.reports.size() > 0
        }

        when: "We change one of the dates"
        projectReports.projectStartDate = "01/07/2018"
        projectReports.projectEndDate = "30/06/2023"
        projectReports.generateButton.click()

        then:
        waitFor 30, { hasBeenReloaded() }

        and: "The reports will have updated to the new dates"
        waitFor {
            projectReports.reports.size() > 0
            projectReports.reports[0].fromDate == "01-07-2018"
        }

        when: "update one of the report from the list"
        projectReports.reports[0].edit()

        then:
        waitFor { at ReportPage }

        when:
        List formSections = getFormSections()

        then:
        formSections == [
                'koRLP_-_Output_WHS',
                'koRLP_-_Change_Management',
                'koRLP_-_Baseline_data',
                'koRLP_-_Communication_materials',
                'koRLP_-_Community_engagement',
                'koRLP_-_Controlling_access',
                'koRLP_-_Pest_animal_management',
                'koRLP_-_Management_plan_development',
                'koRLP_-_Debris_removal',
                'koRLP_-_Erosion_Management',
                'koRLP_-_Maintaining_feral_free_enclosures',
                'koRLP_-_Establishing_ex-situ_breeding_programs',
                'koRLP_-_Establishing_Agreements',
                'koRLP_-_Establishing_monitoring_regimes',
                'koRLP_-_Farm_Management_Survey',
                'koRLP_-_Fauna_survey',
                'koRLP_-_Fire_management',
                'koRLP_-_Flora_survey',
                'koRLP_-_Habitat_augmentation',
                'koRLP_-_Identifying_sites',
                'koRLP_-_Improving_hydrological_regimes',
                'koRLP_-_Improving_land_management_practices',
                'koRLP_-_Disease_management',
                'koRLP_-_Negotiations',
                'koRLP_-_Obtaining_approvals',
                'koRLP_-_Pest_animal_survey',
                'koRLP_-_Plant_survival_survey',
                'koRLP_-_Project_planning',
                'koRLP_-_Remediating_riparian_and_aquatic_areas',
                'koRLP_-_Weed_treatment',
                'koRLP_-_Revegetating_habitat',
                'koRLP_-_Site_preparation',
                'koRLP_-_Skills_and_knowledge_survey',
                'koRLP_-_Soil_testing',
                'koRLP_-_Emergency_Interventions',
                'koRLP_-_Water_quality_survey',
                'koRLP_-_Weed_distribution_survey']

        when: "We complete the form and save, marking optional sections as not applicable"
        hideFloatingToolbar()
        field('whsRequirementsMet').value('Met requirements')
        field('variationSubmitted').value('No')
        field('meriOrWorkOrderChangesRequired').value('No')
        getFormSections().each {
            if (isOptional(it)) {
                markAsNotApplicable(it)
            }
        }
        restoreFloatingToolbar()
        markAsComplete()
        save()
        exitReport()

        then: "then the project start date cannot be change as one of the reports has data"
        waitFor { at RlpProjectPage }

        and: ""
        projectReports.projectStartDate.empty == true
        projectReports.projectEndDate.empty == true
        projectReports.generateButton.empty == true
    }

    def "When a project target is overdelivered, MERIT will display warnings throughout the workflow"() {
        setup:
        String projectId = '1'
        loginAsUser('1', browser)

        when:
        to RlpProjectPage, projectId
        displayReportingTab()
        projectReports.reports[3].edit()

        then:
        waitFor { at ReportPage }

        when: "We complete the form, over-delivering on the target (Weed Distribution Survey)"
        hideFloatingToolbar()
        field('whsRequirementsMet').value('Met requirements')
        field('variationSubmitted').value('No')
        field('meriOrWorkOrderChangesRequired').value('No')
        getFormSections().each {
            // Mark all sections except the Weed Distribution Survey as not applicable
            if (isOptional(it) && it != 'koRLP_-_Weed_distribution_survey') {
                markAsNotApplicable(it)
            }
        }
        def section = $('#koRLP_-_Weed_distribution_survey')
        field("baselineOrIndicator", section).value("Baseline")
        field("numberOfSurveysConducted", section).value(1)
        field("dateRange", section).value("2018")
        field("areaSurveyedHa", section).value("2000")
        field("projectAssuranceDetails", section).value("Testing")
        // This field is dynamically added after the area survey is entered (and focus lost) so we need to wait.
        waitFor {
            field("mappingNotAlignedReason", section).value("Mapped area simplifies more complex area/s where work was undertaken during this period")
            println(field("mappingNotAlignedReason", section).value())
            field("mappingNotAlignedReason", section).value() == "Mapped area simplifies more complex area/s where work was undertaken during this period"
        }
        field("invoicedNotActualReason", section).value("Work was undertaken over a greater area than will be invoiced for")
        section.find("i.fa-remove").click()

        restoreFloatingToolbar()
        markAsComplete()
        save()

        then: "The over delivery warning should be displayed"
        waitFor {
            overDeliveryModal.displayed
        }

        when: "We close the warning and exit the report"
        okBootbox()
        exitReport()

        and: "We submit the report"
        waitFor { at RlpProjectPage }
        displayReportingTab()
        projectReports.reports[3].submit()
        waitFor { projectReports.reportDeclaration.displayed }
        waitFor 20, {
            projectReports.acceptTerms()
            projectReports.submitDeclaration()
        }

        then: "The report is submitted"
        waitFor { hasBeenReloaded() }
        projectReports.reports[0].isSubmitted()

        when: "We login as a grant manager and try and approve the report"
        logout(browser)
        loginAsGrantManager(browser)
        to RlpProjectPage, projectId
        displayReportingTab()
        projectReports.reports[3].approve()

        then: "An over delivery warning will be displayed"
        waitFor {
            projectReports.overDeliveryModal.displayed
        }

        when: "We press view report"
        okBootbox('.btn-info')

        then:
        waitFor {
            at ViewReportPage
        }

        and:
        waitFor {
            overDeliveryModal.displayed
        }

        when:
        okBootbox()
        exitReport()

        then:
        waitFor { at RlpProjectPage }

        when:
        displayReportingTab()
        projectReports.reports[3].approve()
        waitFor {hasBeenReloaded()}

        then:
        projectReports.reports[3].isApproved()
    }

    def "A read only user will not be able to generate reports and change the project start/end dates " () {

        setup:
        String projectId = '1'
        loginAsReadOnlyUser(browser)

        when: "login as read only"
        to ProjectIndex, projectId

        then:
        waitFor { at ProjectIndex }

        when:
        reportingTab.click()

        then:
        waitFor 20, {
            projectReports.displayed
        }

        and:
        projectReports.projectStartDate.empty == true
        projectReports.projectEndDate.empty == true
        projectReports.generateButton.empty == true
    }


}
