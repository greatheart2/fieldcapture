package au.org.ala.fieldcapture

import com.icegreen.greenmail.junit.GreenMailRule
import com.icegreen.greenmail.util.GreenMailUtil
import com.icegreen.greenmail.util.ServerSetup
import com.icegreen.greenmail.util.ServerSetupTest
import org.junit.Rule
import pages.RlpProjectPage
import pages.ReportPage
import spock.lang.Stepwise

import javax.mail.internet.MimeMessage

@Stepwise
class RlpReportingSpec extends StubbedCasSpec {

    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetup.verbose(ServerSetupTest.SMTP))

    def setupSpec() {
        useDataSet('dataset2')
    }

    def cleanup() {
        logout(browser)
    }


    def "the reports can be regenerated by an FC_ADMIN"() {

        setup:
        String projectId = '1'
        login([userId: '1', role: "ROLE_FC_ADMIN", email: 'admin@nowhere.com', firstName: "MERIT", lastName: 'FC_ADMIN'], browser)

        when:
        to RlpProjectPage, projectId

        then:
        waitFor { at RlpProjectPage }

        when: "Display the admin tab, navigate to the settings section then press the re-generate reports button"
        adminTab.click()

        then:
        waitFor { adminContent.displayed }

        when: "Click on the project settings"
        adminContent.projectSettingsTab.click()

        then:
        waitFor { adminContent.projectSettings.displayed }

        when:
        adminContent.projectSettings.regenerateReports()

        then:
        waitFor { at RlpProjectPage }

        when:
        reportingTab.click()


        then:
        waitFor { projectReports.displayed }

        and:

        waitFor {
            projectReports.reports.size() == 16
            projectReports.reports[1].name != ""
        }
        projectReports.reports[0].name == "Year 2018/2019 - Quarter 1 Outputs Report"
        projectReports.reports[0].fromDate == "01-07-2018"
        projectReports.reports[0].toDate == "30-09-2018"

        and: "The end date of the report finishing on the same day of the project is not the day before like other reports"
        projectReports.reports[15].name == "Outcomes Report 2 for Project 1"
        projectReports.reports[15].fromDate == "01-07-2018"
        projectReports.reports[15].toDate == "01-07-2023"

    }

    def "A project editor can edit the report"() {
        setup:
        String projectId = '1'
        login([userId: '10', role: "ROLE_USER", email: 'admin@nowhere.com', firstName: "MERIT", lastName: 'editor'], browser)

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
    }

    def "A project admin can submit the report"() {
        setup:
        String projectId = '1'
        login([userId: '1', role: "ROLE_USER", email: 'admin@nowhere.com', firstName: "MERIT", lastName: 'admin'], browser)

        when:
        to RlpProjectPage, projectId

        then:
        waitFor { at RlpProjectPage }

        when:
        reportingTab.click()

        then:
        waitFor { projectReports.displayed }
        projectReports.reports[0].markedAsComplete()
        projectReports.reports[0].canBeSubmitted()

        when:
        projectReports.reports[0].submit()

        then:
        waitFor { projectReports.reportDeclaration.displayed }


        when:
        projectReports.acceptTerms()
        projectReports.submitDeclaration()

        then:
        waitFor { hasBeenReloaded() }

        then:
        projectReports.reports[0].isSubmitted()

        waitFor 20, {
            MimeMessage[] messages = greenMail.getReceivedMessages()
            messages?.length == 1
            messages[0].getSubject() == "Report submitted subject"
            GreenMailUtil.getBody(messages[0]) == "<p>Report submitted body</p>"
        }
    }

    def "A user with the grant manager role can approve reports"() {
        setup:
        String projectId = '1'
        login([userId: '30', role: "ROLE_FC_OFFICER", email: 'fc_officer@nowhere.com', firstName: "MERIT", lastName: 'FC_OFFICER'], browser)

        when: "Display the reporting tab"
        to RlpProjectPage, projectId
        waitFor { at RlpProjectPage }
        reportingTab.click()

        then:
        waitFor { projectReports.displayed }

        then: "The first report is marked as submitted"
        projectReports.reports[0].isSubmitted()

        when:
        projectReports.reports[0].approve()

        then:
        waitFor {hasBeenReloaded()}

        when:
        waitFor { projectReports.displayed }

        then:
        projectReports.reports[0].isApproved()

    }

//    def "Report can be submit and restored after Timeout"(){
//        setup:
//        String projectId = '1'
//        login([userId: '10', role: "ROLE_USER", email: 'admin@nowhere.com', firstName: "MERIT", lastName: 'editor'], browser)
//
//        when:
//        to RlpProjectPage, projectId
//
//        then:
//        waitFor { at RlpProjectPage }
//
//        when:
//        reportingTab.click()
//
//        then:
//        waitFor { projectReports.displayed }
//
//        when:
//        projectReports.reports[1].edit()
//
//        then:
//        waitFor { at ReportPage }
//
//        when:
//        List formSections = getFormSections()
//
//        then:
//        formSections == [
//                'koRLP_-_Output_WHS',
//                'koRLP_-_Change_Management',
//                'koRLP_-_Baseline_data',
//                'koRLP_-_Communication_materials',
//                'koRLP_-_Community_engagement',
//                'koRLP_-_Controlling_access',
//                'koRLP_-_Pest_animal_management',
//                'koRLP_-_Management_plan_development',
//                'koRLP_-_Debris_removal',
//                'koRLP_-_Erosion_Management',
//                'koRLP_-_Maintaining_feral_free_enclosures',
//                'koRLP_-_Establishing_ex-situ_breeding_programs',
//                'koRLP_-_Establishing_Agreements',
//                'koRLP_-_Establishing_monitoring_regimes',
//                'koRLP_-_Farm_Management_Survey',
//                'koRLP_-_Fauna_survey',
//                'koRLP_-_Fire_management',
//                'koRLP_-_Flora_survey',
//                'koRLP_-_Habitat_augmentation',
//                'koRLP_-_Identifying_sites',
//                'koRLP_-_Improving_hydrological_regimes',
//                'koRLP_-_Improving_land_management_practices',
//                'koRLP_-_Disease_management',
//                'koRLP_-_Negotiations',
//                'koRLP_-_Obtaining_approvals',
//                'koRLP_-_Pest_animal_survey',
//                'koRLP_-_Plant_survival_survey',
//                'koRLP_-_Project_planning',
//                'koRLP_-_Remediating_riparian_and_aquatic_areas',
//                'koRLP_-_Weed_treatment',
//                'koRLP_-_Revegetating_habitat',
//                'koRLP_-_Site_preparation',
//                'koRLP_-_Skills_and_knowledge_survey',
//                'koRLP_-_Soil_testing',
//                'koRLP_-_Emergency_Interventions',
//                'koRLP_-_Water_quality_survey',
//                'koRLP_-_Weed_distribution_survey']
//
//        when: "We complete the form and save, marking optional sections as not applicable"
//        hideFloatingToolbar()
//        field('whsRequirementsMet').value('Met requirements')
//        field('variationSubmitted').value('No')
//        field('meriOrWorkOrderChangesRequired').value('No')
//        getFormSections().each {
//            if (isOptional(it)) {
//                markAsNotApplicable(it)
//            }
//        }
//        restoreFloatingToolbar()
//        simulateTimeout(browser)
//        save()
//
//
//        then: "The save will fail an a dialog is displayed to explain the situation"
//        waitFor 20, { timeoutModal.displayed }
//
//        when: "Click the re-login link and log back in"
//        waitFor {timeoutModal.loginLink.click() }
//
//        then:
//        waitFor {
//            at ReportPage
//            editAnyway.click()
//        }
//
//
//        and: "A dialog is displayed to say there are unsaved edits"
//        waitFor {unsavedEdits.displayed}
//
//        when:
//        okBootbox()
//
//        then:
//        field('whsRequirementsMet').value('Met requirements')
//        field('variationSubmitted').value('No')
//        field('meriOrWorkOrderChangesRequired').value('No')
//        save()
//
//        and:
//       waitFor 20, {
//           field('whsRequirementsMet').value('Met requirements')
//           field('variationSubmitted').value('No')
//           field('meriOrWorkOrderChangesRequired').value('No')
//       }
//
//
//
//    }

}
