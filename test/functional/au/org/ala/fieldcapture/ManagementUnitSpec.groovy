package au.org.ala.fieldcapture

import pages.ManagementUnitPage
import pages.ProgramPage

class ManagementUnitSpec extends StubbedCasSpec {

    def setup() {
        useDataSet('dataset_mu')
    }

    def "As a user, I can view a management unit"() {
        setup:
        login([userId:'1', role:"ROLE_USER", email:'user@nowhere.com', firstName: "MERIT", lastName:'User'], browser)

        when:
        to ManagementUnitPage

        then:
        waitFor {at ManagementUnitPage}

        when:
        //grantIds displayed is still false
        interact {
            moveToElement(projectLinksTd.first())
        }

        then:

        // grantIds() == ['RLP-Test-Program-Project-1'] //will fail when using phantomjs
        grantIds().size() ==1
        projectLinks().size()>=1

        //Cannot click on invisible element - phantomjs
        when:
        gotoProgram()[0].click()

        then:
        at ProgramPage


    }

}
